package com.googlecode.flaxcrawler.concurrent;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

/**
 * Queue implementation using berkley db. {@link DefaultQueue} is used to store first {@code QUEUE_CAPACITY}
 * tasks. Berkley DB is used to store other tasks. When {@link DefaultQueue} size is less than {@code MIN_QUEUE_CAPACITY} -
 * tasks are loaded from the berkley db.
 */
public class BerkleyQueue implements Queue {

    public final static int DEFAULT_QUEUE_CAPACITY = 100000;
    private Logger log = Logger.getLogger(this.getClass());
    private Environment environment;
    private EntityStore berkleyQueueStore;
    private PrimaryIndex<Long, BerkleyQueueElement> berkleyQueueIndex;
    private DefaultQueue innerQueue = new DefaultQueue();
    private int queueCapacity = DEFAULT_QUEUE_CAPACITY;
    private boolean loadToBerkley = false;
    /**
     * Pointer to the head of queue
     */
    private long startId = 1;
    /**
     * Pointer to the next free place in the queue
     */
    private long endId = 1;

    /**
     * Sets inner {@link DefaultQueue} capacity. Other tasks stored in berkley db.
     * @param queueCapacity
     */
    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    /**
     * Creates an instance of the {@code BerkleyQueue}
     * @param environmentFile
     */
    public BerkleyQueue(String environmentFile) throws DatabaseException {
        log.info("Initializing queue repository");

        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(false);
        environmentConfig.setLocking(false);
        environmentConfig.setCachePercent(5);

        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(false);
        storeConfig.setTemporary(true);

        File envFile = new File(environmentFile);
        if (envFile.exists()) {
            log.info("Environment is already exists");
            try {
                FileUtils.cleanDirectory(envFile);
            } catch (Exception ex) {
                log.fatal("Error cleaning directory " + envFile, ex);
                throw new RuntimeException("Error cleaning directory " + envFile);
            }
        }
        envFile.mkdirs();

        log.info("Environment file is " + envFile.getAbsolutePath());

        environment = new Environment(envFile, environmentConfig);
        berkleyQueueStore = new EntityStore(environment, "BerkleyQueueStore", storeConfig);

        berkleyQueueIndex = berkleyQueueStore.getPrimaryIndex(Long.class, BerkleyQueueElement.class);
        log.info("Environment successfully initialized");
    }

    /**
     * Closes queue
     */
    public void dispose() {
        try {
            berkleyQueueStore.close();
            environment.close();

            log.info("Environment closed successfully");
        } catch (DatabaseException ex) {
            log.error("Error while closing environment", ex);
        }
    }

    @Override
    public synchronized void add(Object obj) {
        if (loadToBerkley) {
            defer(obj);
        } else {
            innerQueue.add(obj);

            if (innerQueue.size() >= queueCapacity) {
                log.info("Tasks count is greater than queue capacity, putting other tasks to berkley db");
                loadToBerkley = true;
            }
        }
    }

    @Override
    public void defer(Object obj) {
        putToBerkley(obj);
    }

    @Override
    public synchronized Object poll() {
        Object obj = innerQueue.poll();

        if (innerQueue.size() <= (queueCapacity / 10) && (endId > startId)) {
            log.info("Tasks count is lesser than queueCapacity/10, loading tasks from berkley");
            loadInnerQueueFromBerkley(queueCapacity - innerQueue.size());
        } else if (loadToBerkley && innerQueue.size() <= (queueCapacity / 2)) {
            log.info("Tasks count is lesser than queueCapacity/2, using in-memory queue now");
            loadToBerkley = false;
        }

        return obj;
    }

    /**
     * Loads tasks from the berkley queue to the inner queue
     */
    private void loadInnerQueueFromBerkley(int count) {
        log.info("Loading up to " + count + " tasks from the berkley queue to the inner queue");

        int loaded = 0;
        for (int i = 0; i < count; i++) {
            Object toLoad = pollFromBerkley();

            if (toLoad == null) {
                break;
            }

            innerQueue.add(toLoad);
            loaded++;
        }

        log.info(loaded + " tasks loaded from the berkley queue to the inner queue");
    }

    @Override
    public int size() {
        try {
            return (int) berkleyQueueIndex.count() + innerQueue.size();
        } catch (DatabaseException ex) {
            log.warn("Error getting size of berkley db index", ex);
            return innerQueue.size();
        }
    }

    /**
     * Puts object to berkley db storage
     * @param obj
     * @throws TaskQueueException
     */
    private void putToBerkley(Object obj) {
        if (obj == null) {
            log.error("Error inserting task to the repository, object is null");
            return;
        }

        if (!(obj instanceof Task)) {
            log.error("Error inserting task to the repository, object isn't instanse of task");
            return;
        }

        synchronized (this) {
            BerkleyQueueElement queueElement = new BerkleyQueueElement(endId, (Task) obj);
            try {
                berkleyQueueIndex.put(queueElement);
                log.debug("Put task to berkley queue endId = " + endId++ + ", startId = " + startId);
            } catch (DatabaseException ex) {
                log.error("Error inserting task to the repository", ex);
            }
        }
    }

    /**
     * Polls object from berkley db
     * @return
     * @throws TaskQueueException
     */
    private Object pollFromBerkley() {
        try {
            synchronized (this) {
                BerkleyQueueElement entityBerkleyQueueElement = berkleyQueueIndex.get(startId);
                if (ObjectUtils.equals(entityBerkleyQueueElement, null)) {
                    return null;
                }
                Task task = entityBerkleyQueueElement.getTask();
                if (task != null) {
                    berkleyQueueIndex.delete(startId);
                    ++startId;
                }
                log.debug("Poll task from berkley queue, startId = " + startId);
                return task;
            }
        } catch (DatabaseException ex) {
            log.error("Error while polling task from repository", ex);
            return null;
        }
    }

    @Entity
    public static class BerkleyQueueElement {

        @PrimaryKey
        private long primaryKey;
        private Task task;

        public BerkleyQueueElement() {
        }

        public BerkleyQueueElement(long primaryKey, Task task) {
            this.primaryKey = primaryKey;
            this.task = task;
        }

        /**
         * @return the primaryKey
         */
        public long getPrimaryKey() {
            return primaryKey;
        }

        /**
         * @param primaryKey the primaryKey to set
         */
        public void setPrimaryKey(long primaryKey) {
            this.primaryKey = primaryKey;
        }

        /**
         * @return the task
         */
        public Task getTask() {
            return task;
        }

        /**
         * @param task the task to set
         */
        public void setTask(Task task) {
            this.task = task;
        }
    }
}


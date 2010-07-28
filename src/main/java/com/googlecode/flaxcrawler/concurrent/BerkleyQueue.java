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
 * Queue implementation using berkley db
 */
public class BerkleyQueue implements Queue {

    private Logger log = Logger.getLogger(this.getClass());
    private Environment environment;
    private EntityStore berkleyQueueStore;
    private PrimaryIndex<Integer, BerkleyQueueElement> berkleyQueueIndex;
    /**
     * Pointer to the head of queue
     */
    private int startId = 1;
    /**
     * Pointer to the next free place in the queue
     */
    private int endId = 1;//point to next free place in queue

    /**
     * Creates an instance of the {@code BerkleyQueue}
     * @param environmentFile
     */
    public BerkleyQueue(String environmentFile) {
        log.info("Initializing queue repository");

        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        environmentConfig.setAllowCreate(true);
        storeConfig.setAllowCreate(true);
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

        berkleyQueueIndex = berkleyQueueStore.getPrimaryIndex(Integer.class, BerkleyQueueElement.class);
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
    public synchronized void push(Object obj) throws TaskQueueException {

        if (obj == null) {
            log.error("Error inserting task to the repository, object is null");
            return;
        }

        if (!(obj instanceof Task)) {
            log.error("Error inserting task to the repository, object isn't instanse of task");
            return;
        }

        try {
            BerkleyQueueElement queueElement = new BerkleyQueueElement(endId++, (Task) obj);
            try {
                berkleyQueueIndex.put(queueElement);
                log.debug("Put task to berkley queue endId = " + (endId - 1) + ", startId = " + startId);
            } catch (DatabaseException ex) {
                log.error("Error inserting task to the repository", ex);
            }
        } catch (DatabaseException ex) {
            log.error("Error inserting object to the repository", ex);
            throw new TaskQueueException("Error inserting task to the repository", ex);
        }
    }

    @Override
    public synchronized Object poll() {
        try {
            BerkleyQueueElement entityBerkleyQueueElement = berkleyQueueIndex.get(startId);
            if (ObjectUtils.equals(entityBerkleyQueueElement, null)) {
                return null;
            }
            Task task = entityBerkleyQueueElement.getTask();
            if (task != null) {
                ++startId;
            }
            log.debug("Poll task from berkley queue, startId = " + startId);
            return task;
        } catch (DatabaseException ex) {
            log.error("Error while polling task from repository", ex);
            return null;
        }
    }

    @Entity
    public static class BerkleyQueueElement {

        @PrimaryKey
        private int primaryKey;
        private Task task;

        public BerkleyQueueElement() {
        }

        public BerkleyQueueElement(int primaryKey, Task task) {
            this.primaryKey = primaryKey;
            this.task = task;
        }

        /**
         * @return the primaryKey
         */
        public int getPrimaryKey() {
            return primaryKey;
        }

        /**
         * @param primaryKey the primaryKey to set
         */
        public void setPrimaryKey(int primaryKey) {
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


package com.googlecode.flaxcrawler.frontier;

import com.googlecode.flaxcrawler.concurrent.TaskQueue;
import com.googlecode.flaxcrawler.model.CrawlerTask;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.log4j.Logger;

/**
 * Standart scheduler implementation. Starts an asyncronous worker that reads urls from the queue
 * and adds them to the {@code TaskQueue}.
 */
public class DefaultScheduler implements Scheduler {

    private Logger log = Logger.getLogger(this.getClass());
    private TaskQueue taskQueue;
    private StatisticsService statisticsService;
    private final Object syncRoot = new Object();
    private final Queue<CrawlerTask> schedulerQueue = new LinkedList<CrawlerTask>();
    private final Thread workerThread;
    private String lastDomainName = "";

    public DefaultScheduler(TaskQueue taskQueue, StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
        this.taskQueue = taskQueue;

        workerThread = new Thread(new Runnable() {

            public void run() {
                DoWorkLoop();
            }
        });
        workerThread.setDaemon(true);
        workerThread.start();
        log.info("Scheduler was successfully initialized and started");
    }

    public void schedule(CrawlerTask crawlerTask) {
        synchronized (syncRoot) {
            log.debug("Enqueueing task " + crawlerTask.getUrl() + " to the scheduler queue");
            schedulerQueue.add(crawlerTask);
        }
    }

    /**
     * Reads urls from queue and adds them to the TaskQueue (if url was not crawled yet)
     */
    private void DoWorkLoop() {
        while (true) {
            CrawlerTask task = null;

            try {
                task = schedulerQueue.poll();
                if (task != null) {
                    if (!statisticsService.isCrawled(task.getUrl())) {
                        // Putting tasks that were not crawled yet to the task queue
                        // Also comparing last processed domain name with task domain name
                        // and deferring task if they are equal. This way we're trying
                        // to make queue more "sparsed"
                        String domainName = task.getDomain() == null ? "" : task.getDomain();

                        if (lastDomainName.equals(task.getDomain())) {
                            taskQueue.defer(task);
                        } else {
                            taskQueue.enqueue(task);
                        }

                        statisticsService.afterScheduling(task);
                        lastDomainName = domainName;
                        log.debug("Scheduled crawling of the " + task.getUrl());
                    } else {
                        log.debug("Url " + task.getUrl() + " was already crawled");
                    }
                }

                // Yielding context to another thread
                Thread.sleep(1);
            } catch (Exception ex) {
                log.error("Error processing task " + task == null ? "NOTASK" : task.getUrl() + " from the scheduler queue", ex);
            }
        }
    }
}

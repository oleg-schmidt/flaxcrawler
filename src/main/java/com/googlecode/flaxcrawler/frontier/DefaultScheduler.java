package com.googlecode.flaxcrawler.frontier;

import com.googlecode.flaxcrawler.concurrent.TaskQueue;
import com.googlecode.flaxcrawler.model.CrawlerTask;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.log4j.Logger;

/**
 * Standard scheduler implementation. Starts an asyncronous worker thread that reads urls from the queue
 * and adds them to the {@code TaskQueue}.
 */
public class DefaultScheduler implements Scheduler {

    private Logger log = Logger.getLogger(this.getClass());
    private TaskQueue taskQueue;
    private StatisticsService statisticsService;
    private final Object syncRoot = new Object();
    private final Queue<CrawlerTask> schedulerQueue = new LinkedList<CrawlerTask>();
    private final Thread workerThread;

    public DefaultScheduler(TaskQueue taskQueue, StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
        this.taskQueue = taskQueue;

        workerThread = new Thread(new Runnable() {

            public void run() {
                doWorkLoop();
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
    private void doWorkLoop() {
        while (true) {
            CrawlerTask task = null;

            try {
                synchronized (syncRoot) {
                    task = schedulerQueue.poll();
                }
                if (task != null) {
                    if (!statisticsService.isCrawled(task.getUrl())) {
                        taskQueue.enqueue(task);
                        statisticsService.afterScheduling(task);
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

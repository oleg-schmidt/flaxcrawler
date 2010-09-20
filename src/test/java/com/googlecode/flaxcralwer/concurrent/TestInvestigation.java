package com.googlecode.flaxcralwer.concurrent;

import com.googlecode.flaxcrawler.concurrent.BaseTask;
import com.googlecode.flaxcrawler.concurrent.BerkleyQueue;
import com.sleepycat.persist.model.Persistent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import org.apache.commons.lang.time.StopWatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ameshkov
 */
public class TestInvestigation {

    public TestInvestigation() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

    @Test
    @Ignore
    public void testLinkedListPerformance() {
        Random generator = new Random();
        Queue<String> queue = new LinkedList<String>();

        for (int i = 0; i < 1000000; i++) {
            queue.add(new String("assdasdbiaotoasjgoasjgakjsgkkasjgpasjgpasgasg") + generator.nextInt() + generator.nextDouble());
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (int i = 0; i < 100000000; i++) {
            String element = queue.poll();
            queue.add(element);
        }

        stopWatch.stop();
        System.out.println(stopWatch.getTime() / 1000 + " ms");
    }

    @Test
    @Ignore
    public void testBerkleyQueue() {
        BerkleyQueue queue = new BerkleyQueue("stats");
        try {
            queue.setQueueCapacity(500);

            for (int i = 0; i < 1000; i++) {
                queue.add(new TestTask("tratatatat"));
            }

            for (int i = 0; i < 1000; i++) {
                Object obj = queue.poll();
                assertNotNull(obj);
            }

            assertNull(queue.poll());
        } finally {
            queue.dispose();
        }
    }

    @Persistent
    public static class TestTask extends BaseTask {

        String value;

        public TestTask() {
        }

        public TestTask(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

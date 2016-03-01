package com.example;


import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;

public class FileQueueTest extends TestCase {

    FileQueueService queue;
    final String testQueueName = "FileQueueTest";

    @Before
    public void setUp() {
        this.queue = new FileQueueService();
    }

    @After
    public void tearDown() throws InterruptedException {
        this.queue.empty(testQueueName);
    }

    public void testPullNoMessageExpectsNull() throws IOException, InterruptedException {
        QueueMessage message = queue.pull(testQueueName, 1000);
        assertNull(message);
    }

    public void testPushPullExpectsSame() throws IOException, InterruptedException {
        String expectedMessage = "My message " + System.currentTimeMillis();
        QueueMessage expected = new QueueMessage(expectedMessage);

        queue.push(testQueueName, expected);

        QueueMessage actual = queue.pull(testQueueName, 0);
        assertEquals(expectedMessage, actual.getMessage());
    }

    public void testTimeoutLogic() throws IOException, InterruptedException {
        String expectedMessage1 = "My message 1 " + System.currentTimeMillis();
        String expectedMessage2 = "My message 2 " + System.currentTimeMillis();
        QueueMessage expected1 = new QueueMessage(expectedMessage1);
        QueueMessage expected2 = new QueueMessage(expectedMessage2);

        queue.push(testQueueName, expected1);
        queue.push(testQueueName, expected2);

        QueueMessage actual1 = queue.pull(testQueueName, 100000);
        assertEquals(expectedMessage1, actual1.getMessage());

        QueueMessage actual2 = queue.pull(testQueueName, 100000);
        assertEquals(expectedMessage2, actual2.getMessage());
    }


}

package com.example;

import junit.framework.TestCase;

import java.io.IOException;

public class FileQueueTest extends TestCase {
    public void testPullNoMessageExpectsNull() {
        FileQueueService queue = new FileQueueService();
        QueueMessage message = queue.pull("", 1000);
        assertNull(message);
    }

    public void testPushPullExpectsSame() throws IOException, InterruptedException {
        FileQueueService queue = new FileQueueService();
        QueueMessage expected = new QueueMessage("My file queue message");

        queue.push(expected);

        QueueMessage actual = queue.pull("", 0);
        assertEquals(expected, actual);
    }
}

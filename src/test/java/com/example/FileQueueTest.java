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

    public void testDeleteWithMessage() throws IOException, InterruptedException {
        String expectedMessage = "My message " + System.currentTimeMillis();
        QueueMessage expected = new QueueMessage(expectedMessage);

        queue.push(testQueueName, expected);

        QueueMessage actual = queue.pull(testQueueName, 0);
        queue.delete(testQueueName, actual);

        assertNull(queue.pull(testQueueName, 0));
    }

    public void testEmptyDelete() throws IOException, InterruptedException {
        String expectedMessage = "My message " + System.currentTimeMillis();
        QueueMessage expected = new QueueMessage(expectedMessage);

        queue.delete(testQueueName, expected);
    }

    public void testQueueWithThreads() throws InterruptedException {
        // You can only prove the presence of concurrent bugs, not their absence.
        // Although that's true of any code. Anyway let's see if we can identify any...
        for(int i = 0; i < 5; i++) {
            new Thread() {
                public void run() {
                    int count = 20;
                    while (count > 0) {
                        QueueMessage message = new QueueMessage("Thread queue message");
                        try {
                            queue.   push(testQueueName, message);
                            message = queue.pull(testQueueName, 1000);
                            queue.delete(testQueueName, message);
                            count--;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }

        int count = 200;
        while (count > 0) {
            QueueMessage message = new QueueMessage("Main queue message");
            try {
                queue.push(testQueueName, message);
                message = queue.pull(testQueueName, 1000);
                queue.delete(testQueueName, message);
                count--;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.example;

import com.example.exceptions.QueueFullException;
import junit.framework.TestCase;
import org.junit.Test;

public class InMemoryQueueTest extends TestCase {

    @Test(expected=NegativeArraySizeException.class)
    public void testConstructorNegativeSize() {
        // Not sure when method decorator is not working.
        // going to ghetto it for the moment...
        boolean exceptionThrown = false;
        try {
            new InMemoryQueueService(-1);
        }
        catch(NegativeArraySizeException ex) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
    }

    public void testPullNoMessageExpectsNull() {
        InMemoryQueueService queue = new InMemoryQueueService();
        QueueMessage message = queue.pull();
        assertNull(message);
    }

    public void testPushPullExpectsSame() throws QueueFullException {
        InMemoryQueueService queue = new InMemoryQueueService();
        QueueMessage expected = new QueueMessage("Message");
        queue.push(expected);

        QueueMessage actual = queue.pull();
        assertEquals(expected, actual);
    }

    public void testDeleteMessage() {
        InMemoryQueueService queue = new InMemoryQueueService();
        QueueMessage message = new QueueMessage("Message");
        message.setQueueLocation(0);
        queue.delete(message);
    }

    public void testPushQueue() throws QueueFullException {
        InMemoryQueueService queue = new InMemoryQueueService(2);

        QueueMessage expected = new QueueMessage("Message");

        queue.push(expected);
        queue.push(expected);
    }

    public void testDeleteNullParameter() {
        InMemoryQueueService queue = new InMemoryQueueService();
        queue.delete(null);
    }

    public void testQueueWrap() throws QueueFullException {
        InMemoryQueueService queue = new InMemoryQueueService(2);

        QueueMessage expected;
        QueueMessage actual;

        for(int i = 0; i < 1000; i++) {
            expected = new QueueMessage("Message " + i);

            queue.push(expected);
            actual = queue.pull();
            queue.delete(actual);

            assertEquals(actual, expected);
        }
    }

    public void testPullGivesDifferenceMessages() throws QueueFullException {
        InMemoryQueueService queue = new InMemoryQueueService(10);

        QueueMessage expected1 = new QueueMessage("Message 1");
        QueueMessage expected2 = new QueueMessage("Message 2");

        queue.push(expected1);
        queue.push(expected2);

        assertEquals(queue.pull(), expected1);
        assertEquals(queue.pull(), expected2);
    }
}

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
            InMemoryQueueService queue = new InMemoryQueueService(-1);
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
        QueueMessage expected = new QueueMessage();
        queue.push(expected);

        QueueMessage actual = queue.pull();
        assertEquals(expected, actual);
    }

    public void testDeleteMessage() {
        InMemoryQueueService queue = new InMemoryQueueService();
        QueueMessage message = new QueueMessage();
        message.setQueueLocation(0);
        queue.delete(message);
    }

    public void testPushQueue() throws QueueFullException {
        InMemoryQueueService queue = new InMemoryQueueService(2);

        QueueMessage expected = new QueueMessage();

        queue.push(expected);
        queue.push(expected);
    }

    public void testQueueWraps() throws QueueFullException {
        InMemoryQueueService queue = new InMemoryQueueService(2);

        QueueMessage expected = new QueueMessage();
        QueueMessage actual;

        for(int i = 0; i < 10; i++) {
            queue.push(expected);
            actual = queue.pull();
            queue.delete(actual);
        }
    }
}

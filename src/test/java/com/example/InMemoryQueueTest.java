package com.example;

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

    public void testPushPullExpectsSame() {
        InMemoryQueueService queue = new InMemoryQueueService();
        QueueMessage expected = new QueueMessage();
        queue.push(expected);

        QueueMessage actual = queue.pull();
        assertEquals(expected, actual);
    }

}

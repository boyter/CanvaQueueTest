package com.example;

import com.example.exceptions.QueueFullException;
import junit.framework.TestCase;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InMemoryQueueTest extends TestCase {

    // TODO Fix the use decorator
    @Test(expected=NegativeArraySizeException.class)
    public void testConstructorNegativeSize() {
        // Not sure why method decorator is not working.
        // going to ghetto constructor's tests and investigate later
        boolean exceptionThrown = false;
        try {
            new InMemoryQueueService(-1);
        }
        catch(NegativeArraySizeException ex) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
    }

    // TODO Fix the use decorator
    public void testConstructorZeroSize() {
        boolean exceptionThrown = false;
        try {
            new InMemoryQueueService(0);
        }
        catch(IllegalArgumentException ex) {
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

        QueueMessage expected = mock(QueueMessage.class);
        when(expected.getTimeout()).thenReturn(System.currentTimeMillis() - 10000000);

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

    public void testPushQueueToLimit() throws QueueFullException {
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
        InMemoryQueueService queue = new InMemoryQueueService(10);

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

    public void testQueueWithSingleSpot() throws QueueFullException {
        InMemoryQueueService queue = new InMemoryQueueService(1);

        QueueMessage expected;
        QueueMessage actual;

        for(int i = 0; i < 1000; i++) {
            expected = mock(QueueMessage.class);
            when(expected.getTimeout()).thenReturn(System.currentTimeMillis() - 10000000);

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

    public void testQueueTimeoutFirstLocked() throws QueueFullException {
        InMemoryQueueService queue = new InMemoryQueueService(2);

        QueueMessage mockMessage1 = mock(QueueMessage.class);
        QueueMessage mockMessage2 = mock(QueueMessage.class);

        // We could also use DI to mock away the system time calls but that's
        // needlessly complicated for this case, just mock the object instead
        when(mockMessage1.getTimeout()).thenReturn(System.currentTimeMillis() + 10000000);
        when(mockMessage2.getTimeout()).thenReturn(System.currentTimeMillis() - 10000000);

        queue.push(mockMessage1);
        queue.push(mockMessage2);

        assertEquals(mockMessage2, queue.pull());
    }

    public void testQueueTimeoutLocked() throws QueueFullException {
        InMemoryQueueService queue = new InMemoryQueueService(2);

        QueueMessage mockMessage1 = mock(QueueMessage.class);

        when(mockMessage1.getTimeout()).thenReturn(System.currentTimeMillis() + 10000000);
        queue.push(mockMessage1);

        assertNull(queue.pull());
    }

    public void testQueueTwoItemsTimeoutLocked() throws QueueFullException {
        InMemoryQueueService queue = new InMemoryQueueService(2);

        QueueMessage mockMessage1 = mock(QueueMessage.class);
        QueueMessage mockMessage2 = mock(QueueMessage.class);

        when(mockMessage1.getTimeout()).thenReturn(System.currentTimeMillis() + 10000000);
        when(mockMessage2.getTimeout()).thenReturn(System.currentTimeMillis() + 10000000);
        queue.push(mockMessage1);
        queue.push(mockMessage2);

        assertNull(queue.pull());
        assertNull(queue.pull());
    }
}

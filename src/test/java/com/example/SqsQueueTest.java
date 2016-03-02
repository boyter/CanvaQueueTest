package com.example;


import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.example.exceptions.QueueFullException;
import junit.framework.TestCase;
import org.mockito.Matchers;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Note, I am not familiar with the SQS API so not sure if this
 * is correct. Going to take a stab at how I think it should work.
 * As such have not implemented the visibility timeout here, guessing
 * that Amazon takes care of that for you when you configure the queue
 *
 * Too much mocking going on here... basic unit tests to check it runs
 * without null pointers and the like but at this point I would move
 * over to mainly integration tests since there are so many
 * network/security issues that could occcur
 */
public class SqsQueueTest extends TestCase {

    final String testQueueName = "SQSQueueTest";

    public void testPushMessage() throws QueueFullException {
        String expectedMessage = "My message " + System.currentTimeMillis();
        QueueMessage expected = new QueueMessage(expectedMessage);

        AmazonSQSClient mockSQSClient = mock(AmazonSQSClient.class);
        CreateQueueResult mockQueueResult = mock(CreateQueueResult.class);

        SqsQueueService queue = new SqsQueueService(mockSQSClient);

        when(mockSQSClient.createQueue(Matchers.<CreateQueueRequest>anyObject())).thenReturn(mockQueueResult);
        when(mockQueueResult.getQueueUrl()).thenReturn("http://myqueueurl/");


        queue.push(this.testQueueName, expected);
        Mockito.verify(mockSQSClient).sendMessage("http://myqueueurl/", expectedMessage);
    }

    public void testPullMessage() throws QueueFullException {

        AmazonSQSClient mockSQSClient = mock(AmazonSQSClient.class);
        CreateQueueResult mockQueueResult = mock(CreateQueueResult.class);
        ReceiveMessageResult mockMessageResult = mock(ReceiveMessageResult.class);

        SqsQueueService queue = new SqsQueueService(mockSQSClient);

        when(mockSQSClient.createQueue(Matchers.<CreateQueueRequest>anyObject())).thenReturn(mockQueueResult);
        when(mockSQSClient.receiveMessage(Matchers.anyString())).thenReturn(mockMessageResult);
        when(mockQueueResult.getQueueUrl()).thenReturn("http://myqueueurl/");


        assertNull(queue.pull(this.testQueueName, 0));
    }

    public void testDeleteMessage() {
        AmazonSQSClient mockSQSClient = mock(AmazonSQSClient.class);
        CreateQueueResult mockQueueResult = mock(CreateQueueResult.class);
        com.amazonaws.services.sqs.model.Message mockSqsMessage = mock(com.amazonaws.services.sqs.model.Message.class);

        when(mockSqsMessage.getReceiptHandle()).thenReturn("messagehandle");
        when(mockSQSClient.createQueue(Matchers.<CreateQueueRequest>anyObject())).thenReturn(mockQueueResult);
        when(mockQueueResult.getQueueUrl()).thenReturn("http://myqueueurl/");

        SqsQueueService queue = new SqsQueueService(mockSQSClient);

        QueueMessage queueMessage = new QueueMessage("something");
        queueMessage.setSqsMessage(mockSqsMessage);

        queue.delete(this.testQueueName, queueMessage);

        Mockito.verify(mockSQSClient).deleteMessage("http://myqueueurl/", "messagehandle");
    }
}

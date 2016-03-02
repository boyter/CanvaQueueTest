package com.example;


import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.example.exceptions.QueueFullException;
import junit.framework.TestCase;
import org.mockito.Matchers;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Too much mocking going on here... basic unit tests to check it runs
 * but at this point I would rely on integration tests
 */
public class SqsQueueTest extends TestCase {

    final String testQueueName = "FileQueueTest";

    public void testPushPullExpectsSame() throws QueueFullException {
        String expectedMessage = "My message " + System.currentTimeMillis();
        QueueMessage expected = new QueueMessage(expectedMessage);

        AmazonSQSClient mockSQSClient = mock(AmazonSQSClient.class);
        CreateQueueResult mockQueueResult = mock(CreateQueueResult.class);

        SqsQueueService queue = new SqsQueueService(mockSQSClient);

        when(mockSQSClient.createQueue(Matchers.<CreateQueueRequest>anyObject())).thenReturn(mockQueueResult);
        when(mockQueueResult.getQueueUrl()).thenReturn("http://myqueueurl/");


        queue.push(testQueueName, expected);
        Mockito.verify(mockSQSClient).sendMessage("http://myqueueurl/", expectedMessage);
    }
}

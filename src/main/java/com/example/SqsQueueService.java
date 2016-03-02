package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.example.exceptions.QueueFullException;

import java.util.List;

public class SqsQueueService implements QueueService {
  private final AmazonSQSClient sqsClient;

  public SqsQueueService(AmazonSQSClient sqsClient) {
    // Going to assume role based auth so no need to configure anything
    // or that keys were set before we got this object
    this.sqsClient = sqsClient;
  }

  @Override
  public void push(String queueName, QueueMessage message) throws QueueFullException {
    sqsClient.sendMessage("queueName", message.getMessage());

    CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
    String myQueueUrl = sqsClient.createQueue(createQueueRequest).getQueueUrl();

    sqsClient.sendMessage(myQueueUrl, message.getMessage());
  }

  @Override
  public QueueMessage pull(String queueName, long visibilityTimeout) {
    QueueMessage queueMessage = null;
    CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
    String myQueueUrl = sqsClient.createQueue(createQueueRequest).getQueueUrl();

    ReceiveMessageResult sqsMessageResult = sqsClient.receiveMessage(myQueueUrl);
    List<Message> messages = sqsMessageResult.getMessages();

    if(messages.size() != 0) {
      Message sqsMessage = messages.get(0);
      queueMessage = new QueueMessage(sqsMessage.getBody());
      queueMessage.setSqsMessage(sqsMessage);
    }

    return queueMessage;
  }

  @Override
  public void delete(String queueName, QueueMessage message) {
    CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
    String myQueueUrl = sqsClient.createQueue(createQueueRequest).getQueueUrl();

    String messageReceiptHandle = message.getSqsMessage().getReceiptHandle();
    sqsClient.deleteMessage(myQueueUrl, messageReceiptHandle);
  }
}

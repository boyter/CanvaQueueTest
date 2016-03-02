package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.example.exceptions.QueueFullException;

public class SqsQueueService implements QueueService {
  private final AmazonSQSClient sqsClient;

  public SqsQueueService(AmazonSQSClient sqsClient) {
    // Going to assume role based auth so no need to configure anything
    // or that they were set before we got this object
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

//
//    ReceiveMessageResult sqsMessage = sqsClient.receiveMessage(queueName);
//
//    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueName);


    return null;
  }

  @Override
  public void delete(String queueName, QueueMessage message) {

  }
}

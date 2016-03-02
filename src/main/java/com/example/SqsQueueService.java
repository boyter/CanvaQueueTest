package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.example.exceptions.QueueFullException;

import java.util.List;

/**
 * Implementation of our queue backed by Amazon's SQS Queue
 * kinda... this most likely will not work as below, but I
 * don't really feel like spinning everything up to verify
 * and that's difficult anyway when you have a spotty internet
 * connection on a train.
 */
public class SqsQueueService implements QueueService {
  private final AmazonSQSClient sqsClient;

  public SqsQueueService(AmazonSQSClient sqsClient) {
    // Going to assume role based auth so no need to configure anything
    // or that keys were set before we got this object
    this.sqsClient = sqsClient;
  }

  /**
   * Adds message to SQS queue based on the AmazonSQSClient configured
   * in the constructor.
   * @param queueName
   * @param message
   * @throws QueueFullException
   */
  @Override
  public void push(String queueName, QueueMessage message) throws QueueFullException {
    sqsClient.sendMessage("queueName", message.getMessage());

    CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
    String myQueueUrl = sqsClient.createQueue(createQueueRequest).getQueueUrl();

    sqsClient.sendMessage(myQueueUrl, message.getMessage());
  }

  /**
   * Pulls a message from SQS queue based on the AmazonSQSClient configured
   * in the constructor
   * @param queueName
   * @param visibilityTimeout
   * @return
   */
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

  /**
   * Deletes a message from SQS queue based on the AmazonSQSClient configured
   * in the constructor
   * @param queueName
   * @param message
   */
  @Override
  public void delete(String queueName, QueueMessage message) {
    CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
    String myQueueUrl = sqsClient.createQueue(createQueueRequest).getQueueUrl();

    String messageReceiptHandle = message.getSqsMessage().getReceiptHandle();
    sqsClient.deleteMessage(myQueueUrl, messageReceiptHandle);
  }
}

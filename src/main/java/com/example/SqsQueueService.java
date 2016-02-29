package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.example.exceptions.QueueFullException;

public class SqsQueueService implements QueueService {
  //
  // Task 4: Optionally implement parts of me.
  //
  // This file is a placeholder for an AWS-backed implementation of QueueService.  It is included
  // primarily so you can quickly assess your choices for method signatures in QueueService in
  // terms of how well they map to the implementation intended for a production environment.
  //

  public SqsQueueService(AmazonSQSClient sqsClient) {
  }


  @Override
  public void push(String queueName, QueueMessage message) throws QueueFullException {

  }

  @Override
  public QueueMessage pull(String queueName, long visibilityTimeout) {
    return null;
  }

  @Override
  public void delete(String queueName, QueueMessage message) {

  }
}

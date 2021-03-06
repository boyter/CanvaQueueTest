package com.example;

import com.example.exceptions.QueueFullException;

import java.io.IOException;

public interface QueueService {
    void push(String queueName, QueueMessage message) throws QueueFullException, InterruptedException, IOException;
    QueueMessage pull(String queueName, long visibilityTimeout) throws InterruptedException, IOException;
    void delete(String queueName, QueueMessage message) throws InterruptedException;

  // Task 1: Define me.
  //
  // This interface should include the following methods.  You should choose appropriate
  // signatures for these methods that prioritise simplicity of implementation for the range of
  // intended implementations (in-memory, file, and SQS).  You may include additional methods if
  // you choose.
  //
  // - push
  //   pushes a message onto a queue.
  // - pull
  //   retrieves a single message from a queue.
  // - delete
  //   deletes a message from the queue that was received by pull().
  //

}

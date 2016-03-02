package com.example;

import com.amazonaws.services.sqs.model.Message;

import java.text.MessageFormat;

public class QueueMessage {
    private int queueLocation;
    private long timeout;
    private String message;
    private Message sqsMessage;

    public QueueMessage(String message) {
        this.message = message;
    }

    /**
     * Used internally. Do not change this value.
     * @return
     */
    public int getQueueLocation() {
        return queueLocation;
    }

    /**
     * Used internally. Do not change this value;
     * @param queueLocation
     */
    public void setQueueLocation(int queueLocation) {
        this.queueLocation = queueLocation;
    }

    /**
     * Used internally. No not change this value.
     * @return
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Used internally. Do not change this value.
     * @param timeout
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public QueueMessage clone() {
        QueueMessage clone = new QueueMessage(this.getMessage());
        clone.setTimeout(this.getTimeout());
        return clone;
    }

    public boolean areSame(QueueMessage queueMessage) {
        if(queueMessage == null) {
            return false;
        }

        if(queueMessage.getMessage().equals(this.getMessage()) &&
                queueMessage.getTimeout() == this.getTimeout()) {
            return true;
        }

        return false;
    }

    public String stringEncode() {
        // Making the huge assumption that ::: is ok as a delimiter
        // better to use something like JSON but readme said no other libraries
        return MessageFormat.format("{0}:::{1}", this.getMessage(), Long.toString(this.getTimeout()));
    }

    public void stringDecode(String toDecode) {
        // Making the huge assumption that ::: is ok as a delimiter
        // better to use something like JSON but readme said no other libraries
        String[] splitString = toDecode.split(":::");

        if(splitString.length == 2) {
            this.setMessage(splitString[0]);
            try {
                this.setTimeout(Long.parseLong(splitString[1]));
            }
            catch(NumberFormatException ex) {
                this.setTimeout(0L);
            }
        }
    }

    /**
     * Returns the amazon SQS message for this message or
     * null
     * used internally by SQSQueueService
     * @return
     */
    public Message getSqsMessage() {
        return sqsMessage;
    }

    /**
     * Sets the amazon SQS message for this message
     * used internally by SQSQueueService
     * @param sqsMessage
     */
    public void setSqsMessage(Message sqsMessage) {
        this.sqsMessage = sqsMessage;
    }
}

package com.example;

public class QueueMessage {
    private int queueLocation;
    private long timeout;
    private String message;

    public QueueMessage(String message) {
        this.message = message;
    }

    public int getQueueLocation() {
        return queueLocation;
    }

    public void setQueueLocation(int queueLocation) {
        this.queueLocation = queueLocation;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

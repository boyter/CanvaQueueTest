package com.example;

import java.text.MessageFormat;

public class QueueMessage {
    private int queueLocation;
    private long timeout;
    private String message;

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

    public String stringEncode() {
        // Making the huge assumption that :: is ok as a delimiter
        // better to use something like JSON but I have no internet
        // to pull down a library right now
        return MessageFormat.format("{0}::{1}", this.getMessage(), Long.toString(this.getTimeout()));
    }

    public void stringDecode(String toDecode) {
        // Making the huge assumption that :: is ok as a delimiter
        // better to use something like JSON but I have no internet
        // to pull down a library right now
        String[] splitString = toDecode.split("::");

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
}

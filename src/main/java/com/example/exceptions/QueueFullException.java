package com.example.exceptions;

public class QueueFullException extends Exception {
    public QueueFullException(String message){
        super(message);
    }
}

package com.example;

import org.junit.Test;

public class InMemoryQueueTest {

    @Test(expected=NegativeArraySizeException.class)
    public void testConstructorNegativeSize() {
        InMemoryQueueService queue = new InMemoryQueueService(-1);
    }
}

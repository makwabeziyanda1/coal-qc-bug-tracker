package com.coalqc.model;

public class IllegalStatusTransitionException extends RuntimeException {

    public IllegalStatusTransitionException(Status from, Status to) {
        super("Cannot transition from " + from + " to " + to);
    }
}

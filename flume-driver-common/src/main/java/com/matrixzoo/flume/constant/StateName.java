package com.matrixzoo.flume.constant;

public enum StateName {
    NOT_STARTED, STARTING, IDLE, BUSY, SHUTTING_DOWN, ERROR, DEAD, SUCCESS, RUNNING;

    public String toString() {
        return name().toLowerCase();
    }
}

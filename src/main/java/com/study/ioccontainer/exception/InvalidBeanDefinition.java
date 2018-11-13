package com.study.ioccontainer.exception;

public class InvalidBeanDefinition extends RuntimeException {
    public InvalidBeanDefinition(String message) {
        super(message);
    }

    public InvalidBeanDefinition(String message, Throwable cause) {
        super(message, cause);
    }
}

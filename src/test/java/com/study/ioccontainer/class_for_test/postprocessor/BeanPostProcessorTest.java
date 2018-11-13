package com.study.ioccontainer.class_for_test.postprocessor;

import com.study.ioccontainer.lifecycle.BeanPostProcessor;

public class BeanPostProcessorTest implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String id) throws RuntimeException {
        return new TestPostProcessorReplace();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String id) throws RuntimeException {
        return new TestPostProcessorReplace();
    }
}

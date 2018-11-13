package com.study.ioccontainer.class_for_test.factory_postprocessor;

import com.study.ioccontainer.entity.BeanDefinition;
import com.study.ioccontainer.lifecycle.BeanFactoryPostProcessor;

import java.util.Map;

public class BeanFactoryPostProcessorTest implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(Map<String, BeanDefinition> definitions) {
        for (BeanDefinition definition : definitions.values()) {
            definition.setClassName("ReplacedByBeanFactoryPostProcessor");
        }
    }
}

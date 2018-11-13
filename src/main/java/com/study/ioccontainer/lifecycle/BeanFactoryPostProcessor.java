package com.study.ioccontainer.lifecycle;

import com.study.ioccontainer.entity.BeanDefinition;

import java.util.Map;

public interface BeanFactoryPostProcessor {
    void postProcessBeanFactory(Map<String, BeanDefinition> definitions);
}
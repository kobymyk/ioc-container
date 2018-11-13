package com.study.ioccontainer.context.reader;

import com.study.ioccontainer.entity.BeanDefinition;

import java.util.Map;

public interface BeanDefinitionReader {

    Map<String, BeanDefinition> readBeanDefinitions();
}

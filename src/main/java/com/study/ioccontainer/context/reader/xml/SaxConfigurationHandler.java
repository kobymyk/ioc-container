package com.study.ioccontainer.context.reader.xml;

import com.study.ioccontainer.entity.BeanDefinition;
import com.study.ioccontainer.exception.InvalidBeanDefinition;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class SaxConfigurationHandler extends DefaultHandler {

    private Deque<BeanDefinition> beanDefinitions = new ArrayDeque<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equalsIgnoreCase("bean")) {
            BeanDefinition beanDefinition = new BeanDefinition();
            String id = attributes.getValue("id");
            String className = attributes.getValue("class");
            beanDefinition.setId(id);
            beanDefinition.setClassName(className);
            beanDefinition.setValueDependencies(new HashMap<>());
            beanDefinition.setRefDependencies(new HashMap<>());
            beanDefinitions.push(beanDefinition);
        } else if (qName.equals("property")) {
            BeanDefinition beanDefinition = beanDefinitions.peek();
            String name = attributes.getValue("name");
            validatePropertyName(beanDefinition, name);
            String value = attributes.getValue("value");
            String ref = attributes.getValue("ref");
            validatePropertyAttributes(name, value, ref);
            if (value != null) {
                beanDefinition.getValueDependencies().put(name, value);
            } else {
                beanDefinition.getRefDependencies().put(name, ref);
            }
        }
    }

    public Map<String, BeanDefinition> getBeanDefinitions() {
        Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
        for (BeanDefinition beanDefinition : beanDefinitions) {
            beanDefinitionMap.put(beanDefinition.getId(), beanDefinition);
        }
        return beanDefinitionMap;
    }

    private void validatePropertyName(BeanDefinition beanDefinition, String name) {
        Map<String, String> valueDependencies = beanDefinition.getValueDependencies();
        Map<String, String> refDependencies = beanDefinition.getRefDependencies();

        if (name == null) {
            throw new InvalidBeanDefinition("property does not have name");
        } else if (valueDependencies.containsKey(name) || refDependencies.containsKey(name)) {
            throw new InvalidBeanDefinition("duplicated property " + name);
        }
    }

    private void validatePropertyAttributes(String name, String value, String ref) {
        if (value == null && ref == null) {
            throw new InvalidBeanDefinition("property " + name + " does not have value or ref");
        }
        if (value != null && ref != null) {
            throw new InvalidBeanDefinition("property " + name + " has both value and ref");
        }
    }
}
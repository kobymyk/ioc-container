package com.study.ioccontainer.context.reader.xml;

import com.study.ioccontainer.context.reader.BeanDefinitionReader;
import com.study.ioccontainer.entity.BeanDefinition;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.Map;

public class XmlBeanDefinitionReader implements BeanDefinitionReader {
    private InputStream definitionInputStream;

    public XmlBeanDefinitionReader(InputStream definitionInputStream) {
        this.definitionInputStream = definitionInputStream;
    }

    public Map<String, BeanDefinition> readBeanDefinitions() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            SaxConfigurationHandler handler = new SaxConfigurationHandler();
            parser.parse(definitionInputStream, handler);
            return handler.getBeanDefinitions();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot read Bean definitions", e);
        }
    }

}

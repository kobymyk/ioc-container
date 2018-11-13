package com.study.ioccontainer.context.reader.xml;

import com.study.ioccontainer.entity.BeanDefinition;
import org.junit.Test;

import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class XmlBeanDefinitionReaderTest {

    @Test
    public void readBeanDefinitionsTest() {

        String configFileName = "application-context.xml";
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configFileName);
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(inputStream);
        Map<String, BeanDefinition> beanDefinitions = reader.readBeanDefinitions();

        assertEquals(2, beanDefinitions.size());

        BeanDefinition beanDefinition1 = beanDefinitions.get("test1");
        assertEquals("com.study.ioccontainer.class_for_test.Test1", beanDefinition1.getClassName());

        assertEquals(1, beanDefinition1.getValueDependencies().size());
        String value1 = beanDefinition1.getValueDependencies().get("testName1");
        assertEquals("testValue1", value1);

        assertEquals(1, beanDefinition1.getRefDependencies().size());
        String value2 = beanDefinition1.getRefDependencies().get("testName2");
        assertEquals("test2", value2);

    }
}
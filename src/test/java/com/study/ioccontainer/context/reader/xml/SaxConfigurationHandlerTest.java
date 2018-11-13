package com.study.ioccontainer.context.reader.xml;

import com.study.ioccontainer.entity.BeanDefinition;
import com.study.ioccontainer.exception.InvalidBeanDefinition;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SaxConfigurationHandlerTest {

    SAXParser parser;
    Map<String, BeanDefinition> beanDefinitions;

    @Before
    public void setUp() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        parser = factory.newSAXParser();
    }

    @Test
    public void parseElementWithoutProperties() throws SAXException, IOException {
        String xmlDocument = "<beans><bean id=\"test\" class=\"com.test\"></bean></beans>";
        parse(xmlDocument);

        assertEquals(1, beanDefinitions.size());
        BeanDefinition beanDefinition = beanDefinitions.get("test");
        assertEquals("test", beanDefinition.getId());
        assertEquals("com.test", beanDefinition.getClassName());
    }

    @Test
    public void parseElementWithPropertyValues() throws ParserConfigurationException, SAXException, IOException {
        String xmlDocument = "<beans>" +
                "<bean id=\"test\" class=\"com.test\">" +
                "   <property name=\"testName1\" value=\"testValue1\" />" +
                "   <property name=\"testName2\" value=\"testValue2\" />" +
                "</bean>" +
                "</beans>";
        parse(xmlDocument);

        assertEquals(1, beanDefinitions.size());
        BeanDefinition beanDefinition = beanDefinitions.get("test");

        Map<String, String> valueDependencies = beanDefinition.getValueDependencies();
        assertEquals(2, valueDependencies.size());
        String propertyValue1 = valueDependencies.get("testName1");
        String propertyValue2 = valueDependencies.get("testName2");
        assertEquals("testValue1", propertyValue1);
        assertEquals("testValue2", propertyValue2);

        assertEquals(0, beanDefinition.getRefDependencies().size());
    }

    @Test
    public void parseElementWithPropertyValuesAndRefs() throws ParserConfigurationException, SAXException, IOException {
        String xmlDocument = "<beans>" +
                "<bean id=\"test\" class=\"com.test\">" +
                "   <property name=\"testName1\" value=\"testValue1\" />" +
                "   <property name=\"testName2\" ref=\"testRef2\" />" +
                "</bean>" +
                "</beans>";
        parse(xmlDocument);

        assertEquals(1, beanDefinitions.size());
        BeanDefinition beanDefinition = beanDefinitions.get("test");

        Map<String, String> valueDependencies = beanDefinition.getValueDependencies();
        assertEquals(1, valueDependencies.size());
        String propertyValue1 = valueDependencies.get("testName1");
        assertEquals("testValue1", propertyValue1);

        Map<String, String> refDependencies = beanDefinition.getRefDependencies();
        assertEquals(1, refDependencies.size());
        String propertyValue2 = refDependencies.get("testName2");
        assertEquals("testRef2", propertyValue2);
    }


    @Test(expected = InvalidBeanDefinition.class)
    public void validatePropertyWithoutName() throws IOException, SAXException {
        String xmlDocument = "<beans>" +
                "<bean id=\"test\" class=\"com.test\">" +
                "</bean>" +
                "   <property value=\"testValue1\" />" +
                "</beans>";
        parse(xmlDocument);
    }

    @Test(expected = InvalidBeanDefinition.class)
    public void validatePropertyDuplicateName() throws IOException, SAXException {
        String xmlDocument = "<beans>" +
                "<bean id=\"test\" class=\"com.test\">" +
                "   <property name=\"testName1\" value=\"testValue1\" />" +
                "   <property name=\"testName1\" ref=\"testRef2\" />" +
                "</bean>" +
                "</beans>";
        parse(xmlDocument);
    }

    @Test(expected = InvalidBeanDefinition.class)
    public void validatePropertyWithoutValue() throws IOException, SAXException {
        String xmlDocument = "<beans>" +
                "<bean id=\"test\" class=\"com.test\">" +
                "   <property name=\"testName1\" />" +
                "</bean>" +
                "</beans>";
        parse(xmlDocument);
    }

    @Test(expected = InvalidBeanDefinition.class)
    public void validatePropertyBothValueAndRef() throws IOException, SAXException {
        String xmlDocument = "<beans>" +
                "<bean id=\"test\" class=\"com.test\">" +
                "   <property name=\"testName1\" value=\"testValue1\" ref=\"testRef2\" />" +
                "</bean>" +
                "</beans>";
        parse(xmlDocument);
    }


    private void parse(String xmlDocument) throws IOException, SAXException {
        InputStream inputStream = new ByteArrayInputStream(xmlDocument.getBytes());
        SaxConfigurationHandler handler = new SaxConfigurationHandler();
        parser.parse(inputStream, handler);
        beanDefinitions = handler.getBeanDefinitions();
    }

}
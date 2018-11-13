package com.study.ioccontainer.context.impl;

import com.study.ioccontainer.class_for_test.Test1;
import com.study.ioccontainer.class_for_test.Test2;
import com.study.ioccontainer.class_for_test.postconstruct.TestClassWithPostConstruct;
import com.study.ioccontainer.class_for_test.postprocessor.TestPostProcessorReplace;
import com.study.ioccontainer.context.reader.xml.XmlBeanDefinitionReader;
import com.study.ioccontainer.entity.Bean;
import com.study.ioccontainer.entity.BeanDefinition;
import com.study.ioccontainer.lifecycle.BeanFactoryPostProcessor;
import com.study.ioccontainer.lifecycle.BeanPostProcessor;
import org.junit.Test;

import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


public class ClassPathApplicationContextTest {

    final static String CONTEXT_FILE = "application-context.xml";
    final static String CONTEXT_FILE_EXTENDED_LIFECYCLE = "application-context-for-extended-lifecycle.xml";

    @Test
    public void endToEndBeanCreation() {
        ClassPathApplicationContext applicationContext = new ClassPathApplicationContext(CONTEXT_FILE);
        Map<String, Bean> beans = applicationContext.getBeans();
        assertEquals(2, beans.size());

        String key1 = "test1";
        Bean bean1 = beans.get("test1");
        Object object1 = bean1.getValue();
        assertEquals(Test1.class, object1.getClass());
        assertNotNull(bean1.getId());
        assertNotNull(bean1.getValue());
        assertEquals(key1, bean1.getId());

        String key2 = "test2";
        Bean bean2 = beans.get("test2");
        Object object2 = bean2.getValue();
        assertEquals(Test2.class, object2.getClass());
        assertNotNull(bean2.getId());
        assertNotNull(bean2.getValue());
        assertEquals(key2, bean2.getId());
    }

    @Test
    public void processBeanFactoryPostProcessorTest() {
        ClassPathApplicationContext applicationContext = new ClassPathApplicationContext();
        InputStream inputStream
                = this.getClass().getClassLoader().getResourceAsStream(CONTEXT_FILE_EXTENDED_LIFECYCLE);
        Map<String, BeanDefinition> beanDefinitions = applicationContext.readBeanDefinitions(new XmlBeanDefinitionReader(inputStream));
        assertEquals(6, beanDefinitions.size());
        Map<String, BeanDefinition> beanFactoryPostProcessorDefinitions = applicationContext.findBeanFactoryPostProcessorDefinitions(beanDefinitions);
        assertEquals(1, beanFactoryPostProcessorDefinitions.size());
        beanDefinitions.entrySet().removeAll(beanFactoryPostProcessorDefinitions.entrySet());
        assertEquals(5, beanDefinitions.size());

        Map<String, Bean> beanFactoryPostProcessors = applicationContext.constructBeans(beanFactoryPostProcessorDefinitions);
        assertEquals(1, beanFactoryPostProcessors.size());

        // run processing
        beanDefinitions = applicationContext.processBeanFactoryPostProcessor(beanDefinitions, beanFactoryPostProcessors);

        for (Bean bean : beanFactoryPostProcessors.values()) {
            Class<?> clazz = bean.getValue().getClass();
            assertTrue(BeanFactoryPostProcessor.class.isAssignableFrom(clazz));
        }

        for (BeanDefinition definition : beanDefinitions.values()) {
            assertEquals("ReplacedByBeanFactoryPostProcessor", definition.getClassName());
        }
    }

    @Test
    public void constructBeans() {
        ClassPathApplicationContext applicationContext = new ClassPathApplicationContext();
        InputStream inputStream =
                this.getClass().getClassLoader().getResourceAsStream(CONTEXT_FILE);
        Map<String, BeanDefinition> beanDefinitions = applicationContext.readBeanDefinitions(new XmlBeanDefinitionReader(inputStream));

        Map<String, Bean> beans = applicationContext.constructBeans(beanDefinitions);
        assertEquals(2, beans.size());

        String key1 = "test1";
        Bean bean1 = beans.get("test1");
        Object object1 = bean1.getValue();
        assertEquals(Test1.class, object1.getClass());
        assertNotNull(bean1.getId());
        assertNotNull(bean1.getValue());
        assertEquals(key1, bean1.getId());

        String key2 = "test2";
        Bean bean2 = beans.get("test2");
        Object object2 = bean2.getValue();
        assertEquals(Test2.class, object2.getClass());
        assertNotNull(bean2.getId());
        assertNotNull(bean2.getValue());
        assertEquals(key2, bean2.getId());
    }

    @Test
    public void constructAndInjectValueDependencies() {
        ClassPathApplicationContext applicationContext = new ClassPathApplicationContext();
        InputStream inputStream
                = this.getClass().getClassLoader().getResourceAsStream(CONTEXT_FILE);
        Map<String, BeanDefinition> beanDefinitions = applicationContext.readBeanDefinitions(new XmlBeanDefinitionReader(inputStream));

        Map<String, Bean> beans = applicationContext.constructBeans(beanDefinitions);

        beans = applicationContext.injectValueDependencies(beanDefinitions, beans);

        // bean1
        String key1 = "test1";
        Bean bean1 = beans.get(key1);
        Object object1 = bean1.getValue();
        Test1 test1 = (Test1) object1;

        // Value dependency
        Object paramObject1 = test1.getTestName1();
        assertEquals(String.class, paramObject1.getClass());
        String paramValue1 = (String) paramObject1;
        assertEquals("testValue1", paramValue1);

        // Ref dependency
        assertNull(test1.getTestName2());

        //bean2
        String key2 = "test2";
        Bean bean2 = beans.get(key2);
        Object object2 = bean2.getValue();
        Test2 test2 = (Test2) object2;

        // Value dependency
        Object paramObject2 = test2.getTestName1();
        assertEquals(String.class, paramObject2.getClass());
        String paramValue2 = (String) paramObject2;
        assertEquals("testValue2", paramValue2);

        // Ref dependency
        assertNull(test2.getTestName2());
    }

    @Test
    public void constructAndInjectRefDependencies() {
        ClassPathApplicationContext applicationContext = new ClassPathApplicationContext();
        InputStream inputStream
                = this.getClass().getClassLoader().getResourceAsStream(CONTEXT_FILE);
        Map<String, BeanDefinition> beanDefinitions = applicationContext.readBeanDefinitions(new XmlBeanDefinitionReader(inputStream));

        Map<String, Bean> beans = applicationContext.constructBeans(beanDefinitions);

        beans = applicationContext.injectRefDependencies(beanDefinitions, beans);

        // bean1
        String key1 = "test1";
        Bean bean1 = beans.get(key1);
        Object object1 = bean1.getValue();
        Test1 test1 = (Test1) object1;

        // Value dependency
        assertNull(test1.getTestName1());

        // Ref dependency
        Object paramObject1 = test1.getTestName2();
        assertEquals(Test2.class, paramObject1.getClass());
        Test2 paramValue1 = (Test2) paramObject1;
        assertEquals(beans.get("test2").getValue().getClass(), paramValue1.getClass());

        //bean2
        String key2 = "test2";
        Bean bean2 = beans.get(key2);
        Object object2 = bean2.getValue();
        Test2 test2 = (Test2) object2;

        // Value dependency
        assertNull(test2.getTestName1());

        // Ref dependency
        Object paramObject2 = test2.getTestName2();
        assertEquals(Test1.class, paramObject2.getClass());
        Test1 paramValue2 = (Test1) paramObject2;
        assertEquals(beans.get(key1).getValue().getClass(), paramValue2.getClass());

    }

    @Test
    public void constructAndPostConstructTest() {
        ClassPathApplicationContext applicationContext = new ClassPathApplicationContext();
        InputStream inputStream
                = this.getClass().getClassLoader().getResourceAsStream(CONTEXT_FILE_EXTENDED_LIFECYCLE);
        Map<String, BeanDefinition> beanDefinitions = applicationContext.readBeanDefinitions(new XmlBeanDefinitionReader(inputStream));
        Map<String, Bean> beans = applicationContext.constructBeans(beanDefinitions);

        Bean bean = beans.get("testWithPostConstruct");
        TestClassWithPostConstruct object = (TestClassWithPostConstruct) bean.getValue();
        assertFalse(object.isPostConstruct());

        // run processing
        applicationContext.processPostConstruct(beans);

        assertTrue(object.isPostConstruct());

    }

    @Test
    public void constructAndPostProcessBeforeInitializationTest() {
        ClassPathApplicationContext applicationContext = new ClassPathApplicationContext();
        InputStream inputStream
                = this.getClass().getClassLoader().getResourceAsStream(CONTEXT_FILE_EXTENDED_LIFECYCLE);
        Map<String, BeanDefinition> beanDefinitions = applicationContext.readBeanDefinitions(new XmlBeanDefinitionReader(inputStream));
        Map<String, Bean> beans = applicationContext.constructBeans(beanDefinitions);
        assertEquals(6, beans.size());

        Map<String, Bean> beanPostProcessors = applicationContext.findPostProcessors(beans);
        assertEquals(1, beanPostProcessors.size());

        beans.entrySet().removeAll(beanPostProcessors.entrySet());
        assertEquals(5, beans.size());

        // run processing
        beans = applicationContext.postProcessBeforeInitialization(beans, beanPostProcessors);

        for (Bean bean : beanPostProcessors.values()) {
            Class<?> clazz = bean.getValue().getClass();
            assertTrue(BeanPostProcessor.class.isAssignableFrom(clazz));
        }

        for (Bean bean : beans.values()) {
            assertEquals(TestPostProcessorReplace.class, bean.getValue().getClass());
        }
    }

    @Test
    public void constructAndPostProcessAfterInitializationTest() {
        ClassPathApplicationContext applicationContext = new ClassPathApplicationContext();
        InputStream inputStream
                = this.getClass().getClassLoader().getResourceAsStream(CONTEXT_FILE_EXTENDED_LIFECYCLE);
        Map<String, BeanDefinition> beanDefinitions = applicationContext.readBeanDefinitions(new XmlBeanDefinitionReader(inputStream));
        Map<String, Bean> beans = applicationContext.constructBeans(beanDefinitions);
        assertEquals(6, beans.size());

        Map<String, Bean> beanPostProcessors = applicationContext.findPostProcessors(beans);
        assertEquals(1, beanPostProcessors.size());

        beans.entrySet().removeAll(beanPostProcessors.entrySet());
        assertEquals(5, beans.size());

        // run processing
        beans = applicationContext.postProcessAfterInitialization(beans, beanPostProcessors);

        for (Bean bean : beanPostProcessors.values()) {
            Class<?> clazz = bean.getValue().getClass();
            assertTrue(BeanPostProcessor.class.isAssignableFrom(clazz));
        }

        for (Bean bean : beans.values()) {
            assertEquals(TestPostProcessorReplace.class, bean.getValue().getClass());
        }
    }

}
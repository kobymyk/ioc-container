package com.study.ioccontainer.context.impl;

import com.google.common.annotations.VisibleForTesting;
import com.study.ioccontainer.context.ApplicationContext;
import com.study.ioccontainer.context.reader.BeanDefinitionReader;
import com.study.ioccontainer.context.reader.xml.XmlBeanDefinitionReader;
import com.study.ioccontainer.entity.Bean;
import com.study.ioccontainer.entity.BeanDefinition;
import com.study.ioccontainer.exception.BeanInstantiationException;
import com.study.ioccontainer.exception.GetBeanException;
import com.study.ioccontainer.exception.InvalidBeanDefinition;
import com.study.ioccontainer.lifecycle.BeanFactoryPostProcessor;
import com.study.ioccontainer.lifecycle.BeanPostProcessor;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ClassPathApplicationContext implements ApplicationContext {

    private Map<String, Bean> beans;

    //@VisibleForTesting
    ClassPathApplicationContext() {
    }

    public ClassPathApplicationContext(String configFileName) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configFileName);

        Map<String, BeanDefinition> beanDefinitions = readBeanDefinitions(new XmlBeanDefinitionReader(inputStream));
        Map<String, BeanDefinition> beanFactoryPostProcessorDefinitions = findBeanFactoryPostProcessorDefinitions(beanDefinitions);
        beanDefinitions.entrySet().removeAll(beanFactoryPostProcessorDefinitions.entrySet());

        Map<String, Bean> beanFactoryPostProcessors = constructBeans(beanFactoryPostProcessorDefinitions);
        beanDefinitions = processBeanFactoryPostProcessor(beanDefinitions, beanFactoryPostProcessors);

        beans = constructBeans(beanDefinitions);
        beans = injectValueDependencies(beanDefinitions, beans);
        beans = injectRefDependencies(beanDefinitions, beans);

        Map<String, Bean> beanPostProcessors = findPostProcessors(beans);
        beans.entrySet().removeAll(beanPostProcessors.entrySet());

        beans = postProcessBeforeInitialization(beans, beanPostProcessors);
        beans = processPostConstruct(beans);
        beans = postProcessAfterInitialization(beans, beanPostProcessors);
    }

    //@VisibleForTesting
    Map<String, BeanDefinition> readBeanDefinitions(BeanDefinitionReader beanDefinitionReader) {
        return beanDefinitionReader.readBeanDefinitions();
    }

    //@VisibleForTesting
    Map<String, BeanDefinition> findBeanFactoryPostProcessorDefinitions(Map<String, BeanDefinition> beanDefinitions) {
        Map<String, BeanDefinition> beanFactoryPostProcessorBeanDefinitions = new HashMap<>();
        for (String key : beanDefinitions.keySet()) {
            BeanDefinition beanDefinition = beanDefinitions.get(key);
            String className = beanDefinition.getClassName();
            try {
                Class<?> clazz = Class.forName(className);
                if (BeanFactoryPostProcessor.class.isAssignableFrom(clazz)) {
                    beanFactoryPostProcessorBeanDefinitions.put(key, beanDefinition);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new InvalidBeanDefinition("No class for name " + className, e);
            }
        }
        return beanFactoryPostProcessorBeanDefinitions;
    }

    //@VisibleForTesting
    Map<String, BeanDefinition> processBeanFactoryPostProcessor(Map<String, BeanDefinition> beanDefinitions, Map<String, Bean> beanFactoryPostProcessors) {
        for (Bean bean : beanFactoryPostProcessors.values()) {
            BeanFactoryPostProcessor beanFactoryPostProcessor = (BeanFactoryPostProcessor) bean.getValue();
            beanFactoryPostProcessor.postProcessBeanFactory(beanDefinitions);
        }
        return beanDefinitions;
    }

    //@VisibleForTesting
    Map<String, Bean> constructBeans(Map<String, BeanDefinition> beanDefinitions) {
        Map<String, Bean> beans = new HashMap<>();
        for (String key : beanDefinitions.keySet()) {
            String className = null;
            try {
                BeanDefinition beanDefinition = beanDefinitions.get(key);
                className = beanDefinition.getClassName();
                Class<?> clazz = Class.forName(className);
                Bean bean = new Bean();
                bean.setId(key);
                Object object = clazz.newInstance();
                bean.setValue(object);
                beans.put(key, bean);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new BeanInstantiationException("Class " + className + " not found", e);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new BeanInstantiationException("Cannot access constructor for " + className, e);
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new BeanInstantiationException("Cannot create instance of " + className, e);
            }
        }
        return beans;
    }

    //@VisibleForTesting
    Map<String, Bean> injectValueDependencies(Map<String, BeanDefinition> beanDefinitions, Map<String, Bean> beans) {
        for (String key : beanDefinitions.keySet()) {
            BeanDefinition beanDefinition = beanDefinitions.get(key);
            Bean bean = beans.get(key);
            Object object = bean.getValue();
            Class<?> clazz = object.getClass();
            Map<String, String> valueDependencies = beanDefinition.getValueDependencies();

            for (String dependencyName : valueDependencies.keySet()) {
                Class<?> dependencyClass;
                try {
                    Field field = clazz.getDeclaredField(dependencyName);
                    dependencyClass = field.getType();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    throw new BeanInstantiationException("Field not found", e);
                }
                Method method = getSetter(clazz, dependencyName, dependencyClass);
                if (method == null) {
                    throw new BeanInstantiationException("Setter method not found");
                }
                String value = valueDependencies.get(dependencyName);
                invokeValueParameter(object, method, dependencyClass, value);
            }
        }
        return beans;
    }

    //@VisibleForTesting
    Map<String, Bean> injectRefDependencies(Map<String, BeanDefinition> beanDefinitions, Map<String, Bean> beans) {
        for (String key : beanDefinitions.keySet()) {
            BeanDefinition beanDefinition = beanDefinitions.get(key);
            Bean bean = beans.get(key);
            Object object = bean.getValue();
            Class<?> clazz = object.getClass();
            Map<String, String> refDependencies = beanDefinition.getRefDependencies();

            for (String dependencyName : refDependencies.keySet()) {
                Class<?> dependencyClass;
                try {
                    Field field = clazz.getDeclaredField(dependencyName);
                    dependencyClass = field.getType();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    throw new BeanInstantiationException("Field not found", e);
                }
                Method method = getSetter(clazz, dependencyName, dependencyClass);
                if (method == null) {
                    throw new BeanInstantiationException("Appropriate setter method not found");
                }
                String valueBeanId = refDependencies.get(dependencyName);
                Object value = beans.get(valueBeanId).getValue();
                invokeRefParameter(object, method, dependencyClass, value);
            }
        }
        return beans;
    }

    //@VisibleForTesting
    Map<String, Bean> findPostProcessors(Map<String, Bean> beans) {
        Map<String, Bean> beanBeanPostProcessors = new HashMap<>();
        for (String key : beans.keySet()) {
            Bean bean = beans.get(key);
            Class<?> clazz = bean.getValue().getClass();
            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                beanBeanPostProcessors.put(key, bean);
            }
        }
        return beanBeanPostProcessors;
    }

    //@VisibleForTesting
    Map<String, Bean> postProcessBeforeInitialization(Map<String, Bean> beans, Map<String, Bean> beanPostProcessors) {
        for (Bean systemBean : beanPostProcessors.values()) {
            BeanPostProcessor beanPostProcessor = (BeanPostProcessor) systemBean.getValue();
            for (Map.Entry<String, Bean> entry : beans.entrySet()) {
                Bean bean = entry.getValue();
                Object object = beanPostProcessor.postProcessBeforeInitialization(bean.getValue(), bean.getId());
                bean.setValue(object);
            }
        }
        return beans;
    }

    //@VisibleForTesting
    Map<String, Bean> processPostConstruct(Map<String, Bean> beans) {
        for (Map.Entry<String, Bean> entry : beans.entrySet()) {
            try {
                Object object = entry.getValue().getValue();
                for (Method method : object.getClass().getMethods()) {
                    if (method.getAnnotation(PostConstruct.class) != null) {
                        method.invoke(object);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new BeanInstantiationException("Post construct method isn't accessible for" + entry.getKey(), e);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new BeanInstantiationException("Can't invoke Post construct method for" + entry.getKey(), e);
            }
        }
        return beans;
    }

    //@VisibleForTesting
    Map<String, Bean> postProcessAfterInitialization(Map<String, Bean> beans, Map<String, Bean> beanPostProcessors) {
        for (Bean systemBean : beanPostProcessors.values()) {
            BeanPostProcessor beanPostProcessor = (BeanPostProcessor) systemBean.getValue();
            for (Map.Entry<String, Bean> entry : beans.entrySet()) {
                Bean bean = entry.getValue();
                Object object = beanPostProcessor.postProcessAfterInitialization(bean.getValue(), bean.getId());
                bean.setValue(object);
            }
        }
        return beans;
    }

    private Method getSetter(Class<?> clazz, String dependencyName, Class<?> dependencyClass) {
        String setterName = "set" + Character.toUpperCase(dependencyName.charAt(0)) + dependencyName.substring(1);
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(setterName)
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0] == dependencyClass
            ) {
                return method;
            }
        }
        return null;
    }

    private void invokeValueParameter(Object object, Method method, Class<?> valueClass, String value) {
        try {
            if (valueClass == int.class || valueClass == Integer.class) {
                method.invoke(object, Integer.parseInt(value));
            } else if (valueClass == double.class || valueClass == Double.class) {
                method.invoke(object, Double.parseDouble(value));
            } else if (valueClass == boolean.class || valueClass == Boolean.class) {
                method.invoke(object, Boolean.parseBoolean(value));
            } else if (valueClass == byte.class || valueClass == Byte.class) {
                method.invoke(object, Byte.parseByte(value));
            } else if (valueClass == char.class || valueClass == Character.class) {
                if (value.length() != 1) {
                    throw new BeanInstantiationException("Error casting property to char");
                }
                method.invoke(object, value.charAt(0));
            } else if (valueClass == String.class) {
                method.invoke(object, value);
            } else {
                throw new BeanInstantiationException("Unknown property type + " + valueClass);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new BeanInstantiationException("Cannot access setter method", e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new BeanInstantiationException("Cannot invoke setter method", e);
        }
    }

    private void invokeRefParameter(Object object, Method method, Class<?> valueClass, Object value) {
        if (!valueClass.isAssignableFrom(value.getClass())) {
            throw new BeanInstantiationException("Invalid ref parameter type");
        }
        try {
            method.invoke(object, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new BeanInstantiationException("Cannot access setter method", e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new BeanInstantiationException("Cannot invoke setter method", e);
        }
    }

    public Object getBean(String id) {
        return beans.get(id).getValue();
    }

    public <T> T getBean(Class<T> clazz) {
        T beanObject = null;
        int i = 0;
        for (String beanId : beans.keySet()) {
            Object value = beans.get(beanId).getValue();
            if (clazz.isAssignableFrom(value.getClass())) {
                beanObject = (T) value;
                i++;
            }
        }
        if (i > 1) {
            throw new GetBeanException("There are two Beans assignable from class");
        }
        return beanObject;
    }

    public <T> T getBean(String id, Class<T> clazz) {
        Object value = beans.get(id).getValue();
        return (T) value;
    }

    @VisibleForTesting
    Map<String, Bean> getBeans() {
        return beans;
    }

}

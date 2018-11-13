package com.study.ioccontainer.entity;

import java.util.Map;
import java.util.StringJoiner;

public class BeanDefinition {

    private String id;
    private String className;
    private Map<String, String> valueDependencies;
    private Map<String, String> refDependencies;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, String> getValueDependencies() {
        return valueDependencies;
    }

    public void setValueDependencies(Map<String, String> valueDependencies) {
        this.valueDependencies = valueDependencies;
    }

    public Map<String, String> getRefDependencies() {
        return refDependencies;
    }

    public void setRefDependencies(Map<String, String> refDependencies) {
        this.refDependencies = refDependencies;
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(", ", "BeanDefinition{", "}");
        stringJoiner.add("id=" + id + "\n");
        stringJoiner.add("className=" + className + "\n");
        if(valueDependencies != null){
            StringJoiner stringJoiner1 = new StringJoiner(", ", "valueDependencies{", "}");
            for (String key : valueDependencies.keySet()) {
                stringJoiner1.add(key + "=" + valueDependencies.get(key));
            }
            stringJoiner.add(stringJoiner1.toString() + "\n");
        }
        if(refDependencies != null){
            StringJoiner stringJoiner2 = new StringJoiner(", ", "refDependencies{", "}");
            for (String key : refDependencies.keySet()) {
                stringJoiner2.add(key + "=" + refDependencies.get(key));
            }
            stringJoiner.add(stringJoiner2.toString() + "\n");
        }
        return stringJoiner.toString();
    }
}

package com.study.ioccontainer.class_for_test;

public class Test2 {
    private String testName1;
    private Test1 testName2;

    public void setTestName1(String testName1) {
        this.testName1 = testName1;
    }

    public void setTestName2(Test1 testName2) {
        this.testName2 = testName2;
    }

    public String getTestName1() {
        return testName1;
    }

    public Test1 getTestName2() {
        return testName2;
    }
}

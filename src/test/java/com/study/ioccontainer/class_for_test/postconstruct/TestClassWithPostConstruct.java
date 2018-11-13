package com.study.ioccontainer.class_for_test.postconstruct;

import javax.annotation.PostConstruct;

public class TestClassWithPostConstruct {

    private boolean isPostConstruct;

    @PostConstruct
    public void init(){
        isPostConstruct = true;
    }

    public boolean isPostConstruct() {
        return isPostConstruct;
    }
}

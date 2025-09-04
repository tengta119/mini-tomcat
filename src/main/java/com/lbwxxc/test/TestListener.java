package com.lbwxxc.test;


import com.lbwxxc.ContainerEvent;
import com.lbwxxc.ContainerListener;

public class TestListener implements ContainerListener {

    @Override
    public void containerEvent(ContainerEvent event) {
        System.out.println("ContainerEvent event:" + event);
    }

}

package com.damon.actor.di;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import com.damon.actor.MyActor;

/**
 * 功能：
 *
 * Created by ZhouJiWei Date: 2015/7/10 Time: 15:45
 */
public class DependencyInjector implements IndirectActorProducer {

    final Object applicationContext;

    final String beanName;

    public DependencyInjector(Object applicationContext, String beanName) {
        this.applicationContext = applicationContext;
        this.beanName = beanName;
    }

    public Class<? extends Actor> actorClass() {
        return MyActor.class;
    }

    public MyActor produce() {
        MyActor result;
// obtain fresh Actor instance from DI framework ...
        return null;
    }
}

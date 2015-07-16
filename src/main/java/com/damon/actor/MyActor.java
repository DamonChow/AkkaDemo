package com.damon.actor;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * 功能：
 * <p/>
 * Created by ZhouJiWei Date: 2015/7/10 Time: 15:46
 */
public class MyActor extends UntypedActor{

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    @Override
    public void onReceive(Object message) throws Exception {
         if (message instanceof String) {
             log.info("rizhi ::" + message);
        } else {
            unhandled(message);
        }
    }
}

package com.damon.test1;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.apache.log4j.Logger;

/**
 * Hello world!
 *
 */
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main( String[] args ) {
        ActorSystem root = ActorSystem.create("root");
        ActorRef executeTask = root.actorOf(Props.create(ExecuteTaskActor.class), "executeTask");
        logger.info("启动任务，开始！！！！！！");
        logger.info("跟["+root.name()+"]路径：" + root.toString());
        logger.info("executeTask[" + executeTask.toString() + "]路径：" + executeTask.path());
        for (int i=1;i<10;i++) {
            executeTask.tell(new Task("任务"+i), null);
        }
        //root.shutdown();
        //executeTask
        //executeTask.tell(new Result(), null);
    }
}

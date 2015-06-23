package com.damon.test1;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.log4j.Logger;
import scala.util.Random;

/**
 * 功能：
 *
 * Created by ZhouJW on 2015/6/18 16:55.
 */
public class ExecuteTaskActor extends UntypedActor {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(ExecuteTaskActor.class);

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    ActorRef taskReturnActor = getContext().actorOf(Props.create(TaskReturnActor.class),"return");

    private Random rnd = new Random();

    @Override
    public void preStart() throws Exception {
        //这里一般进行初始化工作
        //logger.info("初始化角色。。。。。。。");
        log.info("初始化角色。。。。。。。");
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Task) {
            //Future<Object> ask = Patterns.ask(taskReturnActor, new Object(), 1000l);
            //taskReturnActor.forward(new Object(), getContext());
            //getContext().stop(taskReturnActor);
            //getContext().re
            //ActorSelection.
            //接收到前面发送过来的任务
            Task task = (Task) message;
            //logger.info(getSelf().toString() + "将会执行【" + task.getName() + "】");
            log.info(getSelf().toString() + "将会执行【" + task.getName() + "】");
            Result result = handler(task);
            //这里我们要往统计任务结果的actor发送消息，
            // 有2中方式
            // 一种是 前面发送task时候 把统计结果的TaskReturnActor 作为sender
            // 2，方式二 是 在此类中初始化taskReturnActor
            //第一种很简单 不说了 这里我们第二种
            //我吧结果全部返回吧  不加result判断了 if(result.isSuccess()){} 我们在后面taskReturn 加吧
            taskReturnActor.tell(result, self());
        } else if (message instanceof Result) {
            taskReturnActor.tell("getResult", ActorRef.noSender());
        } else {
            unhandled(message);
        }
    }


    private Result handler(Task task) {
        //logger.info("【" + task.getName() + "】正在处理中");
        log.info("【" + task.getName() + "】正在处理中");
        //这里我们执行任务 成功返回success 失败 返回 fail 为了演示
        // 我们随机产生正确失败结果 模拟 可能会出现失败成功
        if (rnd.nextInt(20) > 10) {
            return new Result(task.getName(), "success");
        } else {
            return new Result(task.getName(), "fail");
        }

    }

}

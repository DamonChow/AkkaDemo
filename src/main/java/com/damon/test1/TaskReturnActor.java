package com.damon.test1;

import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能：
 *
 * Created by ZhouJW on 2015/6/18 17:02.
 */
public class TaskReturnActor extends UntypedActor {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(TaskReturnActor.class);

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private Map<String, Object> results = new HashMap<String, Object>();

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Result) {
            Result rs = (Result) message;
            //logger.info(getSelf().toString() + ",接收到被处理的结果信息:【" + rs.getTaskName()
            //        + "】,该任务执行的情况:" + rs.getResult());
            log.info(getSelf().toString() + ",接收到被处理的结果信息:【" + rs.getTaskName()
                    + "】,该任务执行的情况:" + rs.getResult());
            if (rs.getResult().equals("success")) {
                results.put(rs.getTaskName(), rs.getResult());
            }
            if (results.size() == 5) {
                //这里我只是简单的写一下 实际上我们可以传递参数 获取任务数目 为了回顾上面讲的知识 我们
                //这里是我注定 告诉 已经处理完了 ok 可以返回结果了 ，如果有些任务等不及 ，
                // 你使用 ExecuteTaskActorRef.tell(new Result(),null)同样可以获得中间的结果
                //logger.info("【" + rs.getTaskName() + "】开始获取结果：");
                log.info("【" + rs.getTaskName() + "】开始获取结果：");
                getSender().tell(new Result(), getSelf());
            }
        } else if (message instanceof String) {
            //获得结果 我们就输出算了
            //logger.info("执行任务成功的任务名有 " + results.keySet().toString());
            log.info("执行任务成功的任务名有 " + results.keySet().toString());
            getSender().tell(PoisonPill.getInstance(), getSelf());
        } else {
            unhandled(message);
        }
    }
}

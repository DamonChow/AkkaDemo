版本：2.3.12，**原文地址:** [这是原文地址链接][0]   

----------
[TOC]

##容错
正如角色系统所描述的，每个actor都是其子actor的监管者，并且每个actor都定义了故障处理监管策略。这个策略作为角色系统结构的一部分，一经创建后就不能再修改。

##实际中的故障处理
首先我们看一个在实际应用中典型故障的案例，演示处理数据存储错误的一种方法。当然这取决于实际的应用中，当存储数据失败时可以做些什么，但在本例中我们使用尽量重新连接的方法。
阅读下面的源代码。代码内部的注释解释了各个片段的故障处理以及为什么添加它们。强烈推荐运行这个示例，因为很容易就顺着日志的输出，理解在运行时发生了什么。

###容错案例图解
![这里写图片描述](http://img.blog.csdn.net/20150716152511763)
上图阐述了正常消息流。
常规流程：
|步骤    |    描述    |
|:------|:------------|
| 1     |Listener开始干活。   |
| 2     |Worker定期的向自己发送Do信息来安排工作。 |
| 3,4,5 |当Worker收到Do信息后，向CounterService发送3条Increment信息去增加计数器。CounterService将Increment信息转发给Counter，由Counter更新计数器变量，并将计数器当前值发送给Storage。|
| 6,7   |Worker 请求CounterService获取当前计数器的值，并将结果返回给Listener。 |


![这里写图片描述](http://img.blog.csdn.net/20150716154711410)

上图阐述了在存储失败时发生了什么
失败流程：
|步骤      |    描述    |
|:------  |:------------|
| 1       |Storage抛出StorageException。|
| 2       |CounterService是Storage的监管者，当StorageException被抛出时并重启Storage。|
| 3,4,5,6 |Storage继续失败，继续被重启。|
| 7       |Storage在5秒内经历3次失败和重启后，会被其监管者(CounterService)停止。|
| 8       |CounterService同样在监视Storage为其终止，在当Storage终止时，接收到Terminated消息。|
| 9,10,11 |CounterService这时告诉Counter没有Storage。|
| 12      |CounterService计划发送一个Reconnect消息给自己。|
| 13,14   |当CounterService接受到Reconnect消息后创建一个新的Storage。 |
| 15,16   |CounterService告诉Counter用这个新的Storage。 |

###容错案例所有源码
这里就不列出了，详细请看[我的github][1]

##创建新的监管策略
下面的章节将说明故障处理的原理以及更深层的替代。
为了达到示范的效果，我们假使有如下的策略：

```
private static SupervisorStrategy strategy =
  new OneForOneStrategy(10, Duration.create("1 minute"),
    new Function<Throwable, Directive>() {
      @Override
      public Directive apply(Throwable t) {
        if (t instanceof ArithmeticException) {
          return resume();
        } else if (t instanceof NullPointerException) {
          return restart();
        } else if (t instanceof IllegalArgumentException) {
          return stop();
        } else {
          return escalate();
        }
      }
    });
 
@Override
public SupervisorStrategy supervisorStrategy() {
  return strategy;
}
```
我选择了几个著名的异常类型演示应用程序中被描述成故障处理指令在监管和监测中的使用。首先，一对一的策略，意味着每一个子actor会被单独的治愈（多对一策略与之相似，唯一不同的是这个策略是针对监管者所有的子actor而不仅仅是出故障的那一个）。这里限制重启的频率，最大程度上每分钟重启10次。-1 和Duration.Inf()的限制并不适用，可以指定一个重启绝对上限或者让重启无限。超过了期限后子actor会被停止。

>注意：
>如果这个策略是在监管者角色内部声明的（而不是一个单独的类），决策者可以在线程安全样式下访问角色内部的所有状态，包含获取失败子节点的引用（故障信息中getSender有效）。

###默认监管策略
如果定义过的策略没有覆盖到被抛出的异常，Escalate (逐步上升，丢给父监管者)志在必行。
当actor中没有定义监管策略，如下的异常将会被默认处理掉：

 - ActorInitializationException  会停止掉失败了的子actor。
 - ActorKilledException  会停止掉失败了的子actor。
 - Exception  会重启失败了的子actor。
 - 其他类型的Throwable 将会上升到父actor。

如果异常被上升一路达到根监护者那，在那也会用上述默认策略方式处理掉。

###停止监管策略
跟Erlang方式类似的策略是当它们失败的时候只停止子actor，以及当DeathWatch通知丢失的子actor的时候会对监管者采取纠正的动作。

###记录actor失败的消息
默认SupervisorStrategy会记录失败信息除非它们会被逐级上升。在高层次机构中处理被逐级上升的错误，并潜在的记录下来。
在初始化的时候你可以通过设置SupervisorStrategy的loggingEnabled为false用来不激活默认的日志。 在Decider内部可以定制日志。注意获取当前失败的子acotrRef是有效的，当SupervisorStrategy在监管角色中描述，如同getSender一样。
你可以自定义化日志通过实现SupervisorStrategy重写logFailure方法。

##顶层角色的监管
顶层角色意味着是用system.actorOf()创建的，是用户监护人的孩子。
在这种情况下没有特殊规则应用，监护只适用于配置的策略。

##应用测试
下面章节展示了在实际中不同指令的效果，因此咱们需要一个测试环境。首先需要一个合适有效的监管者：
```
public class Supervisor extends UntypedActor {
 
  private static SupervisorStrategy strategy =
    new OneForOneStrategy(10, Duration.create("1 minute"),
      new Function<Throwable, Directive>() {
        @Override
        public Directive apply(Throwable t) {
          if (t instanceof ArithmeticException) {
            return resume();
          } else if (t instanceof NullPointerException) {
            return restart();
          } else if (t instanceof IllegalArgumentException) {
            return stop();
          } else {
            return escalate();
          }
        }
      });
 
  @Override
  public SupervisorStrategy supervisorStrategy() {
    return strategy;
  }
 
 
  public void onReceive(Object o) {
    if (o instanceof Props) {
      getSender().tell(getContext().actorOf((Props) o), getSelf());
    } else {
      unhandled(o);
    }
  }
}
```
此监管者将会创建一个子actor，好让我们可以作如下试验:

```
public class Child extends UntypedActor {
  int state = 0;
 
  public void onReceive(Object o) throws Exception {
    if (o instanceof Exception) {
      throw (Exception) o;
    } else if (o instanceof Integer) {
      state = (Integer) o;
    } else if (o.equals("get")) {
      getSender().tell(state, getSelf());
    } else {
      unhandled(o);
    }
  }
}
```
在测试actor系统中测试使用工具更容易简化，TestProbe提供了有用的角色引用，用来接收和检查消息的回复。

```
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.SupervisorStrategy;
import static akka.actor.SupervisorStrategy.resume;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.stop;
import static akka.actor.SupervisorStrategy.escalate;
import akka.actor.SupervisorStrategy.Directive;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import scala.collection.immutable.Seq;
import scala.concurrent.Await;
import static akka.pattern.Patterns.ask;
import scala.concurrent.duration.Duration;
import akka.testkit.TestProbe;
 
public class FaultHandlingTest {
  static ActorSystem system;
  Duration timeout = Duration.create(5, SECONDS);
 
  @BeforeClass
  public static void start() {
    system = ActorSystem.create("test");
  }
 
  @AfterClass
  public static void cleanup() {
    JavaTestKit.shutdownActorSystem(system);
    system = null;
  }
 
  @Test
  public void mustEmploySupervisorStrategy() throws Exception {
    // code here
  }
 
}
```
创建角色：
```
Props superprops = Props.create(Supervisor.class);
ActorRef supervisor = system.actorOf(superprops, "supervisor");
ActorRef child = (ActorRef) Await.result(ask(supervisor,
  Props.create(Child.class), 5000), timeout);
```

第一个测试将演示resume指令，因此我们尝试在actor中设置一些非初始化的状态，并且让actor出现故障：

```
child.tell(42, ActorRef.noSender());
assert Await.result(ask(child, "get", 5000), timeout).equals(42);
child.tell(new ArithmeticException(), ActorRef.noSender());
assert Await.result(ask(child, "get", 5000), timeout).equals(42);
```
你可以看到在错误处理指令完后仍能得到42。现在我们将故障换成更严重的NullPointerException，那将不再是这样的情况：
```
child.tell(new NullPointerException(), ActorRef.noSender());
assert Await.result(ask(child, "get", 5000), timeout).equals(0);
```
最后看看最致命的IllegalArgumentException，监管者会终止其子actor：
```
final TestProbe probe = new TestProbe(system);
probe.watch(child);
child.tell(new IllegalArgumentException(), ActorRef.noSender());
probe.expectMsgClass(Terminated.class);
```
到目前为止，监管者完全不受子actor故障的影响，因为指令集会处理掉。在Exception情况下，就不会是上述情况了，监管者会将失败情况逐级上升传递：

```
child = (ActorRef) Await.result(ask(supervisor,
  Props.create(Child.class), 5000), timeout);
probe.watch(child);
assert Await.result(ask(child, "get", 5000), timeout).equals(0);
child.tell(new Exception(), ActorRef.noSender());
probe.expectMsgClass(Terminated.class);
```

监管者它自己会被ActorSystem提供的顶层actor监管，顶层actor对所有异常（ActorInitializationException 和ActorKilledException异常是例外）使用默认故障策略去重启。因为默认指令的重启是杀死所有子actor，我们预期到这些脆弱的子actor是不会在故障中幸存。

假如这不是所期望的（依赖于实际用例），我们需要使用一个不同的监管者来覆盖它的方法。
```
public class Supervisor2 extends UntypedActor {
 
  private static SupervisorStrategy strategy = new OneForOneStrategy(10,
    Duration.create("1 minute"),
      new Function<Throwable, Directive>() {
        @Override
        public Directive apply(Throwable t) {
          if (t instanceof ArithmeticException) {
            return resume();
          } else if (t instanceof NullPointerException) {
            return restart();
          } else if (t instanceof IllegalArgumentException) {
            return stop();
          } else {
            return escalate();
          }
        }
      });
 
  @Override
  public SupervisorStrategy supervisorStrategy() {
    return strategy;
  }
 
 
  public void onReceive(Object o) {
    if (o instanceof Props) {
      getSender().tell(getContext().actorOf((Props) o), getSelf());
    } else {
      unhandled(o);
    }
  }
 
  @Override
  public void preRestart(Throwable cause, Option<Object> msg) {
    // do not kill all children, which is the default here
  }
}
```

在这个父actor下，子actor会逐步上升中的重启而幸存，如下的最后测试：
```
superprops = Props.create(Supervisor2.class);
supervisor = system.actorOf(superprops);
child = (ActorRef) Await.result(ask(supervisor,
  Props.create(Child.class), 5000), timeout);
child.tell(23, ActorRef.noSender());
assert Await.result(ask(child, "get", 5000), timeout).equals(23);
child.tell(new Exception(), ActorRef.noSender());
assert Await.result(ask(child, "get", 5000), timeout).equals(0);
```

[0]:[http://doc.akka.io/docs/akka/2.3.12/java/fault-tolerance.html]
[1]:https://github.com/DamonChow/AkkaDemo/blob/master/src/main/java/com/damon/actor/faultToleranceTest/FaultHandlingDocSample.java
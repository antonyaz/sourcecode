import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;

public class AkkaDemo {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("demo");
//        ActorSystem actorSystem1 = ActorSystem.create("helloakka", ConfigFactory.load("appsys"));
        ActorRef getNowGateway = system.actorOf(Props.create(GetNowGateway.class, "GetNowGateway"));
        getNowGateway.tell("hello world", ActorRef.noSender());
    }
}

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Greeter extends AbstractActor {

    static public Props props(String msg, ActorRef printerAction) {
        return Props.create(Greeter.class, Greeter::new);
    }

    static public class WhoToGreet {
        public final String who;
        public WhoToGreet(String who){
            this.who = who;
        }
    }

    static public class Greet {
        public Greet(){

        }
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}

package akka.demo;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

import java.io.IOException;

public class AkkaDemo {
    public static void main(String[] args) throws IOException {
        ActorSystem system = ActorSystem.create("testSystem");
        ActorRef firstRef = system.actorOf(PrintActorRefActor.props(), "first-actor");
        System.out.println("first:" + firstRef);
        firstRef.tell("printit",ActorRef.noSender());

        System.out.println("print enter to exit");

        try {
            System.in.read();
        }finally {
            system.terminate();
        }
    }
}

class PrintActorRefActor extends AbstractActor {
    static Props props() {
        return Props.create(PrintActorRefActor.class, PrintActorRefActor::new);
    }

    @Override
    public Receive createReceive() {
        return  ReceiveBuilder.create().matchEquals("printit", p-> {
            ActorRef secondRef = getContext().actorOf(Props.empty(), "second-actor");
            System.out.println("second:" + secondRef);
        }).build();
    }
}

package akka.demo;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

public class SupervisingActor extends AbstractActor {
    static Props props() {
        return Props.create(SupervisingActor.class, SupervisingActor::new);
    }

    ActorRef child = getContext().actorOf(SupervisedActor.props(), "supervised-actor");

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("testSystem");
        ActorRef firstRef = system.actorOf(SupervisingActor.props(), "supervising-actor");
        System.out.println("first:" + firstRef);
        firstRef.tell("failChild", ActorRef.noSender());
    }
    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .matchEquals("failChild", p->{
                    child.tell("fail", getSelf());
                }).build();
    }
}

class SupervisedActor extends AbstractActor {
    static Props props() {
        return Props.create(SupervisedActor.class, SupervisedActor::new);
    }

    @Override
    public void preStart() throws Exception {
        System.out.println("supervised actor started");
    }

    @Override
    public void postStop() throws Exception {
        System.out.println("supervised actor stopped");
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create().matchEquals("fail", p->{
            System.out.println("supervised actor failed now");
            throw  new Exception("supervised actor failed");
        }).build();
    }
}

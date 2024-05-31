import akka.actor.ActorSystem;

import akka.pattern.Patterns;

import org.apache.flink.runtime.akka.AkkaUtils;
import org.apache.flink.runtime.rpc.RpcGateway;
import org.apache.flink.runtime.rpc.akka.AkkaRpcService;
import org.apache.flink.runtime.rpc.akka.AkkaRpcServiceConfiguration;

public class RPCDemo {
    public static void main(String[] args) {

//        1、启动RPC服务
        ActorSystem defaultActorSystem = AkkaUtils.createDefaultActorSystem();
        AkkaRpcService akkaRpcService = new AkkaRpcService(defaultActorSystem, AkkaRpcServiceConfiguration.defaultConfiguration());

//        2、创建RpcEndpoint实例，启动RPC服务
        GetNowRpcEndpoint getNowRpcEndpoint = new GetNowRpcEndpoint(akkaRpcService);
        getNowRpcEndpoint.start();

//        3、通过selfGateway调用Rpc服务
        GetNowGateway selfGateway = getNowRpcEndpoint.getSelfGateway(GetNowGateway.class);
        System.out.println("address->" + selfGateway.getAddress());

        String getNowResult = selfGateway.getNow();
        System.out.println(getNowResult);

//        4、通过RpcEndPoit地址获取代理
        System.out.println("address->" + getNowRpcEndpoint.getAddress());
        GetNowGateway getNowGateway = akkaRpcService.connect(getNowRpcEndpoint.getAddress(), GetNowGateway.class).getNow(getNowRpcEndpoint);
        String getNowResult2 = getNowGateway.getNow();
        System.out.println(getNowResult2);
        System.out.println(getNowGateway.sayHelloTo("snowy"));

        GetNowGateway getNowGateway2 = akkaRpcService.connect(getNowRpcEndpoint.getAddress(), GetNowGateway.class).getNow(getNowRpcEndpoint);
        System.out.println(getNowGateway2.sayHelloTo("antony"));


    }
}


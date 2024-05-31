import org.apache.flink.runtime.rpc.RpcGateway;

public interface GetNowGateway extends RpcGateway {
    String getNow();

    String sayHelloTo(String name);
}

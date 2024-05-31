import org.apache.flink.runtime.rpc.RpcEndpoint;
import org.apache.flink.runtime.rpc.RpcService;

import java.util.concurrent.CompletableFuture;

public class GetNowRpcEndpoint extends RpcEndpoint implements GetNowGateway {
    protected GetNowRpcEndpoint(RpcService rpcService) {
        super(rpcService);
    }

    @Override
    protected void onStart() throws Exception {
        super.onStart();
        System.out.println(getClass().getSimpleName() + " started.... ");
    }

    @Override
    public String getNow() {
        return "2022-04-30 12:00:00";
    }

    @Override
    public String sayHelloTo(String name) {
        return "hi " + name;
    }

    @Override
    protected CompletableFuture<Void> onStop() {
        System.out.println(getClass().getSimpleName() + " stopped.....");
        return super.onStop();
    }
}

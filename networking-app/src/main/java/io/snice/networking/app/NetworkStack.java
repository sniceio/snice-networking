package io.snice.networking.app;

import io.snice.networking.app.impl.NettyNetworkStack;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface NetworkStack {

    static Builder withConfig(final NetworkAppConfig config) {
        return NettyNetworkStack.withConfig(config);
    }


    /**
     * Start the stack. This will configure the underlying network, bind to all
     * ports for all configured transports and the method will block until everything
     * is up and running.
     * <p>
     *
     * @return
     */
    void start();

    /**
     * Get a sync object to be used for knowing when the stack has been shutdown.
     *
     * @return
     */
    CompletionStage<Void> sync();

    /**
     * Shutdown the stack. This method is blocking and will return once
     * the entire stack has been shutdown, which means to stop listening
     * on all configured ports.
     */
    void stop();

    interface Builder<T extends NetworkAppConfig> {
        Builder withApplication(NetworkApplication<T> application);
        Builder withConnectionContexts(List<ConnectionContext> ctxs);
        NetworkStack build();
    }
}

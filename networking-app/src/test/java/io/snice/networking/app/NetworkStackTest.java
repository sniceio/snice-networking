package io.snice.networking.app;

import io.snice.networking.bundles.StringBundle;
import io.snice.networking.common.Connection;
import org.junit.Test;

public class NetworkStackTest {

    @Test
    public void testCreateStack() {
        NetworkStack.Builder<Connection<String>, String, NetworkAppConfig> builder = NetworkStack.withConfiguration(new NetworkAppConfig());
        builder.withAppBundle(new StringBundle());
        builder.withApplication(new TestApp());
        builder.build();
    }

    private static class TestApp extends BasicNetworkApplication<String, NetworkAppConfig> {

        TestApp() {
            super(String.class);
        }

        @Override
        public void initialize(NetworkBootstrap<Connection<String>, String, NetworkAppConfig> bootstrap) {
        }
    }

}
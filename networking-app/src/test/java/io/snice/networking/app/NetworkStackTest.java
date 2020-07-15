package io.snice.networking.app;

import io.snice.networking.common.Connection;
import org.junit.Test;

import static org.junit.Assert.*;

public class NetworkStackTest {

    @Test
    public void testCreateStack() {
        NetworkStack.Builder<Connection<String>, String, NetworkAppConfig> builder = NetworkStack.withConfiguration(new NetworkAppConfig());
        builder.build();
    }

}
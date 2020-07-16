package io.snice.networking.bundles;

import io.snice.networking.bundles.ProtocolBundleRegistry;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ProtocolBundleRegistryTest {

    private ProtocolBundleRegistry registry;

    @Before
    public void setup() {
        registry = ProtocolBundleRegistry.getDefaultRegistry();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultRegistryBadInputParams() throws Exception {
        registry.find(null);
    }

    /**
     * Ensure that the default bundles are correctly configured and discoverable.
     */
    @Test
    public void testDefaultRegistry() throws Exception {
        final var bundle = registry.find(String.class).get();
        assertThat(bundle, notNullValue());
        assertThat(bundle.getType() == String.class, is(true));
    }

}
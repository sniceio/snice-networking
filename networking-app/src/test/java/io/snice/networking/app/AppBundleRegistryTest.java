package io.snice.networking.app;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class AppBundleRegistryTest {

    private AppBundleRegistry registry;

    @Before
    public void setup() {
        registry = AppBundleRegistry.getDefaultRegistry();
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
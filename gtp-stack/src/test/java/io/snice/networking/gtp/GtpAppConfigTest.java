package io.snice.networking.gtp;

import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.config.NetworkInterfaceConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GtpAppConfigTest extends GtpStackTestBase {

    @Test
    public void testLoadConfig() throws Exception {
        final var conf = loadConfig("gtp_config_001.yml");
        assertThat(conf.getConfig().getUserPlane().getNic(), is("gtpu"));
        assertThat(conf.getConfig().getControlPlane().getNic(), is("gtpc"));

        final var nic = getNic(conf, "gtpu");
        assertThat(nic.getVipAddress().getHost(), is("10.11.12.13"));
    }

    private NetworkInterfaceConfiguration getNic(final NetworkAppConfig conf, final String nicName) {
        return conf.getNetworkInterfaces().stream()
                .filter(nic -> nicName.equalsIgnoreCase(nic.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No network interface named " + nicName + " found"));
    }


}
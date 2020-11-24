package io.snice.networking.gtp;

import io.snice.networking.app.ConfigUtils;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.config.NetworkInterfaceConfiguration;
import io.snice.networking.gtp.conf.GtpAppConfig;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GtpAppConfigTest extends GtpStackTestBase {

    @Test
    public void testLoadConfig() throws Exception {
        final var buffer = loadRaw("gtp_config_001.yml");
        final var conf = ConfigUtils.loadConfiguration(GtpAppConfig.class, buffer.getContent());
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
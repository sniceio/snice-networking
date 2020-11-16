package io.snice.networking.gtp;

import io.snice.networking.app.ConfigUtils;
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
    }


}
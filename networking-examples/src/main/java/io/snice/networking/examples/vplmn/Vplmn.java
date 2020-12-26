package io.snice.networking.examples.vplmn;


import io.hektor.core.Hektor;
import io.snice.networking.examples.gtp.GtpConfig;
import io.snice.networking.gtp.GtpApplication;
import io.snice.networking.gtp.GtpBootstrap;
import io.snice.networking.gtp.GtpEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Vplmn extends GtpApplication<GtpConfig> {

    private static final Logger logger = LoggerFactory.getLogger(Vplmn.class);

    private GtpEnvironment<GtpConfig> environment;
    private DeviceManager deviceManager;
    private Hektor hektor;

    @Override
    public void initialize(final GtpBootstrap<GtpConfig> bootstrap) {
        bootstrap.onConnection(c -> true).accept(b -> {
            b.match(evt -> true).consume(evt -> System.err.println(evt));
            // b.match(GtpEvent::isPdu).map(GtpEvent::toGtp1Message).consume(Vplmn::processPdu);
            // b.match(GtpEvent::isCreateSessionResponse).map(GtpEvent::toGtp2Message).consume(Vplmn::processCreateSessionResponse);
            // b.match(GtpEvent::isDeleteSessionResponse).map(GtpEvent::toGtp2Message).consume(Vplmn::processDeleteSessionResponse);
        });

    }

    @Override
    public void run(final GtpConfig configuration, final GtpEnvironment<GtpConfig> environment) {
        this.environment = environment;
        final var hektorConfig = environment.getConfig().getHektorConfig();
        hektor = Hektor.withName("vplmn").withConfiguration(hektorConfig).build();
        deviceManager = DeviceManager.of(hektor, environment);
        deviceManager.addDevice("12354098098098").thenAccept(result -> result.accept(Vplmn::processDeviceError, Vplmn::processNewDevice));
    }

    private static void processDeviceError(final Error error) {
        logger.warn(error.getMessage());
    }

    private static void processNewDevice(final Device device) {
        device.goOnline();
    }


    public static void main(final String... args) throws Exception {
        final var vplmn = new Vplmn();
        vplmn.run("vplmn.yml");
    }
}

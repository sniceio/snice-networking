package io.snice.networking.examples.vplmn;


import io.hektor.core.Hektor;
import io.snice.networking.examples.gtp.GtpConfig;
import io.snice.networking.examples.gtp.Sgw2;
import io.snice.networking.gtp.GtpApplication;
import io.snice.networking.gtp.GtpBootstrap;
import io.snice.networking.gtp.GtpEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public class Vplmn extends GtpApplication<GtpConfig> {

    private static final Logger logger = LoggerFactory.getLogger(Vplmn.class);

    private GtpEnvironment<GtpConfig> environment;
    private final AtomicReference<DeviceManager> deviceManager = new AtomicReference<>();
    private final AtomicReference<UserManager> userManager = new AtomicReference<>();
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

        final var pgw = "127.0.0.1";
        final var port = 2123;
        environment.establishControlPlane(pgw, port).thenAccept(tunnel -> {
            final var devices = DeviceManager.of(hektor, environment, tunnel);
            final var simCards = SimCardManager.of();
            final var users = UserManager.of(hektor, devices, simCards);
            deviceManager.set(devices);
            userManager.set(users);
            users.addUser("Alice", User.ALICE);
            users.addUser("Bob", User.ALICE);
            // users.addUser("Carol", User.ALICE);
            // users.addUser("Dave", User.ALICE);
        }).exceptionally(t -> {
            t.printStackTrace();
            System.err.println("Unable to establish GTP Control Tunnel, bailing out");
            System.exit(1); // hehe, yeah no...
            return null;
        });

    }

    private static void processDeviceError(final Error error) {
        logger.warn(error.getMessage());
    }

    private static void processNewDevice(final Device device) {
        device.goOnline();

        // TODO: need some way to know if we are in the right state.
        try {
            Thread.sleep(200);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        device.sendData(Sgw2.dnsQuery, "8.8.8.8", 53);
    }


    public static void main(final String... args) throws Exception {
        final var vplmn = new Vplmn();
        vplmn.run("vplmn.yml");
    }
}

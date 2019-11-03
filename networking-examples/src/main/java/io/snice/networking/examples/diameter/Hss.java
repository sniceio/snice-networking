package io.snice.networking.examples.diameter;

import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.DiameterSerializationFactory;
import io.snice.networking.codec.diameter.avp.api.ExperimentalResultCode;
import io.snice.networking.codec.diameter.avp.api.ResultCode;
import io.snice.networking.common.Connection;

import static io.snice.networking.app.NetworkBootstrap.ACCEPT_ALL;

public class Hss extends NetworkApplication<DiameterMessage, HssConfig> {

    public Hss() {
        super(DiameterMessage.class);
    }

    @Override
    public void initialize(final NetworkBootstrap<DiameterMessage, HssConfig> bootstrap) {

        bootstrap.registerSerializationFactory(new DiameterSerializationFactory());

        bootstrap.onConnection(ACCEPT_ALL).accept(b -> {
            b.match(DiameterMessage::isCER).consume(Hss::processCER);
            b.match(DiameterMessage::isULR).consume(Hss::processULR);
        });

    }

    private static final void processCER(final Connection con, final DiameterMessage cer) {
        final var cea = cer.createAnswer(ResultCode.DiameterSuccess).build();
        // con.send(cea.getBuffer());
        con.send(cea);
    }

    private static final void processULR(final Connection con, final DiameterMessage ulr) {
        final var ula = ulr.createAnswer(ResultCode.DiameterSuccess)
                .withAvp(ExperimentalResultCode.DiameterErrorUserUnknown)
                .build();
        // con.send(ula.getBuffer());
        con.send(ula);
    }

    public static void main(final String... args) throws Exception {
        final var hss = new Hss();
        hss.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/Hss.yml");
    }
}

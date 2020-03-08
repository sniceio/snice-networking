package io.snice.networking.diameter.peer;

import io.snice.networking.codec.diameter.DiameterAnswer;
import io.snice.networking.codec.diameter.DiameterHeader;
import io.snice.networking.codec.diameter.DiameterRequest;
import io.snice.networking.codec.diameter.avp.api.OriginHost;
import io.snice.networking.codec.diameter.avp.api.OriginRealm;
import io.snice.networking.codec.diameter.avp.api.ResultCode;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DiameterTestBase {

    protected static OriginRealm defaultOriginRealm = OriginRealm.of("epc.mnc001.mcc001.3gppnetwork.org");
    protected static OriginHost defaultOriginHost = OriginHost.of("unit.test.node.epc.mnc001.mcc001.3gppnetwork.org");

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    public static DiameterRequest someCer() {
        return DiameterRequest.createCER()
                .withOriginRealm(defaultOriginRealm)
                .withOriginHost(defaultOriginHost)
                .build();
    }

    public static DiameterAnswer someCea(final ResultCode code) {
        final var header = DiameterHeader.of().isAnswer().withCommandCode(257);
        return DiameterAnswer.withResultCode(code)
                .withDiameterHeader(header)
                .withOriginHost(defaultOriginHost)
                .withOriginRealm(defaultOriginRealm)
                .build();
    }
}

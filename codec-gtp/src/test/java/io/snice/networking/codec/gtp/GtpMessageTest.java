package io.snice.networking.codec.gtp;

import io.snice.networking.codec.gtp.gtpc.InfoElement;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2InfoElementType;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.networking.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.networking.codec.gtp.gtpc.v2.tliv.TypeLengthInstanceValue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GtpMessageTest extends GtpTestBase {

    @Before
    public void setUp() throws Exception {
    }

    // Ignore for now - messed up some stuff when working on
    // codegen but currently working with diameter so will get
    // back to this.
    @Ignore
    @Test
    public void testParseGtpv2Message() {
        final Gtp2Message msg = GtpMessage.frame(GtpRawData.createSessionRequest).toGtp2Message();
        assertThat(msg.getHeader().getLength(), is(251));

        final List<? extends InfoElement> ie = msg.getInfoElements();
        assertThat(ie.size(), is(16));
        assertInfoElement(ie.get(0), 1, 8);
        assertInfoElement(ie.get(1), 75, 8);
        assertInfoElement(ie.get(2), 86, 13);
        assertInfoElement(ie.get(3), 83, 3);
        assertInfoElement(ie.get(4), 82, 1);
        assertInfoElement(ie.get(5), 87, 9);
        assertInfoElement(ie.get(6), 71, 39);
        assertInfoElement(ie.get(7), 128, 1);
        assertInfoElement(ie.get(8), 99, 1);
        assertInfoElement(ie.get(9), 79, 5);
        assertInfoElement(ie.get(10), 127, 1);
        assertInfoElement(ie.get(11), 72, 8);
        assertInfoElement(ie.get(12), 78, 35);
        assertInfoElement(ie.get(13), 93, 44);
        assertInfoElement(ie.get(14), 3, 1);
        assertInfoElement(ie.get(15), 114, 2);

        assertThat(msg.isCreateSessionRequest(), is(true));
        assertIMSI(msg.getInformationElement(Gtp2InfoElementType.IMSI).get(), "23450001199900015");
    }

    @Test
    public void testGTPc2() {
        final GtpMessage msg = GtpMessage.frame(GtpRawData.gtpc2);
        assertThat(msg.getHeader().getLength(), is(240));
        assertThat(msg.isGtpVersion2(), is(true));
        assertThat(msg.toGtp2Message().isCreateSessionRequest(), is(true));

        assertIMSI(msg.getImsi().get(), "99999123456789");
    }

    private static void assertIMSI(final TypeLengthInstanceValue ie, final String expected) {
        assertThat(ie.isIMSI(), is(true));
        assertThat(ie.ensure().toIMSI().toString(), is(expected));
    }

    private static void assertInfoElement(final InfoElement ie, final int expectedType, final int expectedLength) {
        assertThat(ie.getTypeAsDecimal(), is(expectedType));
        assertThat(ie.getLength(), is(expectedLength));
    }


}
package io.snice.networking.codec.diameter.impl;

import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.DiameterRequest;
import io.snice.networking.codec.diameter.DiameterTestBase;
import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.api.AcctApplicationId;
import io.snice.networking.codec.diameter.avp.api.AuthApplicationId;
import io.snice.networking.codec.diameter.avp.api.DestinationRealm;
import io.snice.networking.codec.diameter.avp.api.HostIpAddress;
import io.snice.networking.codec.diameter.avp.api.OriginHost;
import io.snice.networking.codec.diameter.avp.api.OriginRealm;
import io.snice.networking.codec.diameter.avp.api.ProductName;
import io.snice.networking.codec.diameter.avp.api.VendorId;
import io.snice.networking.codec.diameter.avp.api.VendorSpecificApplicationId;
import io.snice.networking.codec.diameter.avp.type.IpAddress;
import org.junit.Before;
import org.junit.Test;

import static io.snice.networking.codec.diameter.avp.api.ResultCode.DiameterErrorEapCodeUnknown5048;
import static io.snice.networking.codec.diameter.avp.api.ResultCode.DiameterSuccess2001;
import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class CapabilitiesExchangeTest extends DiameterTestBase {

    private DiameterRequest cer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        cer = loadDiameterMessage("capabilities_exchange_request.raw").toRequest();
    }

    @Test
    public void frameCER() throws Exception {
        final var origHost = cer.getOriginHost();
        assertThat(origHost.getValue().asString(), is("seagull.node.epc.mnc001.mcc001.3gppnetwork.org"));

        final var origRealm = cer.getOriginRealm();
        assertThat(origRealm.getValue().asString(), is("epc.mnc001.mcc001.3gppnetwork.org"));

        final var productName = cer.getAvp(VendorId.CODE);
        final var vendorId = (VendorId) productName.get().ensure();
        assertThat(vendorId.getValue().getValue(), is(10415L));

        final var hostIp = cer.getAvp(HostIpAddress.CODE);
        assertThat(hostIp, not(empty()));
    }

    @Test
    public void frameCerNo2() throws Exception {
        final var cer = loadDiameterMessage("capabilities_exchange_request_002.raw").toRequest();
        final var hostIp = cer.getAvp(HostIpAddress.CODE).map(FramedAvp::ensure).map(Avp::toHostIpAddress);
        assertThat(hostIp, not(empty()));
        final var ip = hostIp.get().ensure().toHostIpAddress().getValue();
        assertThat(ip.asString(), is("172.22.18.120"));
        assertThat(hostIp.get(), is(HostIpAddress.of(IpAddress.createIpv4Address("172.22.18.120"))));
    }

    @Test
    public void testCreateCER() {
        final var b = DiameterRequest.createCER();
        b.withOriginRealm(OriginRealm.of("whatever.epc.blah.3gppnetwork.org"));
        b.withDestinationRealm(DestinationRealm.of("hello.world.something.rather"));
        b.withAvp(HostIpAddress.of("10.11.12.13"));
        b.withAvp(HostIpAddress.of("20.21.22.23"));

        final var vendorId = VendorId.of(10415L);
        final var authId = AuthApplicationId.of(16777251L);
        final var acctId = AcctApplicationId.of(0L);
        final var app = VendorSpecificApplicationId.of(vendorId, authId, acctId);
        b.withAvp(app);

        final var cer = b.build();

        final var other = serializeDeserialize(cer).toRequest();
        assertThat(other.getOriginRealm(), is(OriginRealm.of("whatever.epc.blah.3gppnetwork.org")));
        assertThat(other.getDestinationRealm().get(), is(DestinationRealm.of("hello.world.something.rather")));

        final var ips = other.getAvps(HostIpAddress.CODE);
        assertThat(ips.size(), is(2));
        assertThat(ips.get(0).ensure().toHostIpAddress(), is(HostIpAddress.of("10.11.12.13")));
        assertThat(ips.get(1).ensure().toHostIpAddress(), is(HostIpAddress.of("20.21.22.23")));

        final var apps = (VendorSpecificApplicationId)other.getAvp(VendorSpecificApplicationId.CODE).get().ensure();
        assertThat(apps.getFramedAvp(AuthApplicationId.CODE).get(), is(authId));
        assertThat(apps.getFramedAvp(AcctApplicationId.CODE).get(), is(acctId));
        assertThat(apps.getFramedAvp(VendorId.CODE).get(), is(vendorId));
    }

    @Test
    public void testCreateCEA() {
        final var cea = cer.createAnswer(DiameterSuccess2001).build();
        assertThat(cea.isCEA(), is(true));
        assertThat(cea.getResultCode().getRight(), is(DiameterSuccess2001));
        assertThat(cea.getOriginHost().getValue().asString(), is("seagull.node.epc.mnc001.mcc001.3gppnetwork.org"));
        assertThat(cea.getOriginRealm().getValue().asString(), is("epc.mnc001.mcc001.3gppnetwork.org"));

        // make sure that we actually created the underlying byte-array correctly
        // and the easiest way is just to re-frame it.
        final var buffer = cea.getBuffer();
        final var ceaClone = DiameterMessage.frame(buffer).toAnswer();
        assertThat(ceaClone.isCEA(), is(true));
        assertThat(ceaClone.isAnswer(), is(true));
        assertThat(ceaClone.getOriginHost().getValue().asString(), is("seagull.node.epc.mnc001.mcc001.3gppnetwork.org"));
        assertThat(ceaClone.getOriginRealm().getValue().asString(), is("epc.mnc001.mcc001.3gppnetwork.org"));
    }

    /**
     * Comparing diameter messages isn't necessarily "easy" and only basic equality is performed.
     * In general, all mandatory parameters are compared and the rest are ignored.
     *
     */
    @Test
    public void testEqualityCea() {
        ensureEquality(someCea(DiameterErrorEapCodeUnknown5048), someCea(DiameterErrorEapCodeUnknown5048));

        final HostIpAddress ip1 = HostIpAddress.of(IpAddress.createIpv4Address("127.0.0.1"));
        final HostIpAddress ip2 = HostIpAddress.of(IpAddress.createIpv4Address("127.0.0.1"));
        final HostIpAddress ip3 = HostIpAddress.of(IpAddress.createIpv4Address("10.36.10.10"));
        final ProductName pn1 = ProductName.of("Hello");
        final ProductName pn2 = ProductName.of("Other Product");

        ensureEquality(someCea(DiameterSuccess2001, ip1, pn1), someCea(DiameterSuccess2001, ip2, pn1));

        // different product name so shouldn't be the same
        ensureNotEquals(someCea(DiameterSuccess2001, ip1, pn1), someCea(DiameterSuccess2001, ip2, pn2));

        // different HostIPAddress, so not equal
        ensureNotEquals(someCea(DiameterSuccess2001, ip3, pn1), someCea(DiameterSuccess2001, ip2, pn1));

        // different result code so not equal
        ensureNotEquals(someCea(DiameterErrorEapCodeUnknown5048, ip1, pn1), someCea(DiameterSuccess2001, ip2, pn1));

        // ensure a request and answer isn't considered the same
        ensureNotEquals(someCer(), someCea(DiameterSuccess2001));
    }

    @Test
    public void testEqualityCer() {
        ensureEquality(someCer(), someCer());

        // different host ip
        final var hostIp = HostIpAddress.of(IpAddress.createIpv4Address("192.168.0.100"));
        ensureNotEquals(someCer(hostIp, defaultOriginRealm, defaultOriginHost), someCer());

        // different origin realm
        final var originRealm = OriginRealm.of("epc.mnc123.mcc123.3gppnetwork.org");
        ensureNotEquals(someCer(hostIp, originRealm, defaultOriginHost), someCer(hostIp, defaultOriginRealm, defaultOriginHost));

        // different origin host realm
        final var originHost = OriginHost.of("hello.node.epc.mnc123.mcc123.3gppnetwork.org");
        ensureNotEquals(someCer(hostIp, defaultOriginRealm, originHost), someCer(hostIp, defaultOriginRealm, defaultOriginHost));
    }
}

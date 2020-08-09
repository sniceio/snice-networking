package io.snice.networking.diameter;

import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.diameter.event.DiameterEvent;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class DiameterBundleTest {

    private DiameterBundle<DiameterAppConfig> bundle;
    private DiameterAppConfig config;
    private NetworkStack<PeerConnection, DiameterEvent, DiameterAppConfig> stack;
    private DiameterRequest ulr;

    @Before
    public void setup() throws Exception {
        config = new DiameterAppConfig();
        bundle = new DiameterBundle<>();
        stack = mock(NetworkStack.class);
        ulr = mock(DiameterRequest.class);
    }

    /**
     * silly test but just making sure that we don't change the
     * bundle name without actually doing some thinking in regards to
     * any potential customer ramifications.
     */
    @Test
    public void testBundleName() {
        assertThat(bundle.getBundleName(), is("DiameterBundle"));
    }

    @Test
    public void testBasicRouting() {
        final DiameterEnvironment env = bundle.createEnvironment(stack, config);
        env.send(ulr);
    }

}
package io.snice.networking.gtp.conf;

import io.snice.networking.gtp.GtpControlTunnel;

import static io.snice.preconditions.PreConditions.assertArgument;

public class ControlPlaneConfig {

    private boolean enable;

    private String nic;

    /**
     * Internally, each {@link GtpControlTunnel} will store outstanding transactions
     * in a hash map and as with any ADT, we really should set the initial size and in particular
     * for hash maps so they don't keep re-hashing as it grows.
     */
    private int initialTransactionStoreSize = 10;

    /**
     * As with {@link #initialTransactionStoreSize}, we store all established
     * tunnels in a hash map and we do not wish to re-hash if we need to. Set this
     * to a value that covers all the tunnels you intend to have established
     * at any given point in time (don't worry, if it is too small, it will re-hash
     * and grow)
     */
    private int initialTunnelStoreSize = 10;

    public boolean isEnable() {
        return enable;
    }

    public String getNic() {
        return nic;
    }

    public void setEnable(final boolean enable) {
        this.enable = enable;
    }

    public void setNic(final String nic) {
        this.nic = nic;
    }

    public int getInitialTransactionStoreSize() {
        return initialTransactionStoreSize;
    }

    public void setInitialTransactionStoreSize(final int initialTransactionStoreSize) {
        assertArgument(initialTransactionStoreSize >= 10, "The initial size of the transaction store must be greater or equal to 10");
        this.initialTransactionStoreSize = initialTransactionStoreSize;
    }

    public int getInitialTunnelStoreSize() {
        return initialTunnelStoreSize;
    }

    public void setInitialTunnelStoreSize(final int size) {
        assertArgument(size >= 10, "The initial size of the tunnel store must be greater or equal to 10");
        this.initialTunnelStoreSize = size;
    }


}

package io.snice.networking.gtp.conf;

import static io.snice.preconditions.PreConditions.assertArgument;

public class UserPlaneConfig {
    private boolean enable;

    private String nic;

    private int initialTunnelStoreSize = 10;

    public boolean isEnable() {
        return enable;
    }

    public String getNic() {
        return nic;
    }

    public int getInitialTunnelStoreSize() {
        return initialTunnelStoreSize;
    }

    public void setEnable(final boolean enable) {
        this.enable = enable;
    }

    public void setNic(final String nic) {
        this.nic = nic;
    }

    public void setInitialTunnelStoreSize(int size) {
        assertArgument(size >= 10, "The initial size of the tunnel store must be greater or equal to 10");
        this.initialTunnelStoreSize = size;
    }
}

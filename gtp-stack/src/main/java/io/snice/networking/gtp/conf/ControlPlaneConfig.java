package io.snice.networking.gtp.conf;

public class ControlPlaneConfig {

    private boolean enable;

    private String nic;

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
}

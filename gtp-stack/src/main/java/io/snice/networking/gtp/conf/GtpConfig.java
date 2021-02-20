package io.snice.networking.gtp.conf;

public class GtpConfig {

    private UserPlaneConfig userPlane;

    private ControlPlaneConfig controlPlane;

    /**
     * The initial size of the data tunnel storage
     */
    private int dataTunnelStorageSize = 10;

    public UserPlaneConfig getUserPlane() {
        return userPlane;
    }

    public void setUserPlane(final UserPlaneConfig userPlane) {
        this.userPlane = userPlane;
    }

    public ControlPlaneConfig getControlPlane() {
        return controlPlane;
    }

    public void setControlPlane(final ControlPlaneConfig controlPlane) {
        this.controlPlane = controlPlane;
    }

    public int getDataTunnelStorageSize() {
        return dataTunnelStorageSize;
    }

    public void setDataTunnelStorageSize(final int dataTunnelStorageSize) {
        this.dataTunnelStorageSize = dataTunnelStorageSize;
    }

}

package io.snice.networking.gtp.conf;

public class GtpConfig {

    private UserPlaneConfig userPlane;

    private ControlPlaneConfig controlPlane;


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
}

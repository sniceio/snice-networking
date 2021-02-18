package io.snice.networking.gtp.impl;

import io.snice.networking.gtp.DataTunnel;
import io.snice.networking.gtp.GtpUserTunnel;

public interface InternalGtpUserTunnel extends GtpUserTunnel {

    <T> DataTunnel.Builder<T> createDataTunnel(final Class<T> type, final String remoteHost, final int port);

}

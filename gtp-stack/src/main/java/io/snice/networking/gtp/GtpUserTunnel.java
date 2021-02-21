package io.snice.networking.gtp;

import io.snice.networking.common.Connection;

/**
 * The {@link GtpUserTunnel} is representing a GTP-U tunnel between two network elements in the
 * Elastic Packet Core (EPC), such as the SGW and the PGW. It provides some (well, right now actually
 * none) convenience methods that are unique to a GTP-U tunnel but in general, it is simply just
 * a {@link Connection} for exchanging primarily PDUs between the SGW & the PGW.
 */
public interface GtpUserTunnel extends GtpTunnel {

}

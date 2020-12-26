package io.snice.networking.examples.gtp;

/**
 * This {@link Device} that mimics a UE (user equipment) and encapsulates all operations,
 * such as attaching/detaching to/from a network, sending data, moving between cell sites
 * etc etc.
 * <p>
 * Note that this device technically actually mimics a "VPLMN" in that it expects to send diameter
 * traffic directly to the HPLMN and it's HSS (most likely via a DEA/DRA) and for GTP, it expects
 * to send the traffic directly to a PGW (again, part of the HPLMN).
 * <p>
 * The tl;dr; I guess, is that this "device" is for testing an HPLMN
 */
public interface Device {


}

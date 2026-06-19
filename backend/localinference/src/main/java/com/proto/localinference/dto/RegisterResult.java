package com.proto.localinference.dto;

import java.util.UUID;

/**
 * Result of a successful REGISTER handshake.
 *
 * @param reconnectKey the (possibly rotated) reconnect key to hand back to the MCU
 * @param firstConnection true if this MCU had never connected before (i.e. {@code lastConnected}
 *     was null prior to this handshake). Pre-registered-but-never-connected MCUs are the only case
 *     where this is true - an MCU re-sending REGISTER after having connected before will get {@code
 *     false}.
 */
public record RegisterResult(UUID reconnectKey, boolean firstConnection) {}

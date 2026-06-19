package com.proto.localinference.socket;

/**
 * Represents a server-initiated command queued for delivery to a connected MCU. Instances are
 * created by server-side code and consumed by {@link McuSocket} during the {@code
 * flushPendingPushes} step after every MCU request is acked.
 */
public record McuPushPacket(McuCommand command, String payload) {}

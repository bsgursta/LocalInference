package com.proto.localinference.dto;

import jakarta.validation.constraints.NotNull;
import java.net.Inet4Address;
import java.time.Instant;
import java.util.UUID;

public record ReconnectRecord(
    @NotNull UUID id,
    @NotNull UUID reconnectKey,
    @NotNull Instant connectionTime,
    @NotNull Inet4Address ipAddress) {}

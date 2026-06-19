package com.proto.localinference.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ClientDetailsRecord(@NotNull UUID id, @NotNull UUID reconnectKey) {}

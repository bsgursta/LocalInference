package com.proto.localinference.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ClientDetailsRecord(@NotBlank UUID uuid, @NotBlank UUID reregisterKey) {}

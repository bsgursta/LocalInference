package com.proto.localinference.dto.requestarguments;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegisterMcuRecord(@NotNull UUID id) {}

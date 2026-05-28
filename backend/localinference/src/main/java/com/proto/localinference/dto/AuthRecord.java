package com.proto.localinference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRecord(
    @Size(min = 2, max = 50) @NotBlank String username,
    @Size(min = 8, max = 255) String password) {}

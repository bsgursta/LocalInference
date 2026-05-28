package com.proto.localinference.dto;

import com.proto.localinference.socket.McuOptions;

public record McuInstructions(McuOptions instruction, String details) {}

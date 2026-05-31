package com.proto.localinference.dto;

import com.proto.localinference.socket.McuOption;

public record McuArg(McuOption instruction, String details) {}

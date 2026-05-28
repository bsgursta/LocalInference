package com.proto.localinference.socket;

import com.proto.localinference.dto.McuInstructions;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** This file is solely to help McuSocket.java */
@Service
public class SocketService {

  private static final int UUID_LENGTH = 36;
  private static final int CHUNK_SIZE = 512; // max chars per payload read
  private static final int MAX_PAYLOAD_CT = 512; // 512 x 512 bytes
  private static final int INSTRUCTION_MAX_LEN = 64; // 64 chars

  public Optional<UUID> validateMcuConnectionAndReturnClientId(BufferedReader reader)
      throws IOException {
    char[] idBuf = new char[UUID_LENGTH];
    int read = reader.read(idBuf, 0, UUID_LENGTH);
    int delimiter = reader.read();

    if (read != UUID_LENGTH) return Optional.empty();

    // accept \n or \r\n as valid terminators
    if (delimiter != '\n') if (delimiter != '\r' || reader.read() != '\n') return Optional.empty();

    Optional<UUID> clientId = read == UUID_LENGTH ? parseUUID(new String(idBuf)) : Optional.empty();
    return clientId;
  }

  private Optional<UUID> parseUUID(String s) {
    try {
      return Optional.of(UUID.fromString(s));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public Optional<McuInstructions> parseInstructionCodeAndDetails(
      UUID clientId, BufferedReader reader, BufferedWriter writer) throws IOException {

    StringBuilder sb = new StringBuilder(INSTRUCTION_MAX_LEN);
    McuOptions instruction = McuOptions.NIL;

    int c;
    while (sb.length() < INSTRUCTION_MAX_LEN) {
      c = reader.read();
      if (c == -1) return Optional.empty(); // stream ended
      if (c == '\n') {
        try {
          instruction = McuOptions.valueOf(sb.toString().trim());
        } catch (IllegalArgumentException e) {
          return Optional.empty(); // invalid instruction
        }
        break; // exit the while
      }
      sb.append((char) c); // cast int to char
    }

    if (instruction == McuOptions.NIL) return Optional.empty();

    sb = new StringBuilder(CHUNK_SIZE);
    char[] cBuf = new char[CHUNK_SIZE];
    while ((c = reader.read(cBuf, 0, CHUNK_SIZE)) != -1) {
      sb.append(cBuf, 0, c);
      if (sb.length() >= MAX_PAYLOAD_CT * CHUNK_SIZE) break;
      // for PING there's no payload so break on empty read
      if (c < CHUNK_SIZE) break;
    }

    return Optional.of(new McuInstructions(instruction, sb.toString()));
  }
}

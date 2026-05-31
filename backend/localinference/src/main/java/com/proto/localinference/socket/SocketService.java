package com.proto.localinference.socket;

import com.proto.localinference.dto.McuArg;
import com.proto.localinference.exceptions.InvalidSocketConnectionException;
import com.proto.localinference.exceptions.UnexpectedException;
import com.proto.localinference.services.McuService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** This file is solely to help McuSocket.java */
@Service
public class SocketService {

  private static final int UUID_LENGTH = 36;
  private static final int DELIMITER = ':';

  private McuService service;

  public SocketService(McuService service) {
    this.service = service;
  }

  public UUID validateMcuConnectionAndReturnClientIdOrRejectCon(
      BufferedReader reader, BufferedWriter writer)
      throws IOException, InvalidSocketConnectionException {
    char[] idBuf = new char[UUID_LENGTH];
    int read = reader.read(idBuf, 0, UUID_LENGTH);

    UUID clientId;
    if (read != UUID_LENGTH) {
      reject(writer);
      throw new InvalidSocketConnectionException(
          InvalidSocketConnectionException.SocketErrorOption.InvalidUuid);
    }

    clientId = parseUUID(new String(idBuf));
    if (clientId == null || service.getClient(clientId).isEmpty()) {
      reject(writer);
      throw new InvalidSocketConnectionException(
          InvalidSocketConnectionException.SocketErrorOption.InvalidUuid);
    }

    if (reader.read() != DELIMITER) {
      reject(writer);
      throw new InvalidSocketConnectionException(
          InvalidSocketConnectionException.SocketErrorOption.ImproperDelimiterUsed);
    }

    return clientId;
  }

  public void reject(BufferedWriter writer) throws IOException {
    writer.write(1);
    writer.flush();
    writer.close();
  }

  public void acknowledge(BufferedWriter writer) throws IOException {
    writer.write(0);
    writer.newLine();
    writer.flush();
  }

  /**
   * Reads the next 2 lines of the socket stream. Should contain a {@code McuOption} on the first
   * line, followed by a second line, if required, that contains the rest of the payload context
   *
   * @param reader
   * @param writer
   * @return
   * @throws IOException
   * @throws IllegalArgumentException
   */
  public McuArg readInstructionsAndPayloadIfAny(BufferedReader reader, BufferedWriter writer)
      throws IOException, InvalidSocketConnectionException {
    // Line 1
    String line = reader.readLine();
    if (line == null) {
      reject(writer);
      throw new InvalidSocketConnectionException(
          InvalidSocketConnectionException.SocketErrorOption.MissingMcuOption);
    }
    McuOption opt;
    try {
      opt = McuOption.valueOf(line);
    } catch (IllegalArgumentException e) {
      reject(writer);
      throw new InvalidSocketConnectionException(
          InvalidSocketConnectionException.SocketErrorOption.MissingMcuOption);
    }
    if (!doesRequirePayloadProcessing(opt)) return new McuArg(opt, "");

    // Line 2
    line = reader.readLine();
    if (line == null || "".equals(line)) {
      reject(writer);
      throw new InvalidSocketConnectionException(
          InvalidSocketConnectionException.SocketErrorOption.MissingRequiredContextLine);
    }

    return new McuArg(opt, line);
  }

  /**
   * Dictates whether or not to expect a line following an {@code McuOption} value that contains
   * requires mandatory information to be provided.
   *
   * @param options
   * @return
   * @throws InvalidSocketConnectionException
   */
  private boolean doesRequirePayloadProcessing(McuOption options) throws UnexpectedException {
    switch (options) {
      case McuOption.NOTIFY:
        return true;
      case McuOption.REREGISTER:
        return true;
      case McuOption.UPDATE:
        return true;
      case McuOption.REGISTER:
        return false;
      case McuOption.PING:
        return false;
      case McuOption.NIL:
        return false;
      default:
        throw new UnexpectedException(
            "SocketService.doesRequirePayloadProcessing() did not expect to reach default case");
    }
  }

  private UUID parseUUID(String s) throws InvalidSocketConnectionException {
    try {
      return UUID.fromString(s);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}

package com.proto.localinference.socket;

import com.proto.localinference.dto.McuArgRecord;
import com.proto.localinference.dto.RegisterResult;
import com.proto.localinference.exceptions.InvalidSocketRequestException;
import com.proto.localinference.services.McuService;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Low-level socket I/O helpers for {@link McuSocket}. */
@Service
public class SocketService {

  private static final int UUID_LENGTH = 36;
  private static final char DELIMITER = ':';

  private final McuService service;

  public SocketService(McuService service) {
    this.service = service;
  }

  /**
   * Reads the UUID + delimiter that opens every new connection, validates it against the whitelist,
   * and returns the parsed {@link UUID}.
   */
  public UUID parseUuid(BufferedReader reader) throws IOException, InvalidSocketRequestException {
    char[] cBuf = new char[UUID_LENGTH];
    int len = reader.read(cBuf, 0, UUID_LENGTH);

    String line = len == UUID_LENGTH ? new String(cBuf) : null;

    if (line == null || reader.read() != (int) DELIMITER)
      throw new InvalidSocketRequestException(
          InvalidSocketRequestException.SocketErrorOption.InvalidUuid);

    UUID clientId = strToUuid(line);
    if (clientId == null || service.getClient(clientId).isEmpty())
      throw new InvalidSocketRequestException(
          InvalidSocketRequestException.SocketErrorOption.InvalidUuid);

    return clientId;
  }

  // ===================================================
  // Per-message I/O
  // ===================================================

  /**
   * Reads the next command and optional payload from the stream.
   *
   * <p>Any protocol error (unknown command, missing payload) throws {@link IOException}, which
   * bubbles to the outer handler and terminates the connection. This is intentional: returning
   * without consuming the full expected input would leave the TCP buffer in an inconsistent state,
   * causing subsequent reads to process stale data from the failed message.
   *
   * <p>Wire format:
   *
   * <pre>
   *   COMMAND\n
   *   PAYLOAD\n   ← only if {@link #requiresPayload} returns true for that command
   * </pre>
   */
  public McuArgRecord parseCommandAndPayload(BufferedReader reader) throws IOException {
    String line = reader.readLine();
    if (line == null)
      throw new InvalidSocketRequestException(InvalidSocketRequestException.SocketErrorOption.EOF);

    McuCommand command;
    try {
      command = McuCommand.valueOf(line.trim());
    } catch (IllegalArgumentException e) {
      throw new InvalidSocketRequestException(
          InvalidSocketRequestException.SocketErrorOption.MissingMcuOption);
    }

    if (!requiresPayload(command)) return new McuArgRecord(command, "");

    line = reader.readLine();
    if (line == null)
      throw new InvalidSocketRequestException(
          InvalidSocketRequestException.SocketErrorOption.MissingRequiredContextLine);

    return new McuArgRecord(command, line);
  }

  /**
   * Handles REGISTER handshake; returns the reconnect key plus whether this is the MCU's first-ever
   * connection (see {@link RegisterResult}).
   */
  public Optional<RegisterResult> processRegister(UUID id, InetAddress ipAddress) {
    try {
      service.getClient(id).orElseThrow();
      return Optional.of(service.updateRegister(id, ipAddress));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /** Handles RECONNECT handshake; validates the presented key and returns a rotated one. */
  public Optional<UUID> processReconnect(UUID id, String reconnectKey, InetAddress ipAddress) {
    try {
      var client = service.getClient(id).orElseThrow();
      if (!Objects.equals(client.reconnectKey(), UUID.fromString(reconnectKey)))
        return Optional.empty();
      return Optional.of(service.updateReconnect(id, ipAddress));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  // ===================================================
  // GPS helpers
  // ===================================================

  /**
   * Parses a {@code "lat,lng"} string from the MCU and persists it. Parsing failures are logged but
   * never thrown - the connection continues regardless.
   *
   * @param clientId MCU UUID for the DB update
   * @param rawGps raw string from the MCU, expected format {@code "37.7749,-122.4194"}
   */
  public void persistGpsPosition(UUID clientId, String rawGps) {
    String[] parts = rawGps.split(",", 2);
    if (parts.length != 2) {
      System.out.println("Malformed GPS from " + clientId + ": " + rawGps);
      return;
    }
    try {
      double lat = Double.parseDouble(parts[0].trim());
      double lng = Double.parseDouble(parts[1].trim());
      service.updateGpsPosition(clientId, lat, lng);
    } catch (NumberFormatException e) {
      System.out.println("Non-numeric GPS coordinates from " + clientId + ": " + rawGps);
    }
  }

  // ===================================================
  // Internal helpers
  // ===================================================

  /**
   * Whether this command requires a payload line to follow. Every value of {@link McuCommand} must
   * appear here — the exhaustive switch is a compile error if a new command is added without
   * deciding.
   */
  private boolean requiresPayload(McuCommand command) {
    return switch (command) {
      case RECONNECT, NOTIFY, UPDATE, GPS, AUDIO_STREAM -> true;
      case REGISTER, HEALTHCHECK, NIL -> false;
    };
  }

  private UUID strToUuid(String s) {
    try {
      return UUID.fromString(s);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public void updateConnectionTime(UUID id) {
    service.updateLastConnectedTime(id);
  }
}

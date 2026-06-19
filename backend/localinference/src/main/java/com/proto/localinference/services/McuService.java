package com.proto.localinference.services;

import com.proto.localinference.dto.ClientDetailsRecord;
import com.proto.localinference.dto.ReconnectRecord;
import com.proto.localinference.dto.RegisterResult;
import com.proto.localinference.model.Mcu;
import com.proto.localinference.repository.McuRepository;
import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class McuService {

  private final McuRepository repository;

  /** In-memory whitelist: MCU UUID -> current reconnect key. */
  private final Map<UUID, UUID> allowedClientsMap = new ConcurrentHashMap<>();

  /** UNUSED currently: clients (users) subscribed to each MCU. */
  // private final Map<UUID, ArrayList<UUID>> subscriberMap = new ConcurrentHashMap<>();

  public McuService(McuRepository repository) {
    this.repository = repository;
  }

  @PostConstruct
  private void init() {
    repository.findAll().forEach(mcu -> allowedClientsMap.put(mcu.getId(), mcu.getRecoveryKey()));
  }

  // =================================
  // REST / controller-facing
  // =================================

  /**
   * Pre-registers an MCU UUID ahead of time (REST call, before the MCU ever opens a socket). {@code
   * lastConnected} stays null until the MCU actually connects — that null is what {@link
   * #updateRegister} checks to detect a first-time connection.
   */
  public Optional<ClientDetailsRecord> registerMcu(UUID id) {
    if (repository.existsById(id)) return Optional.empty();

    Mcu mcu = new Mcu();
    mcu.setId(id);
    mcu.setRecoveryKey(UUID.randomUUID());

    try {
      mcu = repository.save(mcu);
    } catch (Exception e) {
      return Optional.empty();
    }

    allowedClientsMap.put(id, mcu.getRecoveryKey());
    return Optional.of(new ClientDetailsRecord(mcu.getId(), mcu.getRecoveryKey()));
  }

  public Map<UUID, UUID> getAllowedClientsMap() {
    return allowedClientsMap;
  }

  // =================================
  // Socket-facing
  // =================================

  /** Validates a reconnect attempt; on success, rotates the reconnect key. */
  public Optional<ClientDetailsRecord> reconnectMcu(ReconnectRecord details) {
    UUID storedKey = allowedClientsMap.get(details.id());

    if (!Objects.equals(details.reconnectKey(), storedKey)) return Optional.empty();

    Mcu mcu;
    try {
      mcu = repository.findById(details.id()).orElseThrow();
    } catch (Exception e) {
      return Optional.empty();
    }

    UUID newReconnectKey = UUID.randomUUID();
    mcu.setRecoveryKey(newReconnectKey);
    mcu.setLastConnected(details.connectionTime());
    mcu.setLastKnownIp(details.ipAddress());

    repository.save(mcu);
    allowedClientsMap.put(details.id(), newReconnectKey);
    return Optional.of(new ClientDetailsRecord(details.id(), newReconnectKey));
  }

  /**
   * Called during REGISTER handshake; updates last-seen metadata and returns the reconnect key
   * along with whether this is the MCU's first-ever socket connection.
   *
   * <p>First-connection detection: {@code lastConnected} is null only for MCUs that were
   * pre-registered via {@link #registerMcu} but have never completed a socket handshake. We read
   * that flag before overwriting it with {@code Instant.now()} below.
   */
  public RegisterResult updateRegister(UUID id, InetAddress ipAddress) {
    Mcu mcu = repository.findById(id).orElseThrow();

    boolean firstConnection = mcu.getLastConnected() == null;

    mcu.setLastConnected(Instant.now());
    mcu.setLastKnownIp(ipAddress);
    repository.save(mcu);

    return new RegisterResult(mcu.getRecoveryKey(), firstConnection);
  }

  /** Called during RECONNECT handshake; rotates reconnect key and returns the new one. */
  public UUID updateReconnect(UUID id, InetAddress ipAddress) {
    Mcu mcu = repository.findById(id).orElseThrow();
    mcu.setLastConnected(Instant.now());
    mcu.setLastKnownIp(ipAddress);
    UUID newKey = UUID.randomUUID();
    mcu.setRecoveryKey(newKey);
    repository.save(mcu);
    allowedClientsMap.put(id, newKey);
    return newKey;
  }

  /**
   * Persists the GPS position received from the MCU in response to a server GPS push.
   *
   * @param id the MCU's UUID
   * @param lat latitude in decimal degrees (WGS-84)
   * @param lng longitude in decimal degrees (WGS-84)
   */
  public void updateGpsPosition(UUID id, double lat, double lng) {
    Mcu mcu = repository.findById(id).orElseThrow();
    mcu.setLastLat(lat);
    mcu.setLastLng(lng);
    mcu.setLastGpsUpdate(Instant.now());
    repository.save(mcu);
  }

  public void updateLastConnectedTime(UUID id) {
    Mcu mcu = repository.findById(id).orElseThrow();
    mcu.setLastConnected(Instant.now());
    repository.save(mcu);
  }

  // =================================
  // Utility
  // =================================

  public Optional<ClientDetailsRecord> getClient(UUID id) {
    UUID reconnectKey = allowedClientsMap.get(id);
    if (reconnectKey == null) return Optional.empty();
    return Optional.of(new ClientDetailsRecord(id, reconnectKey));
  }

  public boolean clientExists(UUID clientId) {
    return getClient(clientId).isPresent();
  }
}

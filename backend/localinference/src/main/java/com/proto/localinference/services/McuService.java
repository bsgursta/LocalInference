package com.proto.localinference.services;

import com.proto.localinference.dto.ClientDetailsRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class McuService {

  private Map<UUID, UUID> allowedClientsMap =
      new ConcurrentHashMap<>(); /* stores UUID, and public hash key to verify identity */

  private Map<UUID, ArrayList<UUID>> subscriberMap =
      new ConcurrentHashMap<>(); /* stores clients mapped to an MCU */

  public Map<UUID, UUID> getAllowedClientsMap() {
    return allowedClientsMap;
  }

  public Optional<ClientDetailsRecord> reregisterMcu(UUID macAddress, UUID backupKey) {
    /* Ensure valid device registration */
    UUID storedBackupKey = allowedClientsMap.get(macAddress);
    if (Objects.equals(storedBackupKey, backupKey)) return Optional.empty();

    UUID newPublicKey = UUID.randomUUID();

    allowedClientsMap.put(macAddress, newPublicKey);
    return Optional.of(new ClientDetailsRecord(macAddress, newPublicKey));
  }

  /* For authorized users */
  public Optional<ClientDetailsRecord> manuallyAddMcu() {
    UUID macAddress = UUID.randomUUID();
    if (!getClient(macAddress).isEmpty()) return Optional.empty();

    UUID publicKey = UUID.randomUUID();
    allowedClientsMap.put(macAddress, publicKey);

    return Optional.of(new ClientDetailsRecord(macAddress, publicKey));
  }

  // TODO: add pgsql here
  public Optional<ClientDetailsRecord> manuallyRegisterMcu(UUID macAddress) {
    /* Check if macAddress already exists in db. */
    /* If conflict, error. Requires admin intervention or reregister MCU using backup key */
    if (!getClient(macAddress).isEmpty()) return Optional.empty();

    /* Add macAddress to db */
    UUID publicKey = UUID.randomUUID();
    allowedClientsMap.put(macAddress, publicKey);

    return Optional.of(new ClientDetailsRecord(macAddress, publicKey));
  }

  public List<UUID> generateIncidentResponseClients(UUID macAddress) {
    /* TODO: implement client notification of incident */

    /* For each user in subscriberMap that is subscribed to macAddress, send incidentDetails */

    return subscriberMap.get(macAddress);
  }

  public void updateUsedModel(Byte[] model) {
    /* takes in the model weights and uploads to MCU */

    /* or maybe processes model weights and returns it organized neatly to MCU */
  }

  public Optional<ClientDetailsRecord> getClient(UUID uuid) {

    var client = allowedClientsMap.get(uuid);

    if (client == null) {
      return Optional.empty();
    }

    return Optional.of(new ClientDetailsRecord(uuid, client));
  }

  public boolean clientExists(UUID clientId) {
    return !getClient(clientId).isEmpty();
  }
}

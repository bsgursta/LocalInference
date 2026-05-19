package com.proto.localinference.services;

import com.proto.localinference.model.ClientDetailsRecord;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class McuService {

  private Map<UUID, String> allowedClientsMap =
      new ConcurrentHashMap<>(); /* stores UUID, and public hash key to verify identity */

  public Map<UUID, String> getAllowedClientsMap() {
    return allowedClientsMap;
  }

  public Optional<ClientDetailsRecord> registerMcu(int secureCode) {
    /* Ensure valid device registration */
    if (secureCode != 111) return Optional.empty();

    /* Generate unique id (and hash) stored on MCU, used for secure handshakes */
    UUID uuid = UUID.randomUUID();
    String publicKey = generateHash();

    allowedClientsMap.put(uuid, publicKey);
    return Optional.of(new ClientDetailsRecord(uuid, publicKey));
  }

  public String generateHash() {
    return "A very secure hash";
  }

  public Optional<ClientDetailsRecord> getClient(UUID uuid) {
    ClientDetailsRecord record = new ClientDetailsRecord(uuid, allowedClientsMap.get(uuid));

    return record.publicHashKeyString() == null ? Optional.empty() : Optional.of(record);
  }
}

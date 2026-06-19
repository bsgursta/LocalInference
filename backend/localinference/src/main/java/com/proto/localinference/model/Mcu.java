package com.proto.localinference.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "mcu")
public class Mcu {

  @Id private UUID id;

  @Column(nullable = false)
  private UUID recoveryKey;

  @Column
  @JdbcTypeCode(SqlTypes.INET)
  private InetAddress lastKnownIp;

  @Column private Instant lastConnected;

  @Column private Double lastLat;

  @Column private Double lastLng;

  @Column private Instant lastGpsUpdate;

  // ================================================================
  // Getters / setters
  // ================================================================

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getRecoveryKey() {
    return recoveryKey;
  }

  public void setRecoveryKey(UUID recoveryKey) {
    this.recoveryKey = recoveryKey;
  }

  public InetAddress getLastKnownIp() {
    return lastKnownIp;
  }

  public void setLastKnownIp(InetAddress lastKnownIp) {
    this.lastKnownIp = lastKnownIp;
  }

  public Instant getLastConnected() {
    return lastConnected;
  }

  public void setLastConnected(Instant lastConnected) {
    this.lastConnected = lastConnected;
  }

  public Double getLastLat() {
    return lastLat;
  }

  public void setLastLat(Double lastLat) {
    this.lastLat = lastLat;
  }

  public Double getLastLng() {
    return lastLng;
  }

  public void setLastLng(Double lastLng) {
    this.lastLng = lastLng;
  }

  public Instant getLastGpsUpdate() {
    return lastGpsUpdate;
  }

  public void setLastGpsUpdate(Instant lastGpsUpdate) {
    this.lastGpsUpdate = lastGpsUpdate;
  }
}

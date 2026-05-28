package com.proto.localinference.controller;

import com.proto.localinference.dto.ClientDetailsRecord;
import com.proto.localinference.services.McuService;
import com.proto.localinference.socket.McuSocket;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mcu")
@SecurityRequirement(name = "accessToken")
public class McuController {

  McuService service;
  McuSocket mcuSocket;

  public McuController(McuService service, McuSocket mcuSocket) {
    this.service = service;
    this.mcuSocket = mcuSocket;
  }

  @GetMapping("")
  public ResponseEntity<List<UUID>> getAllMcus() {
    List<UUID> mcuList =
        service.getAllowedClientsMap().keySet().stream().collect(Collectors.toList());
    return ResponseEntity.ok(mcuList);
  }

  @PostMapping("/register")
  public ResponseEntity<ClientDetailsRecord> doManualMcuRegistration(
      @RequestBody @NotBlank UUID macAddress) {
    var res = service.manuallyRegisterMcu(macAddress);
    return res.isEmpty() ? ResponseEntity.badRequest().build() : ResponseEntity.ok(res.get());
  }

  /**
   * Use this to generate a UUID and backup key (also UUID)
   *
   * @return {UUID, UUID}
   */
  @PostMapping("")
  public ResponseEntity<UUID> createNewMcu() {
    var res = service.manuallyAddMcu();
    return res.isEmpty()
        ? ResponseEntity.badRequest().build()
        : ResponseEntity.ok(res.get().uuid());
  }

  @PostMapping("/{macAddress}/ping")
  public ResponseEntity<Void> pingMcu(@PathVariable UUID macAddress) {
    return mcuSocket.isConnected(macAddress)
        ? ResponseEntity.ok().build()
        : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
  }

  // @PostMapping("/reregister")
  // public ResponseEntity<UUID> connectMcuToServer(@RequestBody ClientDetailsRecord recordDetails)
  // {
  //   var record = service.reregisterMcu(recordDetails.uuid(), recordDetails.reregisterKey());

  //   if (record.isEmpty()) return ResponseEntity.badRequest().build();

  //   return ResponseEntity.ok(record.get().uuid());
  // }

  // @GetMapping("/verify")
  // public ResponseEntity<Boolean> mcuHasAccess(@RequestBody UUID macAddress) {
  //   var mcu = service.getClient(macAddress);
  //   return ResponseEntity.ok(!mcu.isEmpty());
  // }
}

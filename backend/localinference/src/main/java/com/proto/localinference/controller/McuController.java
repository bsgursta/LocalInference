package com.proto.localinference.controller;

import com.proto.localinference.model.ClientDetailsRecord;
import com.proto.localinference.services.McuService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mcu")
public class McuController {

  McuService service;

  public McuController(McuService service) {
    this.service = service;
  }

  @GetMapping("/mcus")
  public ResponseEntity<List<UUID>> getAllMcus() {
    Map<UUID, String> map = service.getAllowedClientsMap();
    List<UUID> mcuList = map.keySet().stream().collect(Collectors.toList());
    return ResponseEntity.ok(mcuList);
  }

  @PostMapping("/{secure_code}")
  public ResponseEntity<UUID> connectMcuToServer(@PathVariable int secure_code) {
    Optional<ClientDetailsRecord> record = service.registerMcu(secure_code);
    if (record.isEmpty()) return ResponseEntity.badRequest().build();

    return ResponseEntity.ok(record.get().uuid());
  }
}

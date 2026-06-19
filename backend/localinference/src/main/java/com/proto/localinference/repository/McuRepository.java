package com.proto.localinference.repository;

import com.proto.localinference.model.Mcu;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface McuRepository extends CrudRepository<Mcu, UUID> {

  //   Optional<Mcu> findById(UUID id);

  //   boolean existsById(UUID id);
}

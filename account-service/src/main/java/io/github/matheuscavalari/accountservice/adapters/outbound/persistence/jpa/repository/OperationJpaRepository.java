package io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.repository;


import io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.entity.OperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OperationJpaRepository extends JpaRepository<OperationEntity, UUID> {
}

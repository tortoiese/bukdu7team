package io.entry.preregistration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PreregistrationRepository extends JpaRepository<Preregistration, UUID> {
}

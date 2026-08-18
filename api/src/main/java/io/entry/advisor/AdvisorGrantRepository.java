package io.entry.advisor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdvisorGrantRepository extends JpaRepository<AdvisorGrant, UUID> {
    Optional<AdvisorGrant> findByGrantToken(String grantToken);
}

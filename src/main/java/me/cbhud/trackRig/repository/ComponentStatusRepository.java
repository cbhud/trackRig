package me.cbhud.trackRig.repository;

import me.cbhud.trackRig.model.ComponentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComponentStatusRepository extends JpaRepository<ComponentStatus, Integer> {
    Optional<ComponentStatus> findByName(String name);
}

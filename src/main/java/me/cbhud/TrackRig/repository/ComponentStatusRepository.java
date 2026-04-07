package me.cbhud.TrackRig.repository;

import me.cbhud.TrackRig.model.ComponentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComponentStatusRepository extends JpaRepository<ComponentStatus, Integer> {

    // Used during Excel import to resolve status by human-readable name
    Optional<ComponentStatus> findByNameIgnoreCase(String name);
}

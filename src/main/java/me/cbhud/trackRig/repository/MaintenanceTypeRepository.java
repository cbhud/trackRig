package me.cbhud.trackRig.repository;

import me.cbhud.trackRig.model.MaintenanceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceTypeRepository extends JpaRepository<MaintenanceType, Integer> {
    Optional<MaintenanceType> findByName(String name);
    List<MaintenanceType> findByIsActive(Boolean isActive);
}

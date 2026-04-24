package me.cbhud.trackRig.repository;

import me.cbhud.trackRig.model.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Integer> {
    List<MaintenanceLog> findByWorkstationId(Integer workstationId);
    List<MaintenanceLog> findByMaintenanceTypeId(Integer maintenanceTypeId);
}

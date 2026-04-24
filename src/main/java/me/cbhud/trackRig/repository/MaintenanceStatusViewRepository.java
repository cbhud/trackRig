package me.cbhud.trackRig.repository;

import me.cbhud.trackRig.model.MaintenanceStatusView;
import me.cbhud.trackRig.model.MaintenanceStatusViewId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceStatusViewRepository extends JpaRepository<MaintenanceStatusView, MaintenanceStatusViewId> {
    List<MaintenanceStatusView> findByWorkstationId(Integer workstationId);
    List<MaintenanceStatusView> findByStatus(String status);
}

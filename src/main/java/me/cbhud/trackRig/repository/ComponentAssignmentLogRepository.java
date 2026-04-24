package me.cbhud.trackRig.repository;

import me.cbhud.trackRig.model.ComponentAssignmentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComponentAssignmentLogRepository extends JpaRepository<ComponentAssignmentLog, Integer> {
    List<ComponentAssignmentLog> findByComponentId(Integer componentId);
    List<ComponentAssignmentLog> findByWorkstationId(Integer workstationId);
    /** Active assignment = no removed_at yet */
    Optional<ComponentAssignmentLog> findByComponentIdAndRemovedAtIsNull(Integer componentId);
}

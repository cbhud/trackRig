package me.cbhud.trackRig.dto.response;

import me.cbhud.trackRig.model.MaintenanceStatusView;

import java.time.OffsetDateTime;

public record MaintenanceStatusResponse(
        Integer workstationId,
        String workstationName,
        Integer maintenanceTypeId,
        String maintenanceName,
        Integer intervalDays,
        OffsetDateTime lastPerformed,
        OffsetDateTime nextDueDate,
        String status
) {
    public static MaintenanceStatusResponse from(MaintenanceStatusView view) {
        return new MaintenanceStatusResponse(
                view.getWorkstationId(),
                view.getWorkstationName(),
                view.getMaintenanceTypeId(),
                view.getMaintenanceName(),
                view.getIntervalDays(),
                view.getLastPerformed(),
                view.getNextDueDate(),
                view.getStatus()
        );
    }
}

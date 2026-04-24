package me.cbhud.trackRig.dto.response;

import me.cbhud.trackRig.model.MaintenanceLog;

import java.time.OffsetDateTime;

public record MaintenanceLogResponse(
        Integer id,
        Integer workstationId,
        String workstationName,
        Integer maintenanceTypeId,
        String maintenanceTypeName,
        Integer performedByUserId,
        String performedByUsername,
        OffsetDateTime performedAt,
        String notes
) {
    public static MaintenanceLogResponse from(MaintenanceLog log) {
        return new MaintenanceLogResponse(
                log.getId(),
                log.getWorkstation().getId(),
                log.getWorkstation().getName(),
                log.getMaintenanceType().getId(),
                log.getMaintenanceType().getName(),
                log.getPerformedByUser() != null ? log.getPerformedByUser().getId() : null,
                log.getPerformedByUser() != null ? log.getPerformedByUser().getUsername() : null,
                log.getPerformedAt(),
                log.getNotes()
        );
    }
}

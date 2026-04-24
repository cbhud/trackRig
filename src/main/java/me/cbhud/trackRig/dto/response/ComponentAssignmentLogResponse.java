package me.cbhud.trackRig.dto.response;

import me.cbhud.trackRig.model.ComponentAssignmentLog;

import java.time.OffsetDateTime;

public record ComponentAssignmentLogResponse(
        Integer id,
        Integer componentId,
        String componentName,
        Integer workstationId,
        String workstationName,
        Integer assignedByUserId,
        String assignedByUsername,
        OffsetDateTime assignedAt,
        OffsetDateTime removedAt,
        String notes
) {
    public static ComponentAssignmentLogResponse from(ComponentAssignmentLog log) {
        return new ComponentAssignmentLogResponse(
                log.getId(),
                log.getComponent().getId(),
                log.getComponent().getName(),
                log.getWorkstation() != null ? log.getWorkstation().getId() : null,
                log.getWorkstation() != null ? log.getWorkstation().getName() : null,
                log.getAssignedByUser() != null ? log.getAssignedByUser().getId() : null,
                log.getAssignedByUser() != null ? log.getAssignedByUser().getUsername() : null,
                log.getAssignedAt(),
                log.getRemovedAt(),
                log.getNotes()
        );
    }
}

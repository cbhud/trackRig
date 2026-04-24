package me.cbhud.trackRig.dto.response;

import me.cbhud.trackRig.model.MaintenanceType;

public record MaintenanceTypeResponse(
        Integer id,
        String name,
        String description,
        Integer intervalDays,
        Boolean isActive
) {
    public static MaintenanceTypeResponse from(MaintenanceType mt) {
        return new MaintenanceTypeResponse(
                mt.getId(),
                mt.getName(),
                mt.getDescription(),
                mt.getIntervalDays(),
                mt.getIsActive()
        );
    }
}

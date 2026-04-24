package me.cbhud.trackRig.dto;

import me.cbhud.trackRig.model.WorkstationStatus;

public record WorkstationStatusResponse(
        Integer id,
        String name,
        String color
) {
    public static WorkstationStatusResponse from(WorkstationStatus workstationStatus) {
        return new WorkstationStatusResponse(
                workstationStatus.getId(),
                workstationStatus.getName(),
                workstationStatus.getColor());
    }
}

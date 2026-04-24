package me.cbhud.trackRig.dto;

import me.cbhud.trackRig.model.Workstation;

public record WorkstationResponse(
        Integer id,
        String name,
        WorkstationStatusResponse status,
        Integer gridX,
        Integer gridY,
        Integer floor
)
{
    public static WorkstationResponse from(Workstation workstation) {

        return new WorkstationResponse(
                workstation.getId(),
                workstation.getName(),
                workstation.getStatus() != null ? WorkstationStatusResponse.from(workstation.getStatus()) : null,
                workstation.getGridX(),
                workstation.getGridY(),
                workstation.getFloor()
                );
    }
}
package me.cbhud.trackRig.dto.response;

import me.cbhud.trackRig.model.Workstation;

public record WorkstationUpdateResponse(
        String name,
        Integer gridX,
        Integer gridY,
        Integer floor
) {
        public static WorkstationUpdateResponse from(Workstation workstation) {
                return new WorkstationUpdateResponse(
                        workstation.getName(),
                        workstation.getGridX(),
                        workstation.getGridY(),
                        workstation.getFloor()
                );
        }
}

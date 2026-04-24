package me.cbhud.trackRig.dto.response;

import me.cbhud.trackRig.model.ComponentCategory;
import me.cbhud.trackRig.model.ComponentStatus;

public record ComponentStatusResponse(
        Integer id,
        String name
) {
    public static ComponentStatusResponse from(ComponentStatus componentStatus) {
        return new ComponentStatusResponse(
                componentStatus.getId(),
                componentStatus.getName()
        );
    }
}

package me.cbhud.trackRig.dto.response;

import me.cbhud.trackRig.model.Component;

import java.time.OffsetDateTime;

public record ComponentResponse(
        Integer id,
        String serialNumber,
        String name,
        String notes,
        ComponentCategoryResponse category,
        ComponentStatusResponse status,
        WorkstationResponse workstation,
        OffsetDateTime createdAt
) {
    public static ComponentResponse from(Component component) {
        return new ComponentResponse(
                component.getId(),
                component.getSerialNumber(),
                component.getName(),
                component.getNotes(),
                component.getCategory() != null ? ComponentCategoryResponse.from(component.getCategory()) : null,
                component.getStatus() != null ? ComponentStatusResponse.from(component.getStatus()) : null,
                component.getWorkstation() != null ? WorkstationResponse.from(component.getWorkstation()) : null,
                component.getCreatedAt()
        );
    }
}

package me.cbhud.trackRig.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.cbhud.trackRig.model.ComponentCategory;
import me.cbhud.trackRig.model.ComponentStatus;

public record ComponentRequest(
        @Nullable
        @Size(max = 100)
        String serialNumber,
        @NotNull
        String name,
        @Size(max = 255)
        @Nullable
        String notes,
        @NotNull
        ComponentCategory componentCategory,
        @NotNull
        ComponentStatus componentStatus,
        @Nullable
        WorkstationRequest workstation
        ) {
}

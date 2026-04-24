package me.cbhud.trackRig.model;

import java.io.Serializable;
import java.util.Objects;

public class MaintenanceStatusViewId implements Serializable {
    private Integer workstationId;
    private Integer maintenanceTypeId;

    public MaintenanceStatusViewId() {}

    public MaintenanceStatusViewId(Integer workstationId, Integer maintenanceTypeId) {
        this.workstationId = workstationId;
        this.maintenanceTypeId = maintenanceTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaintenanceStatusViewId that)) return false;
        return Objects.equals(workstationId, that.workstationId)
                && Objects.equals(maintenanceTypeId, that.maintenanceTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workstationId, maintenanceTypeId);
    }
}

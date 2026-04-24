package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.MaintenanceTypeRequest;
import me.cbhud.trackRig.dto.request.MaintenanceTypeUpdateRequest;
import me.cbhud.trackRig.dto.response.MaintenanceTypeResponse;

import java.util.List;

public interface MaintenanceTypeService {
    List<MaintenanceTypeResponse> getAllMaintenanceTypes();
    List<MaintenanceTypeResponse> getActiveMaintenanceTypes();
    MaintenanceTypeResponse getMaintenanceTypeById(Integer id);
    MaintenanceTypeResponse createMaintenanceType(MaintenanceTypeRequest request);
    MaintenanceTypeResponse updateMaintenanceType(Integer id, MaintenanceTypeUpdateRequest request);
    void deleteMaintenanceType(Integer id);
}

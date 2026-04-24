package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.MaintenanceLogRequest;
import me.cbhud.trackRig.dto.response.MaintenanceLogResponse;
import me.cbhud.trackRig.dto.response.MaintenanceStatusResponse;

import java.util.List;

public interface MaintenanceLogService {
    List<MaintenanceLogResponse> getAllLogs();
    List<MaintenanceLogResponse> getLogsByWorkstation(Integer workstationId);
    List<MaintenanceLogResponse> getLogsByType(Integer maintenanceTypeId);
    MaintenanceLogResponse getLogById(Integer id);
    MaintenanceLogResponse createLog(MaintenanceLogRequest request, String username);
    void deleteLog(Integer id);

    // View-based status queries
    List<MaintenanceStatusResponse> getMaintenanceStatus();
    List<MaintenanceStatusResponse> getMaintenanceStatusByWorkstation(Integer workstationId);
    List<MaintenanceStatusResponse> getOverdueMaintenances();
}

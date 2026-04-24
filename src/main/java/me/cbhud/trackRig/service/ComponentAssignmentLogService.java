package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.ComponentAssignmentLogRequest;
import me.cbhud.trackRig.dto.response.ComponentAssignmentLogResponse;

import java.util.List;

public interface ComponentAssignmentLogService {
    List<ComponentAssignmentLogResponse> getAllLogs();
    List<ComponentAssignmentLogResponse> getLogsByComponent(Integer componentId);
    List<ComponentAssignmentLogResponse> getLogsByWorkstation(Integer workstationId);
    ComponentAssignmentLogResponse getActiveAssignment(Integer componentId);
    ComponentAssignmentLogResponse createAssignment(ComponentAssignmentLogRequest request, String username);
    ComponentAssignmentLogResponse closeAssignment(Integer componentId, String notes);
}

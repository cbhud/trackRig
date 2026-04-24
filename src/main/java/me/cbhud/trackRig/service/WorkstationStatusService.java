package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.WorkstationStatusRequest;
import me.cbhud.trackRig.dto.request.WorkstationStatusUpdateRequest;
import me.cbhud.trackRig.dto.response.WorkstationStatusResponse;

import java.util.List;

public interface WorkstationStatusService {
    List<WorkstationStatusResponse> getAllWorkstationStatuses();
    WorkstationStatusResponse getWorkstationStatusById(Integer id);
    WorkstationStatusResponse createWorkstationStatus(WorkstationStatusRequest workstation);
    void deleteWorkstationStatus(Integer id);
    public WorkstationStatusResponse updateWorkstationStatus(Integer id, WorkstationStatusUpdateRequest workstationStatusUpdateRequest);
}

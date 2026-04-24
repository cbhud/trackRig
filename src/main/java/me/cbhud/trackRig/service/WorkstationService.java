package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.WorkstationRequest;
import me.cbhud.trackRig.dto.request.WorkstationUpdateRequest;
import me.cbhud.trackRig.dto.request.WorkstationUpdateStatusRequest;
import me.cbhud.trackRig.dto.response.WorkstationResponse;

import java.util.List;

public interface WorkstationService {
    List<WorkstationResponse> getAllWorkstations();
    WorkstationResponse getWorkstationById(Integer id);
    WorkstationResponse createWorkstation(WorkstationRequest workstation);
    void deleteWorkstation(Integer id);
    WorkstationResponse updateWorkstation(Integer id, WorkstationUpdateRequest workstationUpdateRequest);
    WorkstationResponse updateWorkstationStatus(Integer id, WorkstationUpdateStatusRequest workstationUpdateStatusRequest);
}

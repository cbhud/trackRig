package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.*;

import java.util.List;

public interface WorkstationService {
    List<WorkstationResponse> getAllWorkstations();
    WorkstationResponse getWorkstationById(Integer id);
    WorkstationResponse createWorkstation(WorkstationRequest workstation);
    void deleteWorkstation(Integer id);
    WorkstationResponse updateWorkstation(Integer id, WorkstationUpdateRequest workstationUpdateRequest);
    WorkstationResponse updateWorkstationStatus(Integer id, WorkstationUpdateStatusRequest workstationUpdateStatusRequest);
}

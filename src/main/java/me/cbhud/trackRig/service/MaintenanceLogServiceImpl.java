package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.MaintenanceLogRequest;
import me.cbhud.trackRig.dto.response.MaintenanceLogResponse;
import me.cbhud.trackRig.dto.response.MaintenanceStatusResponse;
import me.cbhud.trackRig.exception.ResourceNotFoundException;
import me.cbhud.trackRig.model.AppUser;
import me.cbhud.trackRig.model.MaintenanceLog;
import me.cbhud.trackRig.model.MaintenanceType;
import me.cbhud.trackRig.model.Workstation;
import me.cbhud.trackRig.repository.AppUserRepository;
import me.cbhud.trackRig.repository.MaintenanceLogRepository;
import me.cbhud.trackRig.repository.MaintenanceStatusViewRepository;
import me.cbhud.trackRig.repository.MaintenanceTypeRepository;
import me.cbhud.trackRig.repository.WorkstationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaintenanceLogServiceImpl implements MaintenanceLogService {

    private final MaintenanceLogRepository logRepository;
    private final MaintenanceTypeRepository typeRepository;
    private final WorkstationRepository workstationRepository;
    private final AppUserRepository userRepository;
    private final MaintenanceStatusViewRepository statusViewRepository;

    public MaintenanceLogServiceImpl(
            MaintenanceLogRepository logRepository,
            MaintenanceTypeRepository typeRepository,
            WorkstationRepository workstationRepository,
            AppUserRepository userRepository,
            MaintenanceStatusViewRepository statusViewRepository
    ) {
        this.logRepository = logRepository;
        this.typeRepository = typeRepository;
        this.workstationRepository = workstationRepository;
        this.userRepository = userRepository;
        this.statusViewRepository = statusViewRepository;
    }

    @Override
    public List<MaintenanceLogResponse> getAllLogs() {
        return logRepository.findAll()
                .stream()
                .map(MaintenanceLogResponse::from)
                .toList();
    }

    @Override
    public List<MaintenanceLogResponse> getLogsByWorkstation(Integer workstationId) {
        if (!workstationRepository.existsById(workstationId)) {
            throw new ResourceNotFoundException("Workstation not found");
        }
        return logRepository.findByWorkstationId(workstationId)
                .stream()
                .map(MaintenanceLogResponse::from)
                .toList();
    }

    @Override
    public List<MaintenanceLogResponse> getLogsByType(Integer maintenanceTypeId) {
        if (!typeRepository.existsById(maintenanceTypeId)) {
            throw new ResourceNotFoundException("Maintenance type not found");
        }
        return logRepository.findByMaintenanceTypeId(maintenanceTypeId)
                .stream()
                .map(MaintenanceLogResponse::from)
                .toList();
    }

    @Override
    public MaintenanceLogResponse getLogById(Integer id) {
        return logRepository.findById(id)
                .map(MaintenanceLogResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance log not found"));
    }

    @Override
    @Transactional
    public MaintenanceLogResponse createLog(MaintenanceLogRequest request, String username) {
        Workstation workstation = workstationRepository.findById(request.workstationId())
                .orElseThrow(() -> new ResourceNotFoundException("Workstation not found"));

        MaintenanceType type = typeRepository.findById(request.maintenanceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance type not found"));

        AppUser performer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MaintenanceLog log = new MaintenanceLog();
        log.setWorkstation(workstation);
        log.setMaintenanceType(type);
        log.setPerformedByUser(performer);
        log.setNotes(request.notes());

        return MaintenanceLogResponse.from(logRepository.save(log));
    }

    @Override
    @Transactional
    public void deleteLog(Integer id) {
        MaintenanceLog log = logRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance log not found"));
        logRepository.delete(log);
    }

    @Override
    public List<MaintenanceStatusResponse> getMaintenanceStatus() {
        return statusViewRepository.findAll()
                .stream()
                .map(MaintenanceStatusResponse::from)
                .toList();
    }

    @Override
    public List<MaintenanceStatusResponse> getMaintenanceStatusByWorkstation(Integer workstationId) {
        if (!workstationRepository.existsById(workstationId)) {
            throw new ResourceNotFoundException("Workstation not found");
        }
        return statusViewRepository.findByWorkstationId(workstationId)
                .stream()
                .map(MaintenanceStatusResponse::from)
                .toList();
    }

    @Override
    public List<MaintenanceStatusResponse> getOverdueMaintenances() {
        return statusViewRepository.findByStatus("OVERDUE")
                .stream()
                .map(MaintenanceStatusResponse::from)
                .toList();
    }
}

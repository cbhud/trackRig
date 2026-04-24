package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.MaintenanceTypeRequest;
import me.cbhud.trackRig.dto.request.MaintenanceTypeUpdateRequest;
import me.cbhud.trackRig.dto.response.MaintenanceTypeResponse;
import me.cbhud.trackRig.exception.ResourceAlreadyExistsException;
import me.cbhud.trackRig.exception.ResourceNotFoundException;
import me.cbhud.trackRig.model.MaintenanceType;
import me.cbhud.trackRig.repository.MaintenanceTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class MaintenanceTypeServiceImpl implements MaintenanceTypeService {

    private final MaintenanceTypeRepository maintenanceTypeRepository;

    public MaintenanceTypeServiceImpl(MaintenanceTypeRepository maintenanceTypeRepository) {
        this.maintenanceTypeRepository = maintenanceTypeRepository;
    }

    @Override
    public List<MaintenanceTypeResponse> getAllMaintenanceTypes() {
        return maintenanceTypeRepository.findAll()
                .stream()
                .map(MaintenanceTypeResponse::from)
                .toList();
    }

    @Override
    public List<MaintenanceTypeResponse> getActiveMaintenanceTypes() {
        return maintenanceTypeRepository.findByIsActive(true)
                .stream()
                .map(MaintenanceTypeResponse::from)
                .toList();
    }

    @Override
    public MaintenanceTypeResponse getMaintenanceTypeById(Integer id) {
        return maintenanceTypeRepository.findById(id)
                .map(MaintenanceTypeResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance type not found"));
    }

    @Override
    @Transactional
    public MaintenanceTypeResponse createMaintenanceType(MaintenanceTypeRequest request) {
        if (maintenanceTypeRepository.findByName(request.name()).isPresent()) {
            throw new ResourceAlreadyExistsException("Maintenance type already exists");
        }

        MaintenanceType mt = new MaintenanceType();
        mt.setName(request.name());
        mt.setDescription(request.description());
        mt.setIntervalDays(request.intervalDays());
        mt.setIsActive(request.isActive() != null ? request.isActive() : true);

        return MaintenanceTypeResponse.from(maintenanceTypeRepository.save(mt));
    }

    @Override
    @Transactional
    public MaintenanceTypeResponse updateMaintenanceType(Integer id, MaintenanceTypeUpdateRequest request) {
        MaintenanceType mt = maintenanceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance type not found"));

        boolean changed = false;

        if (request.name() != null && !Objects.equals(mt.getName(), request.name())) {
            maintenanceTypeRepository.findByName(request.name())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new ResourceAlreadyExistsException("Maintenance type with that name already exists");
                    });
            mt.setName(request.name());
            changed = true;
        }

        if (request.description() != null && !Objects.equals(mt.getDescription(), request.description())) {
            mt.setDescription(request.description());
            changed = true;
        }

        if (request.intervalDays() != null && !Objects.equals(mt.getIntervalDays(), request.intervalDays())) {
            mt.setIntervalDays(request.intervalDays());
            changed = true;
        }

        if (request.isActive() != null && !Objects.equals(mt.getIsActive(), request.isActive())) {
            mt.setIsActive(request.isActive());
            changed = true;
        }

        if (changed) {
            mt = maintenanceTypeRepository.save(mt);
        }

        return MaintenanceTypeResponse.from(mt);
    }

    @Override
    @Transactional
    public void deleteMaintenanceType(Integer id) {
        MaintenanceType mt = maintenanceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance type not found"));
        maintenanceTypeRepository.delete(mt);
    }
}

package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.WorkstationRequest;
import me.cbhud.trackRig.dto.WorkstationResponse;
import me.cbhud.trackRig.model.Workstation;
import me.cbhud.trackRig.model.WorkstationStatus;
import me.cbhud.trackRig.repository.WorkstationRepository;
import me.cbhud.trackRig.repository.WorkstationStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkstationServiceImplTest {

    @Mock
    private WorkstationRepository workstationRepository;

    @Mock
    private WorkstationStatusRepository workstationStatusRepository;

    @InjectMocks
    private WorkstationServiceImpl workstationService;

    private WorkstationStatus defaultStatus;

    @BeforeEach
    void setUp() {
        defaultStatus = new WorkstationStatus();
        defaultStatus.setId(1);
        defaultStatus.setName("Available");
    }

    @Test
    void createWorkstation_ShouldSetDefaultStatus() {
        // Given
        WorkstationRequest request = new WorkstationRequest("WS-01");
        when(workstationRepository.findByName("WS-01")).thenReturn(Optional.empty());
        when(workstationStatusRepository.findById(1)).thenReturn(Optional.of(defaultStatus));
        
        Workstation savedWorkstation = new Workstation();
        savedWorkstation.setId(1);
        savedWorkstation.setName("WS-01");
        savedWorkstation.setStatus(defaultStatus);
        
        when(workstationRepository.save(any(Workstation.class))).thenReturn(savedWorkstation);

        // When
        WorkstationResponse response = workstationService.createWorkstation(request);

        // Then
        assertNotNull(response);
        assertEquals("WS-01", response.name());
        verify(workstationRepository).save(argThat(workstation -> {
            assertEquals("WS-01", workstation.getName());
            assertNotNull(workstation.getStatus());
            assertEquals(1, workstation.getStatus().getId());
            return true;
        }));
    }

    @Test
    void createWorkstation_ShouldThrowIfAlreadyExists() {
        // Given
        WorkstationRequest request = new WorkstationRequest("WS-01");
        when(workstationRepository.findByName("WS-01")).thenReturn(Optional.of(new Workstation()));

        // When & Then
        assertThrows(RuntimeException.class, () -> workstationService.createWorkstation(request));
        verify(workstationRepository, never()).save(any());
    }
}

package me.cbhud.trackRig.repository;

import me.cbhud.trackRig.model.WorkstationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkstationStatusRepository extends JpaRepository<WorkstationStatus, Integer> {

    public Optional<WorkstationStatus> findByName(String name);

}

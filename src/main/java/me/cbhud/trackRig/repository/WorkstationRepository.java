package me.cbhud.trackRig.repository;

import me.cbhud.trackRig.model.Workstation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkstationRepository extends JpaRepository<Workstation, Integer> {
    Optional<Workstation> findByName(String name);
}

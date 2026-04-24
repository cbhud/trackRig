package me.cbhud.trackRig.repository;

import me.cbhud.trackRig.model.Component;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComponentRepository extends JpaRepository<Component, Integer> {
    Optional<Component> findBySerialNumber(String serialNumber);
}

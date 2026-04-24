package me.cbhud.trackRig.repository;

import me.cbhud.trackRig.model.ComponentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComponentCategoryRepository extends JpaRepository<ComponentCategory, Integer> {
    Optional<ComponentCategory> findByName(String name);
}

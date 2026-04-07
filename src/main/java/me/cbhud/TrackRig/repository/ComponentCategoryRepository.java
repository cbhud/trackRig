package me.cbhud.TrackRig.repository;

import me.cbhud.TrackRig.model.ComponentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComponentCategoryRepository extends JpaRepository<ComponentCategory, Integer> {

    // Used during Excel import to resolve category by human-readable name
    Optional<ComponentCategory> findByNameIgnoreCase(String name);
}

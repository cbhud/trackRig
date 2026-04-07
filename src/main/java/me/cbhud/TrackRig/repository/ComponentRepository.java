package me.cbhud.TrackRig.repository;

import me.cbhud.TrackRig.model.Component;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComponentRepository extends JpaRepository<Component, Integer> {

    // Get all components assigned to a specific workstation
    List<Component> findByWorkstationId(Integer workstationId);

    // Get all components not assigned to any workstation (in storage/stock)
    List<Component> findByWorkstationIsNull();

    // Get all components assigned to any workstation
    List<Component> findByWorkstationIsNotNull();

    // Lookup a component by its unique serial number
    Optional<Component> findBySerialNumber(String serialNumber);

    // Fast uniqueness check — used during Excel import to detect DB-level
    // duplicates
    boolean existsBySerialNumber(String serialNumber);

    // ========================
    // EXPORT FILTER QUERIES
    // ========================
    // All filter params are optional (null = no restriction on that field).
    // inStorage: true = workstation IS NULL, false = workstation IS NOT NULL, null
    // = both.
    @Query("SELECT c FROM Component c " +
            "WHERE (:statusId IS NULL OR c.componentStatus.id = :statusId) " +
            "AND (:categoryId IS NULL OR c.componentCategory.id = :categoryId) " +
            "AND (:inStorage IS NULL " +
            "     OR (:inStorage = TRUE AND c.workstation IS NULL) " +
            "     OR (:inStorage = FALSE AND c.workstation IS NOT NULL))")
    List<Component> findByFilters(
            @Param("statusId") Integer statusId,
            @Param("categoryId") Integer categoryId,
            @Param("inStorage") Boolean inStorage);
}

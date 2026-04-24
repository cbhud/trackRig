package me.cbhud.trackRig.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.time.OffsetDateTime;

/**
 * Read-only mapping of the view_maintenance_status database view.
 * Uses @Subselect so Hibernate never attempts DDL on this entity.
 * Composite key: (workstationId, maintenanceTypeId).
 */
@Entity
@Immutable
@IdClass(MaintenanceStatusViewId.class)
@Subselect("""
        SELECT
            w.id                                                          AS workstation_id,
            w.name                                                        AS workstation_name,
            mt.id                                                         AS maintenance_type_id,
            mt.name                                                       AS maintenance_name,
            mt.interval_days,
            ll.last_performed,
            ll.last_performed + (mt.interval_days * INTERVAL '1 day')    AS next_due_date,
            CASE
                WHEN ll.last_performed IS NULL THEN 'NEVER_DONE'
                WHEN ll.last_performed + (mt.interval_days * INTERVAL '1 day') < now() THEN 'OVERDUE'
                WHEN ll.last_performed + (mt.interval_days * INTERVAL '1 day') < now() + INTERVAL '3 days' THEN 'DUE_SOON'
                ELSE 'OK'
            END AS status
        FROM workstation w
        CROSS JOIN maintenance_type mt
        LEFT JOIN (
            SELECT workstation_id, maintenance_type_id, MAX(performed_at) AS last_performed
            FROM maintenance_log
            GROUP BY workstation_id, maintenance_type_id
        ) ll ON ll.workstation_id = w.id AND ll.maintenance_type_id = mt.id
        WHERE mt.is_active = TRUE
        """)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class MaintenanceStatusView {

    @Id
    @Column(name = "workstation_id")
    private Integer workstationId;

    @Id
    @Column(name = "maintenance_type_id")
    private Integer maintenanceTypeId;

    @Column(name = "workstation_name")
    private String workstationName;

    @Column(name = "maintenance_name")
    private String maintenanceName;

    @Column(name = "interval_days")
    private Integer intervalDays;

    @Column(name = "last_performed")
    private OffsetDateTime lastPerformed;

    @Column(name = "next_due_date")
    private OffsetDateTime nextDueDate;

    @Column(name = "status")
    private String status;
}

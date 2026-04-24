package me.cbhud.trackRig.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "maintenance_log")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class MaintenanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "workstation_id", nullable = false)
    private Workstation workstation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "maintenance_type_id", nullable = false)
    private MaintenanceType maintenanceType;

    @ManyToOne
    @JoinColumn(name = "performed_by_user_id")
    private AppUser performedByUser;

    @Column(name = "performed_at", nullable = false, updatable = false)
    private OffsetDateTime performedAt;

    @Column
    @Size(max = 65535)
    private String notes;

    @PrePersist
    public void prePersist() {
        if (performedAt == null) {
            performedAt = OffsetDateTime.now();
        }
    }
}

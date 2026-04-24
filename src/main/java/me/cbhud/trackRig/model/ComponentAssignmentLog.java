package me.cbhud.trackRig.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "component_assignment_log")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ComponentAssignmentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;

    @ManyToOne
    @JoinColumn(name = "workstation_id")
    private Workstation workstation;

    @ManyToOne
    @JoinColumn(name = "assigned_by_user_id")
    private AppUser assignedByUser;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "removed_at")
    private OffsetDateTime removedAt;

    @Column
    @Size(max = 65535)
    private String notes;

    @PrePersist
    public void prePersist() {
        if (assignedAt == null) {
            assignedAt = OffsetDateTime.now();
        }
    }
}

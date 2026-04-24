package me.cbhud.trackRig.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "component")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Component {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "serial_number")
    @Size(max = 100)
    private String serialNumber;
    @Column(name = "name", nullable = false)
    @Size(max = 200)
    private String name;
    @Column
    private String notes;
    @ManyToOne()
    @JoinColumn(name = "category_id", nullable = false)
    private ComponentCategory category;
    @ManyToOne()
    @JoinColumn(name = "status_id", nullable = false)
    private ComponentStatus status;
    @ManyToOne()
    @JoinColumn(name = "workstation_id")
    private Workstation workstation;
    @Column(name = "created_at", nullable = false, updatable = false)
    OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

}

package me.cbhud.trackRig.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "workstation")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Workstation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne()
    @JoinColumn(name = "status_id", nullable = true)
    private WorkstationStatus status;

    @Column(name = "grid_x")
    private Integer gridX;

    @Column(name = "grid_y")
    private Integer gridY;

    @Column
    private Integer floor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
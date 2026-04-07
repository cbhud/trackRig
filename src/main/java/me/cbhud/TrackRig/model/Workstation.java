package me.cbhud.TrackRig.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "workstation", uniqueConstraints = {
        @UniqueConstraint(name = "uq_workstation_grid", columnNames = {"grid_x", "grid_y"})
})
@Getter
@Setter
public class Workstation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private WorkstationStatus workstationStatus;

    @Column(name = "grid_x")
    private int gridX;

    @Column(name = "grid_y")
    private int gridY;

    // the value is auto-populated by the DB via DEFAULT CURRENT_TIMESTAMP.
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

}

package me.cbhud.trackRig.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "maintenance_type")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class MaintenanceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column
    private String description;

    @Column(name = "interval_days", nullable = false)
    @Min(1)
    private Integer intervalDays;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}

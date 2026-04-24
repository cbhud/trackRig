package me.cbhud.trackRig.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "component_status")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ComponentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(nullable = false, unique = true, length = 50)
    String name;
}

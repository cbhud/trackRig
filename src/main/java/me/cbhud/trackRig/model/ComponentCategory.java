package me.cbhud.trackRig.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "component_category")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ComponentCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(nullable = false, unique = true, length = 50)
    String name;
    @Column
    String description;
}

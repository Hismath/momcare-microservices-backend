package com.momcare.kids_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kids_info")
@Data // Lombok annotation that generates all getters, setters, toString(), equals(), and hashCode() methods
@NoArgsConstructor // Lombok annotation that generates a no-args constructor
@AllArgsConstructor // Lombok annotation that generates a constructor with all fields
public class KidsInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the child info record

    private String name;  // Name of the child
    private int age;      // Age of the child
    private String gender; // Gender of the child

    // The email of the user to whom the child info belongs
    private String userEmail; // User email for relationship mapping
}

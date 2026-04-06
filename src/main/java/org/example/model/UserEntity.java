package org.example.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Column(unique = true)
    private String apiKey; // This is the "Passport" for your API

    private int requestCount; // Scalability tip: Track usage to monetize later
}

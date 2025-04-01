package com.airplane.userpost.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Generated;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String text;

    @Column(name = "created_at", insertable = false, updatable = false)
    @Generated
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}

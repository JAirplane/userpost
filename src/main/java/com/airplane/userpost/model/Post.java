package com.airplane.userpost.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Generated;

import java.time.LocalDateTime;
import java.util.Objects;

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

    @EqualsAndHashCode.Exclude
    @Column(name = "created_at", insertable = false, updatable = false)
    @Generated
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || this.getClass() != obj.getClass()) return false;

        Post other = (Post) obj;
        Long thisUserId = this.user != null ? this.user.getId() : null;
        Long otherUserId = other.user != null ? other.user.getId() : null;

        return Objects.equals(this.id, other.id)
                && Objects.equals(this.title, other.title)
                && Objects.equals(this.text, other.text)
                && Objects.equals(thisUserId, otherUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, text, user == null ? null : user.getId());
    }
}

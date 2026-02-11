package com.healthclub.Physioplus.Model;

import com.healthclub.Physioplus.Dto.UserRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "Users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true, sparse = true)
    private String email;

    @Indexed(unique = true, sparse = true)
    private String phone;

    private String name;

    private UserRole role;

    private boolean emailVerified;

    private boolean phoneVerified;

    private boolean active;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public User() {
        this.active = true;
        this.emailVerified = false;
        this.phoneVerified = false;
    }

    public User(String email, String phone, String name, UserRole role) {
        this();
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.role = role;
    }

    public boolean isVerified() {
        return emailVerified || phoneVerified;
    }
}

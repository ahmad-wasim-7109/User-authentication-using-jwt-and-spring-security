package com.user.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@Entity
@Table(name = "group_members")
public class GroupMember {

    @Id
    private GroupMemberId groupMemberId;

    @CreationTimestamp
    @Column(name = "joined_at")
    private Date joinedAt;

    @UpdateTimestamp
    @Column(name = "is_active")
    private Boolean isActive;
}
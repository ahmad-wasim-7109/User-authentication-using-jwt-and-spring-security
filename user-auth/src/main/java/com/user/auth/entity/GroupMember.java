package com.user.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@Table(name = "group_members")
public class GroupMember {

    @Id
    private GroupMemberId groupMemberId;

    @CreationTimestamp
    @Column(name = "joined_at")
    private Date joinedAt;

    @Column(name = "is_active")
    private boolean isActive;
}
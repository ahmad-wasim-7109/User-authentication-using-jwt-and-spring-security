package com.user.auth.entity;

import com.user.auth.enums.InvitationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Table(name = "group_members")
public class GroupMember {

    @Id
    private GroupMemberId groupMemberId;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    @CreationTimestamp
    @Column(name = "joined_at")
    private Date joinedAt;

    @UpdateTimestamp
    @Column(name = "is_active")
    private Boolean lastActiveAt;

    @Column(name = "is_admin")
    private boolean isAdmin;
}
package com.user.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Embeddable
public class GroupMemberId implements Serializable {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false, referencedColumnName = "id")
    private Group group;

    @Column(name = "member_id", nullable = false)
    private String memberEmail;
}
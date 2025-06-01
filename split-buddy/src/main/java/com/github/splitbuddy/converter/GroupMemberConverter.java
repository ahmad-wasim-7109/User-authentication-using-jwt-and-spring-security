package com.github.splitbuddy.converter;


import com.github.splitbuddy.dtos.GroupMemberDTO;
import com.github.splitbuddy.entity.Group;
import com.github.splitbuddy.entity.GroupMember;
import com.github.splitbuddy.entity.GroupMemberId;
import com.github.splitbuddy.entity.User;

import java.util.Date;

public class GroupMemberConverter {
    public static GroupMember createGroupMember(String memberEmail, Group group) {
        GroupMemberId groupMemberId = new GroupMemberId();
        groupMemberId.setMemberEmail(memberEmail);
        groupMemberId.setGroup(group);
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupMemberId(groupMemberId);
        groupMember.setJoinedAt(new Date());
        groupMember.setAdmin(false);
        groupMember.setActive(true);
        return groupMember;
    }

    public static GroupMemberDTO convertToGroupMemberDTO(GroupMember groupMember) {
        return GroupMemberDTO.builder()
                .email(groupMember.getGroupMemberId().getMemberEmail())
                .isAdmin(groupMember.isAdmin())
                .isActive(groupMember.isActive())
                .build();
    }
    public static GroupMemberDTO  userToGroupMemberDTO(User user) {
        return GroupMemberDTO.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .isActive(user.getIsEmailVerified())
                .build();
    }
}

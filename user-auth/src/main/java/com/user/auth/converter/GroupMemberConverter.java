package com.user.auth.converter;


import com.user.auth.entity.Group;
import com.user.auth.entity.GroupMember;
import com.user.auth.entity.GroupMemberId;

import java.util.Date;

public class GroupMemberConverter {
    public static GroupMember createGroupMember(String memberEmail, Group group) {
        GroupMemberId groupMemberId = new GroupMemberId();
        groupMemberId.setMemberEmail(memberEmail);
        groupMemberId.setGroup(group);
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupMemberId(groupMemberId);
        groupMember.setJoinedAt(new Date());
        groupMember.setIsActive(true);
        return groupMember;
    }
}

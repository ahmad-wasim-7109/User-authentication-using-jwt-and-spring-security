package com.user.auth.converter;

import com.user.auth.dtos.GroupCreationResponse;
import com.user.auth.dtos.GroupExpenseDTO;
import com.user.auth.dtos.GroupMemberDTO;
import com.user.auth.entity.Group;

import java.util.List;

public class GroupConverter {

    public static GroupCreationResponse convertToGroupCreationResponse(Group group, List<GroupMemberDTO> memberDtos) {
        return GroupCreationResponse.builder()
                .id(group.getId())
                .groupName(group.getName())
                .description(group.getDescription())
                .members(memberDtos)
                .build();
    }

    public static GroupExpenseDTO convertToExpenseDTO(Group group) {
        return GroupExpenseDTO.builder()
                .id(group.getId())
                .groupName(group.getName())
                .description(group.getDescription())
                .createdBy(group.getCreatedBy().getEmail())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}

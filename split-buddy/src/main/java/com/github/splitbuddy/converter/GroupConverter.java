package com.github.splitbuddy.converter;

import com.github.splitbuddy.dtos.GroupCreationResponse;
import com.github.splitbuddy.dtos.GroupExpenseDTO;
import com.github.splitbuddy.dtos.GroupMemberDTO;
import com.github.splitbuddy.entity.Group;

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

package com.user.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupCreationResponse {
    private String id;
    private String groupName;
    private String description;
    private String createdBy;
    private List<GroupMemberDto> members;
}

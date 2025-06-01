package com.github.splitbuddy.dtos;

import com.github.splitbuddy.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddGroupMemberRequest {
    @NotBlank(message = "Group Id is required")
    private String groupId;
    private Role role;
    @Email(message = "Invalid email format")
    @NotBlank(message = "Member email is required")
    private String memberEmail;
}
package com.user.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupCreationRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 30, min = 3, message = "Group name must be at least 3  and max 30 characters long")
    private String groupName;
    @Size(max = 50, message = "Description must be at most 100 characters long")
    private String description;
    @Size(min = 1, message = "Member list cannot be empty")
    private List<String> members;
}

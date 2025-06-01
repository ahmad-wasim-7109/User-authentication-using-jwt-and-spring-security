package com.github.splitbuddy.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class GroupUpdateRequest {
    @NotBlank(message = "Group id is required")
    private String groupId;
    @NotBlank(message = "Group name is required")
    @Size(min = 3, max = 25, message = "Group name must be at most 25 characters long")
    private String groupName;
    @Size(max = 50, message = "Description must be at most 50 characters long")
    private String description;
}

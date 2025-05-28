package com.user.auth.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupCreationResponse {
    private String id;
    private String groupName;
    private String description;
    private String createdBy;
    private Date createdAt;
    private Date updatedAt;
    private Double amountToReceive;
    private Double amountToPay;
    private List<GroupExpenseDTO> groupExpenses;
    private List<GroupMemberDTO> members;
}

package com.user.auth.service;

import com.user.auth.dao.*;
import com.user.auth.dtos.*;
import com.user.auth.entity.Expense;
import com.user.auth.entity.ExpenseSplit;
import com.user.auth.entity.Group;
import com.user.auth.entity.GroupMember;
import com.user.auth.entity.User;
import com.user.auth.enums.SettlementStatus;
import com.user.auth.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.user.auth.converter.GroupConverter.convertToGroupCreationResponse;
import static com.user.auth.converter.GroupMemberConverter.createGroupMember;

@RequiredArgsConstructor
@Service
public class SplitGroupService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final EmailService emailService;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final ExpenseRepository expenseRepository;

    public GroupCreationResponse createGroup(GroupCreationRequest groupCreationRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final var user = (User) authentication.getPrincipal();
        final var email = user.getUsername();
        if (!groupCreationRequest.getCreatedBy().equals(email)) {
            throw new InvalidDataException("group creator must be logged in user");
        }
        if (!groupCreationRequest.getMembers().isEmpty()) {
            if (!groupCreationRequest.getMembers().contains(email)) {
                groupCreationRequest.getMembers().add(email);
            }
            Group group = convertToGroup(groupCreationRequest, user);
            groupRepository.save(group);
            List<GroupMemberDTO> activeMembers = getGroupMembersAndSendNotification(group);
            return convertToGroupCreationResponse(group, activeMembers);
        }
        throw new InvalidDataException("Invalid data");
    }

    private List<GroupMemberDTO> getGroupMembersAndSendNotification(Group group) {

        return group.getMembers().stream().filter(groupMember -> groupMember.isActive() && !groupMember.getGroupMemberId().getMemberEmail().equals(group.getCreatedBy().getEmail()))
                .map(groupMember -> {
                    GroupMemberDTO groupMemberDto;
                    User user = userRepository.findByEmail(groupMember.getGroupMemberId().getMemberEmail()).orElse(null);
                    if (user != null) {
                        groupMemberDto = GroupMemberDTO.builder()
                                .userId(user.getId())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .isActive(groupMember.isActive())
                                .build();
                    } else {
                        groupMemberDto = GroupMemberDTO.builder()
                                .email(groupMember.getGroupMemberId().getMemberEmail())
                                .isActive(false)
                                .build();
                    }
                    emailService.sendEmail(groupMember.getGroupMemberId().getMemberEmail(), "Group Created", "You have been added to a new group");
                    return groupMemberDto;
                })
                .toList();
    }

    private Group convertToGroup(GroupCreationRequest groupCreationRequest, User user) {
        Group group = new Group();
        group.setId(UUID.randomUUID().toString().replace("-", ""));
        group.setName(groupCreationRequest.getGroupName());
        group.setDescription(groupCreationRequest.getDescription());
        group.setCreatedBy(user);
        group.setCreatedAt(new Date());
        group.setUpdatedAt(new Date());
        groupRepository.save(group);
        Set<GroupMember> groupMembers = getGroupMembers(groupCreationRequest.getMembers(), group);
        group.setMembers(groupMembers);
        return group;
    }

    private Set<GroupMember> getGroupMembers(List<String> members, Group group) {
        Set<GroupMember> groupMembers = new HashSet<>();
        members.forEach(email -> {
            GroupMember groupMember = createGroupMember(email, group);
            groupMembers.add(groupMember);
            groupMemberRepository.save(groupMember);
        });
        return groupMembers;
    }

    public void updateGroupInformation(GroupUpdateRequest groupUpdateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final var user  = (User) authentication.getPrincipal();
        final var email = user.getUsername();
        final var groupId = groupUpdateRequest.getGroupId();
        groupMemberRepository.findByGroupIdMemberEmailAndIsActive(groupId, email, true)
                .orElseThrow(() -> new InvalidDataException("User is not present/user is verified"));
        groupRepository.findById(groupId)
                .map(group -> {
                    group.setName(groupUpdateRequest.getGroupName());
                    group.setDescription(groupUpdateRequest.getDescription());
                    group.setUpdatedAt(new Date());
                    return groupRepository.save(group);
                })
                .orElseThrow(() -> new InvalidDataException("Group not found"));
    }

    public void deleteGroup(String groupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final var user  = (User) authentication.getPrincipal();
        final var email = user.getUsername();
        groupMemberRepository.findByGroupIdMemberEmailAndIsActive(groupId, email, true)
                .orElseThrow(() -> new InvalidDataException("User is not present/user is verified"));

        List<String> expenseIds = expenseRepository.findAllByGroupId(groupId).stream().map(Expense::getId).toList();
        checkAndDeleteIfAllExpensesAreSettled(expenseIds);
        expenseRepository.deleteAllByGroupId(expenseIds);
        groupMemberRepository.deleteAllByGroupId(groupId);
        groupRepository.deleteById(groupId);
    }

    public void checkAndDeleteIfAllExpensesAreSettled(List<String> groupId) {
        List<ExpenseSplit> expenseSplits = expenseSplitRepository.findAllByExpenseIdInAndStatusPending(groupId);
        if (!expenseSplits.isEmpty()) {
            throw new InvalidDataException("There are unsettled/pending expenses");
        }
        expenseSplitRepository.deleteAllById(expenseSplits);
    }

    public List<GroupExpenseDTO> fetchAllGroupDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final var user  = (User) authentication.getPrincipal();
        final var email = user.getUsername();
        List<Group> groups = groupRepository.findAllByUserId(email);
        return groups.stream().map(this::convertToGroupExpenseDTO).toList();
    }

    public GroupExpenseDTO convertToGroupExpenseDTO(Group group) {


        GroupExpenseDTO dto = new GroupExpenseDTO();
        dto.setId(group.getId());
        dto.setGroupName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setCreatedBy(group.getCreatedBy().getEmail());
        dto.setMembers(group.getMembers().stream()
                .map(member -> {
                    GroupMemberDTO memberDTO = new GroupMemberDTO();
                    memberDTO.setEmail(member.getGroupMemberId().getMemberEmail());
                    memberDTO.setIsActive(member.isActive());
                    return memberDTO;
                })
                .toList());


        List<Expense> expenses = expenseRepository.findAllByGroupId(group.getId());
        dto.setExpenseSplits(expenses.stream()
                .map(expense -> {
                    ExpenseDTO expenseDTO = new ExpenseDTO();
                    expenseDTO.setExpenseId(expense.getId());
                    expenseDTO.setDescription(expense.getDescription());
                    expenseDTO.setExpenseAmount(expense.getTotalAmount());
                    expenseDTO.setPaidBy(GroupMemberDTO.builder().email(expense.getPaidBy()).build());
                    expenseDTO.setCreatedAt(expense.getCreatedAt());

                    List<ExpenseSplit> splits = expenseSplitRepository.findAllByExpenseId(expense.getId());
                    expenseDTO.setExpenseSplits(splits.stream()
                            .map(split -> {
                                ExpenseSplitDTO splitDTO = new ExpenseSplitDTO();
                                splitDTO.setExpenseSplitId(split.getId());
                                splitDTO.setAmountOwed(split.getAmountOwed());
                                splitDTO.setSplitAt(split.getCreatedAt());
                                splitDTO.setSettled(split.getStatus() == SettlementStatus.SETTLED);
                                return splitDTO;
                            })
                            .toList());

                    return expenseDTO;
                })
                .toList());
        return dto;
    }
}

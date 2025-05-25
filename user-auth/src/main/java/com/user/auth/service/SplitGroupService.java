package com.user.auth.service;

import com.user.auth.dao.*;
import com.user.auth.dtos.GroupCreationRequest;
import com.user.auth.dtos.GroupCreationResponse;
import com.user.auth.dtos.GroupMemberDto;
import com.user.auth.dtos.GroupUpdateRequest;
import com.user.auth.entity.Expense;
import com.user.auth.entity.ExpenseSplit;
import com.user.auth.entity.Group;
import com.user.auth.entity.GroupMember;
import com.user.auth.entity.User;
import com.user.auth.exception.InvalidDataException;
import com.user.auth.exception.UserNotFoundException;
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
        final var email = authentication.getPrincipal();
        if (!(groupCreationRequest.getCreatedBy().isBlank() || groupCreationRequest.getMembers().isEmpty())) {
            User user = userRepository.findById(groupCreationRequest.getCreatedBy()).orElseThrow(() ->
                    new UserNotFoundException("User not found"));
            if (!groupCreationRequest.getMembers().contains(user.getEmail())) {
                groupCreationRequest.getMembers().add(user.getEmail());
            }
            Group group = convertToGroup(groupCreationRequest, user);
            groupRepository.save(group);
            List<GroupMemberDto> activeMembers = getGroupMembersAndSendNotification(group);
            return convertToGroupCreationResponse(group, activeMembers);
        }
        throw new InvalidDataException("Invalid data");
    }

    private List<GroupMemberDto> getGroupMembersAndSendNotification(Group group) {

        return group.getGroupMembers().stream().filter(groupMember -> groupMember.isActive() && !groupMember.getGroupMemberId().getMemberEmail().equals(group.getCreatedBy().getEmail()))
                .map(groupMember -> {
                    GroupMemberDto groupMemberDto;
                    User user = userRepository.findByEmail(groupMember.getGroupMemberId().getMemberEmail()).orElse(null);
                    if (user != null) {
                        groupMemberDto = GroupMemberDto.builder()
                                .userId(user.getId())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .isActive(groupMember.isActive())
                                .build();
                    } else {
                        groupMemberDto = GroupMemberDto.builder()
                                .userId(null)
                                .fullName(null)
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

        Set<GroupMember> groupMembers = getGroupMembers(groupCreationRequest.getMembers(), group);
        group.setGroupMembers(groupMembers);
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
        final var email = authentication.getPrincipal();
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
        final var email = authentication.getPrincipal();
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
        expenseSplitRepository.deleteAll(expenseSplits);
    }
}

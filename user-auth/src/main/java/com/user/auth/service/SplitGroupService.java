package com.user.auth.service;

import com.user.auth.converter.ExpenseConverter;
import com.user.auth.converter.ExpenseSplitConverter;
import com.user.auth.converter.GroupMemberConverter;
import com.user.auth.dao.ExpenseRepository;
import com.user.auth.dao.ExpenseSplitRepository;
import com.user.auth.dao.GroupMemberRepository;
import com.user.auth.dao.GroupRepository;
import com.user.auth.dtos.AddGroupMemberRequest;
import com.user.auth.dtos.ExpenseCreationRequest;
import com.user.auth.dtos.ExpenseDTO;
import com.user.auth.dtos.ExpenseSplitDTO;
import com.user.auth.dtos.GroupCreationRequest;
import com.user.auth.dtos.GroupCreationResponse;
import com.user.auth.dtos.GroupExpenseDTO;
import com.user.auth.dtos.GroupExpenseSummary;
import com.user.auth.dtos.GroupMemberDTO;
import com.user.auth.dtos.GroupUpdateRequest;
import com.user.auth.dtos.IndividualShare;
import com.user.auth.entity.Expense;
import com.user.auth.entity.ExpenseSplit;
import com.user.auth.entity.Group;
import com.user.auth.entity.GroupMember;
import com.user.auth.entity.User;
import com.user.auth.enums.NotificationType;
import com.user.auth.enums.Role;
import com.user.auth.enums.SettlementStatus;
import com.user.auth.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.user.auth.converter.ExpenseConverter.convertToExpense;
import static com.user.auth.converter.ExpenseSplitConverter.convertToExpenseSplit;
import static com.user.auth.converter.GroupConverter.convertToGroupCreationResponse;
import static com.user.auth.converter.GroupMemberConverter.convertToGroupMemberDTO;
import static com.user.auth.converter.GroupMemberConverter.createGroupMember;
import static java.lang.String.format;

@RequiredArgsConstructor
@Service
public class SplitGroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final EmailService emailService;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final ExpenseRepository expenseRepository;
    private final NotificationService notificationService;

    public GroupCreationResponse createGroup(User user, GroupCreationRequest groupCreationRequest) {
        if (!groupCreationRequest.getMembers().isEmpty()) {
            if (!groupCreationRequest.getMembers().contains(user.getUsername())) {
                groupCreationRequest.getMembers().add(user.getUsername());
            }
            Group group = convertToGroup(groupCreationRequest, user);
            groupRepository.save(group);
            List<GroupMemberDTO> activeMembers = getGroupMembersAndSendNotification(group);
            return convertToGroupCreationResponse(group, activeMembers);
        }
        throw new InvalidDataException("Invalid data");
    }

    private List<GroupMemberDTO> getGroupMembersAndSendNotification(Group group) {

        return group.getMembers().stream()
                .filter(groupMember -> groupMember.isActive() &&
                        !groupMember.getGroupMemberId().getMemberEmail().equals(group.getCreatedBy().getEmail()))
                .map(groupMember -> {
                    notificationService.notifyUser(NotificationType.GROUP_CREATED, groupMember.getGroupMemberId().getMemberEmail(),
                            group.getName(), group.getCreatedBy());
                    return convertToGroupMemberDTO(groupMember);
                })
                .toList();
    }

    private Group convertToGroup(GroupCreationRequest groupCreationRequest, User user) {
        Group group = new Group();
        group.setId(UUID.randomUUID().toString().replace("-", ""));
        group.setName(groupCreationRequest.getGroupName());
        group.setDescription(groupCreationRequest.getDescription());
        group.setCreatedBy(user);
        group.setDeleted(false);
        group.setCreatedAt(new Date());
        group.setUpdatedAt(new Date());
        groupRepository.save(group);
        Set<GroupMember> groupMembers = getGroupMembers(groupCreationRequest, group, user);
        group.setMembers(groupMembers);
        return group;
    }

    private Set<GroupMember> getGroupMembers(GroupCreationRequest groupCreationRequest, Group group, User user) {
        Set<GroupMember> groupMembers = new HashSet<>();

        groupCreationRequest.getMembers().forEach(email -> {
            GroupMember groupMember = createGroupMember(email, group);
            groupMember.setAdmin(email.equals(user.getUsername()));
            groupMembers.add(groupMember);
            groupMemberRepository.save(groupMember);
        });
        return groupMembers;
    }

    public void updateGroupInformation(String loggedInEmail, GroupUpdateRequest groupUpdateRequest) {

        groupMemberRepository.findByGroupIdMemberEmailAndIsActive(groupUpdateRequest.getGroupId(), loggedInEmail, true)
                .filter(GroupMember::isAdmin)
                .orElseThrow(() -> new InvalidDataException("User is not present/user is verified"));

        groupRepository.findByIdAndIsDeleted(groupUpdateRequest.getGroupId(), false)
                .map(group -> {
                    group.setName(groupUpdateRequest.getGroupName());
                    group.setDescription(groupUpdateRequest.getDescription());
                    group.setUpdatedAt(new Date());
                    return groupRepository.save(group);
                })
                .orElseThrow(() -> new InvalidDataException("Group not found"));
    }

    public void deleteGroup(String loggedInEmail, String groupId) {

        Group group = groupRepository.findByIdAndIsDeleted(groupId, false)
                .orElseThrow(() -> new InvalidDataException("Group not found"));

        groupMemberRepository.findByGroupIdMemberEmailAndIsActive(groupId, loggedInEmail, true)
                .filter(GroupMember::isAdmin)
                .orElseThrow(() -> new InvalidDataException(format("%s is not an admin", loggedInEmail)));
        boolean hasPendingSplits = expenseRepository.findAllExpensesWithSplits(groupId).stream()
                .flatMap(expense -> expense.getSplits().stream())
                .anyMatch(split -> split.getStatus() == SettlementStatus.PENDING);

        if(hasPendingSplits) {
            throw new InvalidDataException("There are unsettled/pending expenses");
        }
        group.setDeleted(true);
        group.setUpdatedAt(new Date());
        groupRepository.save(group);
    }

    public GroupExpenseSummary fetchAllGroupDetails(String loggedInEmail) {

        List<Group> groups = groupRepository.findAllByUserId(loggedInEmail);
        return new GroupExpenseSummary(groups.stream().map(this::convertToGroupExpenseDTO).toList());
    }

    public GroupExpenseDTO convertToGroupExpenseDTO(Group group) {

        List<Expense> expenses = expenseRepository.findAllExpensesWithSplits(group.getId());

        return GroupExpenseDTO.builder()
                .id(group.getId())
                .groupName(group.getName())
                .description(group.getDescription())
                .createdBy(group.getCreatedBy().getEmail())
                .createdAt(group.getCreatedAt())
                .expenseAmount(expenses.stream().mapToDouble(Expense::getTotalAmount).sum())
                .members(getGroupMemberDTO(group.getMembers()))
                .expenseSplits(convertToExpenseDTOs(expenses))
                .build();
    }

    public List<ExpenseDTO> convertToExpenseDTOs(List<Expense> expenses) {
        return expenses.stream()
                .map(expense -> {
                    ExpenseDTO dto = ExpenseConverter.convertToExpenseDTO(expense);
                    List<ExpenseSplitDTO> splitDTOs = expense.getSplits().stream()
                            .map(ExpenseSplitConverter::convertToExpenseSplitDTO)
                            .toList();
                    dto.setExpenseSplits(splitDTOs);
                    return dto;
                })
                .toList();
    }

    private List<GroupMemberDTO> getGroupMemberDTO(Set<GroupMember> members) {
        return members.stream().map(GroupMemberConverter::convertToGroupMemberDTO).toList();
    }

    public void addExpenseToGroup(User user, String groupId, ExpenseCreationRequest request) {

        Group group = groupRepository.findByIdAndIsDeleted(groupId, false)
                .orElseThrow(() -> new InvalidDataException("Group not found"));

        boolean isMember = group.getMembers().stream()
                .anyMatch(member ->
                        member.getGroupMemberId().getMemberEmail().equals(user.getUsername()) && member.isActive());

        if (!isMember) {
            throw new InvalidDataException("User is not a member of the group");
        }

        Set<String> groupMemberEmails = group.getMembers().stream()
                .filter(GroupMember::isActive)
                .map(member -> member.getGroupMemberId().getMemberEmail())
                .collect(Collectors.toSet());

        for (IndividualShare share : request.getShares()) {
            if (!groupMemberEmails.contains(share.owedBy())) {
                throw new InvalidDataException("User " + share.owedBy() + " is not a member of this group");
            }
        }

        if (!groupMemberEmails.contains(request.getPaidBy())) {
            throw new InvalidDataException("Payer is not a member of the group");
        }

        Expense expense = convertToExpense(group, request, user);
        expenseRepository.save(expense);
        request.getShares().forEach(share -> {
            ExpenseSplit split = convertToExpenseSplit(request, share, expense);
            expenseSplitRepository.save(split);
        });
    }

    public void deleteGroupMember(String loggerInEmail, String groupId, String memberEmail) {

        groupRepository.findByIdAndIsDeleted(groupId, false)
                .orElseThrow(() -> new InvalidDataException("Group not found"));

        groupMemberRepository.findByGroupIdMemberEmailAndIsActive(groupId, loggerInEmail, true)
                .filter(GroupMember::isAdmin)
                .orElseThrow(() -> new InvalidDataException(format("%s is not an admin", loggerInEmail)));

        List<ExpenseSplit> pendingSplits = expenseSplitRepository.findPendingSplitsByUserEmailAndGroupId(memberEmail, groupId);

        if (!pendingSplits.isEmpty()) {
            throw new InvalidDataException("There are unsettled expenses");
        }

        GroupMember groupMember = groupMemberRepository.findByGroupIdMemberEmailAndIsActive(groupId, memberEmail, true)
                .orElseThrow(() -> new InvalidDataException(format("%s is not an active member", memberEmail)));

        groupMember.setActive(false);
        groupMemberRepository.save(groupMember);
    }

    public void addMemberToGroup(String loggedInEmail, AddGroupMemberRequest addGroupMemberRequest) {

        Group group = groupRepository.findByIdAndIsDeleted(addGroupMemberRequest.getGroupId(), false)
                .orElseThrow(() -> new InvalidDataException("Group not found"));

        groupMemberRepository.findByGroupIdMemberEmailAndIsActive(addGroupMemberRequest.getGroupId(), loggedInEmail, true)
                .filter(GroupMember::isAdmin)
                .orElseThrow(() -> new InvalidDataException("User is not an admin"));

        boolean isAnExistingMember = group.getMembers().stream()
                .anyMatch(member -> member.getGroupMemberId().getMemberEmail().equals(addGroupMemberRequest.getMemberEmail()));

        if (isAnExistingMember) {
            throw new InvalidDataException("Member is not part of the group");
        }
        GroupMember groupMember = createGroupMember(addGroupMemberRequest.getMemberEmail(), group);
        groupMember.setAdmin(addGroupMemberRequest.getRole() != null && addGroupMemberRequest.getRole() == Role.ADMIN);
        groupMemberRepository.save(groupMember);
        emailService.sendEmail(addGroupMemberRequest.getMemberEmail(), "Group Update", "You've been added to the group: " + group.getName());
    }
}

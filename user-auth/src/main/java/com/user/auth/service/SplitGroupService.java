package com.user.auth.service;

import com.user.auth.converter.ExpenseConverter;
import com.user.auth.converter.ExpenseSplitConverter;
import com.user.auth.converter.GroupMemberConverter;
import com.user.auth.dao.ExpenseRepository;
import com.user.auth.dao.ExpenseSplitRepository;
import com.user.auth.dao.GroupMemberRepository;
import com.user.auth.dao.GroupRepository;
import com.user.auth.dtos.ExpenseCreationRequest;
import com.user.auth.dtos.ExpenseDTO;
import com.user.auth.dtos.ExpenseSplitDTO;
import com.user.auth.dtos.GroupCreationRequest;
import com.user.auth.dtos.GroupCreationResponse;
import com.user.auth.dtos.GroupExpenseDTO;
import com.user.auth.dtos.GroupMemberDTO;
import com.user.auth.dtos.GroupUpdateRequest;
import com.user.auth.dtos.IndividualShare;
import com.user.auth.entity.Expense;
import com.user.auth.entity.ExpenseSplit;
import com.user.auth.entity.Group;
import com.user.auth.entity.GroupMember;
import com.user.auth.entity.User;
import com.user.auth.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

        return group.getMembers().stream()
                .filter(groupMember -> groupMember.isActive() &&
                        !groupMember.getGroupMemberId().getMemberEmail().equals(group.getCreatedBy().getEmail()))
                .map(groupMember -> {
                    emailService.sendEmail(groupMember.getGroupMemberId().getMemberEmail(),
                            "Group Created", "You have been added to a new group");
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
        final var user = (User) authentication.getPrincipal();
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
        final var user = (User) authentication.getPrincipal();
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
        final var user = (User) authentication.getPrincipal();
        final var email = user.getUsername();
        List<Group> groups = groupRepository.findAllByUserId(email);
        return groups.stream().map(this::convertToGroupExpenseDTO).toList();
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

    public String addExpenseToGroup(String groupId, ExpenseCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final var user = (User) authentication.getPrincipal();
        final var email = user.getUsername();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new InvalidDataException("Group not found"));

        boolean isMember = group.getMembers().stream()
                .anyMatch(member ->
                        member.getGroupMemberId().getMemberEmail().equals(email) && member.isActive()
                );
        if (!isMember) {
            throw new InvalidDataException("User is not a member of the group");
        }
        //validate shares belongs to the group
        Set<String> groupMemberEmails = group.getMembers().stream()
                .filter(GroupMember::isActive)
                .map(member -> member.getGroupMemberId().getMemberEmail())
                .collect(Collectors.toSet());

        for (IndividualShare share : request.getShares()) {
            if (!groupMemberEmails.contains(share.owedBy())) {
                throw new InvalidDataException("User " + share.owedBy() + " is not a member of this group");
            }
        }
        //validate payer belongs to the group
        if (!groupMemberEmails.contains(request.getPaidBy())) {
            throw new InvalidDataException("Payer is not a member of the group");
        }

        Expense expense = convertToExpense(group, request, user);
        expenseRepository.save(expense);
        request.getShares().forEach(share -> {
            ExpenseSplit split = convertToExpenseSplit(request, share, expense);
            expenseSplitRepository.save(split);
        });
        return expense.getId();
    }

    public void deleteGroupMember(String groupId, String memberEmail) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final var user = (User) authentication.getPrincipal();
        final var email = user.getUsername();
        groupMemberRepository.findByGroupIdMemberEmailAndIsActive(groupId, email, true)
                .filter(GroupMember::isAdmin)
                .orElseThrow(() -> new InvalidDataException(format("%s is not an admin", email)));

        // check if there is no any unsettled expenses
        List<ExpenseSplit> pendingSplits = expenseSplitRepository.findPendingSplitsByUserEmailAndGroupId(memberEmail, groupId);

        if (!pendingSplits.isEmpty()) {
            throw new InvalidDataException("There are unsettled expenses");
        }
        //check if member is active member
        GroupMember groupMember = groupMemberRepository.findByGroupIdMemberEmailAndIsActive(groupId, memberEmail, true)
                .orElseThrow(() -> new InvalidDataException(format("%s is not an active member", memberEmail)));

        groupMember.setActive(false);
        groupMemberRepository.save(groupMember);
    }
}

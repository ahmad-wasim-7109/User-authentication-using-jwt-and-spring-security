package com.github.splitbuddy.service;

import com.github.splitbuddy.converter.GroupMemberConverter;
import com.github.splitbuddy.dao.ExpenseRepository;
import com.github.splitbuddy.dao.ExpenseSplitRepository;
import com.github.splitbuddy.dao.GroupMemberRepository;
import com.github.splitbuddy.dao.GroupRepository;
import com.github.splitbuddy.dtos.*;
import com.github.splitbuddy.entity.*;
import com.github.splitbuddy.enums.NotificationType;
import com.github.splitbuddy.enums.Role;
import com.github.splitbuddy.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.splitbuddy.converter.ExpenseConverter.convertToExpense;
import static com.github.splitbuddy.converter.ExpenseSplitConverter.convertToExpenseDTOs;
import static com.github.splitbuddy.converter.ExpenseSplitConverter.convertToExpenseSplit;
import static com.github.splitbuddy.converter.GroupConverter.convertToExpenseDTO;
import static com.github.splitbuddy.converter.GroupConverter.convertToGroupCreationResponse;
import static com.github.splitbuddy.converter.GroupMemberConverter.convertToGroupMemberDTO;
import static com.github.splitbuddy.converter.GroupMemberConverter.createGroupMember;
import static com.github.splitbuddy.enums.NotificationType.EXPENSE_ADDED;
import static com.github.splitbuddy.enums.NotificationType.MEMBER_ADDED;
import static java.lang.String.format;

@RequiredArgsConstructor
@Service
public class SplitGroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
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
                            group.getName(), group.getCreatedBy().getFullName());
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

        group.setDeleted(true);
        group.setUpdatedAt(new Date());
        groupRepository.save(group);
    }

    public GroupExpenseSummary fetchAllGroupSummary(String loggedInEmail) {

        List<Group> groups = groupRepository.findAllByUserId(loggedInEmail).stream()
                .filter(group -> !group.isDeleted())
                .toList();
        List<GroupExpenseDTO> groupExpenseDTOs = groups.stream()
                .map(group -> convertToGroupExpenseDTO(group, loggedInEmail)).toList();
        double totalSettlementAmount = groupExpenseDTOs.stream()
                .mapToDouble(GroupExpenseDTO::getSettlementAmount).sum();
        return new GroupExpenseSummary(totalSettlementAmount, groupExpenseDTOs);
    }

    public GroupExpenseDTO convertToGroupExpenseDTO(Group group, String loggedInEmail) {

        List<Expense> expenses = expenseRepository.findAllExpensesWithSplits(group.getId());

        GroupExpenseDTO groupExpenseDTO = convertToExpenseDTO(group);
        groupExpenseDTO.setSettlementAmount(calculateSettlementAmount(expenses, loggedInEmail));
        groupExpenseDTO.setMembers(getGroupMemberDTO(group.getMembers()));
        return groupExpenseDTO;
    }

    private List<GroupMemberDTO> getGroupMemberDTO(Set<GroupMember> members) {
        return members.stream()
                .filter(GroupMember::isActive)
                .map(GroupMemberConverter::convertToGroupMemberDTO).toList();
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
            ExpenseSplit split = convertToExpenseSplit(share, expense);
            expenseSplitRepository.save(split);
            if (share.amountOwed() > 0) {
                notificationService.notifyUser(EXPENSE_ADDED, share.owedBy(), share.owedBy().split("@")[0],
                        share.amountOwed(), group.getName());
            }
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

    public void addMemberToGroup(User user, AddGroupMemberRequest addGroupMemberRequest) {

        Group group = groupRepository.findByIdAndIsDeleted(addGroupMemberRequest.getGroupId(), false)
                .orElseThrow(() -> new InvalidDataException("Group not found"));

        groupMemberRepository.findByGroupIdMemberEmailAndIsActive(addGroupMemberRequest.getGroupId(), user.getUsername(),
                        true)
                .filter(GroupMember::isAdmin)
                .orElseThrow(() -> new InvalidDataException("User is not an admin"));
        Optional<GroupMember> groupMember = groupMemberRepository.findByGroupIdMemberEmailAndIsActive(addGroupMemberRequest.getGroupId(),
                addGroupMemberRequest.getMemberEmail(), true);
        if (groupMember.isPresent()) {
            throw new InvalidDataException("Member already exists");
        }
        group.getMembers().stream()
                .filter(member -> member.getGroupMemberId().getMemberEmail().equals(addGroupMemberRequest.getMemberEmail())
                        && !member.isActive())
                .findAny()
                .ifPresentOrElse(member -> {
                    member.setActive(true);
                    member.setAdmin(addGroupMemberRequest.getRole() != null && addGroupMemberRequest.getRole() == Role.ADMIN);
                    groupMemberRepository.save(member);
                }, () -> {
                    final var newGroupMember = createGroupMember(addGroupMemberRequest.getMemberEmail(), group);
                    newGroupMember.setAdmin(addGroupMemberRequest.getRole() != null && addGroupMemberRequest.getRole() == Role.ADMIN);
                    groupMemberRepository.save(newGroupMember);
                });

        notificationService.notifyUser(MEMBER_ADDED, addGroupMemberRequest.getMemberEmail(),
                addGroupMemberRequest.getMemberEmail().split("@")[0], group.getName(), user.getFullName());

    }


    public double calculateSettlementAmount(List<Expense> expenses, String userEmail) {

        double settlementAmount = 0.0;
        final var transactions = getLoggedInUserSettlements(expenses, userEmail);
        for (final var transaction : transactions) {
            if (transaction.getFromUser().equals(userEmail)) {
                settlementAmount -= transaction.getAmount();
            } else if (transaction.getToUser().equals(userEmail)) {
                settlementAmount += transaction.getAmount();
            }
        }
        return settlementAmount;
    }

    public Map<String, Double> calculateNetBalances(List<Expense> expenses) {
        Map<String, Double> balances = new HashMap<>();

        for (Expense expense : expenses) {
            String payer = expense.getPaidBy();
            double paidAmount = expense.getTotalAmount();

            balances.put(payer, balances.getOrDefault(payer, 0.0) + paidAmount);

            for (ExpenseSplit split : expense.getSplits()) {

                String debtor = split.getOwedBy();
                double owedAmount = split.getAmountOwed();

                balances.put(debtor, balances.getOrDefault(debtor, 0.0) - owedAmount);
            }
        }

        return balances;
    }

    public List<SettlementTransactionDTO> minimizeTransactions(Map<String, Double> balances) {
        PriorityQueue<Balance> creditors = new PriorityQueue<>((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        PriorityQueue<Balance> debtors = new PriorityQueue<>((a, b) -> Double.compare(b.getAmount(), a.getAmount()));

        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            if (entry.getValue() > 0) {
                creditors.offer(new Balance(entry.getKey(), entry.getValue()));
            } else if (entry.getValue() < 0) {
                debtors.offer(new Balance(entry.getKey(), -entry.getValue()));
            }
        }

        List<SettlementTransactionDTO> transactions = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Balance creditor = creditors.poll();
            Balance debtor = debtors.poll();

            assert debtor != null;
            double settleAmount = Math.min(creditor.getAmount(), debtor.getAmount());
            String from = debtor.getEmail();
            String to = creditor.getEmail();
            transactions.add(new SettlementTransactionDTO(from, to, settleAmount));

            if (creditor.getAmount() > settleAmount) {
                creditors.offer(new Balance(creditor.getEmail(), creditor.getAmount() - settleAmount));
            }
            if (debtor.getAmount() > settleAmount) {
                debtors.offer(new Balance(debtor.getEmail(), debtor.getAmount() - settleAmount));
            }
        }
        return transactions;
    }

    public List<SettlementTransactionDTO> getLoggedInUserSettlements(List<Expense> expenses, String userEmail) {

        Map<String, Double> balances = calculateNetBalances(expenses);

        List<SettlementTransactionDTO> allTransactions = minimizeTransactions(balances);

        return allTransactions.stream()
                .filter(tx -> tx.getFromUser().equals(userEmail) || tx.getToUser().equals(userEmail))
                .toList();
    }

    public List<SettlementTransactionDTO> getAllSettlements(String loggedInEmail, String groupId) {
        Group group = groupRepository.findByIdAndIsDeleted(groupId, false)
                .orElseThrow(() -> new InvalidDataException("Group not found"));
        groupMemberRepository.findByGroupIdMemberEmailAndIsActive(groupId, loggedInEmail, true)
                .orElseThrow(() -> new InvalidDataException("User is not an admin"));
        List<Expense> expenses = expenseRepository.findAllExpensesWithSplits(group.getId());
        return getLoggedInUserSettlements(expenses, loggedInEmail);
    }

    public GroupExpenseDTO getGroupInformation(String currentUserEmail, String groupId) {
        Group group = groupRepository.findByIdAndIsDeleted(groupId, false)
                .orElseThrow(() -> new InvalidDataException("Group not found"));

        groupMemberRepository.findByGroupIdMemberEmailAndIsActive(groupId, currentUserEmail, true)
                .orElseThrow(() -> new InvalidDataException("User is not an active user"));

        List<Expense> expenses = expenseRepository.findAllExpensesWithSplits(group.getId());
        GroupExpenseDTO groupExpenseDTO = convertToExpenseDTO(group);
        groupExpenseDTO.setExpenseSplits(convertToExpenseDTOs(expenses));
        return groupExpenseDTO;
    }
}

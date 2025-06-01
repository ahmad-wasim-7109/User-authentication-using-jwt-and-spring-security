package com.github.splitbuddy.dao;

import com.github.splitbuddy.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, String> {
    List<ExpenseSplit> findAllByExpenseIdIn(List<String> groupId);

    @Query("SELECT es FROM ExpenseSplit es WHERE es.expense.id IN ?1")
    List<ExpenseSplit> findAllByExpenseIdInAndStatusPending(List<String> groupId);

    @Query("DELETE FROM ExpenseSplit es WHERE es.expense.id IN ?1")
    void deleteAllById(List<ExpenseSplit> expenseSplits);

    @Query("SELECT s FROM ExpenseSplit s WHERE s.owedBy = :email AND s.expense.group.id = :groupId")
    List<ExpenseSplit> findPendingSplitsByUserEmailAndGroupId(String email, String groupId);


}
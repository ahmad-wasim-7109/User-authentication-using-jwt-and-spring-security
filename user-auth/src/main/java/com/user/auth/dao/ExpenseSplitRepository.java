package com.user.auth.dao;

import com.user.auth.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, String> {
    List<ExpenseSplit> findAllByExpenseIdIn(List<String> groupId);

    @Query("SELECT es FROM ExpenseSplit es WHERE es.expense.id IN ?1 AND es.status = 'PENDING'")
    List<ExpenseSplit> findAllByExpenseIdInAndStatusPending(List<String> groupId);

    @Query("DELETE FROM ExpenseSplit es WHERE es.expense.id IN ?1")
    void deleteAllById(List<ExpenseSplit> expenseSplits);

    List<ExpenseSplit> findAllByExpenseId(String id);

}
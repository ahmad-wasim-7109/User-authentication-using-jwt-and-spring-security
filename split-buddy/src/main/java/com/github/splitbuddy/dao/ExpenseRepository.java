package com.github.splitbuddy.dao;

import com.github.splitbuddy.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {

    @Query("SELECT e FROM Expense e JOIN FETCH e.splits WHERE e.group.id = :groupId")
    List<Expense> findAllExpensesWithSplits(String groupId);
}
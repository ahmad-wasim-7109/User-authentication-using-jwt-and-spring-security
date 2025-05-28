package com.user.auth.dao;

import com.user.auth.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {

    List<Expense> findAllByGroupId(String groupId);
    @Query("DELETE FROM Expense e WHERE e.group.id IN ?1")
    void deleteAllByGroupId(List<String> groupIds);

}
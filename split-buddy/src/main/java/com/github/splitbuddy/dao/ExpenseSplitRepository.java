package com.github.splitbuddy.dao;

import com.github.splitbuddy.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, String> {
}
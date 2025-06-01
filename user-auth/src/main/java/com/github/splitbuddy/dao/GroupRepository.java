package com.github.splitbuddy.dao;

import com.github.splitbuddy.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    void deleteById(String id);

    @Query("SELECT g FROM Group g JOIN GroupMember gm ON gm.groupMemberId.group.id = g.id where gm.groupMemberId.memberEmail = ?1")
    List<Group> findAllByUserId(String email);

    Optional<Group> findByIdAndIsDeleted(String groupId, boolean b);
}
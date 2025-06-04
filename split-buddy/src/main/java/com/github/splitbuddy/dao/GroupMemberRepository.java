package com.github.splitbuddy.dao;

import com.github.splitbuddy.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, String> {

    @Query("SELECT gm FROM GroupMember gm WHERE gm.groupMemberId.group.id = ?1 AND gm.groupMemberId.memberEmail " +
            "= ?2 AND gm.isActive = ?3")
    Optional<GroupMember> findByGroupIdMemberEmailAndIsActive(String groupId, String email, boolean isActive);

}
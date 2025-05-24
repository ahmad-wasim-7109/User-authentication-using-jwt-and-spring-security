package com.user.auth.service;

import com.user.auth.dao.GroupMemberRepository;
import com.user.auth.dao.GroupRepository;
import com.user.auth.dao.UserRepository;
import com.user.auth.dtos.GroupCreationRequest;
import com.user.auth.dtos.GroupCreationResponse;
import com.user.auth.dtos.GroupMemberDto;
import com.user.auth.entity.Group;
import com.user.auth.entity.GroupMember;
import com.user.auth.entity.User;
import com.user.auth.exception.InvalidDataException;
import com.user.auth.exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.user.auth.converter.GroupConverter.convertToGroupCreationResponse;
import static com.user.auth.converter.GroupMemberConverter.createGroupMember;

@RequiredArgsConstructor
@Service
public class SplitGroupService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final EmailService emailService;

    @Transactional
    public GroupCreationResponse createSplit(GroupCreationRequest groupCreationRequest) {
        if (!(groupCreationRequest.getCreatedBy().isBlank() || groupCreationRequest.getMembers().isEmpty())) {
            User user = userRepository.findById(groupCreationRequest.getCreatedBy()).orElseThrow(() ->
                    new UserNotFoundException("User not found"));
            if (!groupCreationRequest.getMembers().contains(user.getEmail())) {
                groupCreationRequest.getMembers().add(user.getEmail());
            }
            Group group = convertToGroup(groupCreationRequest, user);
            groupRepository.save(group);
            List<GroupMemberDto> groupMemberDtos = getGroupMembersAndSendNotification(group);
            return convertToGroupCreationResponse(group, groupMemberDtos);
        }
        throw new InvalidDataException("Invalid data");
    }

    private List<GroupMemberDto> getGroupMembersAndSendNotification(Group group) {

        return group.getMembers().stream().filter(groupMember -> groupMember.getIsActive() && !groupMember.getGroupMemberId().getMemberEmail().equals(group.getCreatedBy().getEmail()))
                .map(groupMember -> {
                    GroupMemberDto groupMemberDto;
                    User user = userRepository.findByEmail(groupMember.getGroupMemberId().getMemberEmail()).orElse(null);
                    if(user != null) {
                        groupMemberDto = GroupMemberDto.builder()
                                .userId(user.getId())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .isActive(groupMember.getIsActive())
                                .build();
                    } else {
                        groupMemberDto=  GroupMemberDto.builder()
                                .userId(null)
                                .fullName(null)
                                .email(groupMember.getGroupMemberId().getMemberEmail())
                                .isActive(false)
                                .build();
                    }
                    emailService.sendEmail(groupMember.getGroupMemberId().getMemberEmail(), "Group Created", "You have been added to a new group");
                    return groupMemberDto;
                })
                .toList();
    }

    private Group convertToGroup(GroupCreationRequest groupCreationRequest, User user) {
        Group group = Group.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .name(groupCreationRequest.getGroupName())
                .description(groupCreationRequest.getDescription())
                .createdBy(user)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
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
}

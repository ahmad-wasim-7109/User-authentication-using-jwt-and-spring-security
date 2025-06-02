package com.github.splitbuddy.controller;

import com.github.splitbuddy.dtos.*;
import com.github.splitbuddy.exception.InvalidDataException;
import com.github.splitbuddy.service.SplitGroupService;
import com.github.splitbuddy.utils.UserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.github.splitbuddy.utils.UserUtil.getCurrentUser;
import static com.github.splitbuddy.utils.UserUtil.getCurrentUserEmail;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/split/group")
@Tag(name = "3. Group Controller")
public class SplitGroupController {

    private final SplitGroupService splitGroupService;

    @Operation(summary = "Create Split Group")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = GroupCreationResponse.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = InvalidDataException.class)), description = "Bad Request or Invalid Input"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping(value = "/create", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupCreationResponse> createSplit(@Valid @RequestBody GroupCreationRequest groupCreationRequest) {
        return ResponseEntity.ok(splitGroupService.createGroup(UserUtil.getCurrentUser(), groupCreationRequest));
    }

    @Operation(summary = "Update group information")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = String.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping(value = "/update", produces = APPLICATION_JSON_VALUE, consumes =
            APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateGroup(@Valid @RequestBody GroupUpdateRequest groupUpdateRequest) {
        splitGroupService.updateGroupInformation(getCurrentUserEmail(), groupUpdateRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete group")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = String.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @DeleteMapping(value = "/{groupId}/delete", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteGroup(@PathVariable String groupId) {
        splitGroupService.deleteGroup(getCurrentUserEmail(), groupId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get All groups meta data")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = GroupExpenseSummary.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping(value = "/all-groups", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupExpenseSummary> getAllGroupSummary() {
        return ResponseEntity.ok(splitGroupService.fetchAllGroupSummary(getCurrentUserEmail()));
    }

    @Operation(summary = "Get all settlement transactions for a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GroupExpenseDTO.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class)), description = "Bad Request or Invalid Input"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping(value = "/{groupId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupExpenseDTO> getSettlementTransactions(@PathVariable String groupId) {
        return ResponseEntity.ok(splitGroupService.getGroupInformation(getCurrentUserEmail(), groupId));
    }

    @Operation(summary = "Delete a single member from a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or member has unsettled expenses")
    })
    @DeleteMapping(value = "/{groupId}/delete-member", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteGroupMember(@PathVariable String groupId, @RequestParam String memberEmail) {
        splitGroupService.deleteGroupMember(getCurrentUserEmail(), groupId, memberEmail);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Add an expense to a group with individual shares")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping(value = "/{groupId}/add-expense", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addExpense(@PathVariable String groupId, @Valid @RequestBody ExpenseCreationRequest request) {
        splitGroupService.addExpenseToGroup(UserUtil.getCurrentUser(), groupId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Add a member to a group")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = AddGroupMemberRequest.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = InvalidDataException.class)), description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping(value = "/add-member", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addGroupMember(@RequestBody AddGroupMemberRequest request) {
        splitGroupService.addMemberToGroup(getCurrentUser(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all settlement transactions for a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settlement transactions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/{groupId}/settlement-plan")
    public ResponseEntity<List<SettlementTransactionDTO>> getSettlementPlan(@PathVariable String groupId) {
        return ResponseEntity.ok(splitGroupService.getAllSettlements(getCurrentUserEmail(), groupId));
    }

}

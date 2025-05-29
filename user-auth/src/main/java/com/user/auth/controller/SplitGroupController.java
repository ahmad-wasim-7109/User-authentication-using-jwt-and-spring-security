package com.user.auth.controller;

import com.user.auth.dtos.ExpenseCreationRequest;
import com.user.auth.dtos.GroupCreationRequest;
import com.user.auth.dtos.GroupCreationResponse;
import com.user.auth.dtos.GroupUpdateRequest;
import com.user.auth.service.SplitGroupService;
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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/split/group")
@Tag(name = "Controller for splitting spends in group")
public class SplitGroupController {

    private final SplitGroupService splitGroupService;

    @Operation(summary = "Create Split Group")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = GroupCreationResponse.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @PostMapping(value = "/create", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<GroupCreationResponse> createSplit(@Valid @RequestBody GroupCreationRequest groupCreationRequest) {
        return ResponseEntity.ok(splitGroupService.createGroup(groupCreationRequest));
    }

    @Operation(summary = "Update group information")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = String.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @PostMapping(value = "/update", produces = APPLICATION_JSON_VALUE, consumes =
            APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateGroup(@Valid @RequestBody GroupUpdateRequest groupUpdateRequest) {
        splitGroupService.updateGroupInformation(groupUpdateRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete group")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = String.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @DeleteMapping(value = "/{groupId}/delete", produces = APPLICATION_JSON_VALUE, consumes =
            APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteGroup(@PathVariable String groupId) {
        splitGroupService.deleteGroup(groupId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get group information")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = String.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Bad Request")})
    @GetMapping(value = "/groups/{group-id}", produces = APPLICATION_JSON_VALUE, consumes =
            APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getGroup() {
        log.info("Get Group Controller");
        return ResponseEntity.ok("Group Retrieved");
    }

    @Operation(summary = "Fetch all group detail linked with the customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = String.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @GetMapping(value = "/all-groups", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllGroup() {
        log.info("Get All Group Controller");
        return ResponseEntity.ok(splitGroupService.fetchAllGroupDetails());
    }

    @Operation(summary = "Delete a single member from a group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or member has unsettled expenses")
    })
    @DeleteMapping(value = "/{groupId}/delete-member", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteGroupMember(
            @PathVariable String groupId,
            @RequestParam String memberEmail) {

        splitGroupService.deleteGroupMember(groupId, memberEmail);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Add an expense to a group with individual shares")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    @PostMapping(value = "/{groupId}/add-expense", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addExpense(@PathVariable String groupId, @Valid @RequestBody ExpenseCreationRequest request
    ) {
        String expenseId = splitGroupService.addExpenseToGroup(groupId, request);
        return ResponseEntity.ok(expenseId);
    }



    @Operation(summary = "Add a member to a group")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = String.class)), description = "Successful operation"),
            @ApiResponse(responseCode = "400", content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class)), description = "Bad Request")
    })
    @PostMapping(value = "{groupId}/add-member", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addGroupMember() {
        log.info("Add Group Member Controller");
        return ResponseEntity.ok("Group Member Added");
    }
}

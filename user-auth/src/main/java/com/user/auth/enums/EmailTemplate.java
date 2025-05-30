package com.user.auth.enums;

import lombok.Getter;

@Getter
public enum EmailTemplate {

    GROUP_CREATED(
            "You've been added to a group",
            """
                    Hello %s,
                    
                    You've been added to the group '%s' by %s.
                    
                    Best regards,
                    SplitBillsTeam"""
    ),
    MEMBER_ADDED(
            "You've been invited to join a group",
            """
                    Hello %s,
                    
                    You've been added to the group '%s'.
                    
                    Best regards,
                    SplitBillsTeam"""
    ),
    EXPENSE_ADDED(
            "New Expense Added",
            """
                    Hello %s,
                    
                    A new expense of %.2f has been added to group '%s'.
                    
                    Best regards,
                    SplitBillsTeam"""
    ),
    SPLIT_ASSIGNED(
            "New Split Assigned",
            """
                    Hello %s,
                    
                    You have a new split of %.2f in group %s.
                    
                    Best regards,
                    SplitBillsTeam"""
    ),
    EXPENSE_SETTLED(
            "Expense Settled",
            """
                    Hello %s,
                    
                    The expense '%s' has been settled by %s.
                    
                    Best regards,
                    SplitBillsTeam"""
    ),
    OTP_GENERATED(
            "Verify Your Email Address",
            """
                    Dear User,
                    
                    Thank you for registering! To complete your email verification, please use the following One-Time Password (OTP):
                    
                    OTP: %s
                    
                    This OTP is valid for 5 minutes. If you did not request this verification, please ignore this email.
                    
                    Best regards,
                    SplitBillsTeam"""
    ),
    PASSWORD_RESET(
            "Password Reset Request",
            """
                    Dear User,
                    
                    A password reset was requested for your account. Use the following code to proceed.
                    
                    Reset Code: %s
                    
                    If you did not request this, please ignore it.
                    
                    Best regards,
                    SplitBills Team"""
    );

    private final String subject;
    private final String body;

    EmailTemplate(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

}

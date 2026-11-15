package com.immunization.api.entity;

public enum SlotTemplatePriority {
    DEFAULT(0),
    SEASONAL(1),
    TEMPORARY(2);

    private final int precedence;

    SlotTemplatePriority(int precedence) {
        this.precedence = precedence;
    }

    public int getPrecedence() {
        return precedence;
    }
}
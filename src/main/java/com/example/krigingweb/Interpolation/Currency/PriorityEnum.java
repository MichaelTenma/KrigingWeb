package com.example.krigingweb.Interpolation.Currency;

public enum PriorityEnum {
    strong(1),
    middle(2),
    normal(3),
    weak(4),
    ;

    private int priority;
    PriorityEnum(int priority){
        this.priority = priority;
    }
    public int getPriority() {
        return priority;
    }
}
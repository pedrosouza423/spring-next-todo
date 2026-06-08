package com.springnexttodo.tasklist;

public enum ListRole {
    VIEWER, EDITOR, OWNER;

    public boolean atLeast(ListRole required) {
        return this.ordinal() >= required.ordinal();
    }
}

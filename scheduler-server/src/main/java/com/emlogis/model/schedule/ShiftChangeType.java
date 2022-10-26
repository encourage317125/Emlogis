package com.emlogis.model.schedule;

/**
 */
public enum ShiftChangeType {

    NOCHANGE(0),
    WIP(1),
    SWAP(2),
    DROP(3),
    ASSIGN(4),
    EDIT(5), 	// = update/modification
    CREATE(6);		

    private int value;

    private ShiftChangeType(int value) {
    	setValue(value);
    }

    private void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}

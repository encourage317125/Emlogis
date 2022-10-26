package com.emlogis.engine.domain;


/**
 * @author emlogis
 *
 */
public enum DayOfWeek {
	SUNDAY("Sunday"),
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday");

    public static DayOfWeek valueOfDayName(String dayName) {
        for (DayOfWeek dayOfWeek : values()) {
            if (dayName.equalsIgnoreCase(dayOfWeek.getName())) {
                return dayOfWeek;
            }
        }
        return null;
    }
    

    private String stringName;

    private DayOfWeek(String code) {
        this.stringName = code;
    }

    public String getName() {
        return stringName;
    }

    public int getDistanceToNext(DayOfWeek other) {
        int distance = other.ordinal() - ordinal();
        if (distance < 0) {
            distance += 7;
        }
        return distance;
    }
    
    public int getDistanceToPrevious(DayOfWeek other) {
        int distance = ordinal() - other.ordinal();
        if (distance < 0) {
            distance += 7;
        }
        return distance;
    }

    public DayOfWeek determineNextDayOfWeek() {
        switch (this) {
            case MONDAY:
                return TUESDAY;
            case TUESDAY:
                return WEDNESDAY;
            case WEDNESDAY:
                return THURSDAY;
            case THURSDAY:
                return FRIDAY;
            case FRIDAY:
                return SATURDAY;
            case SATURDAY:
                return SUNDAY;
            case SUNDAY:
                return MONDAY;
            default:
                throw new IllegalArgumentException("The dayOfWeek (" + this + ") is not supported.");
        }
    }

    public String getLabel() {
        return stringName.substring(0, 2);
    }

    public String toString() {
        return stringName.substring(0, 3);
    }
   
    /*
     * Get day of week value in the Joda scale
     * where 1 = Monday, 7 = Sunday
     */
    public int getJodaValue(){
    	if(this == SUNDAY) return 7;
    	return ordinal();
    }

}

package com.emlogis.model.employee.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AvailcalUpdateParamsCIDaySelections implements Serializable {
	private boolean sunday     = false;
	private boolean monday     = false;
	private boolean tuesday    = false;
	private boolean wednesday  = false;
	private boolean thursday   = false;
	private boolean friday     = false;
	private boolean saturday   = false;

	public boolean isSunday() {return sunday;}
	public void setSunday(boolean sunday) {this.sunday = sunday;}
	public boolean isMonday() {return monday;}
	public void setMonday(boolean monday) {this.monday = monday;}
	public boolean isTuesday() {return tuesday;}
	public void setTuesday(boolean tuesday) {this.tuesday = tuesday;}
	public boolean isWednesday() {return wednesday;}
	public void setWednesday(boolean wednesday) {this.wednesday = wednesday;}
	public boolean isThursday() {return thursday;}
	public void setThursday(boolean thursday) {this.thursday = thursday;}
	public boolean isFriday() {return friday;}
	public void setFriday(boolean friday) {this.friday = friday;}
	public boolean isSaturday() {return saturday;}
	public void setSaturday(boolean saturday) {this.saturday = saturday;}

    public List<String> dayKeysSelected() {
        List<String> result = new ArrayList<>();
        if (monday) {
            result.add("translation.monday");
        }
        if (tuesday) {
            result.add("translation.tuesday");
        }
        if (wednesday) {
            result.add("translation.wednesday");
        }
        if (thursday) {
            result.add("translation.thursday");
        }
        if (friday) {
            result.add("translation.friday");
        }
        if (saturday) {
            result.add("translation.saturday");
        }
        if (sunday) {
            result.add("translation.sunday");
        }
        return result;
    }
	@Override
	public String toString() {
		return "AvailcalUpdateParamsCIDaySelections [sunday=" + sunday
				+ ", monday=" + monday + ", tuesday=" + tuesday
				+ ", wednesday=" + wednesday + ", thursday=" + thursday
				+ ", friday=" + friday + ", saturday=" + saturday + "]";
	}
    
    
}

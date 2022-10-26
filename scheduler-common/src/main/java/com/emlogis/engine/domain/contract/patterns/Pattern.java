package com.emlogis.engine.domain.contract.patterns;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "type")  
@JsonSubTypes({  
    @Type(value = WeekdayRotationPattern.class, name = "weekdayRotationPattern"),  
    @Type(value = CompleteWeekendWorkPattern.class, name = "weekendWorkPattern")}) 
public abstract class Pattern  {

    protected int weight;

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

}

package com.emlogis.model.schedule.dto;

import java.util.Collection;
import java.util.Map;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.dto.Dto;
import com.emlogis.model.schedule.PostMode;

public class PostOpenShiftDto extends Dto {

    private PostMode postMode = PostMode.Cumulative;
	private	Map<String, Collection<String>> openShifts;	// map of shifts/employee list, k=shiftId, v=[employeeId]
    private long deadline;
    private String comments;
    private String terms;
	private Map<ConstraintOverrideType, Boolean> overrideOptions;	// overrides to use for qualification/eligibility
																			// null means no client override, use schedule overrides

//	private boolean saveOverrides;				// indicates that the overrideOptions must be saved into a PostOverrides entity, specified by overrideOptionsName				
//	private String  overrideOptionsName;		// name of PostOverrides entity to use for saving overrideOptions
												// mandatory if saveOverrides = true, can be null otherwise

    public PostMode getPostMode() {
        return postMode;
    }

    public void setPostMode(PostMode postMode) {
        this.postMode = postMode;
    }

    public Map<String, Collection<String>> getOpenShifts() {
		return openShifts;
	}

	public void setOpenShifts(Map<String, Collection<String>> openShifts) {
		this.openShifts = openShifts;
	}

	public long getDeadline() {
		return deadline;
	}

	public void setDeadline(long deadline) {
		this.deadline = deadline;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getTerms() {
		return terms;
	}

	public void setTerms(String terms) {
		this.terms = terms;
	}

	public Map<ConstraintOverrideType, Boolean> getOverrideOptions() {
		return overrideOptions;
	}

	public void setOverrideOptions(
			Map<ConstraintOverrideType, Boolean> overrideOptions) {
		this.overrideOptions = overrideOptions;
	}

}

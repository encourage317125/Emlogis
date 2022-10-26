package com.emlogis.model.tenant.settings.scheduling;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Wrapper class around List to enable correct json serialization of list elements
 *
 */
//@XmlRootElement
//@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class OptimizationSettingList extends ArrayList<OptimizationSetting>{

	public OptimizationSettingList() {
		super();
		// TODO Auto-generated constructor stub
	}

	public OptimizationSettingList(Collection<? extends OptimizationSetting> c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	public OptimizationSettingList(int initialCapacity) {
		super(initialCapacity);
		// TODO Auto-generated constructor stub
	}
}

package com.emlogis.utils.cmdline;


import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class CmdLineUtils {

	/**
	 * extract an option from an option set (ie set is modified)
	 * @param options
	 * @param optionName
	 * @param defaultVal
	 * @return
	 */
	public static int getIntOptionWithDefault(Set<String> options, String optionName, int defaultVal) {
		
		int val = defaultVal;
		for (String option : options) {
			if (option.startsWith(optionName)) {
				options.remove(option);
				String sval = option.substring(optionName.length());
				try {
					val = Integer.valueOf(sval);
					return val;
				}
				catch (Exception e) {
					throw new RuntimeException( "Error parsing option: '" + optionName + "', invalid Integer value: " + sval);
				}
			}
		}
		return val;
	}

	
	public static int getIntOption(Set<String> options, String optionName) {
		
		for (String option : options) {
			if (option.startsWith(optionName)) {
				options.remove(option);
				String sval = option.substring(optionName.length());
				try {
					int val = Integer.valueOf(sval);
					return val;
				}
				catch (Exception e) {
					throw new RuntimeException( "Error parsing option: '" + optionName + "', invalid Integer value: " + sval);
				}
			}
		}
		throw new RuntimeException( "Unable to find option: '" + optionName + "'");
	}
	
	
	/**
	 * extract an option from an option set (ie set is modified)
	 * @param options
	 * @param optionName
	 * @param defaultVal
	 * @return
	 */
	public static String getStringOptionWithDefault(Set<String> options, String optionName, String defaultVal) {
		
		String val = defaultVal;
		for (String option : options) {
			if (option.startsWith(optionName)) {
				options.remove(option);
				String sval = option.substring(optionName.length());
				if (StringUtils.isEmpty(sval)) {
					throw new RuntimeException( "Error parsing option: '" + optionName + "', invalid Integer value: " + sval);
				}
				return sval;
			}
		}
		return val;
	}

	
	public static String getStringOption(Set<String> options, String optionName) {
		
		String option = getStringOptionWithDefault(options, optionName, null);
		if (option == null) {	
			throw new RuntimeException( "Unable to find option: '" + optionName + "'");
		}
		return option;
	}
	 
}

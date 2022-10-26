package com.emlogis.rest.resources.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Andrii Mozharovskyi on 10/22/15.
 */
public enum QueryPattern {
    NOT_DELETED("isDeleted IS FALSE");

    private List<String> patternValues;

    private QueryPattern(String... patternValues) {
        this.patternValues = Arrays.asList(patternValues);
    }

    /**
     * @return Pattern filters joined with ' AND ' (if more than one).
     */
    public String val() {
        return val("");
    }

    /**
     * @param valuePrefix prefix for every filter in pattern, e.g [prefix.]property ('.' will be added automatically)
     * @return Pattern filters prepended with prefix
     */
    public String val(String valuePrefix) {
        return val(valuePrefix, "", "");
    }

    /**
     * @param valuePrefix
     * @param beforePattern will be added to the result string before the pattern filters
     * @param afterPattern  will be added to the result string after the pattern filters
     * @return
     * @see this.val(String)
     */
    public String val(String valuePrefix, String beforePattern, String afterPattern) {
        return beforePattern + valuesAsString(valuePrefix) + afterPattern;
    }

    private String valuesAsString(String valuePrefix) {
        if (StringUtils.isNotEmpty(valuePrefix)) {
            valuePrefix += ".";
        }
        if (this.patternValues == null || this.patternValues.isEmpty()) {
            return "";
        }
        return valuePrefix + StringUtils.join(this.patternValues, " AND " + valuePrefix);
    }
}

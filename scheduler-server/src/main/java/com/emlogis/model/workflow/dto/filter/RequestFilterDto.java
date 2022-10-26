package com.emlogis.model.workflow.dto.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucas on 27.05.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestFilterDto implements Serializable {

    @JsonProperty(value = "types", required = false)
    private List<String> types;

    @JsonProperty(value = "statuses", required = false)
    private List<String> statuses;

    @JsonProperty(value = "dateFrom", required = false)
    private Long dateFrom;

    @JsonProperty(value = "dateTo", required = false)
    private Long dateTo;

    @JsonProperty(value = "fullTextSearch", required = false)
    private String fullTextSearch;

    @JsonProperty(value = "offset", required = false)
    private Integer offset = 0;

    @JsonProperty(value = "limit", required = false)
    private Integer limit = 20;

    @JsonProperty(value = "orderBy", required = false)
    private String orderBy;

    @JsonProperty(value = "orderDir", required = false)
    private String orderDir;

    public RequestFilterDto() {
    }

    public List<String> getTypes() {
        if (types == null) {
            types = new ArrayList<>();
        }
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public Long getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Long dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Long getDateTo() {
        return dateTo;
    }

    public void setDateTo(Long dateTo) {
        this.dateTo = dateTo;
    }

    public String getFullTextSearch() {
        return fullTextSearch;
    }

    public void setFullTextSearch(String fullTextSearch) {
        this.fullTextSearch = fullTextSearch;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderDir() {
        return orderDir;
    }

    public void setOrderDir(String orderDir) {
        this.orderDir = orderDir;
    }

    public List<String> getStatuses() {
        if (statuses == null) {
            statuses = new ArrayList<>();
        }
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }
}

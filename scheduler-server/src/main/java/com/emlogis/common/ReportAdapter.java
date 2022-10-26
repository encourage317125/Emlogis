package com.emlogis.common;

import java.io.Serializable;
import java.util.Collection;

public class ReportAdapter {

    public static class ReportDto implements Serializable {

        public class ReportQueryDto implements Serializable {

            public class ReportResultsDto implements Serializable {
                private Object[] items;

                public Object[] getItems() {
                    return items;
                }

                public void setItems(Object[] items) {
                    this.items = items;
                }
            }

            private int count;
            private ReportResultsDto results = new ReportResultsDto();

            public int getCount() {
                return count;
            }

            public void setCount(int count) {
                this.count = count;
            }

            public ReportResultsDto getResults() {
                return results;
            }

            public void setResults(ReportResultsDto results) {
                this.results = results;
            }
        }

        private ReportQueryDto query = new ReportQueryDto();

        public ReportQueryDto getQuery() {
            return query;
        }

        public void setQuery(ReportQueryDto query) {
            this.query = query;
        }
    }

    public static String adaptCollection(Collection<?> collection) {
        ReportDto reportDto = new ReportDto();
        reportDto.query.count = collection.size();
        reportDto.query.results.items = collection.toArray();

        return EmlogisUtils.toJsonString(reportDto);
    }

}

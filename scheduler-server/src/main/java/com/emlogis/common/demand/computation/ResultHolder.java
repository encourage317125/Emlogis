package com.emlogis.common.demand.computation;

import java.util.ArrayList;
import java.util.List;

public class ResultHolder {

    private int holeCount = -1;
    private int intervalCount = -1;
    private List<ComputationResult> computationResults = new ArrayList<>();

    public List<ComputationResult> getComputationResults() {
        return computationResults;
    }

    public ComputationResult getFirstComputationResult() {
        return computationResults.size() > 0 ? computationResults.get(0) : null;
    }

    public boolean addIfFit(ComputationResult computationResult) {
        boolean result = false;
        int resultHoleCount = computationResult.holeCount();
        int resultIntervalCount = computationResult.intervalCount();

        if (holeCount == -1 && intervalCount == -1 || resultHoleCount < holeCount
                || resultHoleCount == holeCount && resultIntervalCount < intervalCount) {
            computationResults.clear();

            computationResults.add(computationResult.clone());
            result = true;

            holeCount = resultHoleCount;
            intervalCount = resultIntervalCount;
        } else if (resultHoleCount == holeCount && resultIntervalCount == intervalCount) {
            computationResults.add(computationResult.clone());
            result = true;
        }
        return result;
    }

}

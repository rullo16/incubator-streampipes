package org.gft.processors.trendfiltered;

import org.apache.streampipes.model.graph.DataProcessorInvocation;
import org.apache.streampipes.wrapper.params.binding.EventProcessorBindingParams;
import org.apache.streampipes.wrapper.siddhi.query.expression.RelationalOperator;

import java.util.List;

public class TrendFilteredParams extends EventProcessorBindingParams {

    private final TrendOperator operator;
    private final int increase;
    private final int duration;

    private final String input;
    private final List<String> outputFieldSelectors;


    private final double threshold;
    private final RelationalOperator filterOperation;



    public TrendFilteredParams(DataProcessorInvocation graph, TrendOperator operator, int increase, int duration, String input, RelationalOperator filterOperation, double threshold, List<String> outputFieldSelectors) {
        super(graph);
        this.operator = operator;
        this.increase = increase;
        this.duration = duration;
        this.input = input;
        this.outputFieldSelectors = outputFieldSelectors;
        this.filterOperation = filterOperation;
        this.threshold = threshold;
    }

    public TrendOperator getOperator() {
        return operator;
    }

    public int getIncrease() {
        return increase;
    }

    public int getDuration() {
        return duration;
    }

    public String getInput() {
        return input;
    }

    public List<String> getOutputFieldSelectors() {
        return outputFieldSelectors;
    }

    public double getThreshold() {
        return threshold;
    }

    public RelationalOperator getFilterOperation() {
        return filterOperation;
    }
}
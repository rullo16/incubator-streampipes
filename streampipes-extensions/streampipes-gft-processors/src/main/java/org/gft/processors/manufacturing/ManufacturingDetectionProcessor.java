package org.gft.processors.manufacturing;

import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.model.DataProcessorType;
import org.apache.streampipes.model.graph.DataProcessorDescription;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.model.schema.PropertyScope;
import org.apache.streampipes.sdk.builder.ProcessingElementBuilder;
import org.apache.streampipes.sdk.builder.StreamRequirementsBuilder;
import org.apache.streampipes.sdk.helpers.*;
import org.apache.streampipes.sdk.utils.Assets;
import org.apache.streampipes.vocabulary.SO;
import org.apache.streampipes.wrapper.context.EventProcessorRuntimeContext;
import org.apache.streampipes.wrapper.routing.SpOutputCollector;
import org.apache.streampipes.wrapper.standalone.ProcessorParams;
import org.apache.streampipes.wrapper.standalone.StreamPipesDataProcessor;

public class ManufacturingDetectionProcessor extends StreamPipesDataProcessor {

    private static final String NUMBER_MAPPING = "number-mapping";
    private static final String VALUE = "value";
    private static final String OPERATION = "operation";

    private static final String RESULT_FIELD = "ManufacturingDetected";

    private double threshold;
    private ManufacturingDetectionOperator numericalOperator;
    private String filterProperty;


    @Override
    public DataProcessorDescription declareModel() {
        return ProcessingElementBuilder.create("org.gft.processors.manufacturing")
                .category(DataProcessorType.FILTER)
                .withAssets(Assets.DOCUMENTATION, Assets.ICON)
                .withLocales(Locales.EN)
                .requiredStream(StreamRequirementsBuilder
                        .create()
                        .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                                Labels.withId(NUMBER_MAPPING),
                                PropertyScope.NONE).build())
                .outputStrategy(
                        OutputStrategies.append(
                                EpProperties.booleanEp(Labels.empty(), RESULT_FIELD, SO.Boolean)))
                .requiredSingleValueSelection(Labels.withId(OPERATION), Options.from("<", "<=", ">",
                        ">="))
                .requiredFloatParameter(Labels.withId(VALUE), NUMBER_MAPPING)
                .build();

    }


    @Override
    public void onInvocation(ProcessorParams processorParams, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext eventProcessorRuntimeContext) throws SpRuntimeException {
        this.threshold = processorParams.extractor().singleValueParameter(VALUE, Double.class);
        String stringOperation = processorParams.extractor().selectedSingleValue(OPERATION, String.class);

        String operation = "GT";

        switch (stringOperation) {
            case "<=":
                operation = "LE";
                break;
            case "<":
                operation = "LT";
                break;
            case ">=":
                operation = "GE";
                break;
        }

        this.filterProperty = processorParams.extractor().mappingPropertyValue(NUMBER_MAPPING);
        this.numericalOperator = ManufacturingDetectionOperator.valueOf(operation);

    }

    @Override
    public void onEvent(Event event, SpOutputCollector spOutputCollector) throws SpRuntimeException {
        boolean satisfiesFilter = false;

        Double value = event.getFieldBySelector(this.filterProperty).getAsPrimitive()
                .getAsDouble();

        Double threshold = this.threshold;

       if (this.numericalOperator == ManufacturingDetectionOperator.GE) {
            satisfiesFilter = (value >= threshold);
        } else if (this.numericalOperator == ManufacturingDetectionOperator.GT) {
            satisfiesFilter = value > threshold;
        } else if (this.numericalOperator == ManufacturingDetectionOperator.LE) {
            satisfiesFilter = (value <= threshold);
        } else if (this.numericalOperator == ManufacturingDetectionOperator.LT) {
            satisfiesFilter = (value < threshold);
        }

        if (satisfiesFilter) {
            event.addField("ManufacturingDetected", true);
            spOutputCollector.collect(event);
        } else {
            event.addField("ManufacturingDetected", false);
            spOutputCollector.collect(event);
        }
    }

    @Override
    public void onDetach() throws SpRuntimeException {

    }
}


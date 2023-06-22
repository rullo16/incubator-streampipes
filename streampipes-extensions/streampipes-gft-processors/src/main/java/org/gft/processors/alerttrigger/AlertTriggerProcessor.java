package org.gft.processors.alerttrigger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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


public class AlertTriggerProcessor extends StreamPipesDataProcessor {
    private static final String TIMESTAMP = "timestamp";
    private static final String TIMESTAMP_FICTIVE = "timestamp-fictive";
    private static final String VALUE = "number-mapping";
    private static final String THRESHOLD = "value";
    private static final String OPERATION = "operation";
    public static final String DURATION = "duration";
    public static final String DELAY = "delay";
    private static final String UNIT_DURATION = "unit-duration";
    private static final String UNIT_DELAY = "unit-delay";
    private static final String HOURS = "Hours";
    private static final String MINUTES = "Minutes";
    private static final String NONE = "None";

    private final JsonObject alertValue = new Gson().fromJson("{\"precedent_time\": 0, \"precedent_value\": -1.0, \"changed\": true}", JsonObject.class);
    private final JsonObject  alertTimestamp = new Gson().fromJson("{\"precedent_time\": 0, \"precedent_time_fictive\": 0, \"changed\": true}", JsonObject.class);
    private Long duration;
    private Long delay;
    private double threshold;
    private AlertTriggerOperator numerical_operator;
    private String value;
    private String timestamp;
    private String unit_duration;
    private String unit_delay;
    private String timestamp_fictive;

    @Override
    public DataProcessorDescription declareModel() {
        return ProcessingElementBuilder.create("org.gft.processors.alerttrigger")
                .category(DataProcessorType.FILTER)
                .withAssets(Assets.DOCUMENTATION, Assets.ICON)
                .withLocales(Locales.EN)
                .requiredStream(StreamRequirementsBuilder
                        .create()
                        .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                                Labels.withId(TIMESTAMP), PropertyScope.NONE)
                        .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                                Labels.withId(TIMESTAMP_FICTIVE), PropertyScope.NONE)
                        .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                                Labels.withId(VALUE),
                                PropertyScope.NONE).build())
                .requiredSingleValueSelection(Labels.withId(OPERATION), Options.from("<", "<=", ">",
                        ">=", "==", "!=", "None"))
                .requiredFloatParameter(Labels.withId(THRESHOLD), 0.0F)
                .requiredIntegerParameter(Labels.withId(DURATION), 0)
                .requiredSingleValueSelection(Labels.withId(UNIT_DURATION), Options.from(MINUTES, HOURS, NONE))
                .requiredIntegerParameter(Labels.withId(DELAY), 0)
                .requiredSingleValueSelection(Labels.withId(UNIT_DELAY), Options.from(MINUTES, HOURS, NONE))
                .outputStrategy(
                        OutputStrategies.fixed(EpProperties.timestampProperty("timestamp"),
                                EpProperties.doubleEp(Labels.empty(), "value", SO.Number),
                                EpProperties.booleanEp(Labels.empty(), "Alert_threshold", SO.Boolean),
                                EpProperties.booleanEp(Labels.empty(), "Alert_duplicate", SO.Boolean),
                                EpProperties.booleanEp(Labels.empty(), "Alert_delay", SO.Boolean)
                        ))
                .build();

    }


    @Override
    public void onInvocation(ProcessorParams processorParams, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext eventProcessorRuntimeContext) throws SpRuntimeException {
        this.timestamp = processorParams.extractor().mappingPropertyValue(TIMESTAMP);
        this.timestamp_fictive = processorParams.extractor().mappingPropertyValue(TIMESTAMP_FICTIVE);
        this.value = processorParams.extractor().mappingPropertyValue(VALUE);
        this.threshold = processorParams.extractor().singleValueParameter(THRESHOLD, Double.class);
        String string_operation = processorParams.extractor().selectedSingleValue(OPERATION, String.class);
        Integer duration = processorParams.extractor().singleValueParameter(DURATION, Integer.class);
        this.unit_duration = processorParams.extractor().selectedSingleValue(UNIT_DURATION, String.class);
        Integer delay = processorParams.extractor().singleValueParameter(DELAY, Integer.class);
        this.unit_delay = processorParams.extractor().selectedSingleValue(UNIT_DELAY, String.class);

        String operation = "GT";
        switch (string_operation) {
            case "<=":
                operation = "LE";
                break;
            case "<":
                operation = "LT";
                break;
            case ">=":
                operation = "GE";
                break;
            case "==":
                operation = "EQ";
                break;
            case "!=":
                operation = "IE";
                break;
            case "None":
                operation = "NONE";
                break;
        }
        this.numerical_operator = AlertTriggerOperator.valueOf(operation);


        switch (this.unit_duration) {
            case HOURS:
                this.duration = (long) duration * 60 * 60 * 1000;
                break;
            case MINUTES:
                this.duration = (long) duration * 60 * 1000;
                break;
            case NONE:
                this.duration = 0L;
                break;
        }

        switch (this.unit_delay) {
            case HOURS:
                this.delay = (long) delay * 60 * 60 * 1000;
                break;
            case MINUTES:
                this.delay = (long) delay * 60 * 1000;
                break;
            case NONE:
                this.delay = 0L;
                break;
        }

    }

    @Override
    public void onEvent(Event event, SpOutputCollector spOutputCollector) throws SpRuntimeException {
        boolean alert_duplicate, alert_threshold, alert_delay;

        Long current_time =  event.getFieldBySelector(this.timestamp).getAsPrimitive().getAsLong();
        Long current_time_fictive =  event.getFieldBySelector(this.timestamp_fictive).getAsPrimitive().getAsLong();
        Double current_value = event.getFieldBySelector(this.value).getAsPrimitive().getAsDouble();

        event.addField("timestamp", current_time);
        event.addField("value", current_value);

        if ((this.threshold != 0.0) && (this.numerical_operator != AlertTriggerOperator.NONE)){
            alert_threshold = alertThreshold(current_value);
            event.addField("Alert_threshold", alert_threshold);
        }

        if((this.duration != 0L) && !this.unit_duration.equals(NONE)){
            alert_duplicate = alertDuplicate(current_time, current_value);
            event.addField("Alert_duplicate", alert_duplicate);
        }

        if((this.delay != 0L) && !this.unit_delay.equals(NONE)){
            alert_delay = alertDelay(current_time, current_time_fictive);
            event.addField("Alert_delay", alert_delay);
        }

        spOutputCollector.collect(event);
    }


    private boolean alertThreshold(Double current_value) {
        boolean satisfy_filter = false;

        if (this.numerical_operator == AlertTriggerOperator.EQ) {
            satisfy_filter = (Math.abs(current_value - this.threshold) < 0.000001);
        }else if (this.numerical_operator == AlertTriggerOperator.GE) {
            satisfy_filter = (current_value >= this.threshold);
        } else if (this.numerical_operator == AlertTriggerOperator.GT) {
            satisfy_filter = (current_value > this.threshold);
        } else if (this.numerical_operator == AlertTriggerOperator.LE) {
            satisfy_filter = (current_value <= this.threshold);
        } else if (this.numerical_operator == AlertTriggerOperator.LT) {
            satisfy_filter = (current_value < this.threshold);
        } else if (this.numerical_operator == AlertTriggerOperator.IE) {
            satisfy_filter = (Math.abs(current_value - this.threshold) > 0.000001);
        }

        return satisfy_filter;
    }

    private boolean alertDuplicate(Long current_time, Double current_value) {
        boolean bool = false;
        double precedent_value = this.alertValue.get("precedent_value").getAsDouble();
        long precedent_time = this.alertValue.get("precedent_time").getAsLong();

        if((precedent_value == current_value)){
            long diff = current_time - precedent_time;
            if(diff >= this.duration){
                this.alertValue.addProperty("precedent_time", current_time);
                this.alertValue.addProperty("precedent_value", current_value);
                this.alertValue.addProperty("changed", false);
            }
            if (!this.alertValue.get("changed").getAsBoolean()) {
                bool = true;
            }
        }else{
            this.alertValue.addProperty("precedent_time", current_time);
            this.alertValue.addProperty("precedent_value", current_value);
            this.alertValue.addProperty("changed", true);
        }

        return bool;
    }

    private boolean alertDelay(Long current_time, Long current_time_fictive){
        boolean bool = false;
        long precedent_time = this.alertTimestamp.get("precedent_time").getAsLong();
        long precedent_time_fictive = this.alertTimestamp.get("precedent_time_fictive").getAsLong();

        if (precedent_time == current_time){
            long diff = current_time_fictive - precedent_time_fictive;
            if(diff >= this.delay){
                this.alertTimestamp.addProperty("precedent_time", current_time);
                this.alertTimestamp.addProperty("precedent_time_fictive", current_time_fictive);
                this.alertTimestamp.addProperty("changed", false);
            }
            if(!this.alertTimestamp.get("changed").getAsBoolean()){
                bool = true;
            }
        }else{
            this.alertTimestamp.addProperty("precedent_time", current_time);
            this.alertTimestamp.addProperty("precedent_time_fictive", current_time_fictive);
            this.alertTimestamp.addProperty("changed", true);
        }

        return bool;
    }


    @Override
    public void onDetach() throws SpRuntimeException {

    }
}


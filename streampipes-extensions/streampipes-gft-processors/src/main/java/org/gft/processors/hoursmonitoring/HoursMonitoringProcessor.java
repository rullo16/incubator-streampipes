package org.gft.processors.hoursmonitoring;


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

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HoursMonitoringProcessor extends StreamPipesDataProcessor {

    public static final String BOOL_VALUE = "bool";
    public static final String TRUE_FALSE = "true_false";
    public static final String OUTPUT_UNIT_ID = "output-unit";
    private static final String TRUE = "TRUE";
    private static final String FALSE = "FALSE";


    private static final String HOURS = "Hours";
    private static final String SECONDS = "Seconds";
    private static final String MINUTES = "Minutes";
    private static final String TIMESTAMP = "timestamp";
    private static final String MT = "mt";
    private static final String WTD = "wtd";
    private static final String WTW = "wtw";
    private static final String WTM = "wtm";

    private final DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    List<Double> measuredTimeListDaily = new ArrayList<>();
    List<Double> measuredTimeListWeekly = new ArrayList<>();
    List<Double> measuredTimeListMonthly = new ArrayList<>();
    private String fieldName;
    private Double output_divisor = 1000.0;
    private Long first_time_occurrence = Long.MIN_VALUE;
    private int day_precedent = -1, month_precedent = -1;
    private String measure_bool_string;
    private String timestamp;
    private Double result = 0.0;
    private Double operating_time_daily = 0.0;
    private Double operating_time_weekly = 0.0;
    private Double operating_time_monthly = 0.0;


    @Override
    public DataProcessorDescription declareModel() {
        return ProcessingElementBuilder.create("org.gft.processors.hoursmonitoring")
                .category(DataProcessorType.AGGREGATE) //, DataProcessorType.TIME
                .withLocales(Locales.EN)
                .withAssets(Assets.DOCUMENTATION, Assets.ICON)
                .requiredStream(StreamRequirementsBuilder.create()
                        .requiredPropertyWithUnaryMapping(EpRequirements.numberReq(),
                                Labels.withId(TIMESTAMP), PropertyScope.NONE)
                        .requiredPropertyWithUnaryMapping(EpRequirements.booleanReq(),Labels.withId(BOOL_VALUE),
                                PropertyScope.NONE)
                        .build())
                .requiredSingleValueSelection(Labels.withId(TRUE_FALSE), Options.from(TRUE, FALSE))
                .requiredSingleValueSelection(Labels.withId(OUTPUT_UNIT_ID), Options.from(MINUTES, HOURS))
                .outputStrategy(OutputStrategies.append(
                        EpProperties.numberEp(Labels.withId(MT), "measured_time", SO.Number),
                        EpProperties.numberEp(Labels.withId(WTD), "operating_time_daily", SO.Number),
                        EpProperties.numberEp(Labels.withId(WTW), "operating_time_weekly", SO.Number),
                        EpProperties.numberEp(Labels.withId(WTM), "operating_time_monthly", SO.Number)
                ))
                .build();
    }


    @Override
    public void onInvocation(ProcessorParams processorParams, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext eventProcessorRuntimeContext) throws SpRuntimeException {
        this.timestamp = processorParams.extractor().mappingPropertyValue(TIMESTAMP);
        this.fieldName = processorParams.extractor().mappingPropertyValue(BOOL_VALUE);
        this.measure_bool_string = processorParams.extractor().selectedSingleValue(TRUE_FALSE, String.class);
        String output_unit = processorParams.extractor().selectedSingleValue(OUTPUT_UNIT_ID, String.class);

        if (output_unit.equals(HOURS)) {
            this.output_divisor = 60000.0*60;
        } else if (output_unit.equals(MINUTES)){
            this.output_divisor = 60000.0;
        }

    }

    @Override
    public void onEvent(Event inputEvent, SpOutputCollector out) throws SpRuntimeException {

        Boolean bool_value = inputEvent.getFieldBySelector(this.fieldName).getAsPrimitive().getAsBoolean();
        Long current_time =  inputEvent.getFieldBySelector(this.timestamp).getAsPrimitive().getAsLong();

        Boolean measure_bool = this.measure_bool_string.equals(TRUE);

        if (measure_bool == bool_value) {
            if (this.first_time_occurrence == Long.MIN_VALUE) {
                // set the first time of our sequence of desired boolean value
                this.first_time_occurrence = current_time;
            }
        } else {
            if (this.first_time_occurrence != Long.MIN_VALUE) {
                // calculates the time between the beginning and the end of the "bool_value" sequence
                long difference = current_time - this.first_time_occurrence;
                this.result = difference / this.output_divisor;

                // add result to the lists
                this.measuredTimeListDaily.add(this.result);
                this.measuredTimeListWeekly.add(this.result);
                this.measuredTimeListMonthly.add(this.result);

                // reset the precedent time
                this.first_time_occurrence = Long.MIN_VALUE;

                //recovery date value
                String date = getTheDate(current_time);

                // Day and Month extraction
                String[] ymd_hms = date.split(" ");
                String[] ymd = ymd_hms[0].split("-");
                int day_current = Integer.parseInt(ymd[2]);
                int month_current = Integer.parseInt(ymd[1]);
                String day = getCurrentDay(date);

                //if true compute first the consumption of the day that has just passed
                if(day_current != this.day_precedent && this.day_precedent != -1){
                    // reset day for computations
                    this.day_precedent = day_current;

                    //perform operations to obtain hours per day
                    this.operating_time_daily = calculateHours(measuredTimeListDaily);
                    this.measuredTimeListDaily.clear();

                    //if the day coincide with monday compute the consumption of the week that has just passed
                    if(day.equals("Mon")){
                        this.operating_time_weekly = calculateHours(this.measuredTimeListWeekly);
                        this.measuredTimeListWeekly.clear();
                    }

                    //if true compute consumption of the month that has just passed
                    if(month_current != this.month_precedent){
                        this.month_precedent = month_current;
                        this.operating_time_monthly = calculateHours(this.measuredTimeListMonthly);
                        this.measuredTimeListMonthly.clear();
                    }

                }

                // set the start time for computations
                if (this.day_precedent == -1){
                    this.month_precedent = month_current;
                    this.day_precedent = day_current;
                }
            }
        }

        inputEvent.addField("measured_time", this.result);
        inputEvent.addField("operating_time_daily", this.operating_time_daily);
        inputEvent.addField("operating_time_weekly", this.operating_time_weekly);
        inputEvent.addField("operating_time_monthly", this.operating_time_monthly);
        out.collect(inputEvent);
    }

    private String getTheDate(Long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return  date_format.format(cal.getTime());
    }

    private String getCurrentDay(String date){
        String day = null;
        try{
            Date myDate = date_format.parse(date);
            LocalDateTime localDateTime = myDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            // convert LocalDateTime to date
            Date date_plus = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            String[] s = date_plus.toString().split(" ");
            day = s[0];
        }catch (ParseException e){
            e.printStackTrace();
        }
        return day;
    }

    private Double calculateHours(List<Double> measuredTimeList) {
        double sum = 0.0;
        DecimalFormat df = new DecimalFormat("#.#####");
        df.setRoundingMode(RoundingMode.CEILING);
        for (Double value : measuredTimeList) sum = sum + value;
        return Double.parseDouble(df.format(sum));
    }

    @Override
    public void onDetach() throws SpRuntimeException {

    }
}


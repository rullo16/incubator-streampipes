package org.gft.adapters.backend;


import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class BackendHttpConfig {

    private final Integer length;
    private final String username;
    private final String password;
    private final String signal_id;
    private final String lowest_date;
    private final String highest_date;
    private String first_date = "00-00-00 00:00:00";
    private String second_date = "00-00-00 00:00:00";

    private final JsonObject nodes_id = new Gson().fromJson("{\"PINDOS\":\"61855a064f181d0f3a3b4d42\",\"ASTANDER\":\"6167f8078870124d6f1bc5e2\"}", JsonObject.class);

    DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BackendHttpConfig(String username, String password, String signal_id, String lowest_date, String highest_date, Integer length) {
        this.username = username;
        this.password = password;
        this.signal_id = signal_id;
        this.lowest_date = lowest_date;
        this.highest_date = highest_date;
        this.length = length;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public Integer getLength() {
        return this.length;
    }

    public String getClientId() {
        return "1";
    }

    public String getClientSecret(){
        return "oPKFIotoK1GiccRxQWWeFcXo4TbWq8fEhDFl0TJs";
    }

    public String getPage(){
        return "1";
    }

    public String getLoginUrl(){
        return "https://kyklos-backend.kyklos40project.eu:1121/oauth/token";
    }

    public String getBaseUrl(){
        return "https://kyklos-backend.kyklos40project.eu:1121/api/v0.1/kyklos_core_signal_readings/view/records";
    }

    public String getSort(){
        return "[{\"scope\":\"date\",\"value\":\"asc\"}]";
    }

    public String getGrantType(){
        return "password";
    }
    public String getHighestDate(){
        return highest_date;
    }
    public String getLowestDate(){
        return first_date;
    }
    public String getSecondDate(){return second_date;}

    public String getScope(){
        return "read_scheduler_administrator write_scheduler_administrator read_dashboards_administrator write_dashboards_administrator " +
                    "read_datasources_administrator write_datasources_administrator read_raw_signals_administrator write_raw_signals_administrator " +
                    "read_raw_signal_readings_administrator write_raw_signal_readings_administrator read_nodes_administrator write_nodes_administrator read_components_administrator " +
                    "write_components_administrator read_signal_readings_administrator read_conversions_administrator write_conversions_administrator read_data_fusion_administrator " +
                    "write_data_fusion_administrator read_user_relationships_administrator read_users_administrator read_profile_administrator read_events_signal_administrator " +
                    "read_events_data_source_administrator read_datasinks_administrator write_datasinks_administrator read_dashboards_basic_user write_dashboards_basic_user " +
                    "delete_dashboards_basic_user read_nodes_basic_user write_nodes_basic_user delete_nodes_basic_user share_nodes_basic_user read_components_basic_user " +
                    "write_components_basic_user delete_components_basic_user read_component_signals_basic_user write_component_signals_basic_user delete_component_signals_basic_user " +
                    "read_component_alerts_basic_user write_component_alerts_basic_user delete_component_alerts_basic_user read_signal_readings_basic_user " +
                    "write_signal_readings_basic_user delete_signal_readings_basic_user read_profile_new_user";
    }

    // retrieve filter parameter according to the two date of the polling interval and the signal name
    public String getFilter(String lowest_date, String highest_date) {
        String filter;
        if(this.highest_date.equals("CurrentDateTime")){
            filter = "[{\"scope\":\"comp_signal.node._id\",\"type\":\"object-id\",\"operator\":\"in\", \"value\":["+this.nodes_id.get("PINDOS")+"]}," +
                    "{\"scope\":\"comp_signal_id\",\"type\":\"object-id\",\"operator\":\"in\", \"value\":[\""+ this.signal_id+"\"]}," +
                    "{\"scope\":\"date\",\"type\":\"date-range\",\"operator\":\">= <\",\"value\":\"" + lowest_date +" - "+ highest_date +"\"}]";

        }else{
            filter = "[{\"scope\":\"comp_signal.node._id\",\"type\":\"object-id\",\"operator\":\"in\", \"value\":["+this.nodes_id.get("PINDOS")+"]}," +
                    "{\"scope\":\"comp_signal_id\",\"type\":\"object-id\",\"operator\":\"in\", \"value\":[\""+ this.signal_id+"\"]}," +
                    "{\"scope\":\"date\",\"type\":\"date-range\",\"operator\":\">= <\",\"value\":\"" + lowest_date +" - "+ this.highest_date +"\"}]";

        }
        return filter;
    }


    // return the current date as string
    public String CurrentDateTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }


    // return the first date of the polling interval (sub-interval of the whole date interval as required in input)
    public String firstDateTime() {
        Date myDate = null;
        String first_date;

        // check if it's the first time that polling occur
        // For the first sub_interval for data fetching, the first date must be exactly the lowest_date and since
        //we update every time the current first_date to the next first date it necessary to do this operation
        if(this.first_date.equals("00-00-00 00:00:00")){
            this.first_date = this.lowest_date;
            return this.lowest_date;
        }

        try{
            myDate = date_format.parse(this.first_date);
        }catch (ParseException e){
            e.printStackTrace();
        }

        assert myDate != null;
        LocalDateTime local_date_time = myDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        local_date_time = local_date_time.plusDays(5); // 5 days of polling interval (increasing of 5 days each time there is a polling)
        Date date_plus = Date.from(local_date_time.atZone(ZoneId.systemDefault()).toInstant());

        first_date = date_format.format(date_plus);
        // for the upcoming polling, replace the old first_date with the newer
        this.first_date = first_date;

        return first_date;
    }


    // return the second date of the polling interval (sub-interval of the whole date interval as required in input)
    public String secondDateTime(){
        Date myDate = null;

        // for the current polling, use the current first_date and add it 5 days to obtain the second_date
        try{
            myDate = date_format.parse(this.first_date);
        }catch (ParseException e){
            e.printStackTrace();
        }

        assert myDate != null;
        LocalDateTime local_date_time = myDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        local_date_time = local_date_time.plusDays(5); //5 days of polling interval (increasing of 5 days each time there is a polling)

        Date date_plus = Date.from(local_date_time.atZone(ZoneId.systemDefault()).toInstant());
        this.second_date = date_format.format(date_plus);

        //return the last date (highest_date: required as parameter) in the visualisation date interval, in order to not go out range.
        if(!this.highest_date.equals("CurrentDateTime")  &&  this.second_date.compareToIgnoreCase(this.highest_date) >= 0){
            return this.highest_date;
        }
        return this.second_date;
    }


    // return the CurrentDateTime() minus 5 minutes (occur each time when there will be a polling)
    public String precedentCurrentTime(String current_time) {
        Date myDate = null;
        try{
            myDate = date_format.parse(current_time);
        }catch (ParseException e){
            e.printStackTrace();
        }

        assert myDate != null;
        LocalDateTime local_date_time = myDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        local_date_time = local_date_time.minusMinutes(5); //5 minutes of polling interval (difference of 5 minutes between the two date of the polling interval)
        Date date_plus = Date.from(local_date_time.atZone(ZoneId.systemDefault()).toInstant());

        return date_format.format(date_plus);
    }

}
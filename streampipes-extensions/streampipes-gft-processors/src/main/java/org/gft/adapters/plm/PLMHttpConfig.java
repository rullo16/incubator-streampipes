package org.gft.adapters.plm;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class PLMHttpConfig {

    private final String model;
    private final String username;
    private final String password;
    private final String signal_name;
    private final String lowest_date;
    private final String highest_date;
    private String first_date = "00-00-00 00:00:00";
    DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public PLMHttpConfig(String username, String password, String model, String signal_name, String lowest_date, String highest_date) {
        this.username = username;
        this.password = password;
        this.model = model;
        this.signal_name = signal_name;
        this.lowest_date = lowest_date;
        this.highest_date = highest_date;
    }

    public String getRepository(){
        return "TruePLMprojectsRep";
    }
    public String getBaseUrl(){ return "https://kyklos.jotne.com/EDMtruePLM/api/"; }
    public String getGroup(){
        return "sdai-group";
    }
    public String getUsername() {
        return this.username;
    }
    public String getPassword() {
        return this.password;
    }
    public String getModel() {  return this.model;}
    public String getSignal() {  return this.signal_name;}

    public String getHighestDate(){
        return getMillis(highest_date);
    }
    public String getLowestDate(){
        return getMillis(first_date);
    }

    // Convert date the date format (yyyy-MM-dd HH:mm:ss) to the timestamp format (millis-long)
    private String getMillis(String date){
        String timestamp = null;
        try{
            Date myDate = date_format.parse(date);
           timestamp = String.valueOf(myDate.getTime());
        }catch (ParseException e){
            e.printStackTrace();
        }
        return timestamp;
    }

    //return the second date for the current polling interval (sub-interval of the whole date interval as required in input)
    public String secondDateTime() throws ParseException {
        String second_date = " ";

        // for the current polling, use the current first_date and add it 5 days to obtain the second_date
        try{
            Date myDate = date_format.parse(this.first_date);
            // convert date to local datetime
            LocalDateTime local_date_time = myDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            local_date_time = local_date_time.plusMinutes(2880); // 2 days of polling interval (increasing of 2 days each time there is a polling)
            Date date_plus = Date.from(local_date_time.atZone(ZoneId.systemDefault()).toInstant());
            second_date = date_format.format(date_plus);
        }catch (ParseException e){
            e.printStackTrace();
        }

        //return the last date (highest_date: required as parameter) in the visualisation date interval, in order to not go out range.
        if(second_date.compareToIgnoreCase(this.highest_date) >= 0){
            return getMillis(this.highest_date);
        }

        return getMillis(second_date);
    }

    //return the first date for the current polling interval (part of the whole date interval as required in input)
    public String firstDateTime() throws ParseException {
        Date myDate = null;
        String first_date;

        // check if it's the first time that polling occur
        // For the first sub_interval for data fetching, the first date must be exactly the lowest_date and since
        //we update every time the current first_date to the next first date it necessary to do this operation
        if(this.first_date.equals("00-00-00 00:00:00")){
            this.first_date = this.lowest_date;
            return getMillis(this.lowest_date);
        }

        try{
            myDate = date_format.parse(this.first_date);
        }catch (ParseException e){
            e.printStackTrace();
        }

        assert myDate != null;
        LocalDateTime local_date_time = myDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        local_date_time = local_date_time.plusMinutes(2880); // 2 days of polling interval (increasing of 2 days each time there is a polling)
        Date date_plus = Date.from(local_date_time.atZone(ZoneId.systemDefault()).toInstant());

        first_date = date_format.format(date_plus);
        // for the upcoming polling, replace the old first_date with the newer
        this.first_date = first_date;

        return getMillis(first_date);
    }
}

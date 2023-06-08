package org.gft.adapters.plm;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class PLMHttpConfig {

    private final String model;
    private final String username;
    private final String password;
    private final String signal_name;
    private final String lowest_date;
    private final String highest_date;
    private String first_date = "00-00-00 00:00:00";
    private String second_date = "00-00-00 00:00:00";
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
        if (highest_date.equals("CurrentDateTime"))
            return  "CurrentDateTime";
        else
            return getMillis(highest_date);
    }
    public String getFirstDate(){
        return getMillis(first_date);
    }
    public String getSecondDate(){return getMillis(second_date);}

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

        // for the current polling, use the current first_date and add it 5 days to obtain the second_date
        try{
            Date myDate = date_format.parse(this.first_date);
            // convert date to local datetime
            LocalDateTime local_date_time = myDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            local_date_time = local_date_time.plusDays(5); // 5 days of polling interval (increasing of 2 days each time there is a polling)
            Date date_plus = Date.from(local_date_time.atZone(ZoneId.systemDefault()).toInstant());
            this.second_date = date_format.format(date_plus);
        }catch (ParseException e){
            e.printStackTrace();
        }

        //return the last date (highest_date: required as parameter) in the visualisation date interval, in order to not go out range.
        if(!this.highest_date.equals("CurrentDateTime") &&  this.second_date.compareToIgnoreCase(this.highest_date) >= 0){
            return getMillis(this.highest_date);
        }
        return getMillis(this.second_date);
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
        local_date_time = local_date_time.plusDays(5); // 5 days of polling interval (increasing of 2 days each time there is a polling)
        Date date_plus = Date.from(local_date_time.atZone(ZoneId.systemDefault()).toInstant());

        first_date = date_format.format(date_plus);
        // for the upcoming polling, replace the old first_date with the newer
        this.first_date = first_date;

        return getMillis(first_date);
    }

    public String CurrentDateTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return getMillis(dtf.format(now));
    }

    // return the CurrentDateTime() minus 5 minutes (occur each time when there will be a polling)
    public String precedentCurrentTime(String current_time) {
        long  milliseconds = Long.parseLong(current_time);

        LocalDateTime local_date_time = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneId.systemDefault());
        local_date_time = local_date_time.minusMinutes(5); //5 minutes of polling interval (difference of 5 minutes between the two date of the polling interval)
        Date date_plus = Date.from(local_date_time.atZone(ZoneId.systemDefault()).toInstant());

        String date = date_format.format(date_plus);
        return getMillis(date);
    }
}

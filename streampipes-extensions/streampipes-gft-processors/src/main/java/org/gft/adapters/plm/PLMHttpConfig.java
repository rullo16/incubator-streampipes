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

    public String secondDateTime() throws ParseException {
        String second_date = " ";
        try{
            Date myDate = date_format.parse(this.first_date);

            LocalDateTime local_date_time = myDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            local_date_time = local_date_time.plusMinutes(2880);
            Date date_plus = Date.from(local_date_time.atZone(ZoneId.systemDefault()).toInstant());
            second_date = date_format.format(date_plus);
        }catch (ParseException e){
            e.printStackTrace();
        }

        if(second_date.compareToIgnoreCase(this.highest_date) >= 0){
            return getMillis(this.highest_date);
        }

        return getMillis(second_date);
    }

    public String firstDateTime() throws ParseException {
        Date myDate = null;
        String first_date;

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
        local_date_time = local_date_time.plusMinutes(2880);
        Date date_plus = Date.from(local_date_time.atZone(ZoneId.systemDefault()).toInstant());

        first_date = date_format.format(date_plus);
        this.first_date = first_date;

        return getMillis(first_date);
    }
}

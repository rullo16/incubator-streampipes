package org.gft.adapters.backend;

import org.apache.streampipes.sdk.extractor.StaticPropertyExtractor;
import org.apache.streampipes.sdk.helpers.Label;
import org.apache.streampipes.sdk.helpers.Labels;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class BackendHttpUtils {
    private static final String LENGTH = "length";
    private static final String LOWEST_DATE = "lowest_date";
    private static final String HIGHEST_DATE = "highest_date";
    private static final String SENSOR_SIGNAL = "signal";
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Label getUsernameLabel() {
        return Labels.withId(USERNAME_KEY);
    }

    public static Label getPasswordLabel() {
        return Labels.withId(PASSWORD_KEY);
    }

    public static Label getLengthLabel() {
        return Labels.withId(LENGTH);
    }

    public static Label getSignalLabel() {
        return Labels.withId(SENSOR_SIGNAL);
    }

    public static Label getLowestLabel() {
        return Labels.withId(LOWEST_DATE);
    }

    public static Label getHighestLabel() {
        return Labels.withId(HIGHEST_DATE);
    }

    public static BackendHttpConfig getConfig(StaticPropertyExtractor extractor) {
        //TODO delete length field

        String username = extractor.singleValueParameter(USERNAME_KEY, String.class).trim();
        String password = extractor.secretValue(PASSWORD_KEY);
        String signal_name = extractor.singleValueParameter(SENSOR_SIGNAL, String.class).trim();
        String lowest_date = extractor.singleValueParameter(LOWEST_DATE, String.class).trim();//TODO .strip
        String highest_date = extractor.singleValueParameter(HIGHEST_DATE, String.class).trim();//TODO .strip
        Integer length = 30000;

        if(!highest_date.equals("CurrentDateTime")){
            try {
                sdf.parse(highest_date);
                sdf.setLenient(false);            // strict mode - check 30 or 31 days, leap year
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        try {
            sdf.parse(lowest_date);
            sdf.setLenient(false);            // strict mode - check 30 or 31 days, leap year
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new BackendHttpConfig(username, password, signal_name, lowest_date, highest_date, length);
    }

}

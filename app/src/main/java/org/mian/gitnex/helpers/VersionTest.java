package org.mian.gitnex.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author 6543
 * Datum 2019-10-14
 * License GPL3
 */

public enum VersionTest {
    UNKNOWN,
    SUPPORTED_LATEST,
    SUPPORTED_OLD,
    DEVELOPMENT,
    UNSUPPORTED_OLD,
    UNSUPPORTED_NEW;



    public static VersionTest check(String min, String last, String value) {

        final Pattern pattern_stable_release = Pattern.compile("^(\\d)\\.(\\d+)\\.(\\d+)$");
        final Pattern pattern_dev_release = Pattern.compile("^(\\d).(\\d+).(\\d+)(\\D)(.+)");
        Matcher m ;

        m = pattern_stable_release.matcher(value);
        if (m.find()) {

            switch (correlate(min, last, m.group())){
                case 0:
                    return UNSUPPORTED_OLD;
                case 1:
                    return SUPPORTED_OLD;
                case 2:
                    return SUPPORTED_LATEST;
                default:
                    return UNSUPPORTED_NEW;
            }

        }
        m = pattern_dev_release.matcher(value);
        if (m.find()) {

            m = Pattern.compile("^(\\d)\\.(\\d+)\\.(\\d+)").matcher(value);
            m.find();

            if (correlate(min, last, m.group())>0) {
                return DEVELOPMENT;
            } else {
                return UNSUPPORTED_OLD;
            }

        }

        return UNKNOWN;

    }

    //helper
    // 0 to less
    // 1 in spectrum
    // 2 at the top
    // 3 above
    private static int correlate(String min, String last, String value){
        //init
        int i_min = 0, i_last = 0, i_value = 0;

        //go down each integer (separated by points) until it is the last one
        while (value.indexOf(".")>0) {

            //prepare for checks
            if (min.indexOf(".") >= 0) i_min = Integer.valueOf(min.substring(0,min.indexOf(".")));
            if (last.indexOf(".") >= 0) i_last = Integer.valueOf(last.substring(0,last.indexOf(".")));
            if (value.indexOf(".") >= 0) i_value = Integer.valueOf(value.substring(0,value.indexOf(".")));

            if ( i_min != i_last ) {
                //check
                if (i_value < i_min) return 0;
                if (i_value > i_last) return 3;
            }

            //delete checked integer and move on to next one
            min = min.substring(min.indexOf(".")+1);
            last = last.substring(last.indexOf(".")+1);
            value = value.substring(value.indexOf(".")+1);

        }

        i_min = Integer.valueOf(min);
        i_last = Integer.valueOf(last);
        i_value = Integer.valueOf(value);

        //check last integer
        if (i_value < i_min) return 0;
        if (i_min < i_value && i_value < i_last) return 1;
        if (i_value == i_last) return 2;
        if (i_value > i_last) return 3;


        return 0;
    }

}

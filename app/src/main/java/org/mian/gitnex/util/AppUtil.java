package org.mian.gitnex.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class AppUtil {

    public static String strReplace(String str, String original, String replace) {
        return str.replace(original, replace);
    }

    public static boolean haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public static int getAppBuildNo(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static String getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public int charactersLength(String str) {
         return str.length();
    }

    public Boolean checkStringsWithAlphaNumeric(String str) { // [a-zA-Z0-9]
        return str.matches("^[\\w]+$");
    }

    public Boolean checkStrings(String str) { // [a-zA-Z0-9-_. ]
        return str.matches("^[\\w .-]+$");
    }

    public Boolean checkStringsWithAlphaNumericDashDotUnderscore(String str) { // [a-zA-Z0-9-_]
        return str.matches("^[\\w.-]+$");
    }

    public int getResponseStatusCode(String u) throws Exception {

        URL url = new URL(u);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        return (http.getResponseCode());

    }

    public static void setAppLocale(Resources resource, String locCode) {

        DisplayMetrics dm = resource.getDisplayMetrics();
        Configuration config = resource.getConfiguration();
        config.setLocale(new Locale(locCode.toLowerCase()));
        resource.updateConfiguration(config, dm);

    }

    public static boolean httpCheck(String url) {

        String pattern = "^(http|https)://.*$";
        return url.matches(pattern);

    }

    public static String formatFileSize(long size) {

        String repoSize = null;

        double k = size;
        double m = size/1024.0;
        double g = ((size/1024.0)/1024.0);
        double t = (((size/1024.0)/1024.0)/1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if ( t > 1 ) {
            repoSize = dec.format(t).concat(" TB");
        } else if ( g > 1 ) {
            repoSize = dec.format(g).concat(" GB");
        } else if ( m > 1 ) {
            repoSize = dec.format(m).concat(" MB");
        } else if ( k > 1 ) {
            repoSize = dec.format(k).concat(" KB");
        }

        return repoSize;

    }

    public static String customDateFormat(String customDate) {

        String[] parts = customDate.split("-");
        final String year = parts[0];
        final String month = parts[1];
        final String day = parts[2];

        String sMonth;
        if (Integer.parseInt(month) < 10) {
            sMonth = "0"+String.valueOf(month);
        } else {
            sMonth = String.valueOf(month);
        }

        String sDay;
        if (Integer.parseInt(day) < 10) {
            sDay = "0"+String.valueOf(day);
        } else {
            sDay = String.valueOf(day);
        }

        return year + "-" + sMonth + "-" + sDay;

    }

    public static String customDateCombine(String customDate) {

        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
        int mSeconds = c.get(Calendar.SECOND);

        String sMin;
        if ((mMinute) < 10) {
            sMin = "0"+String.valueOf(mMinute);
        } else {
            sMin = String.valueOf(mMinute);
        }

        String sSec;
        if ((mSeconds) < 10) {
            sSec = "0"+String.valueOf(mSeconds);
        } else {
            sSec = String.valueOf(mSeconds);
        }

        return (customDate + "T" + mHour + ":" + sMin + ":" + sSec + "Z");

    }

}

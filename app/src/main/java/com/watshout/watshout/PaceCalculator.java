package com.watshout.watshout;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.DecimalFormat;

public class PaceCalculator {

    private double rawMetricDistance;
    private int min;
    private int sec;

    private String metricPace;
    private String metricDistance;

    private String imperialPace;
    private String imperialDistance;

    private String units;

    PaceCalculator(double rawMetricDistance, int min, int sec, Context context) {
        this.rawMetricDistance = rawMetricDistance;
        this.min = min;
        this.sec = sec;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String units = settings.getString("Units", "Metric");

        this.units = units;

        calculateValues();
    }

    public String getPaceUnits() {

        if (units.equals("Metric")){
            return "mi/km";
        } else {
            return "mi/m";
        }

    }

    public String getDistanceUnits() {

        if (units.equals("Metric")){
            return "km";
        } else {
            return "m";
        }

    }

    public String getDistance() {

        if (units.equals("Metric")){
            return this.metricDistance;
        } else {
            return this.imperialDistance;
        }
    }

    public String getPace() {

        if (units.equals("Metric")){
            return this.metricPace;
        } else {
            return this.imperialPace;
        }
    }

    public int getTotalSeconds() {return (min * 60) + sec;}

    public String getMetricPace(){
        return metricPace;
    }

    public String getMetricDistance() {
        return metricDistance;
    }

    private void calculateMetricValues(){
        DecimalFormat decimalFormat = new DecimalFormat(".#");
        String metricDistanceString = decimalFormat.format(rawMetricDistance);

        int totalSeconds = (min * 60) + sec;

        // Perform metric calculations first, those are getting uploaded to Firebase either way
        double rawMetricPace = totalSeconds / rawMetricDistance;

        int metricMinutePace = (int) (rawMetricPace / 60);
        int metricSecondPace = (int) (rawMetricPace - metricMinutePace * 60);

        if (rawMetricDistance <= 0.05){
            this.metricDistance = "0.0";
            this.metricPace = "0:00";
        } else {
            String metricMinuteString = String.format("%02d", metricMinutePace);
            String metricSecondString = String.format("%02d", metricSecondPace);

            this.metricPace = metricMinuteString + ":" + metricSecondString;
            this.metricDistance = metricDistanceString;
        }

        Log.d("PaceCalculator", "Raw metric distance: " + rawMetricDistance +
            "\nMetric distance: " + metricDistance + "\nMetric pace: " + metricPace);

    }

    private void calculateImperialValues() {

        double KM_TO_MILE = 0.621371;
        double rawImperialDistance = rawMetricDistance * KM_TO_MILE;
        DecimalFormat decimalFormat = new DecimalFormat(".#");
        String imperialDistanceString = decimalFormat.format(rawImperialDistance);

        int totalSeconds = (min * 60) + sec;

        // Perform metric calculations first, those are getting uploaded to Firebase either way
        double rawMetricPace = totalSeconds / rawImperialDistance;

        int imperialMinutePace = (int) (rawMetricPace / 60);
        int imperialSecondPace = (int) (rawMetricPace - imperialMinutePace * 60);

        // Using metric here to stay consistent with calculateMetricValues()
        if (rawMetricDistance <= 0.05) {
            this.imperialDistance = "0.0";
            this.imperialPace = "0:00";
        } else {
            String metricMinuteString = String.format("%02d", imperialMinutePace);
            String metricSecondString = String.format("%02d", imperialSecondPace);

            this.metricPace = metricMinuteString + ":" + metricSecondString;
            this.metricDistance = imperialDistanceString;
        }

        Log.d("PaceCalculator", "Raw imperial distance: " + rawMetricDistance +
                "\nImperial distance: " + metricDistance + "\nImperial pace: " + metricPace);
    }

    private void calculateValues() {

        calculateMetricValues();
        calculateImperialValues();

    }

}

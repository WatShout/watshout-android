package com.watshout.watshout;

import java.text.DecimalFormat;

public class PaceCalculator {

    private double rawMetricDistance;
    private int min;
    private int sec;

    private String metricPace;
    private String metricDistance;

    private String imperialPace;
    private String imperialDistance;

    PaceCalculator(double rawMetricDistance, int min, int sec) {
        this.rawMetricDistance = rawMetricDistance;
        this.min = min;
        this.sec = sec;

        calculateValues();
    }

    public int getTotalSeconds() {return (min * 60) + sec;}

    public String getMetricPace(){
        return metricPace;
    }

    public String getMetricDistance() {
        return metricDistance;
    }

    public String getImperialPace(){
        return imperialPace;
    }

    public String getImperialDistance() {
        return imperialDistance;
    }

    private void calculateMetricValues(){
        DecimalFormat decimalFormat = new DecimalFormat(".#");
        String metricDistanceString = decimalFormat.format(rawMetricDistance);

        int totalSeconds = (min * 60) + sec;

        // Perform metric calculations first, those are getting uploaded to Firebase either way
        double rawMetricPace = totalSeconds / rawMetricDistance;

        int metricMinutePace = (int) (rawMetricPace / 60);
        int metricSecondPace = (int) (rawMetricPace - metricMinutePace * 60);

        if (rawMetricDistance == 0.0){
            this.metricDistance = "0.0";
            this.metricPace = "0:00";
        } else {
            String metricMinuteString = String.format("%02d", metricMinutePace);
            String metricSecondString = String.format("%02d", metricSecondPace);

            this.metricPace = metricMinuteString + ":" + metricSecondString;
            this.metricDistance = metricDistanceString;
        }

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

        if (rawImperialDistance == 0.0){
            this.imperialDistance = "0.0";
            this.imperialPace = "0:00";
        } else {
            String metricMinuteString = String.format("%02d", imperialMinutePace);
            String metricSecondString = String.format("%02d", imperialSecondPace);

            this.metricPace = metricMinuteString + ":" + metricSecondString;
            this.metricDistance = imperialDistanceString;
        }
    }

    private void calculateValues() {

        calculateMetricValues();
        calculateImperialValues();

    }

}

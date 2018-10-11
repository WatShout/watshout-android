package com.watshout.app;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PaceCalculator {

    private double distance;
    private int min;
    private int sec;


    PaceCalculator(double distance, int min, int sec) {
        this.distance = round(distance, 2);
        this.min = min;
        this.sec = sec;

    }

    public double getDistance() {
        return distance;
    }

    public int getTotalSeconds() {
        return (min * 60) + sec;
    }

    public String getPace() {
        int totalSeconds = getTotalSeconds();

        if (distance > 0.05) {
            double rawMetricPace = totalSeconds / distance;

            int imperialMinutePace = (int) (rawMetricPace / 60);
            int imperialSecondPace = (int) (rawMetricPace - imperialMinutePace * 60);

            String minutes = String.format("%02d", imperialMinutePace);
            String seconds = String.format("%02d", imperialSecondPace);

            return minutes + ":" + seconds;

        } else {
            return "0:00";
        }

    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

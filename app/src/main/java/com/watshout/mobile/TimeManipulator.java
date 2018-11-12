package com.watshout.mobile;

public class TimeManipulator {
    private static final TimeManipulator ourInstance = new TimeManipulator();

    public static TimeManipulator getInstance() {
        return ourInstance;
    }

    private TimeManipulator() {
    }

    public String formatTime (int totalSeconds) {

        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        String formattedMinutes = String.format("%02d", minutes);
        String formattedSeconds = String.format("%02d", seconds);

        return formattedMinutes + ":" + formattedSeconds;

    }
}

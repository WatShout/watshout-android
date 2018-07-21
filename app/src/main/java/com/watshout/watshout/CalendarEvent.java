package com.watshout.tracker;

import com.applandeo.materialcalendarview.EventDay;

import java.util.Calendar;

class CalendarEvent extends EventDay {

    private String mNote;

    CalendarEvent(Calendar day, int imageResource, String note) {
        super(day, imageResource);
        mNote = note;
    }
    String getNote() {
        return mNote;
    }

}
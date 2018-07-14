package com.watshout.tracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class CalendarFragment extends android.app.Fragment {

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String email = thisUser.getEmail();
    String uid = thisUser.getUid();

    private com.applandeo.materialcalendarview.CalendarView mCalendarView;

    private ArrayList<CalendarItem> listItems;

    private ArrayList<String> roundedDates;
    private HashMap<String, ArrayList> allEventInfo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mCalendarView = view.findViewById(R.id.calendarView);

        mCalendarView.setOnDayClickListener(listener);

        getCalendarData();

    }


    private com.applandeo.materialcalendarview.listeners.OnDayClickListener listener = new OnDayClickListener() {
        @Override
        public void onDayClick(EventDay eventDay) {

            String selectedDate = eventDay.getCalendar().getTime().toString();

            try {
                Log.d("CALENDAR", allEventInfo.get(selectedDate).toString());
            } catch (NullPointerException e){}
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    public void getCalendarData() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url ="https://watshout.herokuapp.com/maps/calendar/download/" + uid + "/";

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading data...");
        progressDialog.show();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressDialog.dismiss();

                        listItems = new ArrayList<>();
                        allEventInfo = new HashMap<>();
                        roundedDates = new ArrayList<>();
                        List<EventDay> events = new ArrayList<>();

                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("activities");

                            if (!response.equals("{\"activities\": []}")) {
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject o = array.getJSONObject(i);

                                    listItems.add(new CalendarItem(
                                            o.getString("image"),
                                            o.getString("time")
                                    ));

                                }
                            }

                            for (CalendarItem item : listItems){

                                Timestamp t = new Timestamp(Long.valueOf(item.getTime())); // replace with existing timestamp
                                Date d = new Date(t.getTime());

                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(d);

                                EventDay current = new EventDay(calendar, R.drawable.current);
                                String roundedDate = current.getCalendar().getTime().toString();

                                if(!roundedDates.contains(roundedDate)){
                                    roundedDates.add(roundedDate);
                                }

                                HashMap<String, HashMap> individualItem = new HashMap<>();
                                HashMap<String, String> individualItemInfo = new HashMap<>();

                                individualItemInfo.put("time", item.getTime());
                                individualItemInfo.put("link", item.getImageURL());

                                individualItem.put(item.getTime(), individualItemInfo);

                                if (allEventInfo.get(roundedDate) == null) {

                                    ArrayList<HashMap> activityList = new ArrayList<>();
                                    activityList.add(individualItem);

                                    allEventInfo.put(roundedDate, activityList);

                                } else {

                                    allEventInfo.get(roundedDate).add(individualItem);

                                }

                                events.add(current);

                            }

                            mCalendarView.setEvents(events);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR", error.toString());
            }

        });

        queue.add(stringRequest);

    }

}

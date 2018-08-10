package com.watshout.watshout;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


public class CalendarFragment extends android.app.Fragment {

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String name = thisUser.getDisplayName();
    String email = thisUser.getEmail();
    String uid = thisUser.getUid();

    private com.applandeo.materialcalendarview.CalendarView mCalendarView;

    private ArrayList<NewsFeedItem> listItems;

    private ArrayList<String> roundedDates;
    private HashMap<String, ArrayList> allEventInfo;

    private RecyclerView mRecycleView;
    private RecyclerView.Adapter adapter;

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

        getActivity().setTitle("calendar");

        mCalendarView = view.findViewById(R.id.calendarView);
        mCalendarView.setOnDayClickListener(listener);

        getCalendarData();

    }

    private com.applandeo.materialcalendarview.listeners.OnDayClickListener listener = new OnDayClickListener() {
        @Override
        public void onDayClick(EventDay eventDay) {

            String selectedDate = eventDay.getCalendar().getTime().toString();

            try {

                ArrayList<HashMap> currentlySelected = allEventInfo.get(selectedDate);

                ArrayList<NewsFeedItem> listItems = new ArrayList<>();

                for (HashMap hashMap : currentlySelected) {

                    String key = (String) hashMap.keySet().toArray()[0];

                    HashMap<String, String> individual = (HashMap) hashMap.get(key);
                    NewsFeedItem current = new NewsFeedItem(name, individual.get("link"), individual.get("time"),
                            individual.get("event_name"), individual.get("distance"), individual.get("time_elapsed"));
                    listItems.add(current);

                }

                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_calendar, null);
                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it

                mRecycleView = popupView.findViewById(R.id.calendarRecycle);
                mRecycleView.setHasFixedSize(true);
                mRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));

                adapter = new NewsFeedAdapter(listItems, getActivity());
                mRecycleView.setAdapter(adapter);

                PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.setAnimationStyle(R.style.popup_window_animation);
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);


            } catch (NullPointerException e){

                //Toast.makeText(getActivity(), "Error with retrieving events", Toast.LENGTH_SHORT).show();

            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    public void getCalendarData() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = EndpointURL.getInstance().getHistoryURL(uid);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading data...");
        progressDialog.show();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        listItems = new ArrayList<>();
                        allEventInfo = new HashMap<>();
                        roundedDates = new ArrayList<>();
                        List<EventDay> events = new ArrayList<>();

                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("activities");

                            Log.d("CALENDAR", array.toString());

                            if (!response.equals("{\"activities\": []}")) {
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject o = array.getJSONObject(i);

                                    listItems.add(new NewsFeedItem(
                                            "self",
                                            o.getString("image"),
                                            o.getString("time"),
                                            o.getString("event_name"),
                                            o.getString("distance"),
                                            o.getString("time_elapsed")
                                    ));

                                }
                            }

                            for (NewsFeedItem item : listItems){

                                Timestamp t = new Timestamp(Long.valueOf(item.getTime())); // replace with existing timestamp
                                Date d = new Date(t.getTime());

                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(d);

                                EventDay current = new EventDay(calendar, R.drawable.running_black);
                                String roundedDate = current.getCalendar().getTime().toString();

                                if(!roundedDates.contains(roundedDate)){
                                    roundedDates.add(roundedDate);
                                }

                                HashMap<String, HashMap> individualItem = new HashMap<>();
                                HashMap<String, String> individualItemInfo = new HashMap<>();

                                individualItemInfo.put("time", item.getTime());
                                individualItemInfo.put("link", item.getImageURL());
                                individualItemInfo.put("event_name", item.getActivityName());
                                individualItemInfo.put("distance", item.getDistance());
                                individualItemInfo.put("time_elapsed", item.getTimeElapsed());

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
                            progressDialog.dismiss();

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


        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(stringRequest);

    }



}

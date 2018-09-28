package com.watshout.watshout;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RunCompletionUploader {

    private final static String TAG = "RunCompletionUploader";

    private Context context;
    private String uid;
    private double distance;
    private String pace;
    private int totalSeconds;
    private String mapURL;
    private String date;
    private Long uploadTime;

    public RunCompletionUploader(Context context, String uid, double distance,
                                 String pace, int totalSeconds,
                                 String mapURL) {
        this.context = context;
        this.uid = uid;
        this.distance = distance;
        this.pace = pace;
        this.totalSeconds = totalSeconds;
        this.mapURL = mapURL;
        this.date = createFormattedDate();
        this.uploadTime = System.currentTimeMillis();
    }

    public void createActivityOnServer() {

        RequestQueue queue = Volley.newRequestQueue(context);
        String addActivityURL = EndpointURL.getInstance().addActivityURL();

        StringRequest createMapRequest = new StringRequest(Request.Method.POST,
                addActivityURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, "Activity uploaded successfully!");
                Toast.makeText(context, "Activity uploaded successfully!", Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Activity upload failed");
                Log.e(TAG, error.toString());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("uid", uid);
                params.put("time_stamp", date);
                params.put("time_elapsed", totalSeconds + "");
                params.put("pace", pace);
                params.put("map_url", mapURL);
                params.put("distance", distance + "");
                params.put("time", uploadTime + "");

                return params;
            }
        };

        createMapRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(createMapRequest);
    }

    // Get date in format 'tue-may-29-04-58-14-gmt-00-00-2018'
    private String createFormattedDate() {

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String interDate = date.toString();
        String fullDate = interDate.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();

        return fullDate;

    }



}

package com.watshout.face;

import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// This class deals with POSTing the data to Firebase
class PostData extends AsyncTask<String, Void, Void> {


    // Init. client and data type
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

    protected void onPreExecute() {
        //display progress dialog.

    }

    protected Void doInBackground(String... strings) {

        // Gets the json string from the parameters
        String jsonData = strings[0];
        String id = strings[1];

        String url = "https://gps-app-c31df.firebaseio.com/" + id + ".json";

        // Builds a request then POSTs to Firebase
        RequestBody body = RequestBody.create(JSON, jsonData);
        Request request = new Request.Builder()
                .url(url)
                .post(body)  // Changing this from put to post changes behavior
                .build();

        try {

            // Should find out what to do with response
            Response response = client.newCall(request).execute();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(Void result) {

    }

}




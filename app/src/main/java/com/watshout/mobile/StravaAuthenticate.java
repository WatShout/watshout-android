package com.watshout.mobile;

import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.samsandberg.stravaauthenticator.StravaAuthenticateActivity;
import com.samsandberg.stravaauthenticator.StravaScopes;

import java.util.Arrays;
import java.util.Collection;

public class StravaAuthenticate extends StravaAuthenticateActivity {

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String uid = thisUser.getUid();

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    /*****************************************
     * Methods override START
     */

    /**
     * Client ID
     */
    protected String getStravaClientId() {
        return "26116";
    }

    /**
     * Client Secret
     */
    protected String getStravaClientSecret() {
        return "04ba9a4ac548cdc94c375baf65ceb95eca3af533";
    }

    /**
     * Scopes to auth for
     * (default public)
     */
    protected Collection<String> getStravaScopes() {
        return Arrays.asList(StravaScopes.SCOPE_PUBLIC);
    }

    /**
     * Should we use the local cache?
     * (default true)
     */
    protected boolean getStravaUseCache() {
        return true;
    }

    /**
     * Should we check a token (against Strava's API) or should we just assume it's good?
     * (default true)
     */
    protected boolean getStravaCheckToken() {
        return true;
    }

    /**
     * What intent should we kick off, given OK auth
     */
    protected Intent getStravaActivityIntent() {
        String token = StravaAuthenticateActivity.getStravaAccessToken(this);
        ref.child("users").child(uid).child("strava_token").setValue(token);
        return new Intent(this, MainActivity.class);
    }

    /**
     * Should we finish this activity after successful auth + kicking off next activity?
     * (default true)
     */
    protected boolean getStravaFinishOnComplete() {
        return true;
    }

    /**
     * Methods override END
     ****************************************/
}
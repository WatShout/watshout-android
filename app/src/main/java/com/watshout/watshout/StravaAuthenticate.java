package com.watshout.watshout;

import android.content.Intent;
import android.util.Log;

import com.samsandberg.stravaauthenticator.StravaAuthenticateActivity;
import com.samsandberg.stravaauthenticator.StravaScopes;

import java.util.Arrays;
import java.util.Collection;


public class StravaAuthenticate extends StravaAuthenticateActivity {

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

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("STRAVA_TOKEN", token);

        return intent;
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
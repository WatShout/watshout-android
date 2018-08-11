package com.watshout.watshout;

import com.watshout.watshout.pojo.FriendRequestList;
import com.watshout.watshout.pojo.FriendsList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RetrofitInterface {

    @GET("/api/friends/{uid}/")
    Call<FriendsList> getFriendsList(
            @Path("uid") String token
    );

    @GET("/api/friendrequests/{uid}/")
    Call<FriendRequestList> getFriendRequestList(
            @Path("uid") String token
    );


}

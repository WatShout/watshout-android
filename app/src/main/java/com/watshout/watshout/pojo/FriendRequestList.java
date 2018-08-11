
package com.watshout.watshout.pojo;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FriendRequestList {

    @SerializedName("friend_requests")
    @Expose
    private List<FriendRequest> friendRequests = null;

    public List<FriendRequest> getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(List<FriendRequest> friendRequests) {
        this.friendRequests = friendRequests;
    }

}

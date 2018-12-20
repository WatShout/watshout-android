
package com.watshout.mobile.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Friend extends FriendObject implements Comparable<Friend>{


    @SerializedName("since")
    @Expose
    private Long since;


    public Long getSince() {
        return since;
    }

    public void setSince(Long since) {
        this.since = since;
    }

    @Override
    public int compareTo(Friend otherFriend) {

        int compare = this.getName().compareTo(otherFriend.getName());

        if (compare < 0) {
            return -1;
        }
        else if (compare > 0) {
            return 1;
        }
        else {
            return 0;
        }

    }

}

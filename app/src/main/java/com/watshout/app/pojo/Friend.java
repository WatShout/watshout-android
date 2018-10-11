
package com.watshout.app.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Friend implements Comparable<Friend>{

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("profile_pic")
    @Expose
    private String profilePic;
    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("since")
    @Expose
    private Long since;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

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

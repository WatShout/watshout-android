package com.watshout.watshout;

import android.support.annotation.NonNull;

public class FriendItem implements Comparable<FriendItem>{

    private String name;
    private String profilePic;
    private String uid;
    private long since;

    FriendItem (String name, String uid, String profilePic, long since) {
        this.name = name;
        this.profilePic = profilePic;
        this.uid = uid;
        this.since = since;

    }

    public String getName(){
        return name;
    }

    public String getUID() {
        return uid;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public long getSince() {return since;}

    @Override
    public int compareTo(FriendItem otherFriend) {

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

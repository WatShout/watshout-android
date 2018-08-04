package com.watshout.watshout;

import android.support.annotation.NonNull;

public class FriendItem implements Comparable<FriendItem>{

    private String name;
    private String profilePic;
    private String uid;

    FriendItem (String name, String uid, String profilePic) {
        this.name = name;
        this.profilePic = profilePic;
        this.uid = uid;

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

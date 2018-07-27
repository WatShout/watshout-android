package com.watshout.watshout;

public class FriendItem {

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

}

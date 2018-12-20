package com.watshout.mobile;

import java.util.HashMap;

public class CurrentlyTrackingFriendsHolder {

    private HashMap<String, FriendCurrentLocation> friendsCurrentlyOnMap;

    CurrentlyTrackingFriendsHolder() {
        this.friendsCurrentlyOnMap = new HashMap<>();
    }

    public HashMap<String, FriendCurrentLocation> getCurrent() {
        return friendsCurrentlyOnMap;
    }

    public void update(String uid, FriendCurrentLocation currentLocation){
        this.friendsCurrentlyOnMap.put(uid, currentLocation);
    }

    public void remove(String uid){

        if (friendsCurrentlyOnMap.get(uid) != null){
            friendsCurrentlyOnMap.remove(uid);
        }

    }


}

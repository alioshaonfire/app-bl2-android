package com.buscalibre.app2.models;

import io.realm.Realm;
import io.realm.RealmObject;

public class UserCountry extends RealmObject {

    private String userEmail;
    private String countryID;

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getCountryID() {
        return countryID;
    }

    public void setCountryID(String countryID) {
        this.countryID = countryID;
    }

    public static void create(Realm realm, String countryID, String email){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                UserCountry userCountry = realm.createObject(UserCountry.class);
                userCountry.setCountryID(countryID);
                userCountry.setUserEmail(email);
            }
        });
    }
}

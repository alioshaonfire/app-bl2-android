package com.buscalibre.app2.network;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.buscalibre.app2.R;
import com.buscalibre.app2.activities.WelcomeUserActivity;
import com.buscalibre.app2.models.Country;
import com.buscalibre.app2.models.UserLogin;
import com.facebook.login.LoginManager;

import io.realm.Realm;
import io.realm.RealmResults;


public class NetworkToken {

    public static void refresh(Context context){
        UserLogin.deleteAll(Realm.getDefaultInstance());
        LoginManager.getInstance().logOut();
        RealmResults<Country> countries = Realm.getDefaultInstance().where(Country.class).findAll();
        for (Country country : countries){
            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    country.setSelected(false);
                }
            });
        }
        Intent intent = new Intent(context, WelcomeUserActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        Toast.makeText(context, R.string.text31, Toast.LENGTH_LONG).show();
    }
}

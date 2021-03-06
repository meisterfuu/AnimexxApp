package de.meisterfuu.animexx.api.broker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.meisterfuu.animexx.KEYS;
import de.meisterfuu.animexx.api.web.ReturnObject;
import de.meisterfuu.animexx.objects.SingleValueObjects;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class GCMBroker extends BasicWebBroker {


    ArrayList<String> EventCodes = new ArrayList<>();

    String[] ev1 = new String[]{"XXEventENS", "XXEventGeburtstag", "XXEventGaestebuch"};
    String[] ev2 = new String[]{"XXEventVerrealkontaktet", "XXEventUsernameChange", "XXEventTwitterAnbindung", "XXEventSelbstbeschreibungErwaehnung", "XXEventEventsDabei"};
    String[] ev3 = new String[]{"XXEventSelbstbeschreibungAenderung", "XXEventSteckbriefVideoNeu", "XXEventMagDoujinshi", "XXEventMagWeblogeintrag", "XXEventMagEvent", "XXEventMagFanart", "XXEventMagBastelei", "XXEventMagFanfic", "XXEventMagFoto", "XXEventMagFotoreihe", "XXEventMagOekaki", "XXEventMagNews", "XXEventMagWettbewerb", "XXEventMagManga", "XXEventMagDVD", "XXEventMagArtbook", "XXEventMagCD", "XXEventMagGame", "XXEventMagAsia", "XXEventMagUmfrage", "XXEventMagCosplay", "XXEventMagJFashion", "XXEventMagForumThread", "XXEventMagForumPosting", "XXEventMagZirkelForumThread", "XXEventMagZirkelForumPosting", "XXEventMagEventVideo", "XXEventMagFanVideo", "XXEventMagWebshop", "XXEventMagAudiobook"};
    String[] ev4 = new String[]{"XXEventRPGAdminUebergabe", "XXEventRPGBewerbungAngenommen", "XXEventRPGBewerbungAbgelehnt", "XXEventRPGBewerbung", "XXEventRPGAbmeldung", "XXEventRPGPosting"};


    //Init

    public GCMBroker(Context pContext) {
        super(pContext);

        EventCodes.addAll(Arrays.asList(ev1));
//        EventCodes.addAll(Arrays.asList(ev2));
//        EventCodes.addAll(Arrays.asList(ev3));
        EventCodes.addAll(Arrays.asList(ev4));
    }


    //Public Methods

    /**
     * @param pCallback
     */
    public void activateGCMEvents(final Callback<ReturnObject<Integer>> pCallback) {
        getWebApi().refresh(this.getContext());
        getWebApi().getApi().setGCMEvents(EventCodes, pCallback);
    }

    public void activateGCMEvents() throws RetrofitError {
        getWebApi().refresh(this.getContext());
        getWebApi().getApi().setGCMEvents(EventCodes);
    }


    public boolean registerGCM() {
        getWebApi().refresh(this.getContext());
//                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getContext());
//                String reg_id = gcm.register(KEYS.GCM_SENDER_ID);
        try {
            InstanceID iid = InstanceID.getInstance(getContext());
            String token = iid.getToken(KEYS.GCM_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            saveGCM(token);
            sentGCMID(token);
            Log.e("Animexx GCM", "Registered gcm token on server");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param context
     */
    public String getRegistrationId(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String registrationId = prefs.getString("gcm_reg_id", "");

        Log.e("Animexx GCM", "getRegistrationId()");
        Log.e("Animexx GCM", "GCM ID: " + registrationId);
        if (registrationId.isEmpty()) {
            Log.e("Animexx GCM", "EMTPY ID");
            return null;
        }

        int registeredVersion = prefs.getInt("gcm_app_ver", Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.e("Animexx GCM", "WRONG VERSION - Ignore to fix multiple gcm IDs?");
//            return null;
        }
        return registrationId;
    }

    /**
     * @param hash
     */
    public boolean compareRegistrationIdHash(String hash) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String registrationId = prefs.getString("gcm_reg_id", "");

//        String registrationIdHash = DigestUtils.md5Hex(registrationId);
//        String registrationIdHash = new String(Hex.encodeHex(DigestUtils.md5(registrationId)));
          String registrationIdHash =  registrationId.substring(6);

        if (registrationIdHash.startsWith(hash)) {
            Log.e("Animexx GCM", "VALID GCM MESSAGE");
            return true;
        } else {
            Log.e("Animexx GCM", hash + "  " + registrationIdHash);
        }
        return false;
    }

    public void sentGCMID(String id) throws RetrofitError {
        getWebApi().getApi().setGCMId(id, 0);
    }


    //Persist Config

    private void saveGCM(String reg_id) {
        Log.e("Animexx GCM", "Saving GCM ID: " + reg_id.hashCode());
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("gcm_reg_id", reg_id).commit();
        int appVersion = getAppVersion(getContext());
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("gcm_app_ver", appVersion).commit();
    }

    private String getGCM() {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getString("gcm_reg_id", null);
    }


    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    public SingleValueObjects.GCMIdObject getActiveIds() {
        return this.getWebApi().getApi().getGCMId().getObj();
    }

    public void deleteID(List<String> list) {
        this.getWebApi().getApi().deleteGCMId(list, new Callback<ReturnObject<SingleValueObjects.Empty>>() {
            @Override
            public void success(ReturnObject<SingleValueObjects.Empty> emptyReturnObject, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
}


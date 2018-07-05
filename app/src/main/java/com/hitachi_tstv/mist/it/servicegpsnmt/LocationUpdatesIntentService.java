package com.hitachi_tstv.mist.it.servicegpsnmt;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.util.List;

/**
 * Created by Tunyaporn on 10/4/2017.
 */

public class LocationUpdatesIntentService extends IntentService {

    private static final String ACTION_PROCESS_UPDATES =
            "com.hitachi_tstv.mist.it.servicegpsval.action" +
                    ".PROCESS_UPDATES";
    private static final String TAG = LocationUpdatesIntentService.class.getSimpleName();


    public LocationUpdatesIntentService() {

        // Name the worker thread.
        super(TAG);
        Log.d(TAG, "a");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "b");
        if (intent != null) {
            Log.d(TAG, "b1");
            final String action = intent.getAction();
            Log.d(TAG, intent.getAction().toString());
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                Log.d(TAG, "b2");
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    Log.d(TAG, "b3");
                    List<Location> locations = result.getLocations();
                    Utils.setLocationUpdatesResult(this, locations);
                    Utils.sendNotification(this, Utils.getLocationResultTitle(this, locations));
                    Log.i(TAG, Utils.getLocationUpdatesResult(this));
                } else {

                    List<Location> locations = result.getLocations();
                    Utils.setLocationUpdatesResult(this, locations);
                    Utils.sendNotification(this, Utils.getLocationResultTitle(this, locations));
                }
            }
        }
    }
}

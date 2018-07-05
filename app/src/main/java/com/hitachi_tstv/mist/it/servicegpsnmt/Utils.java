package com.hitachi_tstv.mist.it.servicegpsnmt;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Tunyaporn on 10/4/2017.
 */

public class Utils {
    final static String KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    //final static String url = "http://203.154.103.43/mmth/app/CenterService/updateRecNow.php";
    final static String url = "http://203.154.103.39/nmt/app/CenterService/updateRecNow.php";
    final static String TAG = "Utils";

    Context context;

    public Utils(Context context) {
        this.context = context;
    }

    static void setRequestingLocationUpdates(Context context, boolean value) {

        Log.d(TAG, "a");
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    static boolean getRequestingLocationUpdates(Context context) {

        Log.d(TAG, "b");
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false);
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    static void sendNotification(Context context, String notificationDetails) {

        Log.d(TAG, "c");
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.putExtra("from_notification", true);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle("Location update")
                .setContentText(notificationDetails)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    String getDeviceID(){
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    String getSerial() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            serial = (String) get.invoke(c, "ril.serialnumber", "unknown");
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return serial;
    }

    String getDeviceName(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.getName();
    }


    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     *
     * @param context The {@link Context}.
     */
    static String getLocationResultTitle(Context context, List<Location> locations) {

        Log.d(TAG, "d");
        String numLocationsReported = context.getResources().getQuantityString(
                R.plurals.num_locations_reported, locations.size(), locations.size());
        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
    }

    /**
     * Returns te text for reporting about a list of  {@link Location} objects.
     *
     * @param locations List of {@link Location}s.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String getLocationResultText(Context context, List<Location> locations) {

        Log.d(TAG, "e");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault());
        Date date = new Date();
        String lat = "";
        String lng = "";
        String time = dateFormat.format(date);
        Utils utils = new Utils(context);
        if (locations.isEmpty()) {

            Log.d(TAG, "Send result ==> " + lat + " " + lng + " " + time +" "+ utils.getDeviceID() +" "+ utils.getDeviceName() +" "+ utils.getSerial());

            SynSendGPS synSendGPS = new SynSendGPS(context, lat, lng, time,utils.getDeviceID(),utils.getDeviceName(),utils.getSerial());
            synSendGPS.execute();
            return context.getString(R.string.unknown_location);
        }
        StringBuilder sb = new StringBuilder();
        for (Location location : locations) {
            sb.append("(");
            sb.append(location.getLatitude());
            sb.append(", ");
            sb.append(location.getLongitude());
            sb.append(")");
            sb.append("\n");
            lat = String.valueOf(location.getLatitude());
            lng = String.valueOf(location.getLongitude());
        }
        Log.d(TAG, "Send result ==> " + lat + " " + lng + " " + time +" "+ utils.getDeviceID() +" "+ utils.getDeviceName() +" "+ utils.getSerial());

        SynSendGPS synSendGPS = new SynSendGPS(context, lat, lng, time,utils.getDeviceID(),utils.getDeviceName(),utils.getSerial());
        synSendGPS.execute();
        return sb.toString();
    }

    private static class SynSendGPS extends AsyncTask<Void, Void, String> {
        Context context;
        String latString, longString, timeString,idString,nameString,serialString;

        public SynSendGPS(Context context, String latString, String longString, String timeString, String idString, String nameString, String serialString) {

            Log.d(TAG, "f");
            this.context = context;
            this.latString = latString;
            this.longString = longString;
            this.timeString = timeString;
            this.idString = idString;
            this.nameString = nameString;
            this.serialString = serialString;
        }

        @Override
        protected String doInBackground(Void... voids) {

            Log.d(TAG, "g");
            try{
                Log.d(TAG, "Send ==> " + latString + " " + longString + " " + timeString + " " + idString + " " + nameString + " " + serialString);
                OkHttpClient okHttpClient = new OkHttpClient();
                RequestBody requestBody = new FormEncodingBuilder()
                        .add("isAdd", "true")
                        .add("gps_lat", latString)
                        .add("gps_lon", longString)
                        .add("gps_timeStamp",timeString)
                        .add("device_id", idString)
                        .add("device_name", nameString)
                        .add("device_serial",serialString)
                        .build();
                Request.Builder builder = new Request.Builder();
                Request request = builder.post(requestBody).url(url).build();
                Response response = okHttpClient.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "s " + s);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    static void setLocationUpdatesResult(Context context, List<Location> locations) {

        Log.d(TAG, "h");
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle(context, locations)
                        + "\n" + getLocationResultText(context, locations))
                .apply();
    }

    static String getLocationUpdatesResult(Context context) {

        Log.d(TAG, "i");
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }
}
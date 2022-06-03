package com.example.walter.nursecaller_final;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.walter.nursecaller_final.Constants.URL_Notification;

public class MainActivity extends AppCompatActivity {
    RecyclerView dataList; //a variable used to store values that display on Grid View
    private List<String> notifications;// a list of rooms that require nurse
    private RecyclerView.LayoutManager manager;//that displays Card Views
    private MediaPlayer mediaPlayer=null;//class we can easily fetch, decode and play both audio
    private Vibrator vibrator=null;// deals with the vibrating machine present in your device and will make it vibrate for the given duratio
    boolean windowFocus =false;//check the user is o window focus (directly open and see the app screen or not)
    androidx.appcompat.app.ActionBar actionBar;
    LayoutInflater inflater;
    GridAutofitLayoutManager layoutManager;//Card Views to rearrange according to the screen size
    WifiManager wifi;//This class provides the primary API for managing all aspects of Wi-Fi connectivity.

    Boolean notificationIsClicked;// a variable to check weather notification clicked or not
    boolean wifi_connected;// a variable that chech wifi is connected or not
    int networkId; // a variable that Add a new network description to the set of configured networks
    Button helpbtn;// button that display help screen
    long[] pattern = {0, 1000, 1000};//vibrate 1000 milliseconds and sleep 1000 milliseconds
    Adapter adapter=null; // a bridge between UI component and data source
    Animation animation; //used to animate the help button regularly with in fixed time of interval
    private final static String default_notification_channel_id = "default" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Private Wifi(Network)

        String ssid = "Titi";
        String password = "12345678";
        /////////////////////////////////////////////////////////
        connectWifi(ssid,password); // Method that Call Connect to the Local Network
        notifications=new ArrayList<>();//declear the array list that contian a list of notifications as an array
        vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        mediaPlayer= MediaPlayer.create(getApplicationContext(), R.raw.ring_call);
        notificationIsClicked=false;
        setTheme(R.style.Theme_Caller);
        setContentView(R.layout.activity_main);
        //////////////////////////////////////////////////////////
        actionBar=getSupportActionBar();
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        //////////////////////////////////////////////////////////
        actionBar.setDisplayShowCustomEnabled(true);
        inflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //////////////////////////////////////////////////////////

        dataList=findViewById(R.id.dataList);
        helpbtn=(Button)findViewById(R.id.helpButton);
        //check if there is a notification pop when user out of focus on the app screen
        //if the pop notification is clicked only play one song(notification sound)
        if (getIntent().hasExtra("fromNotification")) {
            notificationIsClicked=true;
        }
        else{
            //mediaPlayer= MediaPlayer.create(getApplicationContext(), R.raw.ring_call);
        }
        //////////////////////////////////////////////
// a method call refresh method within 2 second to get new update
        refresh(2000);


    }
    // a method that check the user is o window focus (directly open and see the app
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus)
            windowFocus=true;
        else
            windowFocus=false;
        super.onWindowFocusChanged(hasFocus);


    }
    //check connected wifi is the same with configered one
    public static String getCurrentSsid(Context context) {
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }
        return ssid;
    }
    private void connectWifi(String ssid, String password) {
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            wifi.setWifiEnabled(true);
            //To open WIFI panel because cannot open wifi automatically for android build version 10 and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                startActivityForResult(panelIntent, 545);
            }
        }
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + ssid + "\"";
        conf.preSharedKey = "\"" + password + "\"";
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        conf.status = WifiConfiguration.Status.ENABLED;
        networkId = wifi.addNetwork(conf);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            // Do whatever

            //check connected wifi is the same with configered one
            String wifi_name=(getCurrentSsid(getApplicationContext()));

            String connected_wifiName = wifi_name.replaceAll("^\"|\"$", "");


            //check connected wifi is the same with configered one
            if(!connected_wifiName.equals(ssid)){
                wifi.disconnect();
                wifi.enableNetwork(networkId, true);
                wifi_connected = wifi.reconnect();
                if (!wifi_connected) {
                    wifi.disconnect();
                    wifi.enableNetwork(networkId, true);
                    wifi.reconnect();
                }
            }}


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_list,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.exit_menu:
                moveTaskToBack(true);
                Process.killProcess(Process.myPid());
                System.exit(1);
        }

        return super.onOptionsItemSelected(item);
    }


    private void refresh(int milisecond) {

        Handler handler=new Handler();
        final Runnable runnable= () -> {
            retrieveNotification();
        };
        handler.postDelayed(runnable,milisecond);
    }

// a method used to call the data from the database


    private void retrieveNotification() {
        //////////////////////////////////////////////////////////////////////

        vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        mediaPlayer= MediaPlayer.create(getBaseContext(), R.raw.ring_call);

        //////////////////////////////////////////////////////////////////////
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_Notification,
                response -> {
                    ArrayList<String> ar = new ArrayList<String>();
                    String room_num=null;

                    NotificationCompat.InboxStyle inboxStyle=new NotificationCompat.InboxStyle();
                    inboxStyle.setBigContentTitle("Room Number Notifications");
                    try {

                        JSONObject jsonObject = new JSONObject(response);
                        String sucsses = jsonObject.getString("success");
                        JSONArray jsonArray = jsonObject.getJSONArray("notification");
                        notifications.removeAll(notifications);
                        if (sucsses.equals("1")) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);

                                room_num = object.getString("room_number");
                                notifications.add(room_num);
////////////////////////////////////////////////////////////////////////
                                ar.add(room_num);
                                if(i!=jsonArray.length()-1)
                                    ar.add(" , ");
                                inboxStyle.addLine(room_num);
////////////////////////////////////////////////////////////////////////
                            }



                            //Concatenate all recived room list to one String form
                            StringBuffer sb = new StringBuffer();
                            for(int i = 0; i < ar.size(); i++) {
                                sb.append(ar.get(i));
                            }
                            String str = sb.toString();

//                        //Explicit intent to open app an Activity in your app
                            Intent notificationIntent=new Intent(this,MainActivity.class);

                            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
                            notificationIntent.putExtra("fromNotification", true);

                            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                            NotificationManager mNotificationManager = (NotificationManager) getSystemService( NOTIFICATION_SERVICE ) ;
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity. this, default_notification_channel_id ) ;
                         //   Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.gono_logo);
                            mBuilder.setLargeIcon(BitmapFactory. decodeResource (getResources() , R.drawable.cirle_image )) ;
                            // Invoking default notification Service
                           mBuilder.setSmallIcon(R.drawable.cirle_image);
                            mBuilder.setContentTitle("Nurse Caller");
                            mBuilder.setContentText(str);
                            mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
                            mBuilder.addAction(R.drawable.notification_background,"Open",contentIntent);

                            mBuilder.setColor(Color.parseColor("#FFFFFF"));
                            mBuilder.setStyle(inboxStyle);
                            mBuilder.setContentIntent(contentIntent);
                            mBuilder.setAutoCancel(true);





                            //Check if there is room number that need a nurse or not
                            if (room_num!=null) {

//Notification allows you to update the notification Later on
//Display Notification not display when the Activity is Open
                                if(windowFocus==false) {


                                    // ===Notification For Android Oreo (8.0) and above

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                    {
                                        String channelId = "Your_channel_id";
                                        NotificationChannel channel = new NotificationChannel(
                                                channelId,
                                                "Channel human readable title",
                                                NotificationManager.IMPORTANCE_HIGH);
                                        mNotificationManager.createNotificationChannel(channel);
                                        mBuilder.setChannelId(channelId);

                                    }

                                    mNotificationManager.notify(0, mBuilder.build());
//                                        mediaPlayer.start();
//                                        vibrator.vibrate(pattern, 0);
                                }

                                if (mediaPlayer == null || mediaPlayer.isPlaying()) {

                                }

                                else {
                                    if (notificationIsClicked==false)
                                    {
                                        mediaPlayer.start();
                                        vibrator.vibrate(pattern, 0);


                                    }

                                }

                                //  notificationIsClicked=false;
                                helpbtn.setVisibility(View.VISIBLE);
                                animation= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.blink);
                                helpbtn.startAnimation(animation);
                            }
                            else
                            {

                                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                    mediaPlayer.stop();
                                    mediaPlayer.release();
                                    mediaPlayer=null;

                                }
                                helpbtn.clearAnimation();
                                helpbtn.setVisibility(View.INVISIBLE);
                                vibrator.cancel();
                            }
                            adapter=new Adapter(MainActivity.this,notifications);
                            layoutManager=new GridAutofitLayoutManager(this,133,1,false);

                            dataList.setLayoutManager(layoutManager);
                            dataList.setAdapter(adapter);
                        }
                    }    catch (JSONException e) {
                        e.printStackTrace();
                    }

                },

                error -> {
                    dataList.setAdapter(null);
                    vibrator.cancel();

                    helpbtn.setVisibility(View.INVISIBLE);
                    wifi.enableNetwork(networkId, true);
                    wifi.reconnect();
                });
        //Check the State of app it is either online or online
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            View view=inflater.inflate(R.layout.custom_change_online,null);
            actionBar.setCustomView(view);

        }
        if (!mWifi.isConnected()) {
            helpbtn.clearAnimation();
            helpbtn.setVisibility(View.INVISIBLE);
            View view=inflater.inflate(R.layout.custom_change_offline,null);
            actionBar.setCustomView(view);
//
        }
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
// recall refresh method to get the updated info from the database

        refresh(2000);

    }

}
// Copyright (c) 2013 Intel Corporation. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.xwalk.app.template;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import android.app.Activity;
import java.util.Observable;
import java.util.Observer;

import org.xwalk.app.XWalkRuntimeActivityBase;

import com.autoupdateapk.AutoUpdateApk;
import com.autoupdateapk.SilentAutoUpdate;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;

import android.view.WindowManager;
import android.os.Handler;
import android.provider.Settings;

import android.app.AlarmManager;
import java.util.Calendar;
import java.util.Date;
import android.app.PendingIntent;
import android.app.IntentService;
import android.os.SystemClock;
import android.os.PowerManager;


public class AppTemplateActivity extends XWalkRuntimeActivityBase implements Observer {

    static final String TAG = "ORGANISM";

    static String apk_updater_server = "http://192.168.1.68";
    static String apk_updater_server_port = "8088";
    static String apk_updater_path = "/organism";
    static boolean apk_update_on_boot = true;
    static long apk_update_interval = 3 * AutoUpdateApk.MINUTES;
    static String str_apk_update_interval = "3:M";

    static String str_wakeup_rtc_time = "8:00";
    static String str_sleep_rtc_time = "19:00";

    static int wakeup_rtc_time_hours;
    static int wakeup_rtc_time_minutes;

    static int sleep_rtc_time_hours;
    static int sleep_rtc_time_minutes;    

    private static AlarmManager alarmMgr;
    private static PendingIntent alarmIntent_Wakeup;
    private static PendingIntent alarmIntent_Sleep;

    public final static String SLEEP_SIG = "SLEEP_SIG";
    public final static String WAKEUP_SIG = "WAKEUP_SIG";    

    private static PowerManager pm;


    @SuppressWarnings("unused")
    private SilentAutoUpdate sau;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // fullscreen immersive mode
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION 
            | View.SYSTEM_UI_FLAG_FULLSCREEN 
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // super user
        try {
            Process p= Runtime.getRuntime().exec("su");
        }
        catch (IOException e) {
            e.printStackTrace();
        }        

        JSONObject obj = UCCIO.read(UCCIO.ConfigFile.SYSTEM);
        if(obj != null) {
            
            try {

                 apk_updater_server = obj.getString("apk_updater_server");
                 apk_updater_server_port = obj.getString("apk_updater_server_port");
                 apk_updater_path = obj.getString("apk_updater_path");  
                 apk_update_on_boot = obj.getBoolean("apk_update_on_boot");  
                 str_apk_update_interval = obj.getString("apk_update_interval");

                 String[] str_a_apk_update_interval = str_apk_update_interval.split(":");
                 if(str_a_apk_update_interval != null && str_a_apk_update_interval.length == 2) {
                    int i = Integer.parseInt(str_a_apk_update_interval[0]);
                    String k = str_a_apk_update_interval[1];

                    Log.d(TAG, "apk update interval: " + i + ":" + k);

                    if(k.equalsIgnoreCase("M"))
                        apk_update_interval = i * AutoUpdateApk.MINUTES;
                    else if(k.equalsIgnoreCase("H"))
                        apk_update_interval = i * AutoUpdateApk.HOURS;
                    else if(k.equalsIgnoreCase("D"))
                        apk_update_interval = i * AutoUpdateApk.DAYS;                    
                 }

             } catch (JSONException e) {
                 e.printStackTrace();
             }

             try {
                str_wakeup_rtc_time = obj.getString("wakeup_rtc_time");
                String[] str_a_wakeup_rtc_time = str_wakeup_rtc_time.split(":");
                if(str_a_wakeup_rtc_time != null && str_a_wakeup_rtc_time.length == 2) {
                    wakeup_rtc_time_hours = Integer.parseInt(str_a_wakeup_rtc_time[0]);
                    wakeup_rtc_time_minutes = Integer.parseInt(str_a_wakeup_rtc_time[1]);
                }

                str_sleep_rtc_time = obj.getString("sleep_rtc_time");
                String[] str_a_sleep_rtc_time = str_sleep_rtc_time.split(":");
                if(str_a_sleep_rtc_time != null && str_a_sleep_rtc_time.length == 2) {
                    sleep_rtc_time_hours = Integer.parseInt(str_a_sleep_rtc_time[0]);
                    sleep_rtc_time_minutes = Integer.parseInt(str_a_sleep_rtc_time[1]);
                }

             } catch (JSONException e) {
                 e.printStackTrace();
             }

        }

        if(apk_updater_server_port != null && !apk_updater_server_port.isEmpty())
            apk_updater_server = apk_updater_server + ":" + apk_updater_server_port;

        Log.i(TAG, "apk server: " + apk_updater_server);
        Log.i(TAG, "apk server path: " + apk_updater_path);
        Log.i(TAG, "apk update interval (ms): " + apk_update_interval);

        sau = new SilentAutoUpdate(getApplicationContext(), apk_updater_path, apk_updater_server);
        sau.addObserver(this);

        if(apk_update_on_boot)
            sau.checkUpdatesManually();

        sau.setUpdateInterval(apk_update_interval);

        // WAKEUP / SLEEP

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        UCCLauncherReceiver.wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);                
        
        Log.i(TAG, "sleep time: " + sleep_rtc_time_hours + ":" + sleep_rtc_time_minutes);
        Log.i(TAG, "wakeup time: " + wakeup_rtc_time_hours + ":" + wakeup_rtc_time_minutes);
        
        set_RTC_sleep((Context)this);        
        set_RTC_wakeup((Context)this);

        if(!UCCLauncherReceiver.wl.isHeld())
            UCCLauncherReceiver.wl.acquire();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "on destroy - releasing resources");

        if (alarmMgr!= null) {
           Log.d(TAG, "canceling RTC alarms");
           alarmMgr.cancel(alarmIntent_Wakeup);
           alarmMgr.cancel(alarmIntent_Sleep);
        }

        if(UCCLauncherReceiver.wl != null)
            UCCLauncherReceiver.wl.release();

    }    

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Passdown the key-up event to runtime view.
        if (getRuntimeView() != null &&
                getRuntimeView().onKeyUp(keyCode, event)) {
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void didTryLoadRuntimeView(View runtimeView) {
        if (runtimeView != null) {
            setContentView(runtimeView);
            getRuntimeView().loadAppFromUrl("file:///android_asset/index.html");
        } else {
            TextView msgText = new TextView(this);
            msgText.setText("Crosswalk failed to initialize.");
            msgText.setTextSize(36);
            msgText.setTextColor(Color.BLACK);
            setContentView(msgText);
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        if( ((String)data).equalsIgnoreCase(AutoUpdateApk.AUTOUPDATE_GOT_UPDATE) ) {
            android.util.Log.i(TAG, "APK UPDATE: received update");
        }
        if( ((String)data).equalsIgnoreCase(AutoUpdateApk.AUTOUPDATE_HAVE_UPDATE) ) {
            android.util.Log.i(TAG, "APK UPDATE: update available");
        }
    }    

    public void UiChangeListener() {
        final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
                }
            }
        });
    }    

    public static class UCCLauncherReceiver extends BroadcastReceiver {

        private static PowerManager.WakeLock wl = null;

        @Override
        public void onReceive(Context context, Intent intent) {     
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
                Intent i = new Intent(context, AppTemplateActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                Log.d(TAG, "ACTION_BOOT_COMPLETED - starting AppTemplateActivity");     
            } else if (intent.getAction().equals(SLEEP_SIG)){
                Log.d(TAG, "going to sleep....");
                if(wl != null)
                    wl.release();
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1000);
            } else if (intent.getAction().equals(WAKEUP_SIG)){
                Log.d(TAG, "waking up....");                
                if(wl != null && !wl.isHeld()) {
                    wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
                    wl.acquire();
                }                
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, -1);
            } else {
                Log.d(TAG, "received a broadcast -- " + intent.getAction());
            }
            
        }

    }

    public static void set_RTC_wakeup(Context context) {

        Log.d(TAG, "setting RTC wakeup");

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, UCCLauncherReceiver.class);
        intent.setAction(WAKEUP_SIG);
        alarmIntent_Wakeup = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, wakeup_rtc_time_hours);
        calendar.set(Calendar.MINUTE, wakeup_rtc_time_minutes);

        // cancel old intent (if any)
        alarmMgr.cancel(alarmIntent_Wakeup);   
        
        // set new intent

        // alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        // SystemClock.elapsedRealtime() + 2 * 60 * 1000, alarmIntent_Wakeup);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
        AlarmManager.INTERVAL_DAY, alarmIntent_Wakeup);            
    }

    public static void set_RTC_sleep(Context context) {

        Log.d(TAG, "setting RTC sleep");

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, UCCLauncherReceiver.class);
        intent.setAction(SLEEP_SIG);
        alarmIntent_Sleep = PendingIntent.getBroadcast(context, 0, intent, 0);        

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());        
        calendar.set(Calendar.HOUR_OF_DAY, sleep_rtc_time_hours);
        calendar.set(Calendar.MINUTE, sleep_rtc_time_minutes);

        // cancel old intent (if any)
        alarmMgr.cancel(alarmIntent_Sleep);

        // set new intent

        // alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        // SystemClock.elapsedRealtime() + 2 * 60 * 1000, alarmIntent_Sleep);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
        AlarmManager.INTERVAL_DAY, alarmIntent_Sleep);            

    }


    public static class UCCIO {
        
        private static File _root = null;
        private static File _sys_conf = null;
        
        public static interface ConfigFile {
            String SYSTEM = "system.conf";
        };
        
        private static File UCCIODir() {
            if(_root == null) {
                _root = new File(Environment.getExternalStorageDirectory(), "Organism/");
            }
            return _root;
        }
        
        private static File getFile(String name) {
            if(ConfigFile.SYSTEM.equals(name)) {
                if(_sys_conf == null) 
                    _sys_conf = new File(UCCIODir(), ConfigFile.SYSTEM);
                return _sys_conf;
            }
            return null;        
        }
        
        private static String load(File f) {
            String s = null;
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(f));
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                s = new String(buffer,  "UTF-8");            
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if(is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return s;
        }
                
        public static JSONObject read(String name) {

            File f = getFile(name);
            if(f == null) return null;
            String s = load(f);
            if(s == null) return null;
            try {
                return new JSONObject(s);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        
    }    

}




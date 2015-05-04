package org.ucc.extension;

import org.xwalk.app.runtime.extension.XWalkExtensionClient;
import org.xwalk.app.runtime.extension.XWalkExtensionContextClient;

import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import org.json.JSONObject;

public class UCCExtension extends XWalkExtensionClient {
    final private XWalkExtensionContextClient _context;
    final private IntentFilter _filter = new IntentFilter("ucc-event");
    final private Intent _intent_alive = new Intent();
    final private Intent _intent_reboot = new Intent();
    final private Intent _intent_shutdown = new Intent();
    final private Intent _intent_relaunch = new Intent();
    private String message; // weird 

    private boolean write = false;
    private String filename = null;

    public UCCExtension(String name, String jsApiContent,XWalkExtensionContextClient context) {
        super(name, jsApiContent, context);
        _context = context;

        Log.i("UCCExtension", "registerReceiver");
        _context.getActivity().registerReceiver(_receiver, _filter);
        
        // alive
        _intent_alive.setAction("ucc-event");
        _intent_alive.putExtra("message", "alive");
        // reboot
        _intent_reboot.setAction("ucc-event");
        _intent_reboot.putExtra("message", "reboot");
        // shutdown
        _intent_shutdown.setAction("ucc-event");
        _intent_shutdown.putExtra("message", "shutdown");        
        // relaunch
        _intent_relaunch.setAction("ucc-event");
        _intent_relaunch.putExtra("message", "relaunch");        

        Log.i("UCCExtension", "ready");
    }
    
    @Override
    public void onMessage(int instanceId, String message) {
        if(message.equals("alive")) postMessage(instanceId, alive());
        else if(message.equals("reboot")) postMessage(instanceId, reboot());
        else if(message.equals("shutdown")) postMessage(instanceId, shutdown());
        else if(message.equals("terminate")) postMessage(instanceId, terminate());
        else if(message.equals("relaunch")) postMessage(instanceId, relaunch());
        else if(message.equals("read_sys_config")) postMessage(instanceId, read(UCCIO.ConfigFile.SYSTEM));
        else if(message.equals("read_cos_config")) postMessage(instanceId, read(UCCIO.ConfigFile.COSMETIC));
        else if(message.equals("write_sys_config")) { write = true; filename = UCCIO.ConfigFile.SYSTEM; }
        else if(message.equals("write_cos_config")) { write = true; filename = UCCIO.ConfigFile.COSMETIC; }
        else {
            if(write) {
                postMessage(instanceId, write(message));
            }
        }

    }
    
    @Override
    public String onSyncMessage(int instanceId, String message) {
        if(message.equals("alive")) return alive();
        else if(message.equals("reboot")) return reboot();
        else if(message.equals("shutdown")) return shutdown();
        else if(message.equals("terminate")) return terminate();
        else if(message.equals("relaunch")) return relaunch();
        else if(message.equals("read_sys_config")) return read(UCCIO.ConfigFile.SYSTEM);
        else if(message.equals("read_cos_config")) return read(UCCIO.ConfigFile.COSMETIC);
        else if(message.equals("write_sys_config")) { write = true; filename = UCCIO.ConfigFile.SYSTEM; return "ok"; }
        else if(message.equals("write_cos_config")) { write = true; filename = UCCIO.ConfigFile.COSMETIC; return "ok"; }
        else {
            if(write)
                return write(message);
        }
        return message;
    }

    @Override
    public void onDestroy() {
        _context.getActivity().unregisterReceiver(_receiver);
        _intent_alive.removeExtra("message");
        _intent_alive.putExtra("message", "destroyed");
        _context.getActivity().sendBroadcast(_intent_alive);   

        Log.d("UCCExtension", "destroyed");      
        // hard kill
        android.os.Process.killProcess(android.os.Process.myPid());

    }

    public String alive() {
        //Log.i("UCCExtension", "alive");
        _context.getActivity().sendBroadcast(_intent_alive);
        return "ok";
    }

    public String reboot() {
        Log.i("UCCExtension", "reboot");
        _context.getActivity().sendBroadcast(_intent_reboot);
        return "ok";
    }

    public String shutdown() {
        Log.i("UCCExtension", "shutdown");
        _context.getActivity().sendBroadcast(_intent_shutdown);
        return "ok";
    }

    public String terminate() {
        Log.i("UCCExtension", "terminate");
        _context.getActivity().finish();
        return "ok";
    }

    public String relaunch() {
        Log.i("UCCExtension", "relaunch");
        _context.getActivity().sendBroadcast(_intent_relaunch);
        return "ok";
    }

    private String read(String fname) {
        String obj = UCCIO.read(fname);
        if(obj != null) return obj;
        else return "error";
    }

    public String write(String data) {
        if(UCCIO.write(filename, data)) { write = false; return "ok"; }
        else { write = false; return "error"; }
    }

    BroadcastReceiver _receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        message = intent.getStringExtra("message");
        if(message.equals("kill")) {
            terminate();
        }        
      }
    };


}


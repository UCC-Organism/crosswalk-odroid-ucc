package org.ucc.extension;

import org.xwalk.app.runtime.extension.XWalkExtensionClient;
import org.xwalk.app.runtime.extension.XWalkExtensionContextClient;

import android.util.Log;
import android.app.Activity;
import android.content.Context;

import org.json.JSONObject;

public class UCCExtension extends XWalkExtensionClient {
    final private XWalkExtensionContextClient _context;
    private String message; // weird 

    public UCCExtension(String name, String jsApiContent,XWalkExtensionContextClient context) {
        super(name, jsApiContent, context);
        _context = context;
    }
    
    @Override
    public void onMessage(int instanceId, String message) {
        if(message.equals("read_sys_conf")) { postMessage(instanceId, read(UCCIO.ConfigFile.SYSTEM)); }        
    }
    
    @Override
    public String onSyncMessage(int instanceId, String message) {
        if(message.equals("read_sys_conf")) { return read(UCCIO.ConfigFile.SYSTEM); }
        return message;
    }

    @Override
    public void onDestroy() {
        Log.d("UCCExtension", "destroyed");      
    }

    private String read(String fname) {
        String obj = UCCIO.read(fname);
        if(obj != null) return obj;
        else return "error";
    }

}


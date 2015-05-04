package org.ucc.extension;

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


public class UCCIO {
	
	private static File _root = null;
	private static File _sys_conf = null;
	private static File _cos_conf = null;
	
	public static interface ConfigFile {
		String SYSTEM = "system.conf";
		String COSMETIC = "cosmetic.conf";
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
		} else if(ConfigFile.COSMETIC.equals(name)) {
			if(_cos_conf == null)
				_cos_conf = new File(UCCIODir(), ConfigFile.COSMETIC);
			return _cos_conf;
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
	
	private static boolean dump(File f, String data) {
		boolean r = false;
		OutputStreamWriter os = null;
		try {
			os = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(f)));
			os.write(data);
			r = true;
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
        	if(os != null) {
        		try {
        			os.close();
        		} catch (IOException ex) {
        			ex.printStackTrace();
        		}
        	}
			
		}
		return r;
	}
	
	public static String read(String name) {
		File f = getFile(name);
		if(f == null) return null;
		return load(f);
	}
	
	public static boolean write(String name, JSONObject jsondata) {
		File f = getFile(name);
		if(f == null) return false;
		return dump(f, jsondata.toString());		
	}	

	public static boolean write(String name, String jsondata) {
		File f = getFile(name);
		if(f == null) return false;
		return dump(f, jsondata);		
	}	


}

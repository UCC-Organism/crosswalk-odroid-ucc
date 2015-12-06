
# HOW TO BUILD UCC APK

## Dependencies

1. Android SDK - https://developer.android.com/sdk/installing/index.html?pkg=tools 

To set the SDK

	cd tools
	. android

Select the following:

	SDK Platform Tools 23.0.1
	SDK Build-Tools 20
	Android 5.0.1 API 21


2. Apache ANT - http://ant.apache.org/bindownload.cgi


Installation 

	git clone https://github.com/UCC-Organism/crosswalk-odroid-ucc.git


Edit the file **setenv.sh** to point to you system’s Android SDK, ANT and CROSSWALK (aka. XWALK) directories


## Building Apks

	source setenv.sh

	python make_apk.py --enable-auto-update --enable-remote-debugging --manifest=[path-to-your-apps-manifest]

this should create an APK in the current folder named after the information found in your app’s Manifest.json file

NOTE: The Manifest.json file is defined in the build directory created by the bundle.sh script of https://github.com/UCC-Organism/ucc-organism

NOTE: SDK Build-Tools 20 and zipalign issues 

scripts/gyp/finalize_apk.py - line 34: os.path.join(android_sdk_root, 'build-tools', '20.0.0', 'zipalign'),

# Install Built APK on ODROID

If your ODROID is connected to the internet (or the LAN), it could be preferable to access it using the network (rather than USB). 

In order to do so, first follow these instruction to set your system up proper (and the ODROID) https://developer.android.com/tools/help/adb.html#wireless

1. To connect over TCPIP 
	
	adb connect XXX.XXX.XXX.XXX:8088

NOTE: You can lookup your ODROID’s IP using the SSH app installed on the machine

2. To install you built APK

	adb install name_of_the.apk


3. Debug information (stdout, stderr, etc) can be access through

	adb logcat







package com.densosp1;

import com.densowave.scannersdk.Common.CommScanner;
import com.densowave.scannersdk.Common.CommStatusChangedEvent;
import com.densowave.scannersdk.Listener.ScannerStatusListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

public abstract class DensoSp1Thread extends Thread {
	private ReactApplicationContext context;

	private static boolean scannerConnected = false;
	private static CommScanner commScanner = null;
	private static CommScannerStatusListener commScannerStatusListener = null;

	public DensoSp1Thread(ReactApplicationContext context) {
		this.context = context;
		commScannerStatusListener = new CommScannerStatusListener();
	}

	public abstract void dispatchEvent(String name, WritableMap data);

	public abstract void dispatchEvent(String name, String data);

	public abstract void dispatchEvent(String name, WritableArray data);

	public abstract void dispatchEvent(String name, boolean data);

	public void onHostResume() {
		//
	}

	public void onHostPause() {
		//
	}

	public void onHostDestroy() {
		//
	}

	class CommScannerStatusListener implements ScannerStatusListener {
		@Override
		public void onScannerStatusChanged(CommScanner commScanner, CommStatusChangedEvent commStatusChangedEvent) {
			//
		}
	}

	public void GetDeviceList() {
		//
	}

	public void DisconnectDevice() throws Exception {
		if (commScanner != null) {
			commScanner.close();
			commScanner.removeStatusListener(commScannerStatusListener);
			commScanner = null;

			scannerConnected = false;
		}
	}

	public boolean IsConnected() {
		return scannerConnected;
	}
}

package com.densosp1;

import android.media.MediaPlayer;
import android.util.Log;

import com.densowave.scannersdk.Barcode.BarcodeDataReceivedEvent;
import com.densowave.scannersdk.Barcode.BarcodeException;
import com.densowave.scannersdk.Common.CommException;
import com.densowave.scannersdk.Common.CommManager;
import com.densowave.scannersdk.Common.CommScanner;
import com.densowave.scannersdk.Common.CommStatusChangedEvent;

import com.densowave.scannersdk.Listener.BarcodeDataDelegate;
import com.densowave.scannersdk.Listener.RFIDDataDelegate;
import com.densowave.scannersdk.Listener.ScannerAcceptStatusListener;
import com.densowave.scannersdk.Listener.ScannerStatusListener;

import com.densowave.scannersdk.Dto.CommScannerParams;
import com.densowave.scannersdk.Dto.CommScannerParams.Notification.Sound.Buzzer;

import com.densowave.scannersdk.Dto.RFIDScannerSettings;
import com.densowave.scannersdk.Dto.BarcodeScannerSettings;

import com.densowave.scannersdk.Dto.RFIDScannerSettings.Scan.SessionFlag;
import com.densowave.scannersdk.Dto.RFIDScannerSettings.Scan.DoubleReading;
import com.densowave.scannersdk.Dto.RFIDScannerSettings.Scan.Polarization;

import com.densowave.scannersdk.RFID.RFIDDataReceivedEvent;
import com.densowave.scannersdk.RFID.RFIDException;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Dispatch_Event {
	static final String TagEvent = "TagEvent";
	static final String RFIDStatusEvent = "RFIDStatusEvent";
	static final String writeTag = "writeTag";
	static final String Barcode = "barcode";
	static final String BarcodeTrigger = "BarcodeTrigger";
	static final String inventoryStart = "inventoryStart";
	static final String inventoryStop = "inventoryStop";
	static final String triggerAction = "triggerAction";
	static final String HandleError = "HandleError";
}

public abstract class DensoSp1Thread extends Thread {
	private ReactApplicationContext context;

	//Denso SP1
	private static CommScanner commScanner = null;
	private static CommScannerStatusListener commScannerStatusListener = null;
	private static CommScannerAcceptStatusListener commScannerAcceptStatusListener = null;
	private static boolean isRunningTimeout = false;

	private static boolean isConnected = false;
	private static String currentRoute = null;

	// User selected reader
	private static String selectedReader = null;

	//Play Sound
	private static MediaPlayer mp = null;
	private static Thread soundThread = null;
	private static boolean isPlaying = false;
	private static int soundRange = -1;

	//Inventory
	private static ArrayList<String> cacheTags = null;
	private static CommScannerDataDelegate commScannerDataDelegate = null;
	private static boolean isOpenedRFID = false;

	//Barcode
	private static CommBarcodeDelegate commBarcodeDelegate = null;
	private static boolean isReadBarcode = false;
	private static boolean isOpenedBarcode = false;

	//Find IT
	private static String tagID = null;
	private static boolean isLocateMode = false;

	DensoSp1Thread(ReactApplicationContext context) {
		this.context = context;
		mp = MediaPlayer.create(this.context, R.raw.beeper);
	}

	public abstract void dispatchEvent(String name, WritableMap data);

	public abstract void dispatchEvent(String name, String data);

	public abstract void dispatchEvent(String name, WritableArray data);

	public abstract void dispatchEvent(String name, boolean data);

	void onHostResume() {
		//
	}

	void onHostPause() {
		//
	}

	void onHostDestroy() {
		if (isConnected) {
			try {
				DisconnectDevice();
			} catch (Exception err) {
				Log.e("onHostDestroy", err.getMessage());
			}
		}
	}

	//Reader status on / off
	class CommScannerStatusListener implements ScannerStatusListener {
		@Override
		public void onScannerStatusChanged(CommScanner commScanner, CommStatusChangedEvent commStatusChangedEvent) {
			try {
				DisconnectDevice();
			} catch (Exception err) {
				HandleError(err.getMessage(), "ScannerStatusChanged");
			}
		}
	}

	//Reader connected
	class CommScannerAcceptStatusListener implements ScannerAcceptStatusListener {
		@Override
		public void OnScannerAppeared(CommScanner scanner) {
			try {
				CommManager.endAccept();

				CommManager.removeAcceptStatusListener(commScannerAcceptStatusListener);

				commScanner = scanner;

				commScanner.claim();

				commScanner.addStatusListener(commScannerStatusListener);

				isConnected = true;

				selectedReader = scanner.getBTLocalName();

				isRunningTimeout = false;

				DefaultSetting();

				InitInventory();

				InitBarcode();

				WritableMap map = Arguments.createMap();
				map.putBoolean("ConnectionState", true);
				dispatchEvent(Dispatch_Event.RFIDStatusEvent, map);
			} catch (Exception e) {
				HandleError(e.getMessage(), "DeviceAppeared");
			}
		}
	}

	//Reader inventory delegate
	class CommScannerDataDelegate implements RFIDDataDelegate {
		@Override
		public void onRFIDDataReceived(CommScanner commScanner, RFIDDataReceivedEvent rfidDataReceivedEvent) {
			for (int i = 0; i < rfidDataReceivedEvent.getRFIDData().size(); i++) {
				StringBuilder data = new StringBuilder();
				byte[] uii = rfidDataReceivedEvent.getRFIDData().get(i).getUII();
				for (byte b : uii) {
					data.append(String.format("%02X ", b).trim());
				}

				int rssi = rfidDataReceivedEvent.getRFIDData().get(i).getRSSI();
				String EPC = data.toString();
				Log.e("Tag", EPC);
				Log.e("RSSI", rssi + "");
				if (currentRoute != null && currentRoute.equalsIgnoreCase("tagit")) {
					if (rssi > 65100) {
						if (addTagToList(EPC) && cacheTags.size() == 1) {
							dispatchEvent("TagEvent", EPC);
						}
					}
				} else if (currentRoute != null) {
					if (addTagToList(EPC)) {
						dispatchEvent("TagEvent", EPC);
					}
				}
			}
		}
	}

	//Reader barcode delegate
	class CommBarcodeDelegate implements BarcodeDataDelegate {
		@Override
		public void onBarcodeDataReceived(CommScanner commScanner, BarcodeDataReceivedEvent barcodeDataReceivedEvent) {
			for (int i = 0; i < barcodeDataReceivedEvent.getBarcodeData().size(); i++) {
				String barcode = new String(barcodeDataReceivedEvent.getBarcodeData().get(i).getData());
				dispatchEvent(Dispatch_Event.Barcode, barcode);
			}
		}
	}

	private void PlaySound(long value) {
		if (value > 0 && value <= 30) {
			soundRange = 1000;
		} else if (value > 31 && value <= 75) {
			soundRange = 600;
		} else {
			soundRange = 100;
		}

		if (soundThread == null) {
			soundThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (isPlaying) {
						if (soundRange > 0) {
							Log.e("LOOP", soundRange + "");
							try {
								Thread.sleep(soundRange);
							} catch (InterruptedException e) {
								e.getMessage();
							}
							mp.start();
						}

					}
				}
			});
			soundThread.start();
		}
	}

	void DisconnectDevice() throws Exception {
		if (commScanner != null) {
			//Denso SP1
			commScanner.removeStatusListener(commScannerStatusListener);
			commScanner.close();
			commScanner = null;
			commScannerStatusListener = null;
			commScannerAcceptStatusListener = null;
			isRunningTimeout = false;

			isConnected = false;
			currentRoute = null;

			// User selected reader
			selectedReader = null;

			//Play Sound
			mp = null;
			isPlaying = false;
			soundThread = null;
			soundRange = -1;

			//Inventory
			cacheTags = null;
			commScannerDataDelegate = null;

			//Barcode
			commBarcodeDelegate = null;
			isReadBarcode = false;

			//Find IT
			tagID = null;
			isLocateMode = false;
		}
	}

	boolean IsConnected() {
		return isConnected;
	}

	void Connect() {
		commScannerStatusListener = new CommScannerStatusListener();
		commScannerAcceptStatusListener = new CommScannerAcceptStatusListener();
		commScannerDataDelegate = new CommScannerDataDelegate();
		commBarcodeDelegate = new CommBarcodeDelegate();

		CommManager.addAcceptStatusListener(commScannerAcceptStatusListener);

		CommManager.startAccept();

		isRunningTimeout = true;

		//Start time out counting
		new Thread(new Runnable() {
			@Override
			public void run() {
				int time = 0;
				while (isRunningTimeout && time < 30) {
					try {
						Thread.sleep(1000);
						time++;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (time >= 30) {
					WritableMap map = Arguments.createMap();
					map.putString("message", "Time out");
					dispatchEvent(Dispatch_Event.RFIDStatusEvent, map);
				}
			}
		}).start();
	}

	private void InitInventory() throws Exception {
		if (isConnected) {
			commScanner.getRFIDScanner().setDataDelegate(commScannerDataDelegate);
		}
	}

	private void InitBarcode() throws Exception {
		if (isConnected) {
			commScanner.getBarcodeScanner().setDataDelegate(commBarcodeDelegate);
		}
	}

	private void InitProgramTag() throws Exception {
		if (isConnected) {
			SetTrigger(RFIDScannerSettings.Scan.TriggerMode.AUTO_OFF);
		}
	}

	void ProgramTag(String oldTag, String newTag) throws Exception {
		if (isConnected) {
			try {
				byte[] uii = oldTag.getBytes();
				byte[] data = newTag.getBytes();
				byte[] pwd = "".getBytes();
				short length = (short) oldTag.length();
				short offset = (short) 2;
				int timeout = 5000;

				commScanner.getRFIDScanner().writeOneTag(RFIDScannerSettings.RFIDBank.UII, offset,
						length, pwd, data, uii, timeout);
				dispatchEvent(Dispatch_Event.writeTag, "success");
			} catch (RFIDException rfidErr) {
				String msg = String.format("Error code: %s \nMessage: %s",
						rfidErr.getErrorCode(), rfidErr.getLocalizedMessage());
				dispatchEvent(Dispatch_Event.writeTag, rfidErr.getLocalizedMessage());
			} catch (Exception err) {
				String msg = err.getMessage();
				dispatchEvent(Dispatch_Event.writeTag, msg);
			} finally {
				//Reopen reading tag and clean cache tags
				setEnable(true);
				CleanCacheTags();
			}
		} else {
			throw new Exception("Reader is not connected");
		}
	}

	private void setEnable(boolean isEnable) throws Exception {
		if (isConnected) {
			if (isEnable) {
				isOpenedRFID = true;
				commScanner.getRFIDScanner().setDataDelegate(commScannerDataDelegate);
				commScanner.getRFIDScanner().openInventory();
			} else {
				isOpenedRFID = false;
				commScanner.getRFIDScanner().setDataDelegate(null);
				commScanner.getRFIDScanner().close();
			}
		} else {
			throw new Exception("Reader is not connected");
		}
	}

	private void setEnableBarcode(boolean isEnable) throws Exception {
		if (isConnected) {
			if (isEnable) {
				isOpenedBarcode = true;
				commScanner.getBarcodeScanner().setDataDelegate(commBarcodeDelegate);
				commScanner.getBarcodeScanner().openReader();
			} else {
				isOpenedBarcode = false;
				commScanner.getBarcodeScanner().setDataDelegate(null);
				commScanner.getBarcodeScanner().closeReader();
			}
		} else {
			throw new Exception("Reader is not connected");
		}
	}

	void ReadBarcode(boolean isEnable) throws Exception {
		isReadBarcode = isEnable;
		if (isReadBarcode) {
			if (isOpenedRFID) setEnable(false);
			setEnableBarcode(true);
		} else {
			if (isOpenedBarcode) setEnableBarcode(false);
			setEnable(true);
		}
	}

	WritableArray GetDeviceList() {
		WritableArray deviceList = Arguments.createArray();
		List<CommScanner> scanner_list = CommManager.getScanners();
		for (CommScanner scanner : scanner_list) {
			WritableMap map = Arguments.createMap();
			map.putString("name", scanner.getBTLocalName());
			map.putString("address", scanner.getBTAddress());
			deviceList.pushMap(map);
		}
		return deviceList;
	}

	String GetConnectedReader() {
		return selectedReader;
	}

	void SaveSelectedScanner(String name) {
		selectedReader = name;
	}

	void CleanCacheTags() {
		cacheTags = new ArrayList<>();
	}

	void SaveCurrentRoute(String routeName) throws Exception {
		if (routeName != null) {
			if (isLocateMode && !routeName.equalsIgnoreCase("locateTag")) {
				isLocateMode = false;
			} else if (routeName.equalsIgnoreCase("locateTag")) {
				isLocateMode = true;
			} else if (routeName.equalsIgnoreCase("tagit")) {
				InitProgramTag();
			}
		} else {
//			if (!isOpenedRFID) setEnable(true);
			if (isOpenedBarcode) setEnableBarcode(false);
			if (isOpenedRFID) setEnable(false);
		}

		if (routeName == null && currentRoute.equalsIgnoreCase("tagit")) {
			SetTrigger(RFIDScannerSettings.Scan.TriggerMode.MOMENTARY);
		}
		currentRoute = routeName;
	}

	private void SetTrigger(RFIDScannerSettings.Scan.TriggerMode mode) throws RFIDException {
		RFIDScannerSettings rfidSettings = commScanner.getRFIDScanner().getSettings();
		rfidSettings.scan.triggerMode = mode;
		commScanner.getRFIDScanner().setSettings(rfidSettings);
	}

	private void SetBuzzer(boolean isEnable) throws Exception {
		if (isConnected) {
			CommScannerParams params = commScanner.getParams();
			params.notification.sound.buzzer = isEnable ? Buzzer.ENABLE : Buzzer.DISABLE;

			commScanner.setParams(params);
			commScanner.saveParams();
		}
	}

	private void PowerSaveMode(boolean isEnable) throws RFIDException {
		if (isConnected) {
			RFIDScannerSettings rfidSettings = commScanner.getRFIDScanner().getSettings();
			rfidSettings.scan.powerSave = isEnable;

			commScanner.getRFIDScanner().setSettings(rfidSettings);
		}
	}

	void SetPowerLevel(int level) throws RFIDException {
		if (isConnected) {
			RFIDScannerSettings rfidSettings = commScanner.getRFIDScanner().getSettings();
			rfidSettings.scan.powerLevelRead = level;

			commScanner.getRFIDScanner().setSettings(rfidSettings);
		}
	}

	private void DefaultSetting() throws Exception {
		if (isConnected) {
			RFIDScannerSettings rfidSettings = commScanner.getRFIDScanner().getSettings();
			rfidSettings.scan.powerLevelRead = 29;
			rfidSettings.scan.sessionFlag = SessionFlag.S0;
			rfidSettings.scan.doubleReading = DoubleReading.Free;
			rfidSettings.scan.qParam = 4;
			rfidSettings.scan.linkProfile = 4;
			rfidSettings.scan.polarization = Polarization.Both;
			rfidSettings.scan.powerSave = true;
			rfidSettings.scan.triggerMode = RFIDScannerSettings.Scan.TriggerMode.MOMENTARY;

			commScanner.getRFIDScanner().setSettings(rfidSettings);

			SetBuzzer(false);

			BarcodeScannerSettings barcodeSettings = commScanner.getBarcodeScanner().getSettings();
			barcodeSettings.scan.triggerMode = BarcodeScannerSettings.Scan.TriggerMode.AUTO_OFF;
			SetEnable1dCodes(barcodeSettings, true);
			SetEnable2dCodes(barcodeSettings, true);
			commScanner.getBarcodeScanner().setSettings(barcodeSettings);
		}
	}

	private void SetEnable1dCodes(BarcodeScannerSettings settings, boolean enable1dFlg) {
		// Always allow EAN code regardless if checked or not.

		settings.decode.symbologies.ean13upcA.enabled = true; // EAN-13 UPC-A

		settings.decode.symbologies.ean8.enabled = true; // EAN-8

		settings.decode.symbologies.upcE.enabled = enable1dFlg; // UPC-E

		settings.decode.symbologies.itf.enabled = enable1dFlg; // ITF

		settings.decode.symbologies.stf.enabled = enable1dFlg; // STF

		settings.decode.symbologies.codabar.enabled = enable1dFlg; // Codabar

		settings.decode.symbologies.code39.enabled = enable1dFlg; // Code39

		settings.decode.symbologies.code93.enabled = enable1dFlg; // Code93

		settings.decode.symbologies.code128.enabled = enable1dFlg; // Code128

		settings.decode.symbologies.msi.enabled = enable1dFlg; // MSI

		settings.decode.symbologies.gs1DataBar.enabled = enable1dFlg; // GS1 Databar

		settings.decode.symbologies.gs1DataBarLimited.enabled = enable1dFlg; // GS1 Databar Limited

		settings.decode.symbologies.gs1DataBarExpanded.enabled = enable1dFlg; // GS1 Databar Expanded

	}

	private void SetEnable2dCodes(BarcodeScannerSettings settings, boolean enable2dFlg) {
		settings.decode.symbologies.qrCode.enabled = enable2dFlg;   // QR Code

		settings.decode.symbologies.qrCode.model1.enabled = enable2dFlg;    // QR Code.Model1

		settings.decode.symbologies.qrCode.model2.enabled = enable2dFlg;    // QR Code.Model2

		settings.decode.symbologies.microQr.enabled = enable2dFlg; // QR Code.Micro QR

		settings.decode.symbologies.iqrCode.enabled = enable2dFlg; // iQR Code

		settings.decode.symbologies.iqrCode.square.enabled = enable2dFlg; // iQR Code.Square

		settings.decode.symbologies.iqrCode.rectangle.enabled = enable2dFlg; // iQR Code.Rectangle

		settings.decode.symbologies.dataMatrix.enabled = enable2dFlg; // Data Matrix

		settings.decode.symbologies.dataMatrix.square.enabled = enable2dFlg; // Data Matrix.Square

		settings.decode.symbologies.dataMatrix.rectangle.enabled = enable2dFlg; // Data Matrix.Rectangle

		settings.decode.symbologies.pdf417.enabled = enable2dFlg; // PDF417

		settings.decode.symbologies.microPdf417.enabled = enable2dFlg; // Micro PDF417

		settings.decode.symbologies.maxiCode.enabled = enable2dFlg; // Maxi Code

		settings.decode.symbologies.gs1Composite.enabled = enable2dFlg; // GS1 Composite

		settings.decode.symbologies.plessey.enabled = enable2dFlg;  // Plessey

		settings.decode.symbologies.aztec.enabled = enable2dFlg; // Aztec

	}

	void SaveTagID(String tag) {
		tagID = tag;
	}

	private void HandleError(String msg, String code) {
		Log.e(code, msg);
		WritableMap map = Arguments.createMap();
		map.putString("code", code);
		map.putString("msg", msg);
		dispatchEvent("HandleError", map);
	}

	private boolean addTagToList(String strEPC) {
		if (strEPC != null) {
			if (!checkIsExisted(strEPC)) {
				cacheTags.add(strEPC);
				return true;
			}
		}
		return false;
	}

	private boolean checkIsExisted(String strEPC) {
		for (int i = 0; i < cacheTags.size(); i++) {
			String tag = cacheTags.get(i);
			if (strEPC != null && strEPC.equals(tag)) {
				return true;
			}
		}
		return false;
	}
}

package com.densosp1;

import android.util.Log;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class DensoSp1Module extends ReactContextBaseJavaModule implements LifecycleEventListener {

	private final ReactApplicationContext reactContext;
	private static DensoSp1Thread scannerThread = null;

	public DensoSp1Module(ReactApplicationContext reactContext) {
		super(reactContext);
		this.reactContext = reactContext;
		this.reactContext.addLifecycleEventListener(this);

		if (scannerThread == null) {
			InitialThread();
		}
	}

	@Override
	public String getName() {
		return "DensoSp1";
	}

	@Override
	public void onHostResume() {
		if (scannerThread != null) {
			scannerThread.onHostResume();
		}
	}

	@Override
	public void onHostPause() {
		if (scannerThread != null) {
			scannerThread.onHostPause();
		}
	}

	@Override
	public void onHostDestroy() {
		if (scannerThread != null) {
			scannerThread.onHostDestroy();
		}
	}

	@ReactMethod
	private void InitialThread() {
		try {
			if (scannerThread != null) {
				scannerThread.interrupt();
			}
			scannerThread = new DensoSp1Thread(this.reactContext) {
				@Override
				public void dispatchEvent(String name, WritableMap data) {
					DensoSp1Module.this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
							.emit(name, data);
				}

				@Override
				public void dispatchEvent(String name, String data) {
					DensoSp1Module.this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
							.emit(name, data);
				}

				@Override
				public void dispatchEvent(String name, WritableArray data) {
					DensoSp1Module.this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
							.emit(name, data);
				}

				@Override
				public void dispatchEvent(String name, boolean data) {
					DensoSp1Module.this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
							.emit(name, data);
				}
			};
			scannerThread.start();
		} catch (Exception err) {
			Log.e("InitialThread", err.getMessage());
		}
	}

	@ReactMethod
	public void DisconnectDevice(Promise promise) {
		try {
			if (scannerThread != null) {
				scannerThread.DisconnectDevice();
				promise.resolve(true);
			}
		} catch (Exception err) {
			promise.reject(err);
		}
	}

	@ReactMethod
	public void IsConnected(Promise promise) {
		try {
			if (scannerThread != null) {
				promise.resolve(scannerThread.IsConnected());
			} else {
				promise.resolve(false);
			}
		} catch (Exception err) {
			promise.reject(err);
		}
	}

	@ReactMethod
	public void Connect(Promise promise) {
		try {
			if (scannerThread != null) {
				scannerThread.Connect();
				promise.resolve(true);
			}
		} catch (Exception err) {
			promise.reject(err);
		}
	}

	@ReactMethod
	public void GetDeviceList(Promise promise) {
		try {
			if (scannerThread != null) {
				promise.resolve(scannerThread.GetDeviceList());
			}
		} catch (Exception err) {
			promise.reject(err);
		}
	}

	@ReactMethod
	public void GetConnectedReader(Promise promise) {
		try {
			if (scannerThread != null) {
				promise.resolve(scannerThread.GetConnectedReader());
			}
		} catch (Exception err) {
			promise.reject(err);
		}

	}

	@ReactMethod
	public void SaveSelectedScanner(String name, Promise promise) {
		try {
			if (scannerThread != null) {
				scannerThread.SaveSelectedScanner(name);
				promise.resolve(true);
			}
		} catch (Exception err) {
			promise.reject(err);
		}

	}

	@ReactMethod
	public void CleanCacheTags(Promise promise) {
		try {
			if (scannerThread != null) {
				scannerThread.CleanCacheTags();
				promise.resolve(true);
			}
		} catch (Exception err) {
			promise.reject(err);
		}
	}

	@ReactMethod
	public void SaveCurrentRoute(String routeName, Promise promise) {
		try {
			if (scannerThread != null) {
				scannerThread.SaveCurrentRoute(routeName);
				promise.resolve(true);
			}
		} catch (Exception err) {
			promise.reject(err);
		}
	}

	@ReactMethod
	public void SetPowerLevel(int level, Promise promise) {
		try {
			if (scannerThread != null) {
				scannerThread.SetPowerLevel(level);
				promise.resolve(true);
			}
		} catch (Exception err) {
			promise.reject(err);
		}
	}

	@ReactMethod
	public void ReadBarcode(boolean isEnable, Promise promise) {
		try {
			if (scannerThread != null) {
				scannerThread.ReadBarcode(isEnable);
				promise.resolve(true);
			}
		} catch (Exception err) {
			promise.reject(err);
		}
	}

	@ReactMethod
	public void SaveTagID(String tagID, Promise promise) {
		try {
			if (scannerThread != null) {
				scannerThread.SaveTagID(tagID);
				promise.resolve(true);
			}
		} catch (Exception err) {
			promise.reject(err);
		}
	}
}

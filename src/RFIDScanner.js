import { NativeModules, NativeEventEmitter } from 'react-native';
import { RFIDScannerEvent } from './RFIDScannerEvent';

const rfidScannerManager = NativeModules.DensoSp1;

class RFIDScanner {
	constructor() {
		if (!this.instance) {
			this.instance = true;
			this.onCallbacks = {};
			this.eventEmitter = new NativeEventEmitter(rfidScannerManager);
		}
	}
	ActiveAllListener = () => {
		if (this.eventEmitter) {
			this.eventEmitter.addListener(RFIDScannerEvent.TAG, this.handleTagEvent);
			this.eventEmitter.addListener(RFIDScannerEvent.RFID_Status, this.handleStatusEvent);
			this.eventEmitter.addListener(
				RFIDScannerEvent.BarcodeTrigger,
				this.handleBarcodeTriggerEvent
			);
			this.eventEmitter.addListener(RFIDScannerEvent.BARCODE, this.handleBarcodeEvent);
			this.eventEmitter.addListener(RFIDScannerEvent.WRITETAG, this.handleWriteTagEvent);
			this.eventEmitter.addListener(RFIDScannerEvent.triggerAction, this.handleTriggerActionEvent);
			this.eventEmitter.addListener(RFIDScannerEvent.HANDLE_ERROR, this.handleErrorEvent);
			this.eventEmitter.addListener(RFIDScannerEvent.LOCATE_TAG, this.handleLocateTagEvent);
		}
	};
	RemoveAllListener = () => {
		if (this.eventEmitter) {
			this.eventEmitter.removeListener(RFIDScannerEvent.TAG, this.handleTagEvent);
			this.eventEmitter.removeListener(RFIDScannerEvent.RFID_Status, this.handleStatusEvent);
			this.eventEmitter.removeListener(
				RFIDScannerEvent.BarcodeTrigger,
				this.handleBarcodeTriggerEvent
			);
			this.eventEmitter.removeListener(RFIDScannerEvent.BARCODE, this.handleBarcodeEvent);
			this.eventEmitter.removeListener(RFIDScannerEvent.WRITETAG, this.handleWriteTagEvent);
			this.eventEmitter.removeListener(
				RFIDScannerEvent.triggerAction,
				this.handleTriggerActionEvent
			);
			this.eventEmitter.removeListener(RFIDScannerEvent.HANDLE_ERROR, this.handleErrorEvent);
			this.eventEmitter.removeListener(RFIDScannerEvent.LOCATE_TAG, this.handleLocateTagEvent);
		}
	};
	handleErrorEvent = (event) => {
		if (this.onCallbacks.hasOwnProperty(RFIDScannerEvent.HANDLE_ERROR)) {
			this.onCallbacks[RFIDScannerEvent.HANDLE_ERROR](event);
		}
	}
	handleStatusEvent = (event) => {
		if (this.onCallbacks.hasOwnProperty(RFIDScannerEvent.RFID_Status)) {
			this.onCallbacks[RFIDScannerEvent.RFID_Status](event);
		}
	}
	handleTagEvent = (tag: String) => {
		if (this.onCallbacks.hasOwnProperty(RFIDScannerEvent.TAG)) {
			this.onCallbacks[RFIDScannerEvent.TAG](tag);
		}
	}
	handleBarcodeTriggerEvent = (event) => {
		if (this.onCallbacks.hasOwnProperty(RFIDScannerEvent.BarcodeTrigger)) {
			this.onCallbacks[RFIDScannerEvent.BarcodeTrigger](event);
		}
	}
	handleBarcodeEvent = (barcode: String) => {
		if (this.onCallbacks.hasOwnProperty(RFIDScannerEvent.BARCODE)) {
			this.onCallbacks[RFIDScannerEvent.BARCODE](barcode);
		}
	}
	handleWriteTagEvent = (event) => {
		if (this.onCallbacks.hasOwnProperty(RFIDScannerEvent.WRITETAG)) {
			this.onCallbacks[RFIDScannerEvent.WRITETAG](event);
		}
	}
	handleTriggerActionEvent = (event) => {
		if (this.onCallbacks.hasOwnProperty(RFIDScannerEvent.triggerAction)) {
			this.onCallbacks[RFIDScannerEvent.triggerAction](event);
		}
	}
	handleLocateTagEvent = (event) => {
		if (this.onCallbacks.hasOwnProperty(RFIDScannerEvent.LOCATE_TAG)) {
			this.onCallbacks[RFIDScannerEvent.LOCATE_TAG](event);
		}
	}
	InitialThread = () => {
		rfidScannerManager.InitialThread();
	}
	on = (event, callback) => {
		this.onCallbacks[event] = callback;
	};
	removeon = (event, callback) => {
		if (this.onCallbacks.hasOwnProperty(event)) {
			this.onCallbacks[event] = null;
			delete this.onCallbacks[event];
		}
	};
	shutdown = () => {
		return rfidScannerManager.DisconnectDevice();
	}
	connect = () => {
		return rfidScannerManager.Connect();
	}
	isConnected = () => {
		return rfidScannerManager.IsConnected();
	}
	AttemptToReconnect = () => {
		return Promise.resolve(false);
	}
	GetDeviceList = () => {
		return rfidScannerManager.GetDeviceList();
	}
	GetConnectedReader = () => {
		return rfidScannerManager.GetConnectedReader();
	}
	SaveSelectedScanner = (name: String) => {
		return rfidScannerManager.SaveSelectedScanner(name);
	}
	cleanTags = () => {
		return rfidScannerManager.CleanCacheTags();
	}
	SaveCurrentRoute = (routeName: String) => {
		return rfidScannerManager.SaveCurrentRoute(routeName);
	}
	SetAntennaLevel = (Antenna: Object) => {
		let num = null;
		if(Antenna.hasOwnProperty('antennaLevel')){
			num = parseInt(Antenna.antennaLevel);
			return rfidScannerManager.SetPowerLevel(num);	
		} else {
			return Promise.reject('Antenna level format error');
		}
	}
	ReadBarcode = (isEnalbe: Boolean) => {
		return rfidScannerManager.ReadBarcode(isEnalbe);
	}
	ProgramTag = (oldTag: String, newTag: String) => {
		return rfidScannerManager.ProgramTag(oldTag, newTag);
	}
	SaveTagID =(tag: String) => {
		return rfidScannerManager.SaveTagID(tag);
	}
}
export default new RFIDScanner();

import { NativeModules, NativeEventEmitter } from 'react-native';
import { RFIDScannerEvent } from './RFIDScannerEvent';

const rfidScannerManager = NativeModules.DensoSp1;

class RFIDScanner {
	constructor(props) {
		super(props);
		if (!this.instance) {
			this.instance = true;
			this.onCallBacks = {};
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
			this.eventEmitter.addListener(RFIDScannerEvent.WRITETAG, this.handleWriteTagEvent);
			this.eventEmitter.addListener(RFIDScannerEvent.triggerAction, this.handleTriggerActionEvent);
			this.eventEmitter.addListener(RFIDScannerEvent.HANDLE_ERROR, this.handleErrorEvent);
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
			this.eventEmitter.removeListener(RFIDScannerEvent.WRITETAG, this.handleWriteTagEvent);
			this.eventEmitter.removeListener(
				RFIDScannerEvent.triggerAction,
				this.handleTriggerActionEvent
			);
			this.eventEmitter.removeListener(RFIDScannerEvent.HANDLE_ERROR, this.handleErrorEvent);
		}
	};

	on = (event, callback) => {
		this.onCallBacks[event] = callback;
	};

	removeon = (event, callback) => {
		if (this.onCallBacks.hasOwnProperty(event)) {
			this.onCallBacks[event] = null;
			delete this.onCallBacks[event];
		}
	};

	IsConnected = () => {
		return rfidScannerManager.IsConnected();
	}
}
export default DensoSp1;

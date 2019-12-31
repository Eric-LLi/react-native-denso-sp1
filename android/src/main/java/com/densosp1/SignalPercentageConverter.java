package com.densosp1;

public class SignalPercentageConverter {
	// These are default RSSI values - you may want to change these for different Readers and/or tag types
	private int mRangeMaximum = 65080;
	private int mRangeMinimum = 64832;

	public SignalPercentageConverter() {
	}

	public SignalPercentageConverter(int minimum, int maximum) {
		mRangeMinimum = minimum;
		mRangeMaximum = maximum;
	}

	public int asPercentage(int value) {
		if (value < mRangeMinimum) {
			mRangeMinimum = value;
		}
		if (value > mRangeMaximum) {
			mRangeMaximum = value;
		}

		return (int) ((100 * (value - mRangeMinimum)) / (mRangeMaximum - mRangeMinimum));
	}
}

package uk.co.ormand.teaseme;

import java.util.ArrayList;

public class metronome {
	private String IifSet;
	private String IifNotSet;
	private String Ibpm;
	private comonFunctions comFun;
	private static final String TAG = "ATM";

	public metronome(String ibpm, String iifSet, String iifNotSet) {
		IifSet = iifSet;
		IifNotSet = iifNotSet;
		Ibpm = ibpm;
		comFun = new comonFunctions(TAG);
	}

	public boolean canShow(ArrayList<String> setList) {
		return comFun.canShow(setList, IifSet, IifNotSet, "");
	}

	public int getbpm() {
		return comFun.getRandom(Ibpm);
	}


}

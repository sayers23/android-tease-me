package uk.co.ormand.teaseme;

import java.util.ArrayList;

public class image {
	private String Iid;
	private String IifSet;
	private String IifNotSet;
	private comonFunctions comFun;
	private static final String TAG = "ATM";

	public image(String iid, String iifSet, String iifNotSet) {
		Iid = iid;
		IifSet = iifSet;
		IifNotSet = iifNotSet;
		comFun = new comonFunctions(TAG);
	}

	public String getIid() {
		return Iid;
	}

	public boolean canShow(ArrayList<String> setList) {
		return comFun.canShow(setList, IifSet, IifNotSet, "");
	}

}

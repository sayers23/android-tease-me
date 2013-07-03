package uk.co.ormand.teaseme;

import java.util.ArrayList;

public class video {
	private String Iid;
	private String IstartAt;
	private String IstopAt;
	private String Itarget;
	private String IifSet;
	private String IifNotSet;
	private String ISet;
	private String IUnSet;
	private String IRepeat;
	private comonFunctions comFun;
	private static final String TAG = "ATM";

	public video(String iid, String istartAt, String istopAt, String itarget, String iifSet, String iifNotSet, String Set, String UnSet, String Repeat) {
		Iid = iid;
		IstartAt = istartAt;
		IstopAt = istopAt;
		Itarget = itarget;
		IifSet = iifSet;
		IifNotSet = iifNotSet;
		ISet = Set;
		IUnSet = UnSet;
		IRepeat = Repeat;
		comFun = new comonFunctions(TAG);
	}

	public String getIid() {
		return Iid;
	}

	public String getIstartAt() {
		return IstartAt;
	}

	public String getIstopAt() {
		return IstopAt;
	}

	public String getItarget() {
		return Itarget;
	}

	public boolean canShow(ArrayList<String> setList) {
		return comFun.canShow(setList, IifSet, IifNotSet, "");
	}

	public void setUnSet(ArrayList<String> setList) {
		comFun.SetFlags(ISet, setList);
		comFun.UnsetFlags(IUnSet, setList);
	}

	public String getRepeat() {
		return IRepeat;
	}

}

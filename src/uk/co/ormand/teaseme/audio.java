package uk.co.ormand.teaseme;

import java.util.ArrayList;

public class audio {
	private String Iid;
	private String IStartAt;
	private String IStopAt;
	private String ITarget;
	private String ISet;
	private String IUnSet;
	private String IRepeat;
	private String IifSet;
	private String IifNotSet;
	private comonFunctions comFun;
	private static final String TAG = "ATM";
	

	public audio(String iid, String istartAt, String istopAt, String itarget, String iifSet, String iifNotSet, String Set, String UnSet, String Repeat) {
		Iid = iid;
		IStartAt = istartAt;
		IStopAt = istopAt;
		ITarget = itarget;
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
		return IStartAt;
	}

	public String getIstopAt() {
		return IStopAt;
	}

	public String getTarget() {
		return ITarget;
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

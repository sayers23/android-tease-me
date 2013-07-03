package uk.co.ormand.teaseme;

import java.util.ArrayList;

public class delay {
	private String IifSet;
	private String IifNotSet;
	private String ISet;
	private String IUnSet;
	private String Idelay;
	private String Itarget;
	private String IstartWith;
	private String Istyle;
	private comonFunctions comFun;
	private static final String TAG = "ATM";
	
	public delay(String target, String delay, String ifSet, String ifNotSet, String StartWith, String Style, String Set, String UnSet) {
		Itarget = target;
		Idelay = delay;
		IifNotSet = ifNotSet;
		IifSet = ifSet;
		IstartWith = StartWith;
		Istyle = Style;
		ISet = Set;
		IUnSet = UnSet;
		comFun = new comonFunctions(TAG);
	}

	public boolean canShow(ArrayList<String> setList) {
		return comFun.canShow(setList, IifSet, IifNotSet, "");
	}

	public int getDelaySec() {
		return comFun.getRandom(Idelay);
	}

	public String getTarget() {
		return Itarget;
	}

	public String getStartWith() {
		return IstartWith;
	}

	public String getstyle() {
		return Istyle;
	}
	
	public void setUnSet(ArrayList<String> setList) {
		comFun.SetFlags(ISet, setList);
		comFun.UnsetFlags(IUnSet, setList);
	}

	public String getSet() {
		return ISet;
	}

	public String getUnSet() {
		return IUnSet;
	}

	

}

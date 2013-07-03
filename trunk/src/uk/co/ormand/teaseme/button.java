package uk.co.ormand.teaseme;

import java.util.ArrayList;

public class button {
	private String IifSet;
	private String IifNotSet;
	private String ISet;
	private String IUnSet;
	private String IText;
	private String Itarget;
	private comonFunctions comFun;
	private static final String TAG = "ATM";
	
	public button(String target, String text, String ifSet, String ifNotSet, String Set, String UnSet) {
		Itarget = target;
		IText = text;
		IifNotSet = ifNotSet;
		IifSet = ifSet;
		ISet = Set;
		IUnSet = UnSet;
		comFun = new comonFunctions(TAG);
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

	public boolean canShow(ArrayList<String> setList) {
		return comFun.canShow(setList, IifSet, IifNotSet, "");
	}

	public String getText() {
		return IText;
	}

	public String getTarget() {
		return Itarget;
	}

}

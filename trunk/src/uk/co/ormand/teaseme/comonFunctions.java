package uk.co.ormand.teaseme;

import java.security.SecureRandom;
import java.util.ArrayList;

import android.util.Log;

public class comonFunctions {
	private SecureRandom mRandom = new SecureRandom();
	private String TAG;

	public comonFunctions(String iTAG) {
		TAG = iTAG;
	}
	
	public boolean canShow(ArrayList<String> setList, String IifSet, String IifNotSet, String IPageName) {
		boolean icanShow;
		boolean blnSet = true;
		boolean blnNotSet = true;
		
		
		if (!IifSet.trim().equals("")) {
			blnSet = MatchesIfSetCondition(IifSet.trim(), setList);
		}
		if (!IifNotSet.trim().equals("")) {
			blnNotSet = MatchesIfNotSetCondition(IifNotSet.trim(), setList);
		}
		if (blnSet && blnNotSet) {
			if (IPageName.equals("")) {
				icanShow = true;
			} else {
				icanShow = MatchesIfNotSetCondition(IPageName, setList);
			}
		} else {
			icanShow = false;
		}
		return icanShow;
	}

	private boolean MatchesIfSetCondition(String condition, ArrayList<String> setList) {
		boolean blnReturn = false;
		boolean blnAnd = false;
		boolean blnOr = false;
		String[] conditions;

		Log.d(TAG, "MatchesIfSetCondition flags " + setList + " condition " + condition);
		try {
			if (condition.indexOf("|") > -1) {
				blnOr = true;
				condition = condition.replace("|", ",");
				conditions = condition.split(",", -1);
				for (int i = 0; i < conditions.length; i++) {
					if (setList.contains(conditions[i].trim())) {
						blnReturn = true;
						break;
					}
				}
			}

			if (condition.indexOf("+") > -1) {
				blnAnd = true;
				blnReturn = true;
				condition = condition.replace("+", ",");
				conditions = condition.split(",", -1);
				for (int i = 0; i < conditions.length; i++) {
					if (!setList.contains(conditions[i].trim())) {
						blnReturn = false;
						break;
					}
				}
			}

			if (!blnAnd && !blnOr) {
				blnReturn = setList.contains(condition);
			}
		} catch (Exception e) {
			Log.e(TAG, "MatchesIfSetCondition Exception ", e);
		}

		Log.d(TAG, "MatchesIfSetCondition returned " + blnReturn);
		return blnReturn;
	}
	
	private boolean MatchesIfNotSetCondition(String condition, ArrayList<String> setList) {
		boolean blnReturn = false;
		boolean blnAnd = false;
		boolean blnOr = false;
		String[] conditions;

		Log.d(TAG, "MatchesIfNotSetCondition flags " + setList + " condition " + condition);
		try {
			if (condition.indexOf("+") > -1) {
				blnAnd = true;
				blnReturn = true;
				condition = condition.replace("+", ",");
				conditions = condition.split(",", -1);
				for (int i = 0; i < conditions.length; i++) {
					if (setList.contains(conditions[i].trim())) {
						blnReturn = false;
						break;
					}
				}
			}

			if (condition.indexOf("|") > -1) {
				blnOr = true;
				condition = condition.replace("|", ",");
				conditions = condition.split(",", -1);
				for (int i = 0; i < conditions.length; i++) {
					if (!setList.contains(conditions[i].trim())) {
						blnReturn = true;
						break;
					}
				}
			}

			if (!blnAnd && !blnOr) {
				blnReturn = !setList.contains(condition);
			}
		} catch (Exception e) {
			Log.e(TAG, "MatchesIfNotSetCondition Exception ", e);
		}

		Log.d(TAG, "MatchesIfNotSetCondition returned " + blnReturn);
		return blnReturn;
	}

	// functions to handle set flags go here
	public void SetFlags(String flagNames, ArrayList<String> setList) {
		String[] flags;
		try {
			flags = flagNames.split(",", -1);
			Log.d(TAG, "SetFlags before " + setList + " add " + flagNames);
			for (int i = 0; i < flags.length; i++) {
				if (!flags[i].trim().equals("")) {
					if (!setList.contains(flags[i].trim())) {
						setList.add(flags[i].trim());
					}
				}
			}
			Log.d(TAG, "SetFlags after " + setList);
		} catch (Exception e) {
			Log.e(TAG, "SetFlags Exception ", e);
		}
	}

	public String GetFlags(ArrayList<String> setList) {
		String strFlags = "";
		try {
			for (int i = 0; i < setList.size(); i++) {
				strFlags = strFlags + "," + setList.get(i);
			}

		} catch (Exception e) {
			Log.e(TAG, "SetFlags Exception ", e);
		}
		Log.d(TAG, "GetFlags " + strFlags);
		return strFlags;
	}

	public void UnsetFlags(String flagNames, ArrayList<String> setList) {
		String[] flags;
		try {
			flags = flagNames.split(",", -1);
			Log.d(TAG, "UnsetFlags remove " + setList + " delete " + flagNames);
			for (int i = 0; i < flags.length; i++) {
				if (!flags[i].trim().equals("")) {
					if (setList.contains(flags[i].trim())) {
						setList.remove(flags[i].trim());
					}
				}
			}
			Log.d(TAG, "UnsetFlags after " + setList);
		} catch (Exception e) {
			Log.e(TAG, "UnsetFlags Exception ", e);
		}
	}
	
	public int getRandom(String iRandom) {
		int intRandom = 0;
		int intPos1;
		int intPos2;
		int intPos3;
		int intMin;
		int intMax;
		String strMin;
		String strMax;
		
		try {
			intPos1 = iRandom.indexOf("(");
			if (intPos1 > -1) {
				intPos2 = iRandom.indexOf("..", intPos1);
				if (intPos2 > -1) {
					intPos3 = iRandom.indexOf(")", intPos2);
					if (intPos3 > -1) {
						strMin = iRandom.substring(intPos1 + 1, intPos2);
						intMin = Integer.parseInt(strMin);
						strMax = iRandom.substring(intPos2 + 2, intPos3);
						intMax = Integer.parseInt(strMax);
						int i1 = mRandom.nextInt(intMax - intMin) + intMin;
						intRandom = i1;
						Log.d(TAG, "comonFunctions Random Min " + strMin + " Max " + strMax + " Chosen " + intRandom);
					}
				}
			} else {
				intRandom = Integer.parseInt(iRandom);
				Log.d(TAG, "comonFunctions Random Chosen " + intRandom);
			}
		} catch (Exception e) {
			Log.e(TAG, "getRandom Exception ", e);
		}
		
		return intRandom;
	}
	
	public int getMilisecFromTime(String iTime) {
		int intPos1;
		int intPos2;
		String strHour;
		String strMinute;
		String strSecond;
		int intTime = 0;
		
		try {
			intPos1 = iTime.indexOf(":");
			if (intPos1 > -1) {
				intPos2 = iTime.indexOf(":", intPos1 + 1);
				if (intPos2 > -1) {
					strHour = iTime.substring(0, intPos1);
					strMinute = iTime.substring(intPos1 + 1, intPos2);
					strSecond = iTime.substring(intPos2 + 1, iTime.length());
					Log.d(TAG, "getMilisecFromTime Hour " + strHour + " Minute " + strMinute + " Second " + strSecond);
					intTime = Integer.parseInt(strSecond) * 1000;
					intTime = intTime + Integer.parseInt(strMinute) * 1000 * 60;
					intTime = intTime + Integer.parseInt(strHour) * 1000 * 60 * 60;
					Log.d(TAG, "getMilisecFromTime Millisec " + intTime);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "getMilisecFromTime Exception ", e);
		}
		return intTime;
	}

}

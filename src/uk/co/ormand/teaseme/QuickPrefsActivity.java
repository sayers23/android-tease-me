package uk.co.ormand.teaseme;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class QuickPrefsActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			addPreferencesFromResource(R.xml.preferences);
		} catch (Exception e) {
			Log.e("QuickPrefsActivity.onCreate",
					"Exception " + e.getLocalizedMessage());
		}
	}

}

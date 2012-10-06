package leoliang.runningcadence;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class PreferenceActivity extends android.preference.PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final String LOG_TAG = "PreferenceActivity";
	private final int DIALOG_TTS_NOT_WORK = 1;
	private TextToSpeechOutput ttsOutput;

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("pref_language")) {
			((Application) getApplication()).setLocale();
			restartActivity();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(LOG_TAG, "onCreate");

		ttsOutput = new TextToSpeechOutput(this);

		addPreferencesFromResource(R.xml.preferences);

		Preference ttsTest = findPreference("pref_tts_test");
		ttsTest.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				playVoiceFeedbackExample();
				return true;
			}
		});

		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		ttsOutput.shutdown();
	}

	private void playVoiceFeedbackExample() {
		switch (ttsOutput.getState()) {
		case ERROR:
		case UNSUPPORTED_LANGUAGE:
			showDialog(DIALOG_TTS_NOT_WORK);
		case RUNNING:
			ttsOutput.say(String.format(this.getString(R.string.voice_current_cadence, 180)));
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case DIALOG_TTS_NOT_WORK:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dialog_tts_not_work).setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			dialog = builder.create();
			break;
		}
		return dialog;
	}

	private void restartActivity() {
		Log.d(LOG_TAG, "Restart activity " + this);
		Intent intent = getIntent();
		startActivity(intent);
		finish();
	}

}

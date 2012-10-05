package leoliang.runningcadence;

import java.util.Locale;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

public class Application extends android.app.Application {

	public class AppConfiguration {
		private static final String CONFIG_TARGET_CADENCE = "targetCadence";
		private static final int DEFAULT_TARGET_CADENCE = 180;

		public int getTargetCadence() {
			return getPreferences().getInt(CONFIG_TARGET_CADENCE, DEFAULT_TARGET_CADENCE);
		}

		public void setTargetCadence(int cadence) {
			getPreferences().edit().putInt(CONFIG_TARGET_CADENCE, cadence).commit();
		}
	}

	private static final String LOG_TAG = "Application";
	private final AppConfiguration configuration = new AppConfiguration();

	private SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}

	public AppConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Some event like screen rotation will trigger this method, with system default locale in configuration. Should
	 * override the locale to which user set in application preference.
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Locale locale = getLocaleFromPref();
		overwriteConfigurationLocale(newConfig, locale);
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * On application startup, override system default locale to which user set in application preference.
	 */
	@Override
	public void onCreate() {
		setLocale();
	}

	private Locale getLocaleFromPref() {
		Locale locale = Locale.getDefault();
		String language = getPreferences().getString("pref_language", "");
		if (!language.equals("")) {
			locale = new Locale(language);
			Locale.setDefault(locale);
		}
		return locale;
	}

	private void overwriteConfigurationLocale(Configuration config, Locale locale) {
		config.locale = locale;
		getBaseContext().getResources()
				.updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
	}

	public void setLocale() {
		Locale locale = getLocaleFromPref();
		Log.d(LOG_TAG, "Set locale to " + locale);
		Configuration config = getBaseContext().getResources().getConfiguration();
		overwriteConfigurationLocale(config, locale);
	}

}

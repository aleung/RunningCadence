package leoliang.runningcadence;

import android.content.SharedPreferences;

public class Application extends android.app.Application {

	public class Configuration {
		private static final String CONFIG_TARGET_CADENCE = "targetCadence";
		private static final int DEFAULT_TARGET_CADENCE = 180;

		public int getTargetCadence() {
			return getPreferences().getInt(CONFIG_TARGET_CADENCE, DEFAULT_TARGET_CADENCE);
		}

		public void setTargetCadence(int cadence) {
			getPreferences().edit().putInt(CONFIG_TARGET_CADENCE, cadence).commit();
		}
	}

	private static final String PREFERENCES_FILE_NAME = "default";
	private final Configuration configuration = new Configuration();

	private SharedPreferences getPreferences() {
		return getSharedPreferences(PREFERENCES_FILE_NAME, MODE_PRIVATE);
	}

	public Configuration getConfiguration() {
		return configuration;
	}

}

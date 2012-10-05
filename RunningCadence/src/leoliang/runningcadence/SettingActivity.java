package leoliang.runningcadence;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import leoliang.runningcadence.Application.AppConfiguration;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class SettingActivity extends Activity {

	private static final String LOG_TAG = "SettingActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.setting);
		
		final AppConfiguration configuration = ((Application) getApplication()).getConfiguration();
		int targetCadence = configuration.getTargetCadence();
		final int minValue = 120;
		final WheelView targetCadenceWheel = (WheelView) findViewById(R.id.targetCadence);
		final NumericWheelAdapter wheelAdapter = new NumericWheelAdapter(this, minValue, 300);
		targetCadenceWheel.setViewAdapter(wheelAdapter);
		targetCadenceWheel.setCurrentItem(targetCadence - minValue);
		targetCadenceWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				configuration.setTargetCadence(newValue + minValue);
			}
		});

		Button mStartButton = (Button) findViewById(R.id.startButton);
		mStartButton.getBackground().setColorFilter(0x8000FF00, PorterDuff.Mode.MULTIPLY);
		mStartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SettingActivity.this, RunningCadenceActivity.class));
			}
		});

		ImageButton preferenceButton = (ImageButton) findViewById(R.id.prefButton);
		preferenceButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(SettingActivity.this, PreferenceActivity.class), 0);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// It will only be called when returns from PreferenceActivity.
		// Restart to load in new locale in case it's changed by user.
		restartActivity();
	}

	private void restartActivity() {
		Log.d(LOG_TAG, "Restart activity");
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

}

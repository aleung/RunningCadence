package leoliang.runningcadence;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import leoliang.runningcadence.Application.Configuration;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SettingActivity extends Activity {

	private Button mStartButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.setting);
		
		final Configuration configuration = ((Application) getApplication()).getConfiguration();
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

		mStartButton = (Button) findViewById(R.id.startButton);
		mStartButton.getBackground().setColorFilter(0x8000FF00, PorterDuff.Mode.MULTIPLY);
		mStartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(SettingActivity.this, RunningCadenceActivity.class));
			}
		});


	}

}

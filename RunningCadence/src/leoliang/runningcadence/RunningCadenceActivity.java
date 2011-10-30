package leoliang.runningcadence;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class RunningCadenceActivity extends Activity {

	private static final String TAG = "RunningCadence";

	protected static final CharSequence DISPLAY_NO_DATA = "- -";

	private TextView mCurrentCadence;
	private Button mStartButton;
	private Button mStopButton;
	private FootFallDetector footFallDetector;
	private TextToSpeechOutput ttsOutput;
	private VoiceFeedback voiceFeedback;
	private final Handler mHandler = new Handler();

	private final Runnable mUpdateCadenceTask = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "UpdateCadenceTask runs.");
			Integer cadence = footFallDetector.getCurrentCadence();
			if (cadence == null) {
				mCurrentCadence.setText(DISPLAY_NO_DATA);
			} else {
				mCurrentCadence.setText(String.valueOf(cadence));
			}
			voiceFeedback.onUpdate(cadence);
			mHandler.postDelayed(this, 1000);
		}
	};

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		ttsOutput = new TextToSpeechOutput(this);
		voiceFeedback = new VoiceFeedback(ttsOutput);

		footFallDetector = new FootFallDetector(this);
		footFallDetector.start();

        setContentView(R.layout.main);

		mCurrentCadence = (TextView) findViewById(R.id.currentCadence);
		mCurrentCadence.setText(DISPLAY_NO_DATA);

		mStartButton = (Button) findViewById(R.id.startButton);
		mStartButton.getBackground().setColorFilter(0x8000FF00, PorterDuff.Mode.MULTIPLY);
		mStopButton = (Button) findViewById(R.id.stopButton);
		mStopButton.getBackground().setColorFilter(0x80FF0000, PorterDuff.Mode.MULTIPLY);

		mStartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mStartButton.setVisibility(View.GONE);
				footFallDetector.start();
				mStopButton.setVisibility(View.VISIBLE);
				voiceFeedback.reset();
				mHandler.removeCallbacks(mUpdateCadenceTask);
				mHandler.post(mUpdateCadenceTask);
			}
		});

		mStopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mStopButton.setVisibility(View.GONE);
				footFallDetector.stop();
				mStartButton.setVisibility(View.VISIBLE);
				mHandler.removeCallbacks(mUpdateCadenceTask);
				mCurrentCadence.setText(DISPLAY_NO_DATA);
			}
		});

    }

}

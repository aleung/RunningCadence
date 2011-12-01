package leoliang.runningcadence;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class RunningCadenceActivity extends Activity {

	protected static final CharSequence DISPLAY_NO_DATA = "- -";

	private TextView mCurrentCadence;
	private Button mStopButton;
	private Intent serviceIntent;

	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			((BackgroundService.LocalBinder) binder).gimmeHandler(updateCadenceDisplayHandler);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// do nothing
		}
	};

	private final Handler updateCadenceDisplayHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			int cadence = message.arg1;
			if (cadence > 0) {
				mCurrentCadence.setText(String.valueOf(cadence));
			} else {
				mCurrentCadence.setText(DISPLAY_NO_DATA);
			}
		}
	};

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setContentView(R.layout.cadence);

		mCurrentCadence = (TextView) findViewById(R.id.currentCadence);
		mCurrentCadence.setText(DISPLAY_NO_DATA);

		mStopButton = (Button) findViewById(R.id.stopButton);
		mStopButton.getBackground().setColorFilter(0x80FF0000, PorterDuff.Mode.MULTIPLY);
		mStopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentCadence.setText(DISPLAY_NO_DATA);
				stopService(new Intent(RunningCadenceActivity.this, BackgroundService.class));
				finish();
			}
		});

		serviceIntent = new Intent(RunningCadenceActivity.this, BackgroundService.class);
		startService(serviceIntent);
    }

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(serviceConnection);
	}

	@Override
	protected void onStart() {
		super.onStart();
		bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onBackPressed() {
		// do nothing, disable returning to previous activity
	}
}

package leoliang.runningcadence;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class BackgroundService extends Service {

	/**
	 * Class for clients to access. <code>RunningCadenceActivity</code> is the only client. Because we know this service
	 * always runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public void gimmeHandler(Handler handler) {
			clientHandler = handler;
		}
	}

	private static final String LOG_TAG = "RunningCadence.BackgroundService";

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private final int NOTIFICATION = R.string.service_running;

	private TextToSpeechOutput ttsOutput;
	private VoiceFeedback voiceFeedback;
	private FootFallDetector footFallDetector;
	private NotificationManager notificationManger;
	private Handler clientHandler;

	private final IBinder mBinder = new LocalBinder();

	private final Handler serviceHandler = new Handler();

	private final Runnable mUpdateCadenceTask = new Runnable() {
		@Override
		public void run() {
			Log.d(LOG_TAG, "UpdateCadenceTask runs.");
			int cadence = footFallDetector.getCurrentCadence();
			voiceFeedback.onUpdate(cadence);
			if (clientHandler != null) {
				Message message = Message.obtain();
				message.arg1 = cadence;
				clientHandler.sendMessage(message);
			}
			serviceHandler.postDelayed(this, 1000);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(LOG_TAG, "onBind'd");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.v(LOG_TAG, "onUnbind'd");
		clientHandler = null;
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(LOG_TAG, "onDestroy");
		notificationManger.cancel(NOTIFICATION);
		serviceHandler.removeCallbacks(mUpdateCadenceTask);
		footFallDetector.stop();
		ttsOutput.shutdown();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(LOG_TAG, "onCreate");
		notificationManger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		int targetCadence = ((Application) getApplication()).getConfiguration().getTargetCadence();
		ttsOutput = new TextToSpeechOutput(this);
		voiceFeedback = new VoiceFeedback(ttsOutput);
		voiceFeedback.setTargetCadence(targetCadence);
		footFallDetector = new FootFallDetector(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStart(intent, startId);
		Log.v(LOG_TAG, "onStart");
		start();
		return START_STICKY;
	}

	private void start() {
		footFallDetector.start();
		voiceFeedback.reset();
		showNotification();
		serviceHandler.post(mUpdateCadenceTask);
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.stat_running, null, System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, RunningCadenceActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.app_name), getText(R.string.service_running),
				contentIntent);

		notificationManger.notify(NOTIFICATION, notification);
	}
}

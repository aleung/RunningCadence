package leoliang.runningcadence;

import android.content.Context;


public class VoiceFeedback {

	private enum RunningState {
		NOT_RUNNING, RUNNING_ON_TARGET_CADENCE, RUNNING_SLOWER_THAN_TARGET_CADENCE, RUNNING_FASTER_THAN_TARGET_CADENCE
	}

	private final static String TAG = "VoiceFeedback";

	private int targetCadence = 180;

	private final TextToSpeechOutput ttsOutput;
	private final Context context;
	private long lastFeedbackTimestamp;
	private int currentCadence;
	private int averageCadence;
	private RunningState lastFeedbackState;

	public VoiceFeedback(TextToSpeechOutput tts, Context context) {
		ttsOutput = tts;
		this.context = context;
		reset();
	}

	public void setTargetCadence(int targetCadence) {
		this.targetCadence = targetCadence;
	}

	public void reset() {
		lastFeedbackState = RunningState.NOT_RUNNING;
		lastFeedbackTimestamp = System.currentTimeMillis();
	}

	public void onUpdate(int cadence) {
		currentCadence = cadence;

		RunningState currentState;
		int allowedDeviation = (int) (targetCadence * 0.025);
		if (currentCadence <= 0) {
			currentState = RunningState.NOT_RUNNING;
		} else if (currentCadence > targetCadence + allowedDeviation) {
			currentState = RunningState.RUNNING_FASTER_THAN_TARGET_CADENCE;
		} else if (currentCadence < targetCadence - allowedDeviation) {
			currentState = RunningState.RUNNING_SLOWER_THAN_TARGET_CADENCE;
		} else {
			currentState = RunningState.RUNNING_ON_TARGET_CADENCE;
		}

		updateAverageCadence(cadence);
		int feedbackInterval = calculateFeedbackInterval(averageCadence, targetCadence, currentState, lastFeedbackState);
		if (System.currentTimeMillis() - lastFeedbackTimestamp > feedbackInterval * 1000) {
			playFeedback(currentState);
			lastFeedbackState = currentState;
		}
	}

	private int calculateFeedbackInterval(int averageCadence, int targetCadence, RunningState currentState,
			RunningState lastFeedbackState) {
		if (currentState == RunningState.NOT_RUNNING) {
			if (lastFeedbackState == RunningState.NOT_RUNNING) {
				return 60;
			} else {
				// stop running just now
				return 5;
			}
		}

		if (lastFeedbackState == RunningState.NOT_RUNNING) {
			// just start running
			return 10;
		}

		int bias = Math.abs(averageCadence - targetCadence) * 100 / targetCadence;
		if (bias < 5) {
			if (currentState == RunningState.RUNNING_ON_TARGET_CADENCE
					&& lastFeedbackState != RunningState.RUNNING_ON_TARGET_CADENCE) {
				// give a timely feedback when reaching target cadence
				return 10;
			}
			return 180;
		} else if (bias < 10) {
			return 60;
		} else {
			return 30;
		}

	}

	/**
	 * Average cadence by last 20 samples. If it isn't running, reset average to zero.
	 * 
	 * @param cadence - current cadence
	 */
	private void updateAverageCadence(int cadence) {
		if (cadence == 0) {
			averageCadence = 0;
			return;
		}

		if (averageCadence == 0) {
			averageCadence = cadence;
		} else {
			averageCadence = (averageCadence * 19 + cadence) / 20;
		}
	}

	private void playFeedback(RunningState currentState) {
		switch (currentState) {
		case NOT_RUNNING:
			ttsOutput.say(context.getString(R.string.voice_not_running));
			break;
		case RUNNING_ON_TARGET_CADENCE:
			ttsOutput.say(context.getString(R.string.voice_on_target_cadence));
			break;
		case RUNNING_FASTER_THAN_TARGET_CADENCE:
			ttsOutput.say(String.format(context.getString(R.string.voice_current_cadence, currentCadence)));
			ttsOutput.say(context.getString(R.string.voice_slow_down));
			break;
		case RUNNING_SLOWER_THAN_TARGET_CADENCE:
			ttsOutput.say(String.format(context.getString(R.string.voice_current_cadence, currentCadence)));
			ttsOutput.say(context.getString(R.string.voice_speed_up));
			break;
		}
		lastFeedbackState = currentState;
		// ttsOutput.say() returns immediately,
		// add average speaking duration 5 seconds
		lastFeedbackTimestamp = System.currentTimeMillis() + 5000;
	}

}

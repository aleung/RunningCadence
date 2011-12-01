package leoliang.runningcadence;

import android.util.Log;

public class VoiceFeedback {

	private enum RunningState {
		NOT_RUNNING, RUNNING_ON_TARGET_CADENCE, RUNNING_SLOWER_THAN_TARGET_CADENCE, RUNNING_FASTER_THAN_TARGET_CADENCE
	}

	/**
	 * Voice feedback interval, from last state to current state.
	 * feedbackInterval[lastFeedbackState.ordinal()][currentState.ordinal()]
	 */
	private final static int feedbackInterval[][] = { { 60, 10, 10, 10 }, { 5, 180, 5, 5 }, { 5, 10, 30, 30 },
			{ 5, 10, 30, 30 } };

	private final static String TAG = "VoiceFeedback";

	private int targetCadence = 180;

	private final TextToSpeechOutput ttsOutput;
	private long lastFeedbackTimestamp;
	private int currentCadence;
	private RunningState lastFeedbackState;

	public VoiceFeedback(TextToSpeechOutput tts) {
		ttsOutput = tts;
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
		if (cadence <= 0) {
			currentState = RunningState.NOT_RUNNING;
		} else {
			int allowedDeviation = (int) (targetCadence * 0.025);
			int slowThresold = 0;
			int fastThresold = 0;
			switch (lastFeedbackState) {
			case NOT_RUNNING:
			case RUNNING_ON_TARGET_CADENCE:
				slowThresold = targetCadence - allowedDeviation;
				fastThresold = targetCadence + allowedDeviation;
				break;
			case RUNNING_FASTER_THAN_TARGET_CADENCE:
				slowThresold = targetCadence - allowedDeviation;
				fastThresold = targetCadence;
				break;
			case RUNNING_SLOWER_THAN_TARGET_CADENCE:
				slowThresold = targetCadence;
				fastThresold = targetCadence + allowedDeviation;
				break;
			}

			if (currentCadence > fastThresold) {
				currentState = RunningState.RUNNING_FASTER_THAN_TARGET_CADENCE;
			} else if (currentCadence < slowThresold) {
				currentState = RunningState.RUNNING_SLOWER_THAN_TARGET_CADENCE;
			} else {
				currentState = RunningState.RUNNING_ON_TARGET_CADENCE;
			}
		}

		int interval = feedbackInterval[lastFeedbackState.ordinal()][currentState.ordinal()];
		Log.v(TAG, String.format("From %s to %s, feedback interval:%d", lastFeedbackState, currentState, interval));
		if (System.currentTimeMillis() - lastFeedbackTimestamp > interval * 1000) {
			playFeedback(currentState);
		}
	}

	private void playFeedback(RunningState currentState) {
		switch (currentState) {
		case NOT_RUNNING:
			ttsOutput.say("Seems you're not running now. Have you finished your workout?");
			break;
		case RUNNING_ON_TARGET_CADENCE:
			ttsOutput.say("Good job! You're running on target cadence. Keep going.");
			break;
		case RUNNING_FASTER_THAN_TARGET_CADENCE:
			ttsOutput.say(String.format("Current cadence is %d cycles per minute.", currentCadence));
			ttsOutput.say("Slow down.");
			break;
		case RUNNING_SLOWER_THAN_TARGET_CADENCE:
			ttsOutput.say(String.format("Current cadence is %d cycles per minute.", currentCadence));
			ttsOutput.say("Speed up.");
			break;
		}
		lastFeedbackState = currentState;
		// ttsOutput.say() returns immediately,
		// add average speaking duration 5 seconds
		lastFeedbackTimestamp = System.currentTimeMillis() + 5000;
	}

}

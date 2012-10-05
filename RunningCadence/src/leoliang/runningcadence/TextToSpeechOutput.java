package leoliang.runningcadence;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class TextToSpeechOutput implements TextToSpeech.OnInitListener {

	public enum State {
		UNINITIALIZED, ERROR, UNSUPPORTED_LANGUAGE, RUNNING, SHUTDOWN
	}

	private static final String TAG = "TextToSpeechOutput";
	private final TextToSpeech tts;
	private State state = State.UNINITIALIZED;

	public TextToSpeechOutput(Context context) {
		Log.i(TAG, "Initializing TextToSpeech...");
		tts = new TextToSpeech(context, this);
	}

	public void shutdown() {
		Log.i(TAG, "Shutting Down TextToSpeech...");
		state = State.SHUTDOWN;
		tts.shutdown();
		Log.i(TAG, "TextToSpeech Shut Down.");

	}

	public void say(String text) {
		if (state == State.RUNNING) {
			tts.speak(text, TextToSpeech.QUEUE_ADD, null);
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.getDefault());
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				state = State.UNSUPPORTED_LANGUAGE;
				Log.e(TAG, "Language is not available.");
			} else {
				state = State.RUNNING;
				Log.i(TAG, "TextToSpeech is ready.");
			}
		} else {
			state = State.ERROR;
			Log.e(TAG, "Could not initialize TextToSpeech.");
		}
	}

	public boolean isSpeaking() {
		return tts.isSpeaking();
	}

	public State getState() {
		return state;
	}
}

package leoliang.runningcadence;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class TextToSpeechOutput implements TextToSpeech.OnInitListener {

	private static final String TAG = "TextToSpeechOutput";
	private final TextToSpeech tts;
	private boolean mSpeakingEngineAvailable = false;

	public TextToSpeechOutput(Context context) {
		Log.i(TAG, "Initializing TextToSpeech...");
		tts = new TextToSpeech(context, this);
	}

	public void shutdownTTS() {
		Log.i(TAG, "Shutting Down TextToSpeech...");
		mSpeakingEngineAvailable = false;
		tts.shutdown();
		Log.i(TAG, "TextToSpeech Shut Down.");

	}

	public void say(String text) {
		if (mSpeakingEngineAvailable) {
			tts.speak(text, TextToSpeech.QUEUE_ADD, null);
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Language data is missing or the language is not supported.
				Log.e(TAG, "Language is not available.");
			} else {
				Log.i(TAG, "TextToSpeech Initialized.");
				mSpeakingEngineAvailable = true;
			}
		} else {
			Log.e(TAG, "Could not initialize TextToSpeech.");
		}
	}

	public boolean isSpeaking() {
		return tts.isSpeaking();
	}
}

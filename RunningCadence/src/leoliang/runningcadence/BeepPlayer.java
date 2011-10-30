package leoliang.runningcadence;

import android.media.AudioManager;
import android.media.ToneGenerator;

public class BeepPlayer {

	private final ToneGenerator toneGenerator;

	public BeepPlayer() {
		toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
	}

	public void beep() {
		toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
	}
}

package leoliang.runningcadence;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class FootFallDetector implements SensorEventListener {

	private static final String TAG = "FootFallDetector";

	private class Acceleration {
		public long timestamp;
		public float[] lowPassFilteredValues = new float[3];
		public float[] averagedValues = new float[3];

		@Override
		public String toString() {
			return String.format("Time,average,filtered,:,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f", timestamp,
					averagedValues[0], averagedValues[1], averagedValues[2], lowPassFilteredValues[0],
					lowPassFilteredValues[1], lowPassFilteredValues[2]);
		}
	}

	/**
	 * Cutoff frequency (fc) in low-pass filter for foot fall detection.
	 * 
	 * 3.5 * 60 = 210 footfalls/min
	 */
	private static final float FC_FOOT_FALL_DETECTION = 3.5F;

	/**
	 * Cutoff frequency (fc) in low-pass filter for earth gravity detection
	 */
	private static final float FC_EARTH_GRAVITY_DETECTION = 0.25F;
	private static final int ACCELERATION_VALUE_KEEP_SECONDS = 10;
	private static final int NUMBER_OF_FOOT_FALLS = 6;
	private static final long SECOND_TO_NANOSECOND = (long) 1e9;

	private final Context context;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private boolean active = false;
	private final LinkedList<Acceleration> values = new LinkedList<Acceleration>();

	public FootFallDetector(Context context) {
		this.context = context;
	}

	public void start() {
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		active = true;
	}

	public void stop() {
		active = false;
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// ignored
	}

	@Override
	public synchronized void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
			return;
		}

		Acceleration acceleration = new Acceleration();
		acceleration.timestamp = event.timestamp;

		Acceleration prevValue = values.isEmpty() ? null : values.getFirst();
		if (prevValue == null) {
			for (int i = 0; i < 3; i++) {
				acceleration.averagedValues[i] = event.values[i];
				acceleration.lowPassFilteredValues[i] = event.values[i];
			}
		} else {
			lowPassFilter(acceleration.averagedValues, event.values, event.timestamp, prevValue.averagedValues,
					prevValue.timestamp, FC_EARTH_GRAVITY_DETECTION);
			lowPassFilter(acceleration.lowPassFilteredValues, event.values, event.timestamp,
					prevValue.lowPassFilteredValues, prevValue.timestamp, FC_FOOT_FALL_DETECTION);
		}
		values.addFirst(acceleration);

		removeValuesOlderThan(event.timestamp - ACCELERATION_VALUE_KEEP_SECONDS * SECOND_TO_NANOSECOND);
	}

	/**
	 * Get current cadence, in steps per minute.
	 * 
	 * @return null if data isn't available
	 */
	public synchronized int getCurrentCadence() {
		if (!active) {
			Log.i(TAG, "Detector is inactive, can not get cadence.");
			return 0;
		}
		try {
			int axisIndex = findVerticalAxis();
			float g = values.getFirst().averagedValues[axisIndex];
			float threshold = Math.abs(g / 2);
			long[] footFallTimestamps = new long[NUMBER_OF_FOOT_FALLS];
			int numberOfFootFalls = 0;
			boolean inThreshold = false;
			int i = 0;
			while (true) {
				Acceleration acceleration = values.get(i++);
				float a = acceleration.lowPassFilteredValues[axisIndex] - g;
				if (inThreshold) {
					if (a < 0) {
						inThreshold = false;
					}
				} else {
					if (a > threshold) {
						inThreshold = true;
						footFallTimestamps[numberOfFootFalls++] = acceleration.timestamp;
					}
				}
				if (numberOfFootFalls == NUMBER_OF_FOOT_FALLS) {
					break;
				}
			}
			return calculateCadenceByFootFallTimestamp(footFallTimestamps);
		} catch (NoSuchElementException e) {
			Log.d(TAG, "No sensor event");
			return 0;
		} catch (IndexOutOfBoundsException e) {
			Log.d(TAG, "No enough sensor events");
			return 0;
		}
	}

	/**
	 * Calculate cadence by timestamp of last foot falls, return the average of middle values.
	 * 
	 * @param footFallTimestamps
	 * @return strides per minute
	 */
	private int calculateCadenceByFootFallTimestamp(long[] footFallTimestamps) {
		long[] footFallIntervale = new long[NUMBER_OF_FOOT_FALLS - 1];
		for (int i = 0; i < (NUMBER_OF_FOOT_FALLS - 1); i++) {
			footFallIntervale[i] = footFallTimestamps[i] - footFallTimestamps[i + 1];
		}
		Arrays.sort(footFallIntervale);
		long sum = 0;
		for (int i = 1; i < NUMBER_OF_FOOT_FALLS - 2; i++) {
			sum += footFallIntervale[i];
		}
		long average = sum / NUMBER_OF_FOOT_FALLS - 3;
		return (int) (60 * SECOND_TO_NANOSECOND / 2 / average);

	}

	/**
	 * The axis which has biggest average acceleration value is close to
	 * vertical. Because the earth gravity is a constant.
	 * 
	 * @return index of the axis (0~2)
	 */
	private int findVerticalAxis() {
		Acceleration latestValue = values.getFirst();
		float maxValue = 0;
		int maxValueAxis = 0;
		for (int i = 0; i < 3; i++) {
			float absValue = Math.abs(latestValue.averagedValues[i]);
			if (absValue > maxValue) {
				maxValue = absValue;
				maxValueAxis = i;
			}
		}
		return maxValueAxis;
	}

	private void removeValuesOlderThan(long timestamp) {
		while (!values.isEmpty()) {
			if (values.getLast().timestamp < timestamp) {
				values.removeLast();
			} else {
				return;
			}
		}
	}

	private void lowPassFilter(float[] result, float[] currentValue, long currentTime, float[] prevValue,
			long prevTime, float cutoffFequency) {
		long deltaTime = currentTime - prevTime;
		float alpha = (float) (cutoffFequency * 3.14 * 2 * deltaTime / SECOND_TO_NANOSECOND);
		if (alpha > 1) {
			alpha = 1;
		}
		for (int i = 0; i < 3; i++) {
			result[i] = prevValue[i] + alpha * (currentValue[i] - prevValue[i]);
		}
	}
}

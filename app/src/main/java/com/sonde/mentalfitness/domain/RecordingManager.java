package com.sonde.mentalfitness.domain;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.sonde.voip_vad.OnWaveFileReadyListener;
import com.sonde.voip_vad.SondeWaveFileManager;

import java.nio.ByteBuffer;

public class RecordingManager {
    private static final String TAG = RecordingManager.class.getSimpleName();


    private final Context context;
    private boolean keepAliveRendererRunnable = true;
    // We want to get as close to 10 msec buffers as possible because this is what the media engine prefers.
    private static final int CALLBACK_BUFFER_SIZE_MS = 10;
    // Default audio data format is PCM 16 bit per sample. Guaranteed to be supported by all devices.
    private static final int BITS_PER_SAMPLE = 16;
    // Average number of callbacks per second.
    private final int BUFFERS_PER_SECOND = 1000 / CALLBACK_BUFFER_SIZE_MS;
    // Ask for a buffer size of BUFFER_SIZE_FACTOR * (minimum required buffer size). The extra space
    // is allocated to guard against glitches under high load.
    private static final int BUFFER_SIZE_FACTOR = 2;
    private static final int WAV_FILE_HEADER_SIZE = 44;

    private ByteBuffer fileWriteByteBuffer;

    private AudioRecord audioRecord;
    private ByteBuffer micWriteBuffer;

    private ByteBuffer readByteBuffer;
    private AudioTrack audioTrack = null;

    // Handlers and Threads
    private Handler capturerHandler;
    private HandlerThread capturerThread;
    private SondeWaveFileManager fileManager;

    private final int AUDIO_SAMPLE_RATE = 44100;
    private final int AUDIO_CHANNEL_MONO = 1;

    /*
     * This Runnable reads data from the microphone and provides the audio frames to the AudioDevice
     * API via AudioDevice.audioDeviceWriteCaptureData(..) until the capturer input switches to
     * microphone or the call ends.
     */
    private final Runnable microphoneCapturerRunnable = () -> {
        fileManager.resetAll();
        Log.d("!!", "microphoneCapturerRunnable called=====");
        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
        audioRecord.startRecording();
        while (true) {
            int bytesRead = audioRecord.read(micWriteBuffer, micWriteBuffer.capacity());
            if (bytesRead == micWriteBuffer.capacity()) {
                if (!fileManager.isCompleteFileCaptured()) {
                    fileManager.writePCMData(micWriteBuffer, true);
                }
            } else {
                String errorMessage = "AudioRecord.read failed: " + bytesRead;
                Log.e(TAG, errorMessage);
                if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                    stopRecording();
                    Log.e(TAG, errorMessage);
                }
                break;
            }
        }
    };



    public RecordingManager(Context context, int audioSource) {
        this.context = context;
        this.fileManager = new SondeWaveFileManager(context);
        onInitCapturer(audioSource);
    }


    /*
     * Init the capturer using the AudioFormat return by getCapturerFormat().
     */
    private boolean onInitCapturer(int audioSource) {
        int bytesPerFrame = 2 * (BITS_PER_SAMPLE / 8);
        int framesPerBuffer = AUDIO_SAMPLE_RATE / BUFFERS_PER_SECOND;
        // Calculate the minimum buffer size required for the successful creation of
        // an AudioRecord object, in byte units.
        int channelConfig = channelCountToConfiguration(AUDIO_CHANNEL_MONO);
        int minBufferSize =
                AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                        channelConfig, android.media.AudioFormat.ENCODING_PCM_16BIT);
        micWriteBuffer = ByteBuffer.allocateDirect(bytesPerFrame * framesPerBuffer);
        int bufferSizeInBytes = Math.max(BUFFER_SIZE_FACTOR * minBufferSize, micWriteBuffer.capacity());
        audioRecord = new AudioRecord(audioSource, AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, android.media.AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);

        fileWriteByteBuffer = ByteBuffer.allocateDirect(bytesPerFrame * framesPerBuffer);
        return true;
    }

    public boolean onStartCapturing() {
        // Create the capturer thread and start
        capturerThread = new HandlerThread("CapturerThread");
        capturerThread.start();
        // Create the capturer handler that processes the capturer Runnables.
        capturerHandler = new Handler(capturerThread.getLooper());
        capturerHandler.post(microphoneCapturerRunnable);
        return true;
    }

    public boolean onStopCapturing() {
        stopRecording();

        /*
         * When onStopCapturing is called, the AudioDevice API expects that at the completion
         * of the callback the capturer has completely stopped. As a result, quit the capturer
         * thread and explicitly wait for the thread to complete.
         */
        capturerThread.quit();
        return true;
    }


    private void stopRecording() {
        Log.d(TAG, "Remove any pending posts of microphoneCapturerRunnable that are in the message queue ");
        capturerHandler.removeCallbacks(microphoneCapturerRunnable);
        try {
            if (audioRecord != null) {
                audioRecord.stop();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "AudioRecord.stop failed: " + e.getMessage());
        }
    }

    private int channelCountToConfiguration(int channels) {
        return (channels == 1 ? android.media.AudioFormat.CHANNEL_IN_MONO : android.media.AudioFormat.CHANNEL_IN_STEREO);
    }


    public void setOnWaveFileReadyListener(OnWaveFileReadyListener onWaveFileReadyListener) {
        this.fileManager.setOnWaveFileReadyListener(onWaveFileReadyListener);
    }


    public synchronized void forceBuildAudioFile() {
        fileManager.forceBuildAudioFile();
    }
}

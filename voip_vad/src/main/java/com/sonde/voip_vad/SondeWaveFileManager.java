package com.sonde.voip_vad;


import static com.sonde.voip_vad.CallAudioFileBuilder.REQUIRED_AUDIO_SIZE_SECONDS;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SondeWaveFileManager {
    private String audioFilename;
    private FileOutputStream outputStream;
    private String fileName;
    private int totalBytesRead;
    //    private int totalDuration = 0;
    private static final int CALLBACK_BUFFER_SIZE_MS = 10;
    // Default audio data format is PCM 16 bit per sample. Guaranteed to be supported by all devices.
    // Average number of callbacks per second.
    private int BUFFERS_PER_SECOND = 1000 / CALLBACK_BUFFER_SIZE_MS;
    private final Context context;
    private final VadLib vadLib;
    private final CallAudioFileBuilder callAudioFileBuilder;
    private int perSecondByteCount = 0;
    public static final int AUDIO_SAMPLE_RATE_44100 = 44100;
    public static final int SEGMENETED_AUDIO_FILE_SIZE = AUDIO_SAMPLE_RATE_44100 * 1 * 2 * 3;
    private OnWaveFileReadyListener onWaveFileReadyListener;

    public SondeWaveFileManager(Context context) {
        this.context = context;
        this.vadLib = new VadLib(context);
        this.callAudioFileBuilder = new CallAudioFileBuilder(context);
    }

    private void createNewFile() throws FileNotFoundException {
        this.fileName = context.getFilesDir().getAbsolutePath() + "/" + System.currentTimeMillis() + ".wav";
        this.audioFilename = fileName + ".pcm";
        this.outputStream = new FileOutputStream(audioFilename);
    }

    public synchronized void writePCMData(ByteBuffer micWriteBuffer, boolean isMono) {
        try {
            if (totalBytesRead == 0) {
                createNewFile();
            }
            byte[] audioBuffer = getByteArrayFromByteBuffer(micWriteBuffer, isMono);
            int numBytesRead = audioBuffer.length;
            perSecondByteCount += numBytesRead;
            totalBytesRead += numBytesRead;
            outputStream.write(audioBuffer, 0, numBytesRead);
            if (totalBytesRead >= SEGMENETED_AUDIO_FILE_SIZE) {
                String audioFilePath = writePcmToWave();
                Log.d("!!", "audioFilePath===:==" + audioFilePath);
//                getScoreOfSegment(audioFilePath);
                processOnSeparateThread(audioFilePath);
            }
        } catch (IOException e) {
            Log.d("!!", "Error: ", e);
        }
    }

    private void getScoreOfSegment(String audioFilePath) {
        new Thread(() -> {
            if (onWaveFileReadyListener != null) {
                onWaveFileReadyListener.onWavFileReadyForSegmentScore(audioFilePath,0);
            }
        }).start();
    }

    private void processOnSeparateThread(String audioFilePath) {
        new Thread(() -> {
            Log.d("!!", "audioFile size===:==" + new File(audioFilePath).length());
            if (audioFilePath != null) {
//                String csvFilePath = vadLib.performVAD(audioFilePath);
//                Log.d("!!", "csvFilePath===:==" + csvFilePath);
//                if (csvFilePath != null) {
////                    callAudioFileBuilder.trimAudio(audioFilePath, csvFilePath);
//                    callAudioFileBuilder.allowAudio(audioFilePath, csvFilePath);
//                }
                callAudioFileBuilder.allowAudio(audioFilePath);

                Log.d("!!", "Done with processOnSeparateThread===:==");
            }
        }).start();
    }

    private void reset() {
        //reset data after writing wav file
//        totalDuration = 0;
        totalBytesRead = 0;
    }

    private static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer, boolean isMono) {
        byte[] array = byteBuffer.array();
        int arrayOffset = byteBuffer.arrayOffset();
        byte[] stereoSamples = Arrays.copyOfRange(array, arrayOffset + byteBuffer.position(),
                arrayOffset + byteBuffer.limit());
        if (isMono) {
            return stereoSamples;
        }

        byte[] monoSamples = new byte[stereoSamples.length / 2];
        for (int i = 0; i < monoSamples.length / 2; ++i) {
            int left = (stereoSamples[i * 4] << 8) | (stereoSamples[i * 4 + 1] & 0xff);
            int right = (stereoSamples[i * 4 + 2] << 8) | (stereoSamples[i * 4 + 3] & 0xff);
            int avg = (left + right) / 2;
            short m = (short) avg;
            monoSamples[i * 2] = (byte) ((short) (m >> 8));
            monoSamples[i * 2 + 1] = (byte) (m & 0xff);
        }
        return monoSamples;
    }

    private synchronized String writePcmToWave() {
        if (totalBytesRead == 0) {
            return null;
        }
        OutputStream waveOutputStream;
        try {
            waveOutputStream = new FileOutputStream(fileName);

            InputStream dataInputStream = new FileInputStream(audioFilename);
            short numChannels = (short) (1);
            short sampleSizeBytes = (short) (2);
            WaveUtils.pcmToWave(waveOutputStream,
                    dataInputStream,
                    totalBytesRead,
                    numChannels,
                    AUDIO_SAMPLE_RATE_44100,
                    sampleSizeBytes);
            new File(audioFilename).delete();
            reset();
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setOnWaveFileReadyListener(OnWaveFileReadyListener onWaveFileReadyListener) {
        this.onWaveFileReadyListener = onWaveFileReadyListener;
        callAudioFileBuilder.setOnWaveFileReadyListener(onWaveFileReadyListener);
    }

    public synchronized void forceBuildAudioFile() {
        String audioFilePath = writePcmToWave();
        if (audioFilePath != null) {
            String csvFilePath = vadLib.performVAD(audioFilePath);
            if (csvFilePath != null) {
                callAudioFileBuilder.trimAudio(audioFilePath, csvFilePath);
                if (callAudioFileBuilder.getTotalDuration() < REQUIRED_AUDIO_SIZE_SECONDS) {
//                    callAudioFileBuilder.forceBuildAudioFile();
                    Toast.makeText(context, "Additional audio is needed to calculate your score", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public boolean isCompleteFileCaptured() {
        return callAudioFileBuilder.getTotalDuration() >= REQUIRED_AUDIO_SIZE_SECONDS;
    }

    public void resetAll() {
        reset();
        callAudioFileBuilder.reset();
    }
}

package com.sonde.voip_vad;

import static com.sonde.voip_vad.SondeWaveFileManager.AUDIO_SAMPLE_RATE_44100;

import android.content.Context;
import android.util.Log;

import com.opencsv.CSVReader;

import net.idrnd.android.media.AssetsExtractor;
import net.idrnd.voicesdk.antispoof2.AntispoofEngine;
import net.idrnd.voicesdk.antispoof2.AntispoofResult;
import net.idrnd.voicesdk.core.common.VoiceTemplate;
import net.idrnd.voicesdk.media.SpeechSummary;
import net.idrnd.voicesdk.media.SpeechSummaryEngine;
import net.idrnd.voicesdk.verify.VerifyResult;
import net.idrnd.voicesdk.verify.VoiceTemplateFactory;
import net.idrnd.voicesdk.verify.VoiceTemplateMatcher;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CallAudioFileBuilder {
    private String audioFilename;
    private FileOutputStream outputStream;
    private String fileName;
    private int totalBytesRead;
    private int totalDuration;
    private int segmentNumber;
    private OnWaveFileReadyListener onWaveFileReadyListener;
    private final Context context;
    private static final String TAG = CallAudioFileBuilder.class.getSimpleName();
    private final VoiceTemplateFactory templateFactory;
    private final VoiceTemplateMatcher templateMatcher;
    private final AntispoofEngine antispoofEngine;
    private final SpeechSummaryEngine speechSummaryEngine;
    public static final int REQUIRED_AUDIO_SIZE_SECONDS = 30;
    public static final float REQUIRED_MINIMUM_ACTIVE_AUDIO_SIZE_SECONDS = 1.1f;
    public static final float REQUIRED_MINIMUM_ACTIVE_AUDIO_SIZE_MILLISECONDS = 500.0f;
    public static final float REQUIRED_MAXIMUM_NO_AUDIO_SIZE_MILLISECONDS = 100.0f;


    public CallAudioFileBuilder(Context context) {
        this.context = context;
        String sharedPreferencesName = "my_preferences";
        // Init singletons
        GlobalPrefs.INSTANCE.init(context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE));
        File initDataFolder = new AssetsExtractor(context).extractAssets();
        this.templateFactory = new VoiceTemplateFactory(new File(initDataFolder, AssetsExtractor.VERIFY_INIT_DATA_MIC_V1_SUBPATH).getAbsolutePath());
        this.templateMatcher = new VoiceTemplateMatcher(new File(initDataFolder, AssetsExtractor.VERIFY_INIT_DATA_MIC_V1_SUBPATH).getAbsolutePath());
        this.antispoofEngine = new AntispoofEngine(new File(initDataFolder, AssetsExtractor.ANTISPOOF_INIT_DATA_SUBPATH).getAbsolutePath());
        this.speechSummaryEngine = new SpeechSummaryEngine(new File(initDataFolder, AssetsExtractor.SPEECH_SUMMARY_INIT_DATA_SUBPATH).getAbsolutePath());

    }


    public synchronized void writePCMData(ByteBuffer micWriteBuffer) {
        try {
            if (totalDuration == 0) {
                createNewFile();
            }

            byte[] audioBuffer = getByteArrayFromByteBuffer(micWriteBuffer);
            int numBytesRead = audioBuffer.length;
            totalBytesRead += numBytesRead;
            outputStream.write(audioBuffer, 0, numBytesRead);
            totalDuration++;

            Log.d("!!", "totalDuration====" + totalDuration);
        } catch (IOException e) {
            Log.e(TAG, "Error : ", e);
        }

    }

    private synchronized void createNewFile() throws FileNotFoundException {
        this.fileName = context.getFilesDir().getAbsolutePath() + "/" + System.currentTimeMillis() + ".wav";
        this.audioFilename = fileName + ".pcm";
        this.outputStream = new FileOutputStream(audioFilename);
    }

    private static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] array = byteBuffer.array();
        int arrayOffset = byteBuffer.arrayOffset();
        return Arrays.copyOfRange(array, arrayOffset + byteBuffer.position(),
                arrayOffset + byteBuffer.limit());
    }

    public synchronized void writePcmToWave() throws IOException {
        OutputStream waveOutputStream = null;
        try {
            if (!new File(audioFilename).exists()) {
                return;
            }
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
            boolean isDeleted = new File(audioFilename).delete();
            Log.d("!!", "File delete status : " + isDeleted);
            if (onWaveFileReadyListener != null) {
                onWaveFileReadyListener.onWavFileReady(fileName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error : ", e);
        }
        finally {
            waveOutputStream.close();
            outputStream.close();
        }
    }

    public synchronized void reset() {
        //reset data after writing wav file
        totalDuration = 0;
        totalBytesRead = 0;
        segmentNumber = 0;
    }

    public void setOnWaveFileReadyListener(OnWaveFileReadyListener onWaveFileReadyListener) {
        this.onWaveFileReadyListener = onWaveFileReadyListener;
    }

    public synchronized void trimAudio(String audioFilePath, String csvFilePath) {
        Set<Integer> activeVoices = new HashSet<>();
        List<String[]> csvDataList;
        FileInputStream inputStream;
        try {
            csvDataList = readCsvFile(csvFilePath);
            for (int i = 1; i < csvDataList.size(); i++) {
                String[] rows = csvDataList.get(i);
                if (rows[3].equals("Active voice")) {
                    int start = Math.round(Float.parseFloat(rows[1]));
                    int end = Math.round(Float.parseFloat(rows[2]));
                    Log.d("!!", "start : " + start + ", end : " + end);
                    activeVoices.add(start);
                    //add the time range b/w start and end
                    for (int j = start + 1; j < end; j++) {
                        activeVoices.add(j);
                    }
                    activeVoices.add(end);
                }
            }
            inputStream = new FileInputStream(audioFilePath);
            long skipped = inputStream.skip(44);
//        skipped = inputStream.skip(AudioFormat.AUDIO_SAMPLE_RATE_44100*1*2);
//        skipped = inputStream.skip(AudioFormat.AUDIO_SAMPLE_RATE_44100*1*2);

            ArrayList<ByteBuffer> byteBuffers = new ArrayList<>();
            ByteBuffer micWriteBuffer = ByteBuffer.allocate(AUDIO_SAMPLE_RATE_44100 * 1 * 2);// 1 second voice
//        int framesPerBuffer = AudioFormat.AUDIO_SAMPLE_RATE_44100/2048;
//        int bytesPerSec = (AudioFormat.AUDIO_SAMPLE_RATE_44100 * (16/8) * 2);
//        byte[] buffer = new byte[2048];
            int count = 1;
            int totalBytes = 0;
            int bytesRead;
            while ((bytesRead = inputStream.read(micWriteBuffer.array())) == micWriteBuffer.capacity()) {
                if (activeVoices.contains(count)) {
                    byteBuffers.add(clone(micWriteBuffer));
                    totalBytes = totalBytes + bytesRead;
                }
                count++;
            }
            RecordingData recordingData = new RecordingData();
            recordingData.noOfSecond = totalDuration;
            if (!isVerifiedUserAudio(byteBuffers, totalBytes)) {
                Log.d("!!", "User is Not verified!!!!!");
                recordingData.isUserVerified = false;
                EventBus.getDefault().post(recordingData);
                return;
            }
            Log.d("!!", "User is verified!!!!!");
            recordingData.isUserVerified = true;
            for (int i = 0; i < byteBuffers.size(); i++) {
                writePCMData(byteBuffers.get(i));
                recordingData.noOfSecond = totalDuration;
                EventBus.getDefault().post(recordingData);
                if (totalDuration >= REQUIRED_AUDIO_SIZE_SECONDS) {
                    writePcmToWave();
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            new File(audioFilePath).delete();
            new File(csvFilePath).delete();
        }
    }

    /*
    Rahul's Code
     */
    public synchronized void allowAudio(String audioFilePath) {

        List<String[]> csvDataList;
        FileInputStream inputStream;
        float totalActiveVoiceDuration = 0f;
        try {
//            csvDataList = readCsvFile(csvFilePath);
//            Log.d(TAG, "csvDataList size==>" + csvDataList.size());
//            for (int i = 1; i < csvDataList.size(); i++) {
//                String[] rows = csvDataList.get(i);
//                if (rows[3].equals("Active voice")) {
//                    float start = Float.parseFloat(rows[1]);
//                    float end = Float.parseFloat(rows[2]);
//                    Log.d(TAG, "start : " + start + ", end : " + end);
//                    totalActiveVoiceDuration += (end - start);
//                    if (totalActiveVoiceDuration >= REQUIRED_MINIMUM_ACTIVE_AUDIO_SIZE_SECONDS) {
//                        Log.d(TAG, "In Break, totalActiveVoiceDuration="+totalActiveVoiceDuration);
//                        break;
//                    }
//                }
//            }
//            if (totalActiveVoiceDuration < REQUIRED_MINIMUM_ACTIVE_AUDIO_SIZE_SECONDS) {
//                Log.d(TAG, "Segment with insufficient active audio, totalActiveVoiceDuration="+totalActiveVoiceDuration);
//                RecordingData recordingData = new RecordingData();
//                recordingData.noOfSecond = totalDuration;
//                recordingData.isNoVoice = true;
//                recordingData.isUserVerified = false;
//                EventBus.getDefault().post(recordingData);
//                return;
//            }
            RecordingDataType type = isVoiceDetected(audioFilePath);
            RecordingData recordingData = new RecordingData();
            recordingData.recordingDataType = type;
            if (type == RecordingDataType.ACTIVE_VOICE) {

                inputStream = new FileInputStream(audioFilePath);
                ArrayList<ByteBuffer> byteBuffers = new ArrayList<>();
                ByteBuffer micWriteBuffer = ByteBuffer.allocate(AUDIO_SAMPLE_RATE_44100 * 1 * 2);// 1 second voice
                int totalBytes = 0;
                int bytesRead;
                while ((bytesRead = inputStream.read(micWriteBuffer.array())) == micWriteBuffer.capacity()) {
                    byteBuffers.add(clone(micWriteBuffer));
                    totalBytes = totalBytes + bytesRead;
                }

                recordingData.noOfSecond = totalDuration;
                if (!isVerifiedUserAudio(byteBuffers, totalBytes)) {
                    Log.d(TAG, "User is Not verified!!!!!");
                    recordingData.isUserVerified = false;
                    new File(audioFilePath).delete();
                    EventBus.getDefault().post(recordingData);
                    return;
                }
                Log.d(TAG, "User is verified!!!!!");
                recordingData.isUserVerified = true;
                segmentNumber++;
                onWaveFileReadyListener.onWavFileReadyForSegmentScore(audioFilePath, segmentNumber);
                for (int i = 0; i < byteBuffers.size(); i++) {
                    writePCMData(byteBuffers.get(i));
                    if (totalDuration % 3 == 0) {
                        recordingData.noOfSecond = totalDuration;
                        EventBus.getDefault().post(recordingData);
                    }
                    if (totalDuration >= REQUIRED_AUDIO_SIZE_SECONDS) {
                        writePcmToWave();
                        return;
                    }
                }

            } else {
                recordingData.isUserVerified = false;
                recordingData.noOfSecond = totalDuration;
                new File(audioFilePath).delete();
                EventBus.getDefault().post(recordingData);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            new File(audioFilePath).delete();
//            new File(csvFilePath).delete();
        }
    }

    private RecordingDataType isVoiceDetected(String audioFilePath) {

        SpeechSummary result = speechSummaryEngine.getSpeechSummary(audioFilePath);
        float speechLengthMs = result.getSpeechInfo().getSpeechLengthMs();
        float backgroundLengthMs = result.getSpeechInfo().getBackgroundLengthMs();
        Log.d("CallAudioFileBuilder", "isVoiceDetected==>" + result);
        if (speechLengthMs >= REQUIRED_MINIMUM_ACTIVE_AUDIO_SIZE_MILLISECONDS) {
            //Having active voice
            return RecordingDataType.ACTIVE_VOICE;
        } else if (speechLengthMs <= REQUIRED_MAXIMUM_NO_AUDIO_SIZE_MILLISECONDS) {
            //no voice
            return RecordingDataType.NO_VOICE;
        } else {
            //insufficient voice
            return RecordingDataType.INSUFFICIENT_VOICE;
        }
    }


    private boolean isVerifiedUserAudio(ArrayList<ByteBuffer> byteBuffers, int totalBytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(totalBytes);
        for (int i = 0; i < byteBuffers.size(); i++) {
            byte[] audioBuffer = getByteArrayFromByteBuffer(byteBuffers.get(i));
            byteBuffer.put(audioBuffer);
        }
        // Read bytes from output file
        byte[] readBytes = byteBuffer.array();

        try {
            // Make template from record
            VoiceTemplate template = templateFactory.createVoiceTemplate(readBytes, AUDIO_SAMPLE_RATE_44100);

            // Get user's template
            VoiceTemplate enrolledTemplate = VoiceTemplate.loadFromFile(GlobalPrefs.INSTANCE.getTemplateFilepath());

            // Match enrolled templates with new template

            VerifyResult verifyResult = templateMatcher.matchVoiceTemplates(template, enrolledTemplate);

            AntispoofResult spoof = antispoofEngine.isSpoof(readBytes, AUDIO_SAMPLE_RATE_44100);
            Log.d("!!", "probability==" + verifyResult.getProbability() + ", spoof score is==" + spoof.getScore());
            return (verifyResult.getProbability() * 100) >= 90 && (spoof.getScore() * 100) >= 80;
        } catch (Exception e) {
            Log.d("!!", "error in user verification : error:" + e);
        }
        return false;
    }

    public synchronized void forceBuildAudioFile() throws IOException {
        writePcmToWave();
    }

    private synchronized List<String[]> readCsvFile(String csvFilePath) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));
        return csvReader.readAll();
    }

    private synchronized ByteBuffer clone(ByteBuffer original) {
        ByteBuffer clone = ByteBuffer.allocate(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }

    public synchronized int getTotalDuration() {
        return totalDuration;
    }
}

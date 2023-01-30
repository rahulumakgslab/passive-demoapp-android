package com.sonde.voip_vad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WaveUtils {
    private static final String RIFF_CHUNK_ID = "RIFF";
    private static final String RIFF_CHUNK_FORMAT = "WAVE";
    private static final String FMT_CHUNK_ID = "fmt ";
    private static final String DATA_CHUNK_ID = "data";
    private static final int FMT_CHUNK_SIZE = 16;
    private static final short FMT_ID_PCM = 1;

    public static void pcmToWave(OutputStream outputStream,
                                 InputStream inputStream,
                                 int numPcmBytes,
                                 short numChannels,
                                 int sampleRateHz,
                                 short sampleSizeBytes) throws IOException {
        IoUtils.writeString(outputStream, RIFF_CHUNK_ID);
        IoUtils.writeIntLittleEndian(outputStream, 36 + numPcmBytes);
        IoUtils.writeString(outputStream, RIFF_CHUNK_FORMAT);

        IoUtils.writeString(outputStream, FMT_CHUNK_ID);
        IoUtils.writeIntLittleEndian(outputStream, FMT_CHUNK_SIZE);
        IoUtils.writeShortLittleEndian(outputStream, FMT_ID_PCM);
        IoUtils.writeShortLittleEndian(outputStream, numChannels);
        IoUtils.writeIntLittleEndian(outputStream, sampleRateHz);
        IoUtils.writeIntLittleEndian(outputStream, numChannels * sampleRateHz * sampleSizeBytes);
        IoUtils.writeShortLittleEndian(outputStream, (short) (numChannels * sampleSizeBytes));
        IoUtils.writeShortLittleEndian(outputStream, (short) (sampleSizeBytes * 8));

        IoUtils.writeString(outputStream, DATA_CHUNK_ID);
        IoUtils.writeIntLittleEndian(outputStream, numPcmBytes);
        byte[] buffer = new byte[4096];
        int numBytesRead;
        while ((numBytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, numBytesRead);
        }
    }
}

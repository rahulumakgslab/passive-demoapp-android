package com.sonde.voip_vad;

import java.io.IOException;
import java.io.OutputStream;

class IoUtils {
    static void writeString(OutputStream outputStream, String val) throws IOException {
        for (int i = 0; i < val.length(); i++) {
            outputStream.write(val.charAt(i));
        }
    }

    static void writeIntLittleEndian(OutputStream outputStream, int val) throws IOException {
        outputStream.write(val);
        outputStream.write(val >> 8);
        outputStream.write(val >> 16);
        outputStream.write(val >> 24);
    }

    static void writeShortLittleEndian(OutputStream outputStream, short val) throws IOException {
        outputStream.write(val);
        outputStream.write(val >> 8);
    }
}

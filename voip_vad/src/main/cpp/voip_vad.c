#include <jni.h>
#include "h/sonde_sp_api.h"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)


JNIEXPORT int JNICALL
Java_com_sonde_voip_1vad_VadLib_performVAD(JNIEnv *env, jobject obj, jstring inputAudioFilePath,
                                           jstring configFilePath, jstring outPutDirPath) {

    const char *input_audio_file_path = (*env)->GetStringUTFChars(env,inputAudioFilePath,0);
    const char *config_file_path = (*env)->GetStringUTFChars(env,configFilePath,0);
    const char *out_put_dir_path = (*env)->GetStringUTFChars(env,outPutDirPath,0);
//     const char *resource_folder_path = (*env)->GetStringUTFChars(env,resourceFolderPath,0);

    return sp_elck( input_audio_file_path, config_file_path, out_put_dir_path);
}
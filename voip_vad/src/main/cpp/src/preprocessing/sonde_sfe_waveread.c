/*
* @brief  read wave file.
*
* @author Swapnil Warkar
*
*/


#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <math.h>
#include <assert.h>
#include <string.h>
#include "sonde_spfe_function_def.h"

int copy_org_algo_params( s_shared_params * shared_params, s_user_config * user_config, int samplerate, int actualsamples );

int handle_stereo(s_fileinfo * file_parameters, s_shared_params * shared_params)
{
    if (file_parameters->WaveHeader.channels == 2)
    {
        t_int16 *RightChan = (t_int16 *)malloc(((shared_params->SampleCount / 2 - 1) + 1) * sizeof(RightChan));
        t_int16 *LeftChan = (t_int16 *)malloc(((shared_params->SampleCount / 2 - 1) + 1) * sizeof(LeftChan));
        static t_int32 Kelem      = 0;
        static t_int32 ChannelDex = 0;
        int left_channel_length   = ((shared_params->SampleCount / 2 - 1) + 1);

        while (Kelem < shared_params->SampleCount - 1)
        {
            LeftChan[ChannelDex] = shared_params->pcm_data[Kelem];
            RightChan[ChannelDex] = shared_params->pcm_data[Kelem + 1];
            Kelem = Kelem + 2;
            ChannelDex++;
        }

        shared_params->SampleCount = ChannelDex;
        file_parameters -> actual_samples = ChannelDex * 2;
        stereo_channel_processing(LeftChan,RightChan,left_channel_length,shared_params);
        free(LeftChan);
        free(RightChan);
    }
}

int raise_exception( s_fileinfo * file_parameters )
{
    char id[15];
    strcpy(id,file_parameters->WaveHeader.wavID);
    id[4] = '\0';
    int sampling_rate = file_parameters->WaveHeader.sampleRate;

    if ( strcmp(file_parameters->WaveHeader.riffID,"RIFF") != 0 )
    {
        fprintf(stderr,"\n\t Error : invalid input file, expected RIFF header not present! \n");
        t_int32 err = elck_raise_error(E_NOT_A_RIFF);
        if(err != 0)
        {
            return -1;
        }
    }

    if ( strcmp(id,"WAVE") != 0 )
    {
        fprintf(stderr,"\n\t Error : invalid input file, expected WAVE format not present! \n");
        t_int32 err = elck_raise_error(E_NOT_A_WAV);
        if(err != 0)
        {
            return -1;
        }
    }

    if ( file_parameters->WaveHeader.fmtSize != 16 )
    {
        fprintf(stderr,"\n\t Error : invalid input file, sample resolution is not 16-bit! \n");
        t_int32 err = elck_raise_error(E_NOT_A_16_BIT_PCM);
        if(err != 0)
        {
            return -1;
        }
    }

    if ( file_parameters->WaveHeader.format != 1 )
    {
        fprintf(stderr,"\n\t Error : invalid input file, compression type is not matching! \n");
        t_int32 err = elck_raise_error(E_FILE_NOT_FOUND);
        if(err != 0)
        {
            return -1;
        }
    }

    if ( !(sampling_rate == 44100 || sampling_rate == 48000 || sampling_rate == 16000 || sampling_rate == 8000) ) // error handling
    {
        fprintf(stderr,"\n\t Error : sampling rate not supported by sonde-sp-fe library, expected is 48k/44.1k/8k/16k! present sr is : %d\n",sampling_rate);
        t_int32 err = elck_raise_error(E_UNSUPPORTED_SAMPLE_RATE);
        if(err != 0)
        {
            return -1;
        }
    }
    return 0;
}

t_int32 read_wave_file(s_fileinfo * file_parameters,s_paths_and_meta * paths_and_meta, s_shared_params * shared_params, s_user_config * user_config)
{
    fprintf(stderr,"\n	start : read wave file\n");
    static t_int32 UsedSamples = 0;

    static FILE *fp;
    fp = fopen(paths_and_meta->input_filename, "r");
    int get_cnt = 0;

    get_cnt = fread(&file_parameters->WaveHeader.riffID, sizeof(file_parameters->WaveHeader.riffID)-1, 1, fp);
    get_cnt = fread(&file_parameters->WaveHeader.size, sizeof(t_uint32), 1, fp);
    get_cnt = fread(&file_parameters->WaveHeader.wavID, sizeof(file_parameters->WaveHeader.wavID), 1, fp);
    get_cnt = fread(&file_parameters->WaveHeader.fmtID, sizeof(file_parameters->WaveHeader.fmtID), 1, fp);
    get_cnt = fread(&file_parameters->WaveHeader.fmtSize, sizeof(t_uint32), 1, fp);
    get_cnt = fread(&file_parameters->WaveHeader.format, sizeof(t_uint16), 1, fp);
    get_cnt = fread(&file_parameters->WaveHeader.channels, sizeof(t_uint16), 1, fp);
    get_cnt = fread(&file_parameters->WaveHeader.sampleRate, sizeof(t_uint32), 1, fp);
    get_cnt = fread(&file_parameters->WaveHeader.bytePerSec, sizeof(t_uint32), 1, fp);
    get_cnt = fread(&file_parameters->WaveHeader.blockSize, sizeof(t_uint16), 1, fp);
    get_cnt = fread(&file_parameters->WaveHeader.bitDepth, sizeof(t_uint16), 1, fp);
    get_cnt = fread(&file_parameters->WaveHeader.dataID, sizeof(file_parameters->WaveHeader.dataID), 1, fp);
    get_cnt = fread(&file_parameters->WaveHeader.dataSize, sizeof(t_uint32), 1, fp);
    file_parameters->WaveHeader.riffID[4] = '\0';
    file_parameters->WaveHeader.dataSize = file_parameters->WaveHeader.size - 36;

    if (file_parameters->WaveHeader.fmtSize == 18)
    {
        file_parameters->WaveHeader.dataSize = file_parameters->WaveHeader.dataSize - 2;
        fseek(fp, 2, SEEK_CUR);
    }
    
    int execpt = raise_exception(file_parameters);

    if( execpt != 0 )
    {
        return execpt;
    }
    
    shared_params->SampleCount = (t_int32)(file_parameters->WaveHeader.dataSize / file_parameters->WaveHeader.blockSize);
    UsedSamples = shared_params->SampleCount;
    
    fprintf(stderr,"\n	fmt_size                = %d", file_parameters->WaveHeader.fmtSize);
    fprintf(stderr,"\n	header_size             = %d", file_parameters->WaveHeader.size);
    fprintf(stderr,"\n	format                  = %d", file_parameters->WaveHeader.format);
    fprintf(stderr,"\n	channels                = %d", file_parameters->WaveHeader.channels);
    fprintf(stderr,"\n	sample rate             = %d", file_parameters->WaveHeader.sampleRate);
    fprintf(stderr,"\n	blocksize               = %d", file_parameters->WaveHeader.blockSize);
    fprintf(stderr,"\n	byte per sec            = %d", file_parameters->WaveHeader.bytePerSec);
    fprintf(stderr,"\n	bit depth               = %d", file_parameters->WaveHeader.bitDepth);
    fprintf(stderr,"\n	header_data_size        = %d", file_parameters->WaveHeader.dataSize);
    fprintf(stderr,"\n	sample count            = %d", shared_params->SampleCount);

    shared_params->pcm_data = malloc(shared_params->SampleCount * __SIZEOF_SHORT__);
    shared_params->eq_pcm   = malloc(( VAD_PADDING2 + shared_params->SampleCount) * __SIZEOF_FLOAT__);
    file_parameters->actual_samples = fread(shared_params->pcm_data, sizeof(t_int16), shared_params->SampleCount, fp);
    
    //fprintf(stderr,"\n\tNumber of frames\t= %d", shared_params->number_of_frames);
    fprintf(stderr,"\n	actual extracted samples extracted = %d", file_parameters->actual_samples);
    
    if (file_parameters->actual_samples != shared_params->SampleCount) // error handling
    {
        shared_params->SampleCount = file_parameters->actual_samples;
        fprintf(stderr, "\n\t Error : actual sample count is not matching with extracted samples \n");
    }
    if( file_parameters->actual_samples < shared_params->frame_samplerate_step )
    {
	    perror("\n\t Error : invalid input file, sample count less than step sample count");
        t_int32 err = elck_raise_error(E_LESS_SAMPLES);
        if(err != 0)
        {
            return -1;
        }
        
    }
    if ( file_parameters->actual_samples <= 1 )
    {
        fprintf(stderr,"\n\t Error : samples can not be process, sample count : %d\n",file_parameters->actual_samples);
    
        t_int32 err = elck_raise_error(E_ZERO_SAMPLES);
        if(err != 0)
        {
            return -1;
        }
    }
    
    copy_org_algo_params( shared_params, user_config, file_parameters->WaveHeader.sampleRate, file_parameters->actual_samples );

    handle_stereo(file_parameters,shared_params); // stereo channel handling
    fclose(fp);
    fprintf(stderr,"\n\n	exit  : read input wave file\n");
    return E_SUCCESS;

}
 int stereo_channel_processing(t_int16 * LeftChan,t_int16 * RightChan,t_int32 left_channel_length,s_shared_params * shared_params)
 {
    int N = left_channel_length,index=0;
    for (int i = 0; i < N; i++)
    {

        shared_params->pcm_data[index] = ((short)((LeftChan[i] + RightChan[i]) / 2));
        shared_params->pcm_data[index + 1] = ((short)(LeftChan[i])); // adding right channel. 
        index+=2;

    }
    return E_SUCCESS;
 }

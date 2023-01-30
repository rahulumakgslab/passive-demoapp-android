#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <complex.h>
#include "../../h/resample_fft.h"
#include "../../h/sonde_spfe_function_def.h"

static void copy_pcm_data_perframe( t_float * input_pcm, t_int32 num_samples, t_float * out_pcm )
{
    for( t_int32 i = 0; i < num_samples; i++ )
    {
        out_pcm[i] = input_pcm[i];
    }
}

t_int32 get_em_response( t_int32 total_frames, t_int32 voice_frames_count, t_float threshold_percent )
{
	t_int32 result = 0;
	t_float threshold = threshold_percent / 100.0;
	result = ( voice_frames_count >= ( total_frames * threshold ) );
	return result;
}

t_float * energy_meter( s_paths_and_meta * paths_and_meta, s_shared_params * shared_params, s_fileinfo * file_parameters, t_float * em_th_arr )
{
    int PER_FRAME_THRESHOLD = (int)em_th_arr[0];
    int fft_size = shared_params->fft_size;
    int fft_size2 = shared_params->fft_size2;
    int number_of_frames = shared_params->number_of_frames;
    int framestep = shared_params->frame_samplerate_step;
    int framesize = shared_params->frame_samplerate_size;
    
    t_float ham_sum = 0.0;
    t_float I0 = 1e-6; //0.000001;
    int low_energy_count = 0;

	t_float * resample_pcm = NULL;
    t_float * em_output_arr = NULL;
    t_float cur = 0.0;
    t_float samplesum = 0.0;
    t_int32 numsamples_perframe = framesize; //1103
    t_float fft_out[fft_size2];  // 2048
    t_float sound_intensity = 0.0;
    t_float loud1 = 0.0;
    t_float loudness = 0.0;
    int voice_frames = 0;
    t_float percent = 0.0;

    resample_pcm = shared_params->resampled_pcm_data;
    em_output_arr = malloc( 5 * sizeof(t_float) );

    t_float tmp_emphasis[numsamples_perframe];
    t_float in_data[fft_size];
    t_int32 index = 0;
    char idx_num[30];
    t_float resample_pcm_step[framestep];

    compute_windowing(framestep, shared_params);

    t_double * ham = shared_params->ham; 

    for( int i = 0; i < framestep; i++ )
    {
        ham_sum += shared_params->ham[i];
    }

    if( ham_sum <= 0.0 )
    {
        ham_sum = 1.0;
    }

    for( int frame_num = 0; frame_num < number_of_frames; frame_num++ )
    {
        index = frame_num * framestep;	// step calculation
        samplesum = 0;
        copy_pcm_data_perframe( resample_pcm + index, framestep,  resample_pcm_step);

        for (t_int32 i = 0; i < framestep; i++, index++)	// 0 to 1103
        {
            cur  =  resample_pcm_step[i];  
            samplesum = samplesum + ( ham[i] * (cur * cur) );
        }

        sound_intensity = samplesum / ham_sum;
        loud1 = sound_intensity / I0;
        loudness = 10 * log10(loud1+__DBL_EPSILON__);
        
        if( loudness <= PER_FRAME_THRESHOLD)
        {
            low_energy_count = low_energy_count + 1; 
        }    
    }
    
    voice_frames = number_of_frames - low_energy_count;
    
    //fprintf(stderr,"\n");
    fprintf(stderr,"\tnum of frames are = %d\n", number_of_frames);
    fprintf(stderr,"\tlow enery frames are = %d\n", low_energy_count);
    fprintf(stderr,"\tvoice frames are = %d\n", voice_frames);
    fprintf(stderr,"\n");

    percent = ( voice_frames / (t_float)number_of_frames ) * 100;

    em_output_arr[0] = number_of_frames;
    em_output_arr[1] = low_energy_count;
    em_output_arr[2] = voice_frames;
    em_output_arr[3] = percent;
    em_output_arr[4] = (t_float)get_em_response( number_of_frames, voice_frames, em_th_arr[1] );

    return em_output_arr;
}
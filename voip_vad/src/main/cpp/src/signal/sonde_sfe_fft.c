#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include<complex.h>
#include "../../h/resample_fft.h"
#include "../../h/sonde_spfe_function_def.h"

FILE * tmp;

static int write_fft( t_float * fft_out, t_int32 fft_size )
{
    for(int i = 0; i < fft_size; i++ )
    {
        fprintf(tmp, "%f,", fft_out[i]);
    }
    
    fprintf(tmp, "\n");
}

static int write_headers( int fft_size )
{
    char count[20] = {0};
    char token[15] = {0};
    char * str = "fft_fft";
    strcpy( token, str );

    fprintf(tmp, "frametime,");

    for( int i = 0; i < fft_size; i++ )
    {
        sprintf( count, "%d", i );
        strcpy( token, str );
        strcat( token, count );
        fprintf( tmp, "%s,", token );
    }
    fprintf( tmp, "\n" );
}

t_int8 * fft_get_path( s_paths_and_meta * paths_and_meta )
{
    t_int8 * token = "_fft_speech_fe.csv";
    t_int8 * output_dir = get_output_path( paths_and_meta );
    t_int8 * audio_name = get_audio_name( paths_and_meta->input_filename ); 
    t_int8 * fft_out_path = malloc( strlen( output_dir ) + strlen( audio_name ) + strlen( token ) + 1 );
    audio_name = erase_ext( audio_name );
    
    strcpy( fft_out_path, output_dir );
    strcat( fft_out_path, "/");
    strcat( fft_out_path, audio_name );
    strcat( fft_out_path, token );
    
    free( output_dir );

    return fft_out_path;
}

int proc_fft_fbf( s_features * low_level_fe, s_paths_and_meta * paths_and_meta, s_shared_params * shared_params, s_fileinfo * file_parameters )
{
    int fft_size = shared_params->fft_size;
    int fft_size2 = shared_params->fft_size2;
    int number_of_frames = shared_params->number_of_frames;
    
    t_int8 * path = fft_get_path( paths_and_meta );

    tmp = fopen( path, "w" );

	t_float * resample_pcm 		  = NULL;
    t_float *out_emphasisbuf 	  = NULL;

    t_int32 numsamples_perframe = shared_params->frame_samplerate_size; //1103
    t_float fft_out[fft_size2];  // 2048
    resample_pcm 				= shared_params->resampled_pcm_data;

    t_float tmp_emphasis[numsamples_perframe];
    t_float in_data[fft_size];
    t_int32 index               = 0;
    char idx_num[30];
    
    compute_windowing(shared_params->frame_samplerate_size, shared_params);
    write_headers( fft_size2 );

    for( int frame_num = 0; frame_num < number_of_frames; frame_num++ )
    {
        sprintf(idx_num, "%d", abs((t_double)frame_num * 1000 * shared_params->frame_samplerate_step / file_parameters->WaveHeader.sampleRate ));
        fprintf(tmp, "%s,", idx_num);
        
        index = frame_num * shared_params->frame_samplerate_step;	// step calculation
         
        out_emphasisbuf = pre_emphasis_gen( tmp_emphasis, resample_pcm + index, numsamples_perframe);
        //out_emphasisbuf = pre_emphasis_gen(tmp_emphasis, shared_params->normalised_pcm+index, numsamples_perframe);
              
        for (t_int32 i = 0; i < numsamples_perframe; i++, index++)	// 0 to 1103
        {
			// apply hamming window on normalized samples
            out_emphasisbuf[i] = out_emphasisbuf[i] * (t_float)shared_params->ham[i];	
        }

		// symmetric padding 
        pad_symmetric( out_emphasisbuf, in_data, numsamples_perframe, fft_size );	
		// fft calculation
        calc_rdft( fft_size, in_data );	

        complex_to_real( in_data, fft_out, fft_size );	

        write_fft( fft_out, fft_size2 );
    }

    fprintf(stderr, "\n\tFFT details\n");
    fprintf(stderr, "\tFFT bins\t: %d\n", fft_size);
    fprintf(stderr, "\tFFT magnitude\t: %d\n\n", fft_size2);

    fclose(tmp);
}
/*
* @brief preprocessing and setting of all required parameters
*
* @author Swapnil Warkar
*
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <unistd.h>
#include <ctype.h>
#include <libgen.h>
#include <assert.h>
#include "sonde_spfe_define.h"
#include "sonde_spfe_function_def.h"

void memcpy_padding_vad_gen(  t_float * src, t_float * dst, t_int32 count ) 
{
  for( int i = 0; i < VAD_PADDING; i++ )
  {
    dst[i] = 0;
  }
  
  for( int i = VAD_PADDING, j = 0; i < count - VAD_PADDING, j < count - VAD_PADDING2; i++, j++ )
  {
    dst[i] = src[j];
  }

  for( int i = count - VAD_PADDING; i < count; i++ )
  {
    dst[i] = 0;
  }
}

void memcpy1d_ftos(  t_int16 * src, t_int16 * dst, t_int32 count ) 
{
  for( int i = 0; i < count; i++ )
    dst[i] = src[i];
}

t_int32 swap(t_int32 *xp, t_int32 *yp) 
{ 
    t_int32 temp = *xp; 
    *xp = *yp; 
    *yp = temp; 
} 
  
t_int32  sort_features_array(t_int32 arr[], t_int32 n) 
{ 
   t_int32 i, j; 
   for (i = 0; i < n-1; i++)       
       for (j = 0; j < n-i-1; j++)  
           if (arr[j] > arr[j+1]) 
              swap(&arr[j], &arr[j+1]); 
} 

t_int32 check_input_existence(s_paths_and_meta * path){
    char tmp_out[strlen(path->output_path_vad_1)+2];
    strcpy(tmp_out, path->output_path_vad_1);
	
    if((access(dirname(tmp_out),F_OK)) == -1)
	{
		fprint_log_string("ERROR : output dir does not exist\n");
        t_int32 err = elck_raise_error(E_FILE_NOT_FOUND);
        if(err != 0)
        {
            return -1;
        }
    }
	if((access(path->sonde_config_filename,F_OK)) == -1)
	{
		fprint_log_string("ERROR : omni config does not exist\n");
        t_int32 err = elck_raise_error(E_FILE_NOT_FOUND);
        if(err != 0)
        {
            return -1;
        }
	}
	if((access(path->input_filename,F_OK)) == -1)
	{
		fprint_log_string("ERROR : input wav does not exist\n");
        t_int32 err = elck_raise_error(E_FILE_NOT_FOUND);
        if(err != 0)
        {
            return -1;
        }
	}
	// if((access(path->resources,F_OK)) == -1)
	// {
	// 	fprint_log_string("\n	ERROR : resources does not exist\n");
    //     t_int32 err = elck_raise_error(E_FILE_NOT_FOUND);
    //     if(err != 0)
    //     {
    //         return -1;
    //     }
	// }
    return E_SUCCESS;
}

t_int32 interdependent_features_config(s_features_config * features_config)
{
    if(features_config->sonde_vad||features_config->orgfmt||features_config->vad_annotation)
    {
        features_config->features_array[features_config->feature_count] = vad_annotation_fmt;
        features_config->feature_count++;
    }
    if ( features_config->jitter == 1 || features_config->shimmer == 1 || features_config->f0 == 1 || features_config->ampl == 1 || features_config->jitter1 == 1  )
	{
        features_config->features_array[features_config->feature_count] = js_family;
        features_config->feature_count++; 
    }
    if( features_config->dd_mfcc == 1 || features_config->delta_mfcc == 1 || features_config->mfcc == 1 || features_config->scf == 1 )
	{
        features_config->features_array[features_config->feature_count] = mfcc_family;
        features_config->feature_count++;   
    }
    if( features_config->orgmfcc == 1 || features_config->org_deltamfcc == 1 || features_config->org_ddmfcc == 1 || features_config->org_scf == 1 )
	{
        features_config->features_array[features_config->feature_count] = orgmfcc_family;
        features_config->feature_count++;   
    }
	if ( features_config->aqs_avg == 1 || features_config->aqs_max == 1 || features_config->aqs_min == 1 || features_config->aqs_avgnorm == 1 || features_config->aqs_rmsnorm == 1 || features_config->aqs_rms == 1 )
	{
        features_config->features_array[features_config->feature_count] = auto_qos_family;
        features_config->feature_count++; 
    }
    if ( features_config->sonde_vad == 0 )
    {
        features_config->vad_annotation = 0;
    }
}

t_int32 copy_org_algo_params( s_shared_params * shared_params, s_user_config * user_config, t_int32 samplerate, t_int32 actualsamples ){
    fprintf(stderr, "\n\n\tcopying original wave parameters");
    shared_params->org_wave_params.sample_rate = samplerate;
    shared_params->org_wave_params.sample_count = actualsamples;
    shared_params->org_wave_params.frame_samplerate_step       = round(user_config->sonde_framestep * samplerate / 1000);
    shared_params->org_wave_params.frame_samplerate_size       = round(user_config->sonde_framesize * samplerate / (float)1000);
    return E_SUCCESS;
}

t_int32 set_algo_params( s_shared_params * shared_params, s_user_config * user_config, t_int32 samplerate, t_int32 actualsamples ){
    shared_params->frame_samplerate_step       = round(user_config->sonde_framestep * samplerate / 1000);
    shared_params->frame_samplerate_size       = round(user_config->sonde_framesize * samplerate / (float)1000);
    shared_params->number_of_frames = (actualsamples - shared_params->frame_samplerate_size) / shared_params->frame_samplerate_step;  
    shared_params->number_of_frames_vad = ((actualsamples + VAD_PADDING2) - shared_params->frame_samplerate_size) / shared_params->frame_samplerate_step;  
    shared_params->fft_size = 2048; //(t_int32)pow(2.0, ceil(log2((t_double)shared_params->frame_samplerate_size)));
    shared_params->fft_size2 = shared_params->fft_size / 2 + 1;
    return E_SUCCESS;
}

t_int32 set_audio_path( s_paths_and_meta * paths_and_meta )
{    
    char output_filename[300] = {0};
    char buffer_filename[300] = {0};
    char string_buffer[300]   = {0};

    strcpy(buffer_filename, paths_and_meta->input_filename);
    strcat(output_filename, basename(buffer_filename));  // generates audio_name from path e.g demo.wav
    strcpy(paths_and_meta->audio_name, output_filename);
    strcpy(string_buffer, paths_and_meta->resources);  // generates vad region based fe address
    dirname(string_buffer);
	strcat(string_buffer,"/region_fe_input.txt");
    strcpy(paths_and_meta->region_ip, string_buffer);  

    return E_SUCCESS;
}

t_int32 set_feature_array( s_features_config * features_config ){
    sort_features_array(features_config->features_array,features_config->feature_count);
    memcpy(features_config->raw_features_array,features_config->features_array,50*__SIZEOF_INT__);
    interdependent_features_config(features_config);
    return E_SUCCESS;
}

t_int32 init_algorithm_param(s_paths_and_meta * paths_and_meta,  s_features_config * features_config, s_user_config * user_config, s_shared_params * shared_params, s_fileinfo * file_parameters)
{   
    t_int32 actual_samples = file_parameters->actual_samples ;
    t_int32 samplerate = file_parameters->WaveHeader.sampleRate;
    set_feature_array( features_config );   //setting inderdependent features
    set_algo_params( shared_params, user_config, samplerate, actual_samples );  // configure required algorithm params like fftsize
    set_audio_path( paths_and_meta );  //handle paths
    return E_SUCCESS;
}

t_int32 get_vad_status( t_int32 * vad_arr, t_int32 frame_count )
{
    t_int32 vad_status = 0;
    for(t_int32 i = 0; i < frame_count; i++){
        if(vad_arr[i]){
            vad_status = 1;
            break;
        }
    }
    return vad_status;
}

t_int32 pre_emphasis_filter(s_fileinfo *fileread , s_shared_params * shared_params )
{
    #if LEVEL2_LOG
    fprintf(stderr, "\n Function Entry : pre_emphasis_filter()");
    #endif  
    for (t_int32 i = 1; i < fileread->actual_samples; i++)
    {
        //shared_params->emphasis_pcm[i] = ((t_double)shared_params->pcm_data[i] - 0.97 * (t_double)(shared_params->pcm_data[i-1])) / (t_double)32767;
        shared_params->emphasis_pcm[i] = ((t_double)shared_params->resampled_pcm_data[i] - 0.97 * (t_double)(shared_params->resampled_pcm_data[i-1])) / (t_double)32767;
    }
    return E_SUCCESS;
}

t_int8 * get_output_path( s_paths_and_meta * paths_and_meta )
{
    t_int8 * out_path = paths_and_meta->output_path_fe_2;
    t_int8 * tmp_out_path = malloc(strlen( out_path ) + 1);
    strcpy( tmp_out_path, out_path );
    dirname( tmp_out_path );
    return tmp_out_path;
}

t_int8 * get_output_path_gen( char * output_path )
{
    t_int8 * out_path = output_path;
    t_int8 * tmp_out_path = malloc(strlen( out_path ) + 1);
    strcpy( tmp_out_path, out_path );
    tmp_out_path = dirname( tmp_out_path );
    return tmp_out_path;
}

t_int8 * get_audio_name( const t_int8 * input_path )
{
    t_int8 * tmp = strdup( input_path );
    return basename( tmp );
}

t_int8 *erase_ext(t_int8 *filename) 
{
    t_int8 *dot = strrchr(filename, '.');
    if(!dot || dot == filename) return filename;
    *dot = 0;
    return filename;
}

void fprint_log_string( t_int8 * string )
{
	//fprintf(stderr,"%s", string);
    LOGD( "%s", string);
}

void fprint_log_string_float( t_int8 * string, t_float arg )
{
	//fprintf(stderr, string, arg);
    LOGD(string,arg);
}

void fprint_log_string_str( t_int8 * string, t_int8 * arg )
{
	//fprintf(stderr, string, arg);
    LOGD(string,arg);
}

void fprint_log_string_int( t_int8 * string, t_int32 arg )
{
	//fprintf(stderr, string, arg);
    LOGD(string,arg);
}

/*********************************************************************/

void fprint_log_string_fd( t_int8 * string )
{
	//fprintf(FD,"%s", string);
    LOGD( "%s", string);
}

void fprint_log_string_float_fd( t_int8 * string, t_float arg )
{
	//fprintf(FD, string, arg);
    LOGD(string,arg);
}

void fprint_log_string_str_fd( t_int8 * string, t_int8 * arg )
{
	//fprintf(FD, string, arg);
    LOGD(string,arg);
}

void fprint_log_string_int_fd( t_int8 * string, t_int32 arg )
{
	//fprintf(FD, string, arg);
    LOGD(string,arg);
}

// void derive_paths( t_int8 * input_audio, t_int8 * config_file, t_int8 * out_dir, out_path * obj )
// {
// 	t_int8 * vad_t = "_only_vad.csv";
// 	t_int8 * fbf_t = "_speech_fe.csv";
	
// 	t_int8 * output_dir = get_output_path_gen( out_dir );
//     t_int8 * audio_name = get_audio_name( input_audio ); 
    
//     t_int8 * fbf_path = malloc( strlen( output_dir ) + strlen( audio_name ) + strlen( fbf_t ) + 1 );
//     t_int8 * vad_path = malloc( strlen( output_dir ) + strlen( audio_name ) + strlen( vad_t ) + 1 );
    
//     audio_name = erase_ext( audio_name );
    
//     strcpy( fbf_path, output_dir );
//     strcat( fbf_path, "/");
//     strcat( fbf_path, audio_name );
//     strcat( fbf_path, fbf_t );
    
//     strcpy( vad_path, output_dir );
//     strcat( vad_path, "/");
//     strcat( vad_path, audio_name );
//     strcat( vad_path, vad_t );
    
//     obj->vad_path = vad_path;
//     obj->fbf_path = fbf_path;
// } 
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include "sonde_spfe_filter.h"
#include "sonde_spfe_function_def.h"

typedef struct
{
    t_float * total_power;
    t_float * total_power_up;
    t_int32 total_power_size;
    t_int32 total_power_up_size;
} s_smooth_eg_out;

void get_max_float(const t_float *, const t_int32, t_float *);
void get_min_float(const t_float *, const t_int32, t_float *);
void mean_float(t_float *, t_int32, t_float *);
void sum_float(const t_float *, const t_int32, t_float *);
static void ad_divide_array_float(const t_float *, const t_int32, t_float **);
static void ad_energy_cut(const t_float *, const t_int32, t_float **);
static void ad_fill_total_power_up(const t_float, const t_int32, const t_int32, t_float *);
static void ad_get_avg(const t_float *, const t_int32, t_float *);
static void ad_get_avg_by_index(const t_float *, const t_int32, const t_int32, t_float *);
static void ad_get_final_power(const t_float *, const t_int32, t_float **);
static void ad_get_mean_abs(const t_float *, const t_int32, t_float *);
static void ad_get_peaks_count(const t_float *, const t_int32, t_int32 *);
static void ad_reset_less_than_y_dn(const t_float, const t_int32, t_float *);
static void ad_select_actual_signal(const t_float *, const t_float *, const t_int32, t_float **, t_float **);
static void ad_set_greater_than_y_up(const t_float, const t_int32, t_float *);
static void ad_shift_compansate(const t_float *, const t_int32, const t_int32, t_float *);
static void ad_smooth_eg(const t_float *, const t_int32, const t_int32, s_smooth_eg_out *);
static void ad_sub_array_float(const t_float *, const t_int32, t_float *);
static void ad_sub_noise_power(const t_float, const t_int32, t_float *);

void elck_write_output( s_paths_and_meta  * paths_and_meta, s_elck_meta * elck_meta, t_int8 * output_dir )
{
    s_elck_user_thresholds elck_user_thresholds = elck_meta->elck_user_thresholds;

    t_float thr_arr[7];
    t_float vad_write_arr[6];
    t_float ahh_write_arr[6];
    t_float em_write_arr[5];
    
    thr_arr[0] = elck_user_thresholds.vad_thresh_arr[0];
    thr_arr[1] = elck_user_thresholds.vad_thresh_arr[1];
    thr_arr[2] = elck_user_thresholds.lmk_threshold;
    thr_arr[3] = elck_user_thresholds.ahh_thresh_arr[0];
    thr_arr[4] = elck_user_thresholds.ahh_thresh_arr[1];
    thr_arr[5] = elck_user_thresholds.em_thresh_arr[0];
    thr_arr[6] = elck_user_thresholds.em_thresh_arr[1];

    vad_write_arr[0] = elck_meta->elck_output.elck_write_out.elck_vad_results[0];
    vad_write_arr[1] = elck_meta->elck_output.elck_write_out.elck_vad_results[1];
    vad_write_arr[2] = elck_meta->elck_output.elck_write_out.elck_vad_results[2];
    vad_write_arr[3] = elck_meta->elck_output.elck_write_out.elck_vad_results[3];
    vad_write_arr[4] = elck_meta->elck_output.elck_write_out.elck_vad_results[4];
    vad_write_arr[5] = elck_meta->elck_output.elck_out_response.elck_vad_response;

    ahh_write_arr[0] = elck_meta->elck_output.elck_write_out.elck_ahh_results[0];
    ahh_write_arr[1] = elck_meta->elck_output.elck_write_out.elck_ahh_results[1];
    ahh_write_arr[2] = elck_meta->elck_output.elck_write_out.elck_ahh_results[2];
    ahh_write_arr[3] = elck_meta->elck_output.elck_write_out.elck_ahh_results[3];
    ahh_write_arr[4] = elck_meta->elck_output.elck_write_out.elck_ahh_results[4];
    ahh_write_arr[5] = elck_meta->elck_output.elck_write_out.elck_ahh_results[5];

    em_write_arr[0] = elck_meta->elck_output.elck_write_out.elck_em_results[0];
    em_write_arr[1] = elck_meta->elck_output.elck_write_out.elck_em_results[1];
    em_write_arr[2] = elck_meta->elck_output.elck_write_out.elck_em_results[2];
    em_write_arr[3] = elck_meta->elck_output.elck_write_out.elck_em_results[3];
    em_write_arr[4] = elck_meta->elck_output.elck_write_out.elck_em_results[4];

    t_int8 * token = "_elck.csv";

    t_int8 * file_path = store_output_at( output_dir, paths_and_meta->input_filename, token );

    FILE * fp = fopen( file_path, "w" );

    if( fp == NULL )
    {
        t_int32 err = elck_raise_error(E_FILE_NOT_FOUND);
        if(err != 0)
        {
            perror("Output directory doesn't exist");
            return;
        }
    }

    t_int8 * headers[] = {
    "vad_th1", "vad_th2", "lmk_th1", "ahh_th1", "ahh_th2", "em_th1", "em_th2", 
    "total_number_of_frames", "trimmed_number_of_frames", "vad_voiceframes", "vad_ratio1", "vad_ratio2", "vad_output", 
    "ahh_v1", "ahh_v2", "ahh_v3", "ahh_v4", "ahh_v5", "ahh_output",
    "em_numframes", "em_lowenergyframes", "em_voice_frames", "em_ratio", "em_output" };

    t_float data[] = { 
    thr_arr[0], thr_arr[1], thr_arr[2], thr_arr[3], thr_arr[4], thr_arr[5], thr_arr[6],
    vad_write_arr[0], vad_write_arr[1], vad_write_arr[2], vad_write_arr[3], vad_write_arr[4], vad_write_arr[5],
    ahh_write_arr[0], ahh_write_arr[1], ahh_write_arr[2], ahh_write_arr[3], ahh_write_arr[4], ahh_write_arr[5],
    em_write_arr[0], em_write_arr[1], em_write_arr[2], em_write_arr[3], em_write_arr[4]};

    int h_arr_count = sizeof(headers) / sizeof(char *);
    int d_arr_count = sizeof(data) / sizeof(t_float);

    if( h_arr_count != d_arr_count )
    {
        fprint_log_string("\t error in elck output write: Inbalanced headers and data arrays");
        return;
    }

    for( int i = 0; i < h_arr_count; i++ )
    {
        fprintf( fp, "%s", headers[i] );
        
        if( i != h_arr_count-1 )
        {
            fprintf(fp, ",");
        }
    }
    
    fprintf(fp, "\n");
    
    for( int i = 0; i < h_arr_count; i++ )
    {
        fprintf( fp, "%f", data[i] );
        
        if( i != h_arr_count-1 )
        {
            fprintf(fp, ",");
        }
    }

    fprintf(fp, "\n");

    fclose(fp);
}  

t_int8 * store_output_at( t_int8 * output_dir_path, t_int8 * input_audio_path, t_int8 * token )
{
    // t_int8 * dir_path = get_output_path_gen(output_dir_path); // op -> ./output
    t_int8 * dir_path = output_dir_path; //get_output_path_gen(output_dir_path); // op -> ./output
    t_int8 * tmp_aud_name = get_audio_name (input_audio_path);  // op -> demo.wav 
    t_int8 * audio_name = erase_ext( tmp_aud_name ); // op -> demo
    t_int8 * file_path = malloc( strlen(output_dir_path) + strlen( token ) + strlen( audio_name ) + 2 );
    strcpy( file_path, dir_path );    // op -> demo 
    strcat( file_path, audio_name );    // op -> demo 
    strcat( file_path, token ); // op -> demo_token

    return file_path;
}

void padding_yc( t_float * input_yc, t_float ** output_yc_padded, t_int32 yc_count, t_int32 pad_coeff )
{
    int total = yc_count + pad_coeff;
    t_float * tmp = malloc( sizeof(t_float) * total );
    int j = 0;

    for( int i = 0; i < total; i++ )
    {
        if( i < pad_coeff )
        {
            tmp[i] = 0;
        }
        else
        {
            tmp[i] = input_yc[j++];
        }
    }

    *output_yc_padded = tmp;
}

t_float * ahh_detection( s_paths_and_meta * paths_and_meta, s_shared_params * shared_params, s_fileinfo * file_parameters )
{
	fprintf(stderr,"\n	start : ahh detection processing\n" );

    t_float scale = 0.0;
    t_float input_avg_out = 0.0;
    t_float total_power_avg_out = 0.0;
    t_float avg_out = 0.0;
    t_float * total_power = NULL;
    t_float * total_power_up = NULL;
    t_int32 total_power_size = 0;
    t_int32 total_power_up_size = 0;
    t_float * yc_eng_cut_out = NULL;
    t_float * pad_yc_eng_cut_out = NULL;
    t_float * yupc_eng_cut_out = NULL;
    t_float yc_eng = 0.0;
    t_float yc_eng_scaled = 0.0;
    t_float * signal = NULL;
    t_float * noise =  NULL;
    t_float sig_power = 0.0;
    t_float noise_power = 0.0;
    t_float SNR = 0.0;
    t_float * delta = NULL;
    t_int32 no_of_peaks = 0;
    t_int32 decision = 0;
    t_float * ahh_output = NULL;

    s_smooth_eg_out * smooth_eg_out = malloc( sizeof(s_smooth_eg_out) );
    
    t_int32 sample_rate = file_parameters->WaveHeader.sampleRate;   // get wave parameters
    t_int32 size = file_parameters->actual_samples;
    t_float * input = shared_params->resampled_pcm_data;  
    ahh_output = malloc( sizeof(t_float) * 10);  

    ad_get_mean_abs( input, size, &scale ); // energy scale
    ad_get_avg( input, size, &input_avg_out ); // original input signal energy xEg
    ad_smooth_eg( input, sample_rate, size, smooth_eg_out ); // calculating the smoothed energy signal (+ upsampled version)
    
    total_power = smooth_eg_out->total_power; 
    total_power_up = smooth_eg_out->total_power_up;
    total_power_size = smooth_eg_out->total_power_size;
    total_power_up_size = smooth_eg_out->total_power_up_size;

    ad_get_avg_by_index( total_power, 0, total_power_size, &total_power_avg_out); // energy of normalized y = total_power_avg_out
    ad_energy_cut( total_power, total_power_size, &yc_eng_cut_out );    // cutting the smoothed energy signal 
    
    padding_yc( yc_eng_cut_out, &pad_yc_eng_cut_out, total_power_size, 10 ); //change 50 to size of pad
    total_power_size += 10;
    
    ad_energy_cut( total_power_up, total_power_up_size, &yupc_eng_cut_out );    // cutting the smoothed energy signal (upsampled)
    ad_get_avg( pad_yc_eng_cut_out, total_power_size, &yc_eng );    // energy of cutted signal ycEg
    yc_eng *= 100;
    yc_eng_scaled = yc_eng * scale;     // scaled energy of cutted signal 
    ad_select_actual_signal( input, yupc_eng_cut_out, size, &signal, &noise );  // selecting actual signal & noise content 
    ad_get_avg( signal, size, &sig_power ); // signal power
    ad_get_avg( noise, size, &noise_power );    // noise power
    SNR  =  sig_power / noise_power;    // signal to noise ratio
    ad_get_final_power( pad_yc_eng_cut_out, total_power_size, &delta ); // delta peaks
    ad_get_peaks_count( delta, total_power_size, &no_of_peaks); // peaks

    // v1 = input_avg_out > thresh1;  // decision making 
    // v2 = yc_eng > 50;
    // v3 = yc_eng_scaled > 1e-3;
    // v4 = no_of_peaks < 3;
    // v41 =  (no_of_peaks > 2) && (no_of_peaks < 5);
    // v5 = SNR >= thresh2;

    ahh_output[0] = input_avg_out;
    ahh_output[1] = yc_eng;
    ahh_output[2] = yc_eng_scaled;
    ahh_output[3] = no_of_peaks;
    ahh_output[4] = SNR;

    free( yc_eng_cut_out );
    free( yupc_eng_cut_out );
    free( signal );
    free( noise );
    free( delta );
    free( total_power );
    free( total_power_up ); 
    free( smooth_eg_out );
    free(pad_yc_eng_cut_out);

	fprintf(stderr,"	done  : ahh detection processing\n\n" );

    return ahh_output;
}

t_int32 get_ahh_response( t_float * ahh_output_arr, t_float * thresholds )
{
    t_int32 v1, v2, v3, v4, v41, v5;
    
    t_float input_avg_out, yc_eng, 
    yc_eng_scaled, no_of_peaks, SNR;
    t_float thresh1;
    t_float thresh2;

    thresh1 = thresholds[0];
    thresh2 = thresholds[1];

    input_avg_out = ahh_output_arr[0];
    yc_eng = ahh_output_arr[1];
    yc_eng_scaled = ahh_output_arr[2];
    no_of_peaks = ahh_output_arr[3];
    SNR = ahh_output_arr[4]; 

    v1 = input_avg_out > thresh1;  // decision making 
    v2 = yc_eng > 50;
    v3 = yc_eng_scaled > 1e-3;
    v4 = no_of_peaks < 3;
    v41 =  (no_of_peaks > 2) && (no_of_peaks < 5);
    v5 = SNR >= thresh2;
    
    if ( v1 && v2 && v3 && v4 && v5 )
    {
        //fprintf(stderr,"Task is an acceptable 'Ahh'");
        ahh_output_arr[5] = 1;
        return 1;
    }
    else if ( v1 && v2 && v3 && v41 && v5 )
    {
        //fprintf(stderr,"Task has multiple 'Ahh' ");
        ahh_output_arr[5] = 2;
        return 2;
    }
    else
    {
        //fprintf(stderr,"Please repeat the task!");
        ahh_output_arr[5] = 0;
        return 0;
    }
}

void mean_float( t_float * input_array, t_int32 row_size, t_float * scale )
{
    t_float sum = 0.0;
    t_int32 loop = 0;
    t_float mean_lowfe = 0.0;

    for (loop = 0; loop < row_size; loop++)
    {
        sum = sum + input_array[loop];
    }
    
    mean_lowfe = (t_float)sum / loop;
    *scale = mean_lowfe ;
}

static void ad_get_mean_abs( const t_float * input, const t_int32 size, t_float * scale )
{
    t_float * tmp = malloc( size * sizeof(t_float) );
    t_float mean = 0.0;

    for( t_int32 i = 0; i < size; i++ )
    {
        tmp[i] = fabs(input[i]);
    }

    mean_float( tmp, size, &mean );
    *scale = pow( mean, 2 ) ;
    free(tmp);
}

void sum_float( const t_float * input, const t_int32 size, t_float * sum )
{
    t_float tmp = 0.0;
    for( t_int32 i = 0; i < size; i++ )
    {
        tmp += input[i];
    }
    *sum = tmp;
}

static void ad_get_avg( const t_float * input, const t_int32 size, t_float * avg_out )
{
    t_float sum = 0.0;
    for( t_int32 i = 0; i < size; i++ )
    {
        sum += pow( input[i], 2 );
    }
    *avg_out = sum / (t_float)size;
}

static void ad_get_avg_by_index( const t_float * input, const t_int32 start, const t_int32 end, t_float * avg_out )
{
    t_float sum = 0.0;
    for( t_int32 i = start; i < end; i++ )
    {
        sum += pow( input[i], 2 );
    }
    *avg_out = sum / ( end - start );
}

static void ad_fill_total_power_up( const t_float val, const t_int32 start, const t_int32 end,t_float * total_power_up )
{
    for( t_int32 i = start; i < end; i++ )
    {
        total_power_up[i] = val;
    }
}

static void ad_shift_compansate( const t_float * tmp_total_power_up, const t_int32 rem_l, const t_int32 input_size, t_float * total_power_up )
{
    t_int32 start = 0;

    for( t_int32 i = rem_l; i < input_size; i++ )
    {
        total_power_up[i] = tmp_total_power_up[start];
        start++;
    }
}

static void ad_sub_noise_power( const t_float noise_power, const t_int32 size, t_float * power_arr )
{
    for( t_int32 i = 0; i < size; i++ )
    {
        power_arr[i] -= noise_power;
    }
}

void get_max_float( const t_float * input, const t_int32 size, t_float * out_max )
{
    t_float tmp_out_max = __FLT_MIN__;

    for( t_int32 i = 0; i < size; i++ )
    {
        if( tmp_out_max < input[i] )
        {
            tmp_out_max = input[i];
        }  
    }

    *out_max = tmp_out_max;
}

void get_min_float( const t_float * input, const t_int32 size, t_float * out_min )
{
    t_float tmp_out_min = __FLT_MAX__;

    for( t_int32 i = 0; i < size; i++ )
    {
        if( input[i] < tmp_out_min )
        {
            tmp_out_min = input[i];
        }  
    }

    *out_min= tmp_out_min;
}

static void ad_divide_array_float( const t_float * input, const t_int32 size, t_float ** output )
{
    t_float * tmp_out = NULL;
    t_float max = 0.0;

    tmp_out = malloc( size * sizeof(t_float) ); 

    get_max_float( input, size, &max );

    for( t_int32 i = 0; i < size; i++ )
    {
        tmp_out[i] = input[i] / max;
    }

    *output = tmp_out;
}

static void ad_sub_array_float( const t_float * input, const t_int32 size, t_float * output )
{
    t_float * tmp_out = NULL;
    t_float min = 0.0;

    get_min_float( input, size, &min );

    for( t_int32 i = 0; i < size; i++ )
    {
        output[i] = input[i] - min;
    }
}

static void ad_set_greater_than_y_up( const t_float y_up, const t_int32 size, t_float * in_out )
{
    for( t_int32 i = 0; i < size; i++ )
    {
        if( in_out[i] > y_up )
            in_out[i] = 1;
    }
}

static void ad_reset_less_than_y_dn( const t_float y_dn, const t_int32 size, t_float * in_out )
{
    for( t_int32 i = 0; i < size; i++ )
    {
        if( in_out[i] < y_dn )
            in_out[i] = 0;
    }
}

static void ad_energy_cut( const t_float * input, const t_int32 size, t_float ** output )
{
    t_float y_up = 0;
    t_float y_dn = 0;
    t_float * tmp = NULL;

    y_up = 0.055; 
    y_dn = 0.05; 

    ad_divide_array_float( input, size, &tmp );
    ad_sub_array_float( tmp, size, tmp );
    ad_set_greater_than_y_up( y_up, size, tmp );
    ad_reset_less_than_y_dn( y_dn, size, tmp );

    *output = tmp;
}

static void ad_select_actual_signal( const t_float * input, const t_float * yupc_eng_cut_out, const t_int32 size, t_float ** signal, t_float ** noise )
{
    t_float * tmp_signal = malloc( sizeof(t_float) * size );
    t_float * tmp_noise = malloc( sizeof(t_float) * size );

    for( t_int32 i = 0; i < size; i++ )
    {
        tmp_signal[i] = 0.0;
        tmp_noise[i] = 0.0;

        if( ( input[i] * yupc_eng_cut_out[i] ) > 0 )
        {
            tmp_signal[i] = input[i];
        }
        else
        {
            tmp_noise[i] = input[i];
        }
    }

    *signal = tmp_signal;
    *noise = tmp_noise;
}

static void ad_get_final_power( const t_float * input, const t_int32 size, t_float ** power )
{
    t_int32 shift_s = 1;
    t_int32 shift_l = size - 1;
    t_int32 j = shift_l;

    t_float * tmp = malloc( size * sizeof(t_float));

    for( t_int32 i = 0; i < size - 1; i++ )
    {
        tmp[i] = input[i+1] - input[i];
        if( tmp[i] < 0 )
        {
            tmp[i] = 0;
        }
    }
    *power = tmp;
}

static void ad_smooth_eg( const t_float * input, const t_int32 sample_rate, const t_int32 input_size, s_smooth_eg_out * smooth_eg_out )
{
    t_float wn = 0.0;
    t_int32 filter_order = 6;
    t_int32 noise_samples = 4500;
    t_int32 tmp_noise_samples = 4;
    t_float noise_power = 0.0;
    t_int32 frame_shift = 500;
    t_int32 frame_length = 5000;
    t_float * total_power = NULL;
    t_float * tmp_total_power_up = NULL;
    t_float * total_power_up = NULL;
    t_float frame_power = 0.0;
    t_int32 n_frames = 0;
    t_int32 up_scale = 0;
    t_int32 start_idx = 0;
    t_int32 end_idx = 0;
    t_int32 fr = 0;
    t_float rem_l = 0.0;
    t_float y = 0.0;
    t_float y_up = 0.0;
    
    t_float * filter_output = malloc( input_size * sizeof(t_float) );

    t_float *a; 
    t_float *b; 

    wn = 1000/( sample_rate / 2.0);

    butter( 6, wn, &b, &a );
    filter( 6, a, b, input_size - 1, input, filter_output);
    ad_get_avg( filter_output, tmp_noise_samples, &noise_power );
    
    total_power = calloc( ceil( input_size / (t_float)frame_shift ) , sizeof( t_float ) );
    tmp_total_power_up = malloc( input_size * sizeof( t_float ));
    total_power_up = calloc( input_size , sizeof( t_float ));

    n_frames = floor( input_size / (t_float)frame_shift ) - 1;
    up_scale = floor( input_size / (n_frames + 1) );

    for( fr = 0; fr < n_frames; fr++ )
    {
        start_idx = fr * frame_shift;
        end_idx = start_idx + frame_length - 1;
        
        if( end_idx < input_size )
        {
            ad_get_avg_by_index( filter_output, start_idx, end_idx, &frame_power ); 
        }
        else
        {
            ad_get_avg_by_index( filter_output, start_idx, input_size, &frame_power ); 
        }

        total_power[fr] = frame_power;
        ad_fill_total_power_up( frame_power, fr * up_scale, (fr + 1) * up_scale, tmp_total_power_up );
    }

    rem_l = input_size - (fr+1) * up_scale + 3000;
    
    ad_shift_compansate( tmp_total_power_up, rem_l, input_size, total_power_up );
    ad_sub_noise_power( noise_power, ceil(input_size / (t_float)frame_shift), total_power ); // total_power - input/output
    ad_sub_noise_power( noise_power, input_size, total_power_up );  // total_power_up - input/output

    smooth_eg_out->total_power = total_power;
    smooth_eg_out->total_power_up = total_power_up;
    smooth_eg_out->total_power_size = ceil(input_size / (t_float)frame_shift);
    smooth_eg_out->total_power_up_size = input_size;
    
    free( filter_output );
    free( tmp_total_power_up );

}

static void ad_get_peaks_count( const t_float *  input, const t_int32 size, t_int32 * no_of_peaks )
{
    t_float tmp_max = 0.0;
    t_float threshold = 0.0;
    t_int32 tmp_peaks = 0;

    threshold = 0.1;
    get_max_float(input, size, &tmp_max);
    
    threshold = tmp_max - threshold;

    for( t_int32 i = 0; i < size; i++ )
    {
        if( input[i] > threshold )
        {
            tmp_peaks++;
        }
    }
    *no_of_peaks = tmp_peaks;
}


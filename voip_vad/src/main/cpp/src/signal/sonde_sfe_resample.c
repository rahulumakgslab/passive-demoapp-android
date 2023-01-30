#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include<complex.h>
#include "../../h/resample_fft.h"
#include "../../h/sonde_spfe_function_def.h"

typedef float complex cplx;
t_int32 test_fft( cplx * inputx, t_int32 n);

static void scale_ifft( cplx * ifft_out, const t_int32 win_size )
{
	t_double scale_coeff = 0;

	scale_coeff = 1 / (t_double)win_size;
	
	for( t_int32 j = 0; j < win_size; j++ )
	{
		ifft_out[j] *= scale_coeff;
	} 
}

void copy_float_to_cplx( t_int32 count, t_float * src, cplx * dst )
{
    for( t_int32 i = 0; i < count; i++ )
    {
      dst[i] = src[i];
    }
}

void copy_double_to_cplx( t_int32 count, const t_double * src, cplx * dst )
{
    for( t_int32 i = 0; i < count; i++ )
    {
      dst[i] = src[i];
    }
}

void short_to_float( t_int32 count, t_int16 * src, t_float * dst )
{
    for( t_int32 i = 0; i < count; i++ )
    {
      dst[i] = (t_float)src[i];
    }
}

t_int32 copy_by_index( t_float * in_buffer, t_float * out_buffer, t_int32 start, t_int32 end )
{
    static t_int32 idx = 0;

    for( t_int32 i = start; i < end; i++ )
    {
        out_buffer[idx] = in_buffer[i];
        idx++;
    }
}

t_int32 copy_by_index_complex( cplx * in_buffer, cplx * out_buffer, t_int32 start, t_int32 end )
{
    static t_int32 idx = 0;
    
    for( t_int32 i = start; i < end; i++ )
    {
        out_buffer[idx] = in_buffer[i];
        idx++;
    }
    return idx;
}
 
void dump_complex(const char * s, cplx buf[], t_int32 len) 
{
	printf("%s", s);
    FILE * rosetta = fopen( "rosetta.csv", "w" );
	for (t_int32 i = 0; i < len; i++)
		if (!cimag(buf[i]))
        {
            fprintf( rosetta, "%g\n", creal(buf[i]) );
        }
		else
        {
            fprintf( rosetta, "%g\n", creal(buf[i]) );
        }
    fclose(rosetta);
}

void copy_real(cplx * in_buf, t_float * out_buf, t_int32 len) 
{
	for (t_int32 i = 0; i < len; i++)
    {
        out_buf[i] = creal(in_buf[i]);
    }
}

void copy_real_to_double(cplx * in_buf, t_double * out_buf, t_int32 len) 
{
	for (t_int32 i = 0; i < len; i++)
    {
        out_buf[i] = creal(in_buf[i]);
    }
}

t_int32 gcd(t_int32 a, t_int32 b) 
{ 
    if (b == 0) 
        return a; 
    else
        return gcd(b, a % b); 
} 

t_int32 change_audio_meta( t_int32 new_sample_count, t_int32 new_sample_rate, t_int32 * org_sample_count, t_int32 * org_sample_rate )
{
    *org_sample_count = new_sample_count;
    *org_sample_rate  = new_sample_rate;
}

t_int32 print_changed_audio_meta( s_user_config * user_config, t_int32 new_sample_rate, t_int32 new_sample_count )
{
    t_int32 frame_samplerate_step = round(user_config->sonde_framestep * new_sample_rate / 1000);
    t_int32 frame_samplerate_size = round(user_config->sonde_framesize * new_sample_rate / (float)1000);
    t_int32 number_of_frames = (new_sample_count - frame_samplerate_size) / frame_samplerate_step;  
    t_int32 fft_size = 2048; 
    t_int32 fft_size2 = fft_size / 2 + 1;

    fprintf( stderr, "\n\tnew sample rate               : %d", new_sample_rate );
    fprintf( stderr, "\n\tnew sample count              : %d", new_sample_count );
    fprintf( stderr, "\n\tnew frame_samplerate_step     : %d", frame_samplerate_step );
    fprintf( stderr, "\n\tnew frame_samplerate_size     : %d", frame_samplerate_size );
    fprintf( stderr, "\n\tnew fft size                  : %d", fft_size );
    fprintf( stderr, "\n\tnew fft size2                 : %d\n", fft_size2 );

}

t_float * resample( s_shared_params * shared_params, s_fileinfo * file_parameters, s_user_config * user_config )
{    
    t_int32 sample_rate             = 0;
    t_int32 target_sampling_rate    = 0;
    t_int32 div                     = 0;
    t_double us_rate                = 0;
    t_double ds_rate                = 0;
    t_int32 sample_count            = 0;
    t_int32 start_index             = 0;
    t_int32 end_index               = 0;
    t_int32 first_half              = 0;
    t_int32 second_half             = 0;
    t_int32 d                       = 0; 

    t_int16 * pcm_data              = NULL;
    t_float * normalized_pcm        = NULL;
    t_float * resampled_pcm         = NULL;
    t_float * fft_magnitude         = NULL;
    t_float * tmp_pcm               = NULL;
    cplx * cplx_fft_buffer          = NULL;
    cplx * cplx_fft_buffer_reduced  = NULL;
    t_int32 * org_sample_count      = NULL;
    t_int32 * org_sample_rate       = NULL;

    target_sampling_rate = 16000;   // replace with config

    sample_rate      = file_parameters->WaveHeader.sampleRate;
    org_sample_rate  = &file_parameters->WaveHeader.sampleRate;
    sample_count     = file_parameters->actual_samples;
    org_sample_count = &file_parameters->actual_samples;
    pcm_data         = shared_params->pcm_data;
    div              = gcd(sample_rate, target_sampling_rate);
    us_rate          = target_sampling_rate / div;
    ds_rate          = sample_rate / div;
    tmp_pcm          = malloc( sample_count * sizeof( t_float ) );
    
    pcm_data_normalisation_gen( sample_count, pcm_data, tmp_pcm );  // normalizing pcm
    //short_to_float( sample_count, pcm_data, tmp_pcm );  
    
    // if( sample_rate == 16000 || sample_rate == 8000 )
    // {
    //     fprintf( stderr, "\n\tresampled audio file detected. resampling module will not be activated\n");
    // }
	
    fprintf(stderr, "\n\tResamping module is disabled \n" );

    return tmp_pcm;

    t_int32 len1 = sample_count;
    t_int32 len2 = round( len1 * us_rate / ds_rate );
    t_int32 midl = 1 + len1 / 2;
    midl         = floor(midl);

    cplx_fft_buffer         = malloc( (len1 + 1) * sizeof(cplx) );
    cplx_fft_buffer_reduced = malloc( (len2 + 1) * sizeof(cplx) );
    resampled_pcm           = malloc( (len2 + 1) * sizeof( t_float ) );
    
    copy_float_to_cplx( len1, tmp_pcm, cplx_fft_buffer);    // copy float to complex  
    //test_fft( cplx_fft_buffer, len1 );    // test fft for various configurations 
    resample_fft(cplx_fft_buffer, len1, false);    // fft of 275456
    int out = 0;

    if( len2 < len1 )
    {
        d = len1 - len2;
        start_index = floor(midl - ( d - 1 ) / 2.0) - 1;
        end_index   = floor(midl + ( d - 1 ) / 2.0);

        copy_by_index_complex( cplx_fft_buffer, cplx_fft_buffer_reduced, 0, start_index );
        out = copy_by_index_complex( cplx_fft_buffer, cplx_fft_buffer_reduced, end_index, len1 );  // 99939
    }
    
    resample_fft( cplx_fft_buffer_reduced, len2, true );   // ifft of 99939
    scale_ifft( cplx_fft_buffer_reduced, len2 );
    copy_real( cplx_fft_buffer_reduced, resampled_pcm, len2 );

    change_audio_meta( len2, target_sampling_rate, org_sample_count, org_sample_rate ); // changing header meta
    shared_params->SampleCount = *org_sample_count;
    print_changed_audio_meta( user_config, *org_sample_rate, *org_sample_count );   

    free(tmp_pcm);
    free(cplx_fft_buffer);
    free(cplx_fft_buffer_reduced);
    return resampled_pcm;
}

// t_int32 test_fft( cplx * inputx, t_int32 n) {
//     n = 15;
// 	cplx input[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 }; //random_complexes(n);
//     t_float real[n];
//     t_float real2[n];
// 	//cplx *expect = malloc(n * sizeof(cplx));
// 	// cplx *actual = memdup(input, n * sizeof(cplx));
// 	resample_fft(input, n, false);
//     copy_real( input, real, n );
//     resample_fft(input, n, true);
//     copy_real( input, real2, n );

//     t_float input_y[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 }; //random_complexes(n);
//     t_double mag[n];
//     resample_rdft( 15, 0, input_y );
//     calc_magnitude( mag, input_y, n );

// 	//double err0 = log10_rms_err(expect, actual, n);
	
// 	// for (t_int32 i = 0; i < n; i++)
// 	// 	actual[i] /= n;
// 	// resample_fft(actual, n, true);
// 	// double err1 = log10_rms_err(input, actual, n);
// 	// printf("fftsize=%4d  logerr=%5.1f\n", n, (err0 > err1 ? err0 : err1));
// 	//free(input);
// 	//free(expect);
// }
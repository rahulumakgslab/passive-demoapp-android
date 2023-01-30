/*
* @brief speech signal preprocessing methods and 
* setting of all required parameters
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

/******************************************************************************
 * Function Name:  pcm_data_normalisation
 *
 * Description: do nomrmalisation 
 *
 * Param:
 *   (in) actual_samples      - num of samples for wav sequence. 
 *   (in) *shared_params      - common parameters used accross features. 
 * Return:  SUCCESS
 *
 *****************************************************************************/
// t_int32 pcm_data_normalisation
// ( 
//   t_int32 actual_samples, 
//   s_shared_params * shared_params
// )
// {
// 	  t_int32 i = 1; 
//     for (i = 1; i < actual_samples; i++)
//     {
//         //shared_params->normalised_pcm[i] = ((t_float)shared_params->pcm_data[i] / (t_float)32767);
//         shared_params->normalised_pcm[i] = ((t_float)shared_params->resampled_pcm_data[i] / (t_float)32767);
//     }
//     return E_SUCCESS;
// }

/******************************************************************************
 * Function Name:  pcm_data_normalisation
 *
 * Description: do nomrmalisation 
 *
 * Param:
 *   (in) actual_samples      - num of samples for wav sequence. 
 *   (in) *shared_params      - common parameters used accross features. 
 * Return:  SUCCESS
 *
 *****************************************************************************/
t_int32 pcm_data_normalisation_gen
( 
  t_int32 actual_samples, 
  t_int16 * in_pcmbuf,
  t_float * out_normalized_pcm
)
{
	  t_int32 i = 1; 
    for (i = 1; i < actual_samples; i++)
    {
        out_normalized_pcm[i] = ((t_float)in_pcmbuf[i] / (t_float)32767);
    }
    return E_SUCCESS;
}

// t_int32 resample_pcm_normalisation
// ( 
//   t_int32 actual_samples, 
//   t_float * in_pcmbuf,
//   t_float * out_normalized_pcm
// )
// {
// 	  t_int32 i = 1; 
//     for (i = 1; i < actual_samples; i++)
//     {
//         out_normalized_pcm[i] = ((t_float)in_pcmbuf[i] / (t_float)32767);
//     }
//     return E_SUCCESS;
// }

/******************************************************************************
 * Function Name:  compute_windowing
 *
 * Description: apply windowing 
 *
 * Param:
 *   (in) num_samples_perframe - frameSize.
 *   (in) *shared_params       - common parameters used accross features. 
 * Return:  SUCCESS
 *
 *****************************************************************************/
t_int32 compute_windowing
(
  t_int32 num_samples_perframe, 
  s_shared_params * shared_params
)
{	
    t_int32 i = 0;
    t_int32 num_samples = num_samples_perframe - 1;
	  t_double pi2 = 2 * SONDE_PI;
    for (i = 0; i < num_samples_perframe; i++)
    {
        shared_params->ham[i] = (t_double)(0.54 - 0.46 * cos(pi2 * (t_double)i / (t_double)(num_samples)));
    }
    return E_SUCCESS;   
}

/******************************************************************************
 * Function Name:  pre_emphasis_gen
 *
 * Description: pre emphasis generalized
 *
 * Param:
 *   (out) out_emphasisbuf  - output 1103
 *   (in)  in_pcmbuf        - input 1103
 *   (in) frame_siz         - 1103
 *   (in) idx               - frame index 
 * Return:  out_emphasisbuf
 *
 *****************************************************************************/
t_float *pre_emphasis_gen
(
  t_float * out_emphasisbuf, 
  t_float* in_pcmbuf, 
  t_int32 frame_size
)
{
    *(out_emphasisbuf) = (1-0.97) * *(in_pcmbuf);
    t_float * str = out_emphasisbuf++;
    t_int32 i = 1; 
    
	  for (i = 1; i < frame_size; i++)
    {
        *out_emphasisbuf = in_pcmbuf[i] - 0.97 * (in_pcmbuf[i-1]);
        out_emphasisbuf++;
    }
    return str;    
}

/******************************************************************************
 * Function Name:  complex_to_double
 *
 * Description: converting conplex number to real.
 *
 * Param:
 *   (in) t_double u0                    
 *   (in) t_double u1                  
 * Return: single real ( double ) value
 *
 *****************************************************************************/
t_double complex_to_double
(
  t_double u0, 
  t_double u1
)
{
  t_double y;
  t_double a;
  t_double b;
  a = fabs(u0);
  b = fabs(u1);
  if (a < b) 
  {
    a /= b;
    y = b * sqrt(a * a + 1.0);
  } 
  else if (a > b) 
  {
    b /= a;
    y = a * sqrt(b * b + 1.0);
  } 
  else 
  {
    y = a * 1.4142135623730951;
  }
  return y;
}


/******************************************************************************
 * Function Name:  complex_to_real
 *
 * Description: get fft magnitude from fft frame
 *
 * Param:
 *   (in) in_fftbuf   - fft frame 2048
 *   (in) out_magbuf  - fft magnitude 1025
 *   (in) fft_size    - 2048
 * Return:  E_SUCCESS
 *
 *****************************************************************************/
t_int32 complex_to_real
(
  const t_float *in_fftbuf, 
  t_float *out_magbuf, 
  t_long fft_size
) 
{
    t_long index;
    if (fft_size <= 0)
        return 0;

    out_magbuf[0] = fabs(in_fftbuf[0]);
    for (index = 2; index < fft_size; index += 2)
    {
        out_magbuf[index / 2] = sqrt(in_fftbuf[index] * in_fftbuf[index] + in_fftbuf[index + 1] * in_fftbuf[index + 1]);
    }
    out_magbuf[fft_size / 2] = fabs(in_fftbuf[1]);

    return 1;
}


/**********************************************************
 * Function Name:  filter_scale_select
 *
 * Description: scale transformation
 *
 * Param:
 *   (in/out) freq      -  hi / low freq 
 *   (in) filter_scale  -  mel or linear
 * Return:  E_SUCCESS
 *
 **********************************************************/
t_double filter_scale_select
(
  t_double freq, 
  t_int32 filter_scale
)
{
  switch(filter_scale) 
  {
    case SCALE_MEL: 
      return (1127.01048  * log(1.0 + freq / 700.0) ); 
    case SCALE_LINEAR: 
    default:
      return freq;
  }

  return freq;
}

/**********************************************************
 * Function Name:  filter_scale_inverse
 *
 * Description: inverse scale transformation
 *
 * Param:
 *   (in/out) freq      -  hi / low freq 
 *   (in) filter_scale  -  mel or linear
 * Return:  E_SUCCESS
 *
 **********************************************************/
t_double filter_scale_inverse
(
  t_double freq, 
  t_int32 filter_scale
)
{
  t_double zz;
  t_double z0;
  
  switch(filter_scale) 
  {
    case SCALE_MEL : 
      return (700.0 * (exp(freq / 1127.0)- 1.0));        
    
    case SCALE_LINEAR:
    default:
      return freq;
  }
  return freq;
}


t_float scaleconv_freqtomel
(
  t_long num, 
  t_float base_freq, 
  t_int32 filter_scale
)
{
    return (t_float)filter_scale_select( ((t_float)num)*base_freq, filter_scale);
}

/**********************************************************
 * Function Name:  pad_asymmetric
 *
 * Description: padding operation
 *
 * Param:
 *   (in) src    -  1103 pcm data
 *   (out) dst   -  2048 padded data
 *   (in) nsrc   -  1103
 *   (out) ndst  -  2048 
 * Return:  E_SUCCESS
 *
 **********************************************************/
void pad_asymmetric
( 
  t_float * src, 
  t_float * dst, 
  t_int32 nsrc, 
  t_int32 ndst 
)
{
	  t_int32 i = 0;
    t_int32 padlen2 = (ndst - nsrc) / 2;
	
    for (i = 0; i < padlen2; i++)
    {
      dst[i] = 0;
    }
    for (t_int32 i = 0, j = 0; i < nsrc; i++, j++)
    {
      dst[i + padlen2] = (t_float)src[j];
    }
    for (t_int32 i = nsrc + padlen2; i < ndst; i++)
    {
      dst[i] = 0;
    }
}

/**********************************************************
 * Function Name:  pad_symmetric
 *
 * Description: padding operation
 *
 * Param:
 *   (in) src                   -  1103 pcm data
 *   (out) dst                  -  2048 padded data
 *   (in) numsamples_perframe   -  1103
 *   (out) fft_size             -  2048 
 * Return:  E_SUCCESS
 *
 **********************************************************/
void pad_symmetric
(
  t_float * src,
  t_float * dst, 
  t_int32 numsamples_perframe, 
  t_int32 fft_size 
)
{
	  t_int32 i = 0;
    for (i = 0; i < numsamples_perframe; i++)
    {
        dst[i] = (t_float)src[i];
    }
    //padding the input-data with zero. //from 1103 to 2048. 
    for (int i = numsamples_perframe; i < fft_size; i++)
    { 
        dst[i] = 0;
    }
}

void dbg_fsum(t_float *src_, t_int32 ndst_)
{
  fprintf(stderr, "\n");
  float sum = 0, sum_f = 0;

  for (t_int32 i = 0; i < ndst_; i++)
  {
    if( src_[i] > 0 )
    {
      sum += src_[i];
    }
    sum_f += src_[i]; 
  }

  fprintf(stderr, " > 0 : %f, all %f ", sum, sum_f);
}

t_double f2m(t_double x)
{
    return 1127.01048 * log(1 + x / 700);
}

t_double m2f(t_double x)
{
    return 700 * (exp(x / 1127.01048) - 1);
}

void memset_float( t_float * in_buf, t_int32 num_elements )
{
  memset( in_buf, 0.0, num_elements * __SIZEOF_FLOAT__ );
}

void memset_double( t_double * in_buf, t_int32 num_elements )
{
  memset( in_buf, 0.0, num_elements * __SIZEOF_DOUBLE__ );
}

void memset_int( t_int32 * in_buf, t_int32 num_elements )
{
  memset( in_buf, 0, num_elements * __SIZEOF_INT__ );
}
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include "sonde_spfe_filter.h"
#include "sonde_spfe_function_def.h"

t_float *shift_frame(t_float *sel_frame, t_int32 framesize, t_int32 n_shift);
t_float dot_multiply(t_float *arr1, t_float *arr2, t_int32 size);
t_float fsd_corr(t_float *arr1, t_float *arr2, t_int32 size);
t_int32 fill_corr(t_float *all_corr_up, t_int32 start, t_int32 end, t_float val);
void get_max_float(const t_float *input, const t_int32 size, t_float *out_max);
void ad_divide_array_float(const t_float *input, const t_int32 size, t_float **output);
t_float *dot_multiply_abs(t_float *arr1, t_float *arr2, t_int32 size);
t_float calc_y_mask_rms(t_float *y_mask, t_int32 size);
t_float median_float(t_float *arr, int size);
void get_abs_max_float(const t_float *input, const t_int32 size, t_float *out_max);
void mean_float(t_float *, t_int32, t_float *);
void fsd_pcm_normalize(t_float *input_pcm, t_int32 size);
void copy_segment(t_float *input_pcm, t_int32 start, t_int32 end, t_int32 num_samples, t_float *data);
void zero_segment(t_float *input_pcm, t_int32 start, t_int32 end, t_int32 num_samples, t_float *data);
void filter_mask(t_float *y_mask, t_int32 size, t_float y_th);
t_float *get_voiced(t_float *all_data, t_float *y_mask, t_int32 size, t_int32 *);
t_float *get_unvoiced(t_float *all_data, t_float *y_mask, t_int32 size, t_int32 *);
t_float get_sum(t_float *input, t_int32 size);
t_float get_signal_power(t_float *sig, t_int32 size, t_float den);
t_int32 qos_sum(t_float *qos_arr, t_int32 size);
t_int32 get_fsd_resp(t_float *qos_arr, t_int32 size, t_int32 reject_count);

float *copy_pcm(const t_float *input_resampled_pcm, t_int32 num_of_samples)
{
    t_float *copy = malloc(num_of_samples * sizeof(t_float));
    for (size_t i = 0; i < num_of_samples; i++)
    {
        copy[i] = input_resampled_pcm[i];
    }
    return copy;
}

int elck_fsd(s_elck_vad_structs *elck_structs, s_elck_meta *elck_meta)
{
    s_paths_and_meta *paths_and_meta = elck_structs->paths_and_meta;
    s_shared_params *shared_params = elck_structs->shared_params;
    s_fileinfo *file_parameters = elck_structs->file_parameters;
    s_user_config *user_config = elck_structs->user_config;
    t_float *thresholds = elck_meta->elck_user_thresholds.fsd_thresh_arr; 

    t_int32 sample_count = shared_params->SampleCount;
    t_int32 sample_rate = file_parameters->WaveHeader.sampleRate;
    t_int32 framesize = shared_params->frame_samplerate_size;
    t_int32 framestep = shared_params->frame_samplerate_step;
    t_int32 number_of_frames = shared_params->number_of_frames;
    t_int32 n_shift = user_config->fsd_nshift;
    t_int32 total_segm = user_config->fsd_tsegm;
    t_int32 qos_th = thresholds[0];
    t_float thr_adj = thresholds[1];

    t_int32 upscale = 0;
    t_float *normalised_by_max = NULL;
    t_float *resample_pcm_copy = NULL;
    t_float *data_p = NULL;
    t_float *all_corr_up_p = NULL;
    t_float *y_mask = NULL;
    t_float *voiced = NULL;
    t_float **all_data = NULL;
    t_float *unvoiced = NULL;
    t_float *all_corr_up = NULL;
    t_float framecorr = 0;
    t_float y_mask_rms = 0;
    t_float y_th = 0;
    t_int32 voiced_length = 0;
    t_int32 unvoiced_length = 0;
    t_float voiced_power = 0;
    t_float unvoiced_power = 0;
    t_float SNR = 0;
    t_float SNR_dBv = 0;
    t_int32 segm_l = sample_rate * 5;

    t_int32 l = segm_l;
    t_float ms_l = 0;
    t_float frame_size = 0;
    t_float frame_shift = 0;
    t_int32 n_frames = 0;
    t_int32 reject_count = 3;

    t_float tsegm = ceil(sample_count / (t_float)segm_l);
    t_float *data = malloc(sizeof(t_float) * segm_l);

    if (tsegm > total_segm)
    {
        tsegm = total_segm;
    }

    //output
    t_float *snr_lin = malloc(sizeof(t_float) * tsegm);
    t_float *snr_db = malloc(sizeof(t_float) * tsegm);
    t_float *v_power = malloc(sizeof(t_float) * tsegm);
    t_float *un_power = malloc(sizeof(t_float) * tsegm);
    t_float *y_rms = malloc(sizeof(t_float) * tsegm);
    t_float *y_thr = malloc(sizeof(t_float) * tsegm);
    t_float *elck_qos = malloc(sizeof(t_float) * tsegm);

    resample_pcm_copy = copy_pcm(shared_params->resampled_pcm_data, sample_count);

    fsd_pcm_normalize(resample_pcm_copy, sample_count);

    for (size_t segm = 0; segm < tsegm; segm++)
    {
        if (segm < tsegm - 1)
        {
            t_int32 start = (segm * segm_l);
            t_int32 end = (segm + 1) * segm_l;
            copy_segment(resample_pcm_copy, start, end, segm_l, data);
        }
        else if (segm == tsegm - 1)
        {
            t_int32 start = (segm * segm_l);
            t_int32 end = sample_count;
            copy_segment(resample_pcm_copy, start, end, segm_l, data);
        }
        else
        {
            t_int32 start = 0;
            t_int32 end = segm_l;
            zero_segment(resample_pcm_copy, start, end, segm_l, data);
        }

        l = segm_l;
        ms_l = sample_rate / 1000.0;
        frame_size = round(25 * ms_l);
        frame_shift = round(10 * ms_l);
        n_frames = floor(l / (t_float)(frame_shift));
        t_float all_corr[n_frames];
        upscale = floor(l / (t_float)(n_frames + 1));

        if (segm == 0)
        {
            all_data = (t_float **)double_memory_alloc(NULL, n_frames, framesize + n_shift, sizeof(t_float), 0);
            all_corr_up = malloc(l * sizeof(t_float));
        }

        for (int frame_num = 0; frame_num < n_frames; frame_num++)
        {
            t_int32 index = frame_num * frame_shift; // step calculation

            if (frame_size + frame_num * frame_shift < l)
            {
                copy_pcm_data_perframe(data + index, frame_size, all_data[frame_num]);
            }
            else
            {
                t_int32 diff_samples = frame_size + frame_num * frame_shift - l;
                copy_pcm_data_perframe(data + index, frame_size - diff_samples, all_data[frame_num]);
                for (size_t i = frame_size - diff_samples; i < frame_size; i++)
                {
                    all_data[frame_num][i] = 0.0;
                }
            }
        }

        for (int frame_num = 0; frame_num < n_frames; frame_num++)
        {
            t_int32 start = frame_num * upscale;
            t_int32 end = upscale * (frame_num + 1);
            t_float *sel_frame = all_data[frame_num];
            t_float *shift_sel_frame = shift_frame(sel_frame, framesize, n_shift);
            framecorr = fsd_corr(sel_frame, shift_sel_frame, framesize);
            all_corr[frame_num] = framecorr;
            fill_corr(all_corr_up, start, end, framecorr);
        }

        ad_divide_array_float(data, segm_l, &data_p);
        ad_divide_array_float(all_corr_up, segm_l, &all_corr_up_p);
        y_mask = dot_multiply_abs(all_corr_up_p, data_p, segm_l);
        y_mask_rms = calc_y_mask_rms(y_mask, segm_l);
        y_th = median_float(y_mask, segm_l);
        filter_mask(y_mask, segm_l, y_th);
        voiced = get_voiced(data, y_mask, segm_l, &voiced_length);
        unvoiced = get_unvoiced(data, y_mask, segm_l, &unvoiced_length);
        voiced_power = get_signal_power(voiced, segm_l, voiced_length);
        unvoiced_power = get_signal_power(unvoiced, segm_l, unvoiced_length);
        SNR = voiced_power / unvoiced_power;
        SNR_dBv = 10 * log10(SNR);

        snr_lin[segm] = SNR;
        snr_db[segm] = SNR_dBv;
        v_power[segm] = voiced_power;
        un_power[segm] = unvoiced_power;
        y_rms[segm] = y_mask_rms;
        y_thr[segm] = y_th;

        if (SNR_dBv > qos_th && !isinf(SNR_dBv))
        {
            elck_qos[segm] = 1;
        }
        else
        {
            elck_qos[segm] = 0;
        }
        free(all_corr_up_p);
        free(y_mask);
        free(voiced);
        free(unvoiced);
        free(data_p);
    }

    free(all_corr_up);
    free(data);
    free(resample_pcm_copy);
    double_memory_alloc((void **)all_data, n_frames, framesize + n_shift, sizeof(t_float), 1);

    elck_meta->elck_output.elck_write_out.fsd_results[0] = snr_lin;
    elck_meta->elck_output.elck_write_out.fsd_results[1] = snr_db;
    elck_meta->elck_output.elck_write_out.fsd_results[2] = v_power;
    elck_meta->elck_output.elck_write_out.fsd_results[3] = un_power;
    elck_meta->elck_output.elck_write_out.fsd_results[4] = y_rms;
    elck_meta->elck_output.elck_write_out.fsd_results[5] = y_thr;
    elck_meta->elck_output.elck_write_out.fsd_results[6] = elck_qos;
    elck_meta->elck_output.elck_out_response.fsd_response = get_fsd_resp(elck_qos, tsegm, reject_count);
}

// t_int32 qos_sum(t_float *qos_arr, t_int32 size)
// {
//     t_int32 one_counter = 1;
//     for (size_t i = 0; i < size; i++)
//     {
//         if (qos_arr[i] == 1)
//         {
//             one_counter++;
//         }
//     }
//     return one_counter;
// }

t_int32 get_fsd_resp(t_float *qos_arr, t_int32 size, t_int32 reject_count)
{
    t_int32 zero_counter = 1;
    for (size_t i = 0; i < size; i++)
    {
        if (qos_arr[i] == 0)
        {
            zero_counter++;
        }
    }
    return zero_counter > reject_count ? 0 : 1;
}

void zero_segment(t_float *input_pcm, t_int32 start, t_int32 end, t_int32 num_samples, t_float *data)
{
    t_int32 j = 0;
    for (t_int32 i = start; i < end; i++)
    {
        data[j] = __DBL_EPSILON__;
        j++;
    }
}

void copy_segment(t_float *input_pcm, t_int32 start, t_int32 end, t_int32 num_samples, t_float *data)
{
    t_int32 j = 0;
    for (t_int32 i = start; i < end; i++)
    {
        data[j] = input_pcm[i];
        j++;
    }
}

void fsd_pcm_normalize(t_float *input_pcm, t_int32 size)
{
    t_float tmp_mean = 0;
    t_float abs_max = 0;

    get_abs_max_float(input_pcm, size, &abs_max);

    for (size_t i = 0; i < size; i++)
    {
        input_pcm[i] = input_pcm[i] / abs_max;
    }

    mean_float(input_pcm, size, &tmp_mean);

    for (size_t i = 0; i < size; i++)
    {
        input_pcm[i] = input_pcm[i] - tmp_mean;
    }
}

void get_abs_max_float(const t_float *input, const t_int32 size, t_float *out_max)
{
    t_float tmp_out_max = __FLT_MIN__;
    t_float *tmp_store = malloc(sizeof(t_float) * size);

    for (t_int32 i = 0; i < size; i++)
    {
        tmp_store[i] = fabs(input[i]);
    }

    for (t_int32 i = 0; i < size; i++)
    {
        if (tmp_out_max < tmp_store[i])
        {
            tmp_out_max = tmp_store[i];
        }
    }
    *out_max = tmp_out_max;
    free(tmp_store);
}

t_float get_signal_power(t_float *sig, t_int32 size, t_float den)
{
    t_float mul = 1 / den;
    t_float power = 0;
    t_float sum = 0;

    for (size_t i = 0; i < size; i++)
    {
        power = pow(sig[i], 2);
        sum += power;
    }
    return sum * mul;
}

t_float get_sum(t_float *input, t_int32 size)
{
    t_float sum = 0;
    for (size_t i = 0; i < size; i++)
    {
        sum += input[i];
    }
    return sum;
}

t_float *get_voiced(t_float *all_data, t_float *y_mask, t_int32 size, t_int32 *voiced_len)
{
    t_float *ret = calloc(size, sizeof(t_float));
    t_int32 len = 0;

    for (size_t i = 0; i < size; i++)
    {
        if (y_mask[i] > 0)
        {
            ret[i] = all_data[i];
            len++;
        }
    }
    *voiced_len = len;
    return ret;
}

t_float *get_unvoiced(t_float *all_data, t_float *y_mask, t_int32 size, t_int32 *unvoiced_len)
{
    t_float *ret = calloc(size, sizeof(t_float));
    t_int32 len = 0;

    for (size_t i = 0; i < size; i++)
    {
        if (y_mask[i] == 0)
        {
            ret[i] = all_data[i];
            len++;
        }
    }
    *unvoiced_len = len;
    return ret;
}

t_int32 fill_corr(t_float *all_corr_up, t_int32 start, t_int32 end, t_float val)
{
    for (size_t i = start; i < end; i++)
    {
        all_corr_up[i] = val;
    }
}

t_float *shift_frame(t_float *sel_frame, t_int32 framesize, t_int32 n_shift)
{
    t_int32 end = framesize;

    for (t_int32 i = end; i < framesize + n_shift; i++)
    {
        sel_frame[i] = 0;
    }
    return sel_frame + n_shift;
}

t_float dot_multiply(t_float *arr1, t_float *arr2, t_int32 size)
{
    t_float arr3[size];
    t_float sum = 0;

    for (t_int32 i = 0; i < size; i++)
    {
        arr3[i] = arr1[i] * arr2[i];
        sum += arr3[i];
    }
    return sum;
}

t_float calc_y_mask_rms(t_float *y_mask, t_int32 size)
{
    t_float arr3[size];
    t_float sum = 0;

    for (t_int32 i = 0; i < size; i++)
    {
        arr3[i] = pow(y_mask[i], 2);
        sum += arr3[i];
    }
    return sqrt(sum / size);
}

t_float *dot_multiply_abs(t_float *arr1, t_float *arr2, t_int32 size)
{
    t_float *arr3 = malloc(size * sizeof(t_float));

    for (t_int32 i = 0; i < size; i++)
    {
        arr3[i] = arr1[i] * fabs(arr2[i]);
    }
    return arr3;
}

t_float fsd_corr(t_float *arr1, t_float *arr2, t_int32 size)
{
    t_float sum = dot_multiply(arr1, arr2, size);
    return pow(sum, 2) / size;
}

t_int32 compare_float(const void *a, const void *b)
{
    t_float arg1 = *(const float *)a;
    t_float arg2 = *(const float *)b;

    return (arg1 > arg2) - (arg1 < arg2); // possible shortcut
}

t_float median_float(t_float *tmp_arr, int size)
{
    t_float arr[size];
    for (size_t i = 0; i < size; i++)
    {
        arr[i] = tmp_arr[i];
    }

    qsort(arr, size, sizeof(t_float), compare_float);
    if (size % 2 == 0)
    {
        return (arr[size / 2] + arr[size / 2 - 1]) / 2.0;
    }
    else
    {
        return arr[size / 2];
    }
}

void filter_mask(t_float *y_mask, t_int32 size, t_float y_th)
{
    for (size_t i = 0; i < size; i++)
    {
        if (y_mask[i] >= y_th)
        {
            y_mask[i] = 1;
        }
        else if (y_mask[i] < y_th)
        {
            y_mask[i] = 0;
        }
    }
}

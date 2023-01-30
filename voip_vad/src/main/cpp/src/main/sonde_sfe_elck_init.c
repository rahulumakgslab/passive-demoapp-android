/*
* @brief sonde speech feature extraction library entry function.
*
* @author Swapnil Warkar
* 
* Version - 1.0.0
*
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <libgen.h>
#include <stdarg.h>
#include <math.h>
#include <unistd.h>
#include <assert.h>
#include "sonde_spfe_function_def.h"

char *cfg_version = "\"v3.2.1\"";

t_int32 elck_raise_error( t_int32 error_code )
{
	#if LAMBDA
		exit(error_code);
	#elif EDGE
		return error_code;
	#endif
}

t_int8 * elck_dump(s_elck_vad_structs *elck_calc_structs, s_elck_meta *elck_meta, t_int8 *output_dir)
{
	t_int8 *version = elck_meta->version;
	t_int8 *json_string = form_json(elck_meta->elck_output.elck_out_response, version);

	#if RESEARCH
		elck_write_output(elck_calc_structs->paths_and_meta, elck_meta, output_dir);
	#endif

	#if LAMBDA
		write_json(json_string, output_dir);
	#endif

	return json_string;
}

t_int32 elck_vad_calc( s_elck_vad_structs *elck_vad_structs, s_elck_meta *elck_meta)
{
	if (elck_vad_structs->features_config->sonde_vad == 1)
	{
		t_float percent = 0.0;
		t_float percent_r = 0.0;
		t_float reduced_to_total_diff = 0.0;
		t_float vad_thresh_arr[2];
		t_int32 total_frames = 0;
		t_int32 total_frames_r = 0;
		t_int32 active_frames = 0;
		t_int32 vad_response = 0;

		vad_thresh_arr[0] = elck_meta->elck_user_thresholds.vad_thresh_arr[0];
		vad_thresh_arr[1] = elck_meta->elck_user_thresholds.vad_thresh_arr[1];

		compute_vad(elck_vad_structs->paths_and_meta, elck_vad_structs->file_parameters, elck_vad_structs->user_config, elck_vad_structs->vad_params, elck_vad_structs->shared_params, elck_vad_structs->features_config);

		if (elck_vad_structs->shared_params->voice_frames == 0)
		{
			percent = 0;
			percent_r = 0;
			vad_response = 0;
		}
		else
		{
			percent = (elck_vad_structs->shared_params->voice_frames / (t_float)elck_vad_structs->shared_params->number_of_frames_vad) * 100;
			percent_r = (elck_vad_structs->shared_params->voice_frames / (t_float)elck_vad_structs->shared_params->reduced_number_of_frames) * 100;
			reduced_to_total_diff = fabs(percent_r - percent);
			if (fabs(percent_r - percent) >= vad_thresh_arr[1])
			{
				vad_response = 0;
			}
			else
			{
				vad_response = get_vad_response(elck_vad_structs->shared_params->reduced_number_of_frames, elck_vad_structs->shared_params->voice_frames, vad_thresh_arr[0]);
			}
		}

		total_frames = elck_vad_structs->shared_params->number_of_frames_vad;
		total_frames_r = elck_vad_structs->shared_params->reduced_number_of_frames;
		active_frames = elck_vad_structs->shared_params->voice_frames;

		elck_meta->elck_output.elck_out_response.elck_vad_response = vad_response;
		elck_meta->elck_output.elck_write_out.elck_vad_results[0] = total_frames;
		elck_meta->elck_output.elck_write_out.elck_vad_results[1] = total_frames_r;
		elck_meta->elck_output.elck_write_out.elck_vad_results[2] = active_frames;
		elck_meta->elck_output.elck_write_out.elck_vad_results[3] = percent_r;
		elck_meta->elck_output.elck_write_out.elck_vad_results[4] = reduced_to_total_diff;

		fprint_log_string_int_fd("\n\tAVF - Active Voice Frames Count      : %d\n", elck_vad_structs->shared_params->voice_frames);
		fprint_log_string_int_fd("\tTF - Total Frames Count              : %d\n", elck_vad_structs->shared_params->number_of_frames_vad);
		fprint_log_string_int_fd("\tTFR - After Trimming Both the Ends   : %d\n", elck_vad_structs->shared_params->reduced_number_of_frames);
		fprint_log_string_float_fd("\t%% Ratio : AVF to TF                  : %.2f%%\n", percent);
		fprint_log_string_float_fd("\t%% Ratio : AVF to TFR                 : %.2f%%\n", percent_r);
		fprint_log_string_float_fd("\t%% Diff  : TFR - TF                   : %.2f%%\n", reduced_to_total_diff);
	}
	return E_SUCCESS;
}

t_int32 elck_ahh_calc( s_elck_vad_structs *elck_ahh_structs, s_elck_meta *elck_meta)
{
	if (elck_ahh_structs->features_config->ahh_enable == 1)
	{
		t_float *ahh_output_arr = ahh_detection(elck_ahh_structs->paths_and_meta, elck_ahh_structs->shared_params, elck_ahh_structs->file_parameters);
		elck_meta->elck_output.elck_out_response.elck_ahh_response = get_ahh_response(ahh_output_arr, elck_meta->elck_user_thresholds.ahh_thresh_arr);
		elck_meta->elck_output.elck_write_out.elck_ahh_results = ahh_output_arr;
	}
	return E_SUCCESS;
}

t_int32 elck_em_calc( s_elck_vad_structs *elck_em_structs, s_elck_meta *elck_meta)
{
	if (elck_em_structs->features_config->energy_meter_enable == 1)
	{
		t_float *em_output_arr = energy_meter(elck_em_structs->paths_and_meta, elck_em_structs->shared_params, elck_em_structs->file_parameters, elck_meta->elck_user_thresholds.em_thresh_arr);
		elck_meta->elck_output.elck_write_out.elck_em_results = em_output_arr;
		elck_meta->elck_output.elck_out_response.elck_em_response = em_output_arr[4];
	}
	return E_SUCCESS;
}

s_elck_vad_structs *init_elck_structs(
	s_paths_and_meta *paths_and_meta,
	s_fileinfo *file_parameters,
	s_user_config *user_config,
	s_vad *vad_params,
	s_shared_params *shared_params,
	s_features_config *features_config)
{
	s_elck_vad_structs *elck_vad_structs = malloc(sizeof(s_elck_vad_structs));
	elck_vad_structs->paths_and_meta = paths_and_meta;
	elck_vad_structs->file_parameters = file_parameters;
	elck_vad_structs->user_config = user_config;
	elck_vad_structs->vad_params = vad_params;
	elck_vad_structs->shared_params = shared_params;
	elck_vad_structs->features_config = features_config;
	return elck_vad_structs;
}

s_elck_ahh_structs *init_elck_ahh(
	s_paths_and_meta *paths_and_meta,
	s_fileinfo *file_parameters,
	s_shared_params *shared_params)
{
	s_elck_ahh_structs *elck_ahh_structs = malloc(sizeof(s_elck_ahh_structs));
	elck_ahh_structs->paths_and_meta = paths_and_meta;
	elck_ahh_structs->file_parameters = file_parameters;
	elck_ahh_structs->shared_params = shared_params;
	return elck_ahh_structs;
}

s_elck_em_structs *init_elck_em(
	s_paths_and_meta *paths_and_meta,
	s_fileinfo *file_parameters,
	s_shared_params *shared_params)
{
	s_elck_em_structs *elck_em_structs = malloc(sizeof(s_elck_em_structs));
	elck_em_structs->paths_and_meta = paths_and_meta;
	elck_em_structs->file_parameters = file_parameters;
	elck_em_structs->shared_params = shared_params;
	return elck_em_structs;
}

s_elck_structs *elck_init(
	t_int8 *input_filename,
	t_int8 *config_file,
	t_int8 *output_dir)
{
	s_fileinfo *file_parameters = calloc(1, sizeof(s_fileinfo));
	s_config *config = calloc(1, sizeof(s_config));
	s_paths_and_meta *paths_and_meta = calloc(1, sizeof(s_paths_and_meta));
	s_shared_params *shared_params = calloc(1, sizeof(s_shared_params));
	s_features_config *features_config = calloc(1, sizeof(s_features_config));
	s_user_config *user_config = calloc(1, sizeof(s_user_config));
	s_sma_config *sma_config = calloc(1, sizeof(s_sma_config));
	s_vad *vad_params = calloc(1, sizeof(s_vad));
	s_elck_user_thresholds *elck_thresholds = NULL;

	t_float *resampled_pcm = NULL;
	t_int32 waveread_status = -1;
	t_int32 input_ext_status = -1;
	t_int32 th_valid_status = -1;
	t_int8 *resource_path = NULL;

	config->features_config = features_config;
	config->sma_config = sma_config;
	config->user_config = user_config;

	s_elck_structs *elck_structs = malloc(sizeof(s_elck_structs));
	elck_structs->elck_meta = malloc(sizeof(s_elck_meta));
	elck_structs->other_structs = malloc(sizeof(s_other_structs));

	set_paths(paths_and_meta, input_filename, config_file, output_dir);
	fprint_log_string("\n\n Welcome to sonde( c language ) speech processing feature extraction library");
	fprint_log_string_str("\n input wave file\t: %s", paths_and_meta->input_filename);
	fprint_log_string_str("\n config file    \t: %s", paths_and_meta->sonde_config_filename);
	fprint_log_string_str("\n threshold config file \t: %s", paths_and_meta->threshold_config_path);
	fprint_log_string_str("\n output path\t\t: %s", paths_and_meta->output_path_vad_1);
	fprint_log_string_str("\n resource path\t\t: %s\n", paths_and_meta->resources);

	shared_params->memory_deallocation_flag = 0;
	read_config_file(config, file_parameters, paths_and_meta);
	// read_threshold_config_file(config, paths_and_meta, &elck_structs->elck_meta->elck_user_thresholds);
	fprint_log_string_str("\n\tELCK Version  :  %s\n", paths_and_meta->algo_version);

	validate_version(paths_and_meta->algo_version);

	elck_structs->elck_meta->version = paths_and_meta->algo_version;

	if (features_config->sonde_vad == 0)
	{
		features_config->vad_annotation = 0;
	}

	input_ext_status = check_input_existence(paths_and_meta);

	if (input_ext_status != 0)
	{
		return NULL; //default_json(elck_output);
	}

	waveread_status = read_wave_file(file_parameters, paths_and_meta, shared_params, user_config);

	if (waveread_status != 0)
	{
		return NULL; //default_json(elck_output);
	}

	resampled_pcm = resample(shared_params, file_parameters, user_config);
	shared_params->resampled_pcm_data = resampled_pcm;
	memcpy_padding_vad_gen(resampled_pcm, shared_params->eq_pcm, file_parameters->actual_samples + VAD_PADDING2);
	init_algorithm_param(paths_and_meta, features_config, user_config, shared_params, file_parameters);
	init_fe_dynamic_memory(file_parameters, config, shared_params, vad_params);
	equalize_gain(shared_params);

	//store them to free later
	elck_structs->other_structs->file_parameters = file_parameters;
	elck_structs->other_structs->config = config;
	elck_structs->other_structs->paths_and_meta = paths_and_meta;
	elck_structs->other_structs->shared_params = shared_params;
	elck_structs->other_structs->features_config = features_config;
	elck_structs->other_structs->user_config = user_config;
	elck_structs->other_structs->sma_config = sma_config;
	elck_structs->other_structs->vad_params = vad_params;
	strcpy(paths_and_meta->output_path_vad_1, output_dir);
	elck_structs->elck_calc_structs = init_elck_structs(paths_and_meta, file_parameters, user_config, vad_params, shared_params, features_config);
	return elck_structs;
}

void elck_destruct(s_elck_structs * elck_structs)
{
	elck_structs->other_structs->shared_params->memory_deallocation_flag = 1; //to free allocated memory, set this flag;

	init_fe_dynamic_memory(elck_structs->other_structs->file_parameters, elck_structs->other_structs->config, elck_structs->other_structs->shared_params, elck_structs->other_structs->vad_params);

	free(elck_structs->elck_meta);
	free(elck_structs->other_structs->file_parameters);
	free(elck_structs->other_structs->config);
	free(elck_structs->other_structs->paths_and_meta);
	free(elck_structs->other_structs->features_config);
	free(elck_structs->other_structs->user_config);
	free(elck_structs->other_structs->sma_config);
	free(elck_structs->other_structs->vad_params);
	free(elck_structs->other_structs->shared_params->resampled_pcm_data);
	free(elck_structs->other_structs->shared_params->eq_pcm);
	free(elck_structs->other_structs->shared_params->pcm_data);
	free(elck_structs->other_structs->shared_params);
	free(elck_structs->other_structs);
	free(elck_structs->elck_calc_structs);
	free(elck_structs);
}

void set_paths(s_paths_and_meta *paths_and_meta, t_int8 *audio,
			   t_int8 *config, t_int8 *output_files_dir)
{
	t_int8 *cfg_path = get_output_path_gen(config);
	t_int8 *res_name = "/resources";
	t_int8 *res_path = malloc(strlen(cfg_path) + strlen(res_name) + 1);
	strcpy(res_path, cfg_path);
	strcat(res_path, res_name);
	strcpy(paths_and_meta->output_path_vad_1, output_files_dir);
	strcpy(paths_and_meta->resources, res_path);
	strcpy(paths_and_meta->input_filename, audio);
	strcpy(paths_and_meta->sonde_config_filename, config);
	strcpy(paths_and_meta->csv_name, "None");
}

typedef struct
{
	t_int8 *vad_path;
	t_int8 *fbf_path;
} out_path;

t_int8 *default_json(s_elck_out_response elck_out_response, t_int8 * version)
{
	elck_out_response.elck_vad_response = -1;
	elck_out_response.elck_ahh_response = -1;
	elck_out_response.elck_em_response = -1;
	return form_json(elck_out_response, version);
}


void validate_version(char *version)
{
	fprint_log_string_str("\tExpected %s\n", cfg_version);
	fprint_log_string_str("\tValidating library version%s","...\n");

	assert(strcmp(cfg_version, version) == 0);
	fprint_log_string_str("\tLibrary version validation successful...%s", "\n");
}

t_int8 *derive_path_resources(t_int8 *config_file)
{
	t_int8 *res_t = "resources";
	t_int8 *output_dir = get_output_path_gen(config_file);
	t_int8 *config_path = malloc(strlen(output_dir) + strlen(res_t) + 1);

	strcpy(config_path, output_dir);
	strcat(config_path, "/");
	strcat(config_path, res_t);

	return config_path;
}

t_int32 get_vad_response(t_int32 trimmed_number_of_frames, t_int32 voice_frames_count, t_float threshold_percent)
{
	t_int32 result = 0;
	t_float threshold = threshold_percent / 100.0;
	result = (voice_frames_count >= (trimmed_number_of_frames * threshold));
	return result;
}

void write_json(t_int8 *input_json, t_int8 *output_dir)
{
	t_int8 *name = "/elck-flags.json";
	t_int8 filename[strlen(name) + strlen(output_dir) + 1];
	strcpy(filename, output_dir);
	strcat(filename, name);

	t_int8 *check = strdup(filename);
	if ((access(dirname(check), F_OK)) == -1)
	{
		fprint_log_string("ERROR : output dir does not exist\n");
		free(check);
		elck_raise_error(E_FILE_NOT_FOUND);
	}

	FILE *f_elck = fopen(filename, "w");
	fprintf(f_elck, "%s", input_json);
	fclose(f_elck);
}

//t_int8 * form_json( t_int32 vad_response, t_int32 ahh_response, t_int32 lmk_response, t_int32 em_response )
t_int8 *form_json(s_elck_out_response elck_out_response, t_int8 *elck_version)
{
	t_int32 vad_response = elck_out_response.elck_vad_response;
	t_int32 ahh_response = elck_out_response.elck_ahh_response;
	t_int32 em_response = elck_out_response.elck_em_response;

	t_int8 *version_t = "{ \"version\" : %s,";
	t_int8 *vad_t = " \"vad\" : %d,";
	t_int8 *ahh_t = "\"ahh\" : %d,";
	//t_int8 * lmk_t = "\"lmk\" : %d,";
	t_int8 *em_t = "\"energy_meter\" : %d }";

	t_int8 *tmp_t = NULL;
	t_int8 *tmp = NULL;

	//tmp_t = malloc( strlen(vad_t) + strlen(lmk_t) + strlen(ahh_t) + strlen(em_t) + 5 );
	//tmp = malloc( strlen(vad_t) + strlen(lmk_t) + strlen(ahh_t) + strlen(em_t)  + 5 );

	tmp_t = malloc(strlen(version_t) + strlen(vad_t) + strlen(ahh_t) + strlen(em_t) + 5);
	tmp = malloc(strlen(version_t) + strlen(vad_t) + strlen(ahh_t) + strlen(em_t) + 5);

	strcpy(tmp_t, version_t);
	strcat(tmp_t, vad_t);
	strcat(tmp_t, ahh_t);
	// strcat( tmp_t, lmk_t );
	strcat(tmp_t, em_t);

	sprintf(tmp, tmp_t, elck_version, vad_response, ahh_response, em_response);

	free(tmp_t);

	return tmp;
}

static int validate_thresholds(t_float *th_array)
{
	int flag = 0;

	t_float vad_th = th_array[0];
	t_float vad_th2 = th_array[1];
	t_float lmk_th = th_array[2];
	t_float ahh_th1 = th_array[3];
	t_float ahh_th2 = th_array[4];
	t_float em_th1 = th_array[5];
	t_float em_th2 = th_array[6];

	fprint_log_string_float("\n\n\tvad threshold 1 : %f", vad_th);
	fprint_log_string_float("\n\tvad threshold 1 : %f", vad_th2);
	fprint_log_string_float("\n\tlmk threshold 1 : %f", lmk_th);
	fprint_log_string_float("\n\tahh threshold 1 : %f", ahh_th1);
	fprint_log_string_float("\n\tahh threshold 2 : %f", ahh_th2);
	fprint_log_string_float("\n\tenergy mtr threshold 1 : %f", em_th1);
	fprint_log_string_float("\n\tenergy mtr threshold 2 : %f\n", em_th2);

	if (vad_th < 0 || vad_th > 100)
	{
		fprint_log_string_float("\n\tReceived invalid VAD threshold : %f\n", vad_th);
		flag = 1;
	}

	if (lmk_th < 0 || lmk_th > 100)
	{
		fprint_log_string_float("\n\tReceived invalid LMK threshold : %f\n", lmk_th);
		flag = 1;
	}

	if (ahh_th1 < 0 || ahh_th1 > 100)
	{
		fprint_log_string_float("\n\tReceived invalid AHH threshold 1 : %f\n", ahh_th1);
		flag = 1;
	}

	if (ahh_th2 < 0 || ahh_th2 > 100)
	{
		fprint_log_string_float("\n\tReceived invalid AHH threshold 2 : %f\n", ahh_th1);
		flag = 1;
	}

	if (em_th1 < 0 || em_th1 > 100)
	{
		fprint_log_string_float("\n\tReceived invalid Energy meter threshold 1 : %f\n", ahh_th1);
		flag = 1;
	}

	if (em_th2 < 0 || em_th2 > 100)
	{
		fprint_log_string_float("\n\tReceived invalid Energy meter threshold 2 : %f\n", ahh_th1);
		flag = 1;
	}

	if (flag == 1)
	{
		return -1;
	}
	else
	{
		return 0;
	}
}

// void set_thresholds( s_elck_threshold )
// {
// 	vad_thresh_arr[0] = threshold_array[0];
// 	vad_thresh_arr[1] = threshold_array[1];
// 	t_float lmk_threshold = threshold_array[2];
// 	ahh_thresh_arr[0] = threshold_array[3];
// 	ahh_thresh_arr[1] = threshold_array[4];
// 	em_thresh_arr[0] = threshold_array[5];
// 	em_thresh_arr[1] = threshold_array[6];
// th_valid_status = validate_thresholds( threshold_array );

// if( th_valid_status != 0 )
// {
// 	return default_json();
// }
// }
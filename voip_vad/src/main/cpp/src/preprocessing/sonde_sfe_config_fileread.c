/*
* @brief read config file, read wave file.
*
* @author Swapnil Warkar
*
*/

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <ctype.h>
#include <math.h>
#include <assert.h>
#include <string.h>
#include "sonde_spfe_function_def.h"

#define READ_CHAR 125
static FILE *input_yaml = NULL;
char *feature_name_copy[READ_CHAR];

typedef struct
{
    char *key;
    char *val;
    char *section;
    char *free;
    int section_id;
} meta;

typedef struct
{
    int isf;
    float fval;
} usr_conf_ds;

void remove_chars(char *str, char c)
{
    char *pr = str, *pw = str;
    while (*pr)
    {
        *pw = *pr++;
        pw += (*pw != c);
    }
    *pw = '\0';
}

char *trim_string(char *line)
{
    char *ptr = NULL;
    while (strpbrk(line, "- #\r\n"))
    {
        if ((ptr = strpbrk(line, "#")) != NULL)
        {
            *ptr = '\0';
        }
        else if ((ptr = strpbrk(line, " ")) != NULL)
        {
            remove_chars(line, *ptr);
        }
        else if ((ptr = strpbrk(line, "-")) != NULL)
        {
            remove_chars(line, *ptr);
        }
        else if ((ptr = strpbrk(line, "\r")) != NULL)
        {
            *ptr = '\0';
        }
        else if ((ptr = strpbrk(line, "\n")) != NULL)
        {
            *ptr = '\0';
        }
    }
    return line;
}

char *insert(char *source)
{
    return trim_string(source);
}

void split_string(char *line, meta *x)
{
    char *str = strdup(line);
    char *new_str = trim_string(str);
    char *token = strtok(new_str, ": ");
    x->key = NULL;
    x->val = NULL;

    if (token != NULL)
        x->key = insert(token);

    while (token != NULL)
    {
        token = strtok(NULL, "-");
        if (token != NULL)
            x->val = insert(token);
    }
    x->free = str;
}

int validate_size(int feat_name_size, int flags_size)
{
    if (!((feat_name_size == flags_size)))
    {
        perror("Inbalanced count in feature setting array");
        assert(0);
    }
    else
        return flags_size;
}

usr_conf_ds fetch_val(char *val_string)
{
    float val = 0.0;
    usr_conf_ds data;
    if (val_string != NULL)
    {
        if (!strcmp(val_string, "mel"))
        {
            data.fval = 1;
            data.isf = 0;
        }
        else if (!strcmp(val_string, "true"))
        {
            data.isf = 0;
            data.fval = 1;
        }
        else
        {
            val = atof(val_string);
            if (floor(val) == ceil(val))
                data.isf = 0;
            else
                data.isf = 1;
            data.fval = val;
        }
    }
    return data;
}

char *set_feature_set(void *f_conf)
{
    s_features_config *features_config = (s_features_config *)f_conf;
    void *update_feature[] = {
        &features_config->mfcc,
        &features_config->sonde_lsp,
        &features_config->sonde_formant,
        &features_config->delta_mfcc,
        &features_config->dd_mfcc,
        &features_config->vad_annotation,
        &features_config->scf,
        &features_config->f0,
        &features_config->jitter,
        &features_config->jitter1,
        &features_config->shimmer,
        &features_config->ampl,
        &features_config->spectral_slope,
        &features_config->landmarks,
        &features_config->aqs_rms,
        &features_config->aqs_rmsnorm,
        &features_config->aqs_min,
        &features_config->aqs_max,
        &features_config->aqs_avg,
        &features_config->aqs_avgnorm,
        &features_config->orgmfcc,
        &features_config->org_deltamfcc,
        &features_config->org_ddmfcc,
        &features_config->org_scf,
        &features_config->orgfmt};
    void *feature_name[] = {
        "mfcc",
        "lsp",
        "fmt",
        "deltamfcc",
        "deltadeltamfcc",
        "vadannotation",
        "scf",
        "f0",
        "jitter",
        "jitterddp",
        "shimmer",
        "amplitude",
        "spectralslope",
        "lmk",
        "rmsamp",
        "rmsampnorm",
        "minamp",
        "maxamp",
        "avgamp",
        "avgampnorm",
        "orgmfcc",
        "orgdeltamfcc",
        "orgdeltadeltamfcc",
        "orgscf",
        "orgfmt"};

    void *non_llf[] = {
        "vtc",
        "audio_filters",
        "vad",
        "fft"};
    void *non_llf_cfg[] = {
        &features_config->vtc,
        &features_config->smoothfilter,
        &features_config->sonde_vad,
        &features_config->feature_fft};

    static char lines[READ_CHAR];
    meta read_feature = {NULL, NULL};
    int size = 0, nllf_size = 0;
    int feat_name_size = sizeof(feature_name) / sizeof(void *);
    int flags_size = sizeof(update_feature) / sizeof(void *);
    size = validate_size(feat_name_size, flags_size);
    int non_llf_name_size = sizeof(non_llf) / sizeof(void *);
    int non_llf_flags_size = sizeof(non_llf_cfg) / sizeof(void *);
    nllf_size = validate_size(non_llf_name_size, non_llf_flags_size);
    for (int i = 0; i < size; i++)
        feature_name_copy[i] = (char *)feature_name[i];

    for (; (fgets(lines, READ_CHAR, input_yaml)) != NULL;)
    {
        split_string(lines, &read_feature);
        for (int i = 0; i < size; i++)
        {
            if (read_feature.key != NULL && !strcmp(read_feature.key, feature_name[i]))
            {
                *(int *)update_feature[i] = 1;
                break;
            }
            else if (read_feature.key != NULL)
            {
                for (int nllfidx = 0; nllfidx < nllf_size; nllfidx++)
                {
                    if (!strcmp(read_feature.key, non_llf[nllfidx]))
                    {
                        if (read_feature.key != NULL && !strcmp(read_feature.key, "vtc") && !strcmp(read_feature.val, "true"))
                        {
                            *((int *)non_llf_cfg[0]) = 1;
                            return read_feature.key;
                        }
                        for (; (fgets(lines, READ_CHAR, input_yaml)) != NULL;)
                        {
                            split_string(lines, &read_feature);

                            if (read_feature.key != NULL && !strcmp(read_feature.key, "sma"))
                                *((int *)non_llf_cfg[1]) = 1;
                            else if (read_feature.key != NULL && !strcmp(read_feature.key, "vad") && !strcmp(read_feature.val, "true"))
                                *((int *)non_llf_cfg[2]) = 1;
                            else if (read_feature.key != NULL && !strcmp(read_feature.key, "fft") && !strcmp(read_feature.val, "true"))
                                *((int *)non_llf_cfg[3]) = 1;
                            else if (read_feature.key != NULL && !strcmp(read_feature.key, "user_config"))
                            {
                                return read_feature.key;
                            }
                        }
                    }
                }
            }
            else if (read_feature.key != NULL && !strcmp(read_feature.key, "user_config"))
                return read_feature.key;
        }
    }
}

char *set_user_config(void *user_conf)
{
    s_user_config *user_config = (s_user_config *)user_conf;

    void *usr_conf_keys[] = {
        "pre_emphasis_factor",
        "sonde_framesize",
        "sonde_framestep",
        "vad_blocksize",
        "vad_decimate",
        "vad_filterlength",
        "vad_overlap",
        "mfcc_filterbanks",
        "mfcc_cepstral_coeff",
        "mfcc_lower_frequency",
        "mfcc_higher_frequency",
        "mfcc_ceplifter",
        "mfcc_scale",
        "orgmfcc_filterbanks",
        "orgmfcc_cepstral_coeff",
        "orgmfcc_lower_frequency",
        "orgmfcc_higher_frequency",
        "orgmfcc_scale",
        "scf_filterbanks",
        "scf_cepstral_coeff",
        "scf_lower_frequency",
        "scf_higher_frequency",
        "scf_scale",
        "number_of_lsp",
        "sma_filt_length",
        "slopeL0",
        "slopeL1",
        "slopeH0",
        "slopeH1",
        "region_based_fe",
        "num_of_formants",
        "formant_resampling_freq",
        "lpc_order",
        "fmt_min_freq",
        "fmt_max_freq"};

    void *usr_conf_val[] = {
        &user_config->pre_emphasis_factor,
        &user_config->sonde_framesize,
        &user_config->sonde_framestep,
        &user_config->BlockSize,
        &user_config->DecimationOrder,
        &user_config->FilterLength,
        &user_config->OverlapRatio,
        &user_config->mfcc_filterbanks,
        &user_config->mfcc_filterorder,
        &user_config->mfcc_lower_frequency,
        &user_config->mfcc_higher_frequency,
        &user_config->mfcc_ceplifter,
        &user_config->mfcc_mel_enabled,
        &user_config->orgmfcc_filterbanks,
        &user_config->orgmfcc_filterorder,
        &user_config->orgmfcc_lower_frequency,
        &user_config->orgmfcc_higher_frequency,
        &user_config->orgmfcc_mel_enabled,
        &user_config->scf_filterbanks,
        &user_config->scf_cepstral_coeff,
        &user_config->scf_lower_frequency,
        &user_config->scf_higher_frequency,
        &user_config->scf_mel_enabled,
        &user_config->lsp_filter_order,
        &user_config->SmoothFilterLength,
        &user_config->spect_slope_low0,
        &user_config->spect_slope_low1,
        &user_config->spect_slope_high0,
        &user_config->spect_slope_high1,
        &user_config->region_fe,
        &user_config->nfmt,
        &user_config->fmt_resample_freq,
        &user_config->lpc_order,
        &user_config->fmt_min_freq,
        &user_config->fmt_max_freq};

    static char lines[READ_CHAR];
    int size = 0, nllf_size = 0;
    int key_size = sizeof(usr_conf_keys) / sizeof(void *);
    int val_size = sizeof(usr_conf_val) / sizeof(void *);
    size = validate_size(key_size, val_size);
    meta get_config = {NULL, NULL};
    usr_conf_ds get_val;

    for (; (fgets(lines, READ_CHAR, input_yaml)) != NULL;)
    {
        split_string(lines, &get_config);
        for (int i = 0; i < size; i++)
        {
            if (get_config.key != NULL && !strcmp(get_config.key, usr_conf_keys[i]))
            {
                get_val = fetch_val(get_config.val);
                float val = get_val.fval;
                if (get_val.isf == 0)
                    *(int *)usr_conf_val[i] = val;
                else if (get_val.isf == 1)
                    *(float *)usr_conf_val[i] = val;
                else
                    assert(0);
                break;
            }
            else if (get_config.key == NULL)
                return get_config.key;
        }
    }
}

char **get_vtc_features(int *number_of_vtc_features)
{
    char *line = NULL;
    size_t _len = 0;
    ssize_t read;
    int count = 0;
    meta fpair;
    char **features = malloc(1000 * sizeof(char *));
    char **final_features = NULL;

    while ((read = getline(&line, &_len, input_yaml)) != -1)
    {
        if (strstr(line, "-"))
        {
            split_string(line, &fpair);
            features[count] = strdup(fpair.key);
            count++;
        }
        else
        {
            break;
        }
    }
    *number_of_vtc_features = count;
    final_features = realloc(features, (count + 1) * sizeof(char *));
    return final_features;
}

char *set_vtc_config(void *vtc_conf)
{
    s_vtc *vtc_params = (s_vtc *)vtc_conf;
    void *vtc_conf_keys[] = {
        "fpairs",
        "num_corrpoints",
        "winsize",
        "numscale",
        "num_scale0",
        "num_scale1",
        "num_scale2",
        "num_scale3"};
    void *vtc_conf_val[] = {
        &vtc_params->fpairs,
        &vtc_params->num_corr_points,
        &vtc_params->winsize,
        &vtc_params->numscale_count,
        &vtc_params->num_scale[0],
        &vtc_params->num_scale[1],
        &vtc_params->num_scale[2],
        &vtc_params->num_scale[3]};

    static char lines[READ_CHAR];
    int size = 0, nllf_size = 0;
    int key_size = sizeof(vtc_conf_keys) / sizeof(void *);
    int val_size = sizeof(vtc_conf_val) / sizeof(void *);
    size = validate_size(key_size, val_size);
    meta get_config = {NULL, NULL};
    usr_conf_ds get_val;
    int vtc_feature_count = 0;

    for (; (fgets(lines, READ_CHAR, input_yaml)) != NULL;)
    {
        split_string(lines, &get_config);
        if (get_config.key != NULL && !strcmp(get_config.key, "features"))
        {
            vtc_params->fpairs_features = get_vtc_features(&vtc_feature_count);
            vtc_params->fpairs_features_count = vtc_feature_count;
        }
        for (int i = 0; i < size; i++)
        {
            if (get_config.key != NULL && !strcmp(get_config.key, vtc_conf_keys[i]))
            {
                get_val = fetch_val(get_config.val);
                float val = get_val.fval;
                if (get_val.isf == 0)
                    *(int *)vtc_conf_val[i] = val;
                else if (get_val.isf == 1)
                    *(float *)vtc_conf_val[i] = val;
                else
                    assert(0);
                break;
            }
            else if (get_config.key == NULL)
                return NULL;
        }
    }
    return NULL;
}

char *set_sma_config(char *key, void *sma_conf)
{
    s_sma_config *sma_config = (s_sma_config *)sma_conf;

    void *sma_conf_keys[] = {
        &sma_config->mfcc_sma,
        &sma_config->lsp_sma,
        &sma_config->fmt_sma,
        &sma_config->dmfcc_sma,
        &sma_config->ddmfcc_sma,
        &sma_config->scf_sma,
        &sma_config->f0_sma,
        &sma_config->jitter_sma,
        &sma_config->jitter1_sma,
        &sma_config->shimmer_sma,
        &sma_config->ampl_sma,
        &sma_config->spect_slope_sma,
        &sma_config->orgmfcc_sma,
        &sma_config->orgdeltamfcc_sma,
        &sma_config->orgddmfcc_sma,
        &sma_config->orgscf_sma,
        &sma_config->orgfmt_sma};

    void *sma_conf_val[] = {
        "mfcc",
        "lsp",
        "fmt",
        "deltamfcc",
        "deltadeltamfcc",
        "scf",
        "f0",
        "jitter",
        "jitterddp",
        "shimmer",
        "amplitude",
        "spectralslope",
        "orgmfcc",
        "orgdeltamfcc",
        "orgdeltadeltamfcc",
        "orgscf",
        "orgfmt"};

    static char lines[READ_CHAR];
    int size = 0, nllf_size = 0;
    int key_size = sizeof(sma_conf_keys) / sizeof(void *);
    int val_size = sizeof(sma_conf_val) / sizeof(void *);
    size = validate_size(key_size, val_size);
    meta get_config = {NULL, NULL};
    usr_conf_ds get_val;
    int feature = 0;

    for (; feature < size; feature++)
    {
        if (!strcmp(key, sma_conf_val[feature]))
            break;
    }

    for (; (fgets(lines, READ_CHAR, input_yaml)) != NULL;)
    {
        split_string(lines, &get_config);
        if (get_config.key != NULL && !strcmp(get_config.key, "sma"))
            *(int *)sma_conf_keys[feature] = 1;
        else if (get_config.key == NULL)
            return NULL;
    }
}

int process_section(meta *sec, s_config *config)
{

    s_features_config *features_config = config->features_config;
    s_user_config *user_config = config->user_config;
    s_sma_config *sma_config = config->sma_config;

    char *sections[] = {"user_config"};
    char *(*sec_ptr[])(void *) = {set_user_config};
    void *args[] = {user_config};
    static int exec = 0;

    int size = sizeof(sections) / sizeof(sections[0]);
    for (int i = 0; i < size; i++)
    {
        if (sec->key != NULL && !strcmp(sec->key, sections[i]))
        {
            sec->section = sections[i];
            sec->key = (*sec_ptr[i])(args[i]);
            exec = 1;
        }
    }

    for (int i = 0; i < READ_CHAR && exec && sec->key != NULL; i++)
    {
        if (feature_name_copy[i] != NULL && !strcmp(sec->key, feature_name_copy[i]))
        {
            sec->key = set_sma_config(sec->key, sma_config);
        }
    }
}

int configure_enum(s_features_config *features_config, s_sma_config *sma_config)
{
    int update_feature[] = {
        features_config->mfcc,
        features_config->sonde_lsp,
        features_config->sonde_formant,
        features_config->delta_mfcc,
        features_config->dd_mfcc,
        features_config->vad_annotation,
        features_config->scf,
        features_config->f0,
        features_config->ampl,
        features_config->jitter,
        features_config->jitter1,
        features_config->shimmer,
        features_config->spectral_slope,
        features_config->landmarks,
        features_config->aqs_rms,
        features_config->aqs_rmsnorm,
        features_config->aqs_min,
        features_config->aqs_max,
        features_config->aqs_avg,
        features_config->aqs_avgnorm,
        features_config->sonde_vad,
        features_config->orgmfcc,
        features_config->org_deltamfcc,
        features_config->org_ddmfcc,
        features_config->org_scf,
        features_config->orgfmt,
        features_config->feature_fft};

    int sma_conf_keys[] = {
        sma_config->mfcc_sma,
        sma_config->lsp_sma,
        sma_config->fmt_sma,
        sma_config->dmfcc_sma,
        sma_config->ddmfcc_sma,
        sma_config->scf_sma,
        sma_config->f0_sma,
        sma_config->ampl_sma,
        sma_config->jitter_sma,
        sma_config->jitter1_sma,
        sma_config->shimmer_sma,
        sma_config->spect_slope_sma,
        sma_config->mfcc_sma,
        sma_config->dmfcc_sma,
        sma_config->ddmfcc_sma,
        sma_config->scf_sma,
        sma_config->orgfmt_sma};

    int sma_feature_enable[] = {
        features_config->mfcc,
        features_config->sonde_lsp,
        features_config->sonde_formant,
        features_config->delta_mfcc,
        features_config->dd_mfcc,
        features_config->scf,
        features_config->f0,
        features_config->ampl,
        features_config->jitter,
        features_config->jitter1,
        features_config->shimmer,
        features_config->spectral_slope,
        features_config->orgmfcc,
        features_config->org_deltamfcc,
        features_config->org_ddmfcc,
        features_config->org_scf,
        features_config->orgfmt};

    int feature_array_size = sizeof(update_feature) / sizeof(update_feature[0]);
    int sma_feature_check = sizeof(sma_feature_enable) / sizeof(sma_feature_enable[0]);
    int sma_array_size = sizeof(sma_conf_keys) / sizeof(sma_conf_keys[0]);
    validate_size(sma_array_size, sma_feature_check);
    features_config->feature_count = feature_array_size;
    sma_config->total_sma_count = sma_array_size;
    int sma_val[sma_array_size];
    int feat_val[feature_array_size];
    int sidx = 0;
    int fidx = 0;

    for (int en = e_mfcc_sma; en != e_sma_enum_pad; en++)
    {
        sma_val[sidx] = en;
        sidx++;
    }

    for (int en = e_mfcc; en != e_feat_enum_pad; en++)
    {
        feat_val[fidx] = en;
        fidx++;
    }

    for (int feat = 0, fidx = 0; feat < feature_array_size; feat++)
    {
        if (update_feature[feat])
            features_config->features_array[fidx++] = feat_val[feat];
    }

    for (int sma = 0, sidx = 0; sma < sma_array_size; sma++)
    {
        if (sma_conf_keys[sma] == 1 && sma_feature_enable[sma] == 1)
            sma_config->sma_array[sidx++] = sma_val[sma];
    }
    return 1;
}

void replacechar(char *s,char find,char replace)
{
    int i=0;
  	for(i=0; s[i]; i++)
	{  
		if(s[i] == find)
		{
		   s[i] = replace;
	    }
	}
}

t_int32 elck_parse_flags(meta *pair, s_features_config *config, s_paths_and_meta *paths_and_meta)
{
    if (pair->key != NULL && !strcmp(pair->key, "vad") && !strcmp(pair->val, "true"))
    {
        config->sonde_vad = 1;
    }
    if (pair->key != NULL && !strcmp(pair->key, "lmk") && !strcmp(pair->val, "true"))
    {
        config->landmarks = 1;
    }
    if (pair->key != NULL && !strcmp(pair->key, "ahh") && !strcmp(pair->val, "true"))
    {
        config->ahh_enable = 1;
    }
    if (pair->key != NULL && !strcmp(pair->key, "energy_meter") && !strcmp(pair->val, "true"))
    {
        config->energy_meter_enable = 1;
    }
    if (pair->key != NULL && !strcmp(pair->key, "version"))
    {
        char algo_version[10];
        strcpy(algo_version, pair->val);
        replacechar(algo_version, '\'', '\"' );
        strcpy(paths_and_meta->algo_version, algo_version);
    }
}

void *elck_parse_thresholds(meta *pair, s_elck_user_thresholds *elck_user_thresholds)
{
    void *elck_thresh_keys[] = {
        "vad_threshold1",
        "vad_threshold2",
        "lmk_threshold",
        "ahh_threshold1",
        "ahh_threshold2",
        "em_threshold1",
        "em_threshold2"};

    void *elck_thresh_val[] = {
        &elck_user_thresholds->vad_thresh_arr[0],
        &elck_user_thresholds->vad_thresh_arr[1],
        &elck_user_thresholds->lmk_threshold,
        &elck_user_thresholds->ahh_thresh_arr[0],
        &elck_user_thresholds->ahh_thresh_arr[1],
        &elck_user_thresholds->em_thresh_arr[0],
        &elck_user_thresholds->em_thresh_arr[1],
    };

    static char lines[READ_CHAR];
    int size = 0, nllf_size = 0;
    int key_size = sizeof(elck_thresh_keys) / sizeof(void *);
    int val_size = sizeof(elck_thresh_val) / sizeof(void *);
    size = validate_size(key_size, val_size);
    meta get_config = {NULL, NULL};
    usr_conf_ds get_val;
    int feature = 0;

    for (; (fgets(lines, READ_CHAR, input_yaml)) != NULL;)
    {
        split_string(lines, &get_config);
        for (int i = 0; i < size; i++)
        {
            if (get_config.key != NULL && !strcmp(get_config.key, elck_thresh_keys[i]))
            {
                get_val = fetch_val(get_config.val);
                float val = get_val.fval;
                *(float *)elck_thresh_val[i] = val;
                break;
            }
            else if (get_config.key == NULL)
                return NULL;
        }
    }
}

t_int32 read_config_file(s_config *config, s_fileinfo *fileread, s_paths_and_meta *paths_and_meta)
{
    s_features_config *features_config = config->features_config;
    s_user_config *user_config = config->user_config;
    s_sma_config *sma_config = config->sma_config;

    #if LEVEL2_LOG
        fprintf(stderr, "\n Function Entry : read_config_file()");
    #endif

    if ((access(paths_and_meta->sonde_config_filename, F_OK)) == -1)
    {
        perror("\n	ERROR : omni config does not exist\n");
        exit(E_FILE_NOT_FOUND);
    }

    fprintf(stderr, "\n\tstart : read config file\n");
    static char lines[100];
    input_yaml = fopen(paths_and_meta->sonde_config_filename, "r");
    int feature_set_flag = 0;
    char *p = NULL;
    char *tempstr;
    int audio_filter_flag = 0;
    int vad_flag = 0, landmarks_flag = 0, high_level_flag = 0;
    int sonde_vtc = 0, vtc_config_flag = 0;
    int user_conf_flag = 0;
    char *token = NULL;
    int ver_flag = 1;
    int name_flag = 1;
    int feature_index = 2;
    meta cfg = {NULL, NULL};

    if (input_yaml)
    {
        fseek(input_yaml, 0L, SEEK_END);
        int len = ftell(input_yaml);
        rewind(input_yaml);
        if (len > 0)
        {
            for (int i = 0; (fgets(lines, 50, input_yaml)) != NULL; i++)
            {
                split_string(lines, &cfg);
                elck_parse_flags(&cfg, features_config, paths_and_meta);
                //trim_string( lines );
                process_section(&cfg, config);
                free(cfg.free);
            }
        }
        print_config(features_config, user_config);
    }
    else
    {
        perror("Unable to access input config ");
        exit(-1);
    }
}

t_int32 read_threshold_config_file(s_config *config, s_paths_and_meta *paths_and_meta, s_elck_user_thresholds * elck_thresholds)
{
    s_features_config *features_config = config->features_config;
    s_user_config *user_config = config->user_config;
    s_sma_config *sma_config = config->sma_config;

#if LEVEL2_LOG
    fprintf(stderr, "\n Function Entry : read_config_file()");
#endif

    if ((access(paths_and_meta->sonde_config_filename, F_OK)) == -1)
    {
        perror("\n	ERROR : threshold config does not exist\n");
        exit(E_FILE_NOT_FOUND);
    }

    fprintf(stderr, "\n\tstart : parsing threshold config\n");
    static char lines[100];
    input_yaml = fopen(paths_and_meta->threshold_config_path, "r");
    int feature_set_flag = 0;
    char *p = NULL;
    char *tempstr;
    int audio_filter_flag = 0;
    int vad_flag = 0, landmarks_flag = 0, high_level_flag = 0;
    int sonde_vtc = 0, vtc_config_flag = 0;
    int user_conf_flag = 0;
    char *token = NULL;
    int ver_flag = 1;
    int name_flag = 1;
    int feature_index = 2;
    meta cfg = {NULL, NULL};

    if (input_yaml)
    {
        fseek(input_yaml, 0L, SEEK_END);
        int len = ftell(input_yaml);
        rewind(input_yaml);
        if (len > 0)
        {
            for (int i = 0; (fgets(lines, 50, input_yaml)) != NULL; i++)
            {
                split_string(lines, &cfg);
                elck_parse_thresholds(&cfg, elck_thresholds);
                free(cfg.free);
            }
        }
        print_elck_thresholds(elck_thresholds);
    }
    else
    {
        perror("enable to access input config ");
        exit(-1);
    }
}

int print_elck_thresholds(s_elck_user_thresholds *elck_user_thresholds)
{
    fprintf(stderr, "\n	Printing ELCK thresholds :\n");

    void *elck_thresh_keys[] = {
        "\tvad_threshold  : %f\n",
        "\tvad_threshold2 : %f\n",
        "\tlmk_threshold  : %f\n",
        "\tahh_threshold1 : %f\n",
        "\tahh_threshold2 : %f\n",
        "\tem_threshold1  : %f\n",
        "\tem_threshold2  : %f\n"};

    void *elck_thresh_val[] = {
        &elck_user_thresholds->vad_thresh_arr[0],
        &elck_user_thresholds->vad_thresh_arr[1],
        &elck_user_thresholds->lmk_threshold,
        &elck_user_thresholds->ahh_thresh_arr[0],
        &elck_user_thresholds->ahh_thresh_arr[1],
        &elck_user_thresholds->em_thresh_arr[0],
        &elck_user_thresholds->em_thresh_arr[1],
    };

    int count = sizeof(elck_thresh_keys) / sizeof(void *);

    for (int i = 0; i < count; i++)
    {
        fprint_log_string_float(elck_thresh_keys[i], *(float *)elck_thresh_val[i]);
    }
}

int print_config(s_features_config *features_config, s_user_config *user_config)
{
    if (features_config->vtc == 0)
    {
        fprintf(stderr, "\n	enabled features :\n");
        if (features_config->sonde_vad)
            fprintf(stderr, "\n	- vad");
        if (features_config->landmarks)
            fprintf(stderr, "\n	- landmarks");
        if (features_config->ahh_enable)
            fprintf(stderr, "\n	- ahh detection");
        if (features_config->energy_meter_enable)
            fprintf(stderr, "\n	- energy meter");
        fprintf(stderr, "\n");

        fprintf(stderr, "\n	disabled features :\n");
        if (!features_config->sonde_vad)
            fprintf(stderr, "	- vad");
        if (!features_config->landmarks)
            fprintf(stderr, "\n	- lmk");
        if (!features_config->ahh_enable)
            fprintf(stderr, "\n	- ahh detection");
        if (!features_config->energy_meter_enable)
            fprintf(stderr, "\n	- energy meter");

        fprintf(stderr, "\n");

        fprintf(stderr, "\n	user configuration parameters :\n");
        fprintf(stderr, "\n	frame_step              = %d", user_config->sonde_framestep);
        fprintf(stderr, "\n	frame_size              = %d", user_config->sonde_framesize);
        fprintf(stderr, "\n	blockSize               = %d", user_config->BlockSize);
        fprintf(stderr, "\n	pre_emphasis_factor     = %lf", user_config->pre_emphasis_factor);
        fprintf(stderr, "\n	vad decimation order    = %d", user_config->DecimationOrder);
        fprintf(stderr, "\n	vad_filter_length       = %d", user_config->FilterLength);
        fprintf(stderr, "\n	vad_overlap_ratio       = %d", user_config->OverlapRatio);
    }

    fprintf(stderr, "\n\n	exit : read config file\n");
    return E_SUCCESS;
}
//         if(strstr(lines,"version:") && ver_flag == 1)
//         {
//             token = strtok(tempstr[0],"version:");
//             token = strtok(NULL,"version:'");
//             strcpy(paths_and_meta->version,token);
//             ver_flag = 0;
//             continue;
//         }
//         if(strstr(lines,"name:") && name_flag == 1)
//         {
//             token = strtok(tempstr[0],": '");
//             token = strtok(NULL,": '");
//             strcpy(paths_and_meta->algo_name,token);
//             name_flag = 0;
//             continue;
//         }
//         if(strstr(lines,"feature_set:"))
//         {
//             feature_set_flag=1;
//             continue;
//         }
//         if( feature_set_flag )              //feature-set parsing
//         {
//             if(!(strstr(lines,"-")))
//             {
//                 feature_set_flag = 0;
//                 continue;
//             }
//             if ((p=strstr(lines,"mfcc")) && !isalpha(p[-1]))
//             {
//                 features_config->optmfcc = 1;
//                 features_config->feature_count = fill_features_array(features_config->features_array,e_mfcc);
//             }
//             if ((p=strstr(lines,"orgmfcc")) && !isalpha(p[-1]))
//             {
//                 features_config->sonde_mfcc = 1;
//                 features_config->feature_count = fill_features_array(features_config->features_array,e_orgmfcc);
//             }
//             if ((p = strstr(lines, "orgdeltamfcc")) && !isalpha(p[-1]))
//             {
//                 features_config->deltamfcc = 1;
//                     features_config->feature_count = fill_features_array(features_config->features_array, e_orgdeltamfcc);
//             }
//             if ((p = strstr(lines, "orgdeltadeltamfcc")) && !isalpha(p[-1]))
//             {
//                 features_config->deltadeltamfcc = 1;
//                 features_config->feature_count = fill_features_array(features_config->features_array, e_orgdeltadeltamfcc);
//             }
//             if ((p = strstr(lines, "orgscf")) && !isalpha(p[-1]))
//             {
//                 features_config->spec_centroid = 1;
//                 features_config->feature_count = fill_features_array(features_config->features_array, e_orgscf);
//             }
//             if ((p=strstr(lines,"lsp")) && !isalpha(p[-1]))
//             {
//                 features_config->sonde_lsp = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_lsp);
//             }
//             if ((p=strstr(lines,"deltamfcc")) && !isalpha(p[-1]))
//             {
//                 features_config->optdelta_mfcc = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_dmfcc);
//             }
//             if ((p=strstr(lines,"deltadeltamfcc")) && !isalpha(p[-1]))
//             {
//                 features_config->optdd_mfcc = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_ddmfcc);
//             }
//             if ((p=strstr(lines,"fmt")) && !isalpha(p[-1]))
//             {
//                 features_config->sonde_formant = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_fmt);
//             }
//             if ((p=strstr(lines,"jitter")) && !isalpha(p[-1]))
//             {
//                 features_config->jitter = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_jitter);
//             }
//             if ((p=strstr(lines,"jitterddp")) && !isalpha(p[-1]))
//             {
//                 features_config->jitter1 = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_jitterddp);
//             }
//             if ((p=strstr(lines,"shimmer")) && !isalpha(p[-1]))
//             {
//                 features_config->shimmer = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_shimmer);
//             }
//             if ((p=strstr(lines,"ampl")) && !isalpha(p[-1]))
//             {
//                 features_config->ampl = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_ampl);
//             }
//             if ((p=strstr(lines,"f0")) && !isalpha(p[-1]))
//             {
//                 features_config->f0 = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_f0);
//             }
//             if ((p=strstr(lines,"scf")) && !isalpha(p[-1]))
//             {
//                 features_config->optscf = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_scf);
//             }
//             if ((p=strstr(lines,"vadannotation")) && !isalpha(p[-1]))
//             {
//                 features_config->vad_annotation = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_va);
//             }
//             if ((p=strstr(lines,"rmsamp")) && !isalpha(p[-1]))
//             {
//                 features_config->aqs_rms = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_rmsamp);
//             }
//             if ((p=strstr(lines,"spectralslope")) && !isalpha(p[-1]))
//             {
//                 features_config->spectral_slope = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_spectralslope);
//             }
//             if( (p=strcasestr(lines,"lmk")) && !isalpha(p[-1]))
//             {
//                 features_config->landmarks = 1;
//                 features_config->feature_count = fill_features_array(features_config->features_array,e_landmarks);
//                 continue;
//             }
//             if ((p=strstr(lines,"rmsampnorm")) && !isalpha(p[-1]))
//             {
//                 features_config->aqs_rmsnorm = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_rmsamp_norm);
//             }
//             if ((p=strstr(lines,"minamp")) && !isalpha(p[-1]))
//             {
//                 features_config->aqs_min  = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_min);
//             }
//             if ((p=strstr(lines,"maxamp")) && !isalpha(p[-1]))
//             {
//                 features_config->aqs_max = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_max);
//             }
//             if ((p=strstr(lines,"avgampnorm")) && !isalpha(p[-1]))
//             {
//                 features_config->aqs_avgnorm = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_avgamp_norm);
//             }
//             if ((p=strstr(lines,"avgamp")) && !isalpha(p[-1]))
//             {
//                 features_config->aqs_avg = 1;
//                 features_config->feature_count =  fill_features_array(features_config->features_array,e_avgamp);
//             }
//         }

//         if( (p=strcasestr(lines,"vad")) && p[-1]!=32 )
//         {
//             vad_flag = 1;
//         }
//         if(vad_flag)
//         {
//             if(strcasestr(lines,"true"))
//             {
//                 //fprintf(stderr,"\n	omni config [I] : VAD       = 1");
//                 features_config->sonde_vad = 1;
//                 features_config->feature_count = fill_features_array(features_config->features_array,e_vad);
//             }
//             vad_flag=0;
//         }

//         if( (p=strcasestr(lines,"minmax_check")) )
//         {
//             if(strcasestr(lines,"true"))
//             {
//                 //fprintf(stderr,"\n	omni config [I] : VAD       = 1");
//                 user_config->threshold = 1;
//             }
//         }

//         if( (p=strcasestr(lines,"high_level:")) )
//         {
//             high_level_flag = 1;
//             continue;
//         }
//         if( high_level_flag && sonde_vtc==0 )
//         {
//             if(strcasestr(lines,"vtc"))
//             {
//                 if(strcasestr(lines,"true"))
//                 {
//                     features_config->vtc = 1;
//                     //fprintf(stderr,"\n	omni config [I] : VTC       = 1");
//                 }
//             }
//             else
//             {
//                 features_config->vtc = 0;
//                 //fprintf(stderr,"\n	omni config [I] : VTC       = 0");
//             }
//             sonde_vtc = 1;
//             continue;
//         }
//         if(strstr(lines,"user_config:"))
//         {
//             user_conf_flag = 1;
//             continue;
//         }
//         if ( user_conf_flag )
//         {
//             if(!(strstr(lines,"-")))
//             {
//                 user_conf_flag = 0;
//                 continue;
//             }
//             if(strcasestr(lines,"pre_emphasis_factor"))
//             {
//                 token = strtok(tempstr[0],"pre_emphasis_factor: ");
//                 token = strtok(NULL,"pre_emphasis_factor: ");
//                 user_config->pre_emphasis_factor = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"sonde_framesize"))
//             {
//                 token = strtok(tempstr[0],"sonde_framesize: ");
//                 token = strtok(NULL,"sonde_framesize: ");
//                 user_config->sonde_framesize = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"sonde_framestep"))
//             {
//                 token = strtok(tempstr[0],"sonde_framestep: ");
//                 token = strtok(NULL,"sonde_framestep: ");
//                 user_config->sonde_framestep = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"vad_blocksize"))
//             {
//                 token = strtok(tempstr[0],"vad_blocksize: ");
//                 token = strtok(NULL,"vad_blocksize: ");
//                 user_config->BlockSize = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"vad_decimate"))
//             {
//                 token = strtok(tempstr[0],"vad_decimate: ");
//                 token = strtok(NULL,"vad_decimate: ");
//                 user_config->DecimationOrder = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"vad_filterlength"))
//             {
//                 token = strtok(tempstr[0],"vad_filterlength: ");
//                 token = strtok(NULL,"vad_filterlength: ");
//                 user_config->FilterLength = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"vad_overlap"))
//             {
//                 token = strtok(tempstr[0],"vad_overlap: ");
//                 token = strtok(NULL,"vad_overlap: ");
//                 user_config->OverlapRatio = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"orgfamily_filterbanks"))
//             {
//                 token = strtok(tempstr[0],"orgfamily_filterbanks: ");
//                 token = strtok(NULL,"orgfamily_filterbanks: ");
//                 user_config->mfcc_filterbanks = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"orgfamily_cepstral_coeff"))
//             {
//                 token = strtok(tempstr[0],"orgfamily_cepstral_coeff: ");
//                 token = strtok(NULL,"orgfamily_cepstral_coeff: ");
//                 user_config->mfcc_filterorder = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"orgfamily_lower_frequency"))
//             {
//                 token = strtok(tempstr[0],"orgfamily_lower_frequency: ");
//                 token = strtok(NULL,"orgfamily_lower_frequency: ");
//                 user_config->mfcc_lower_frequency = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"orgfamily_higher_frequency"))
//             {
//                 token = strtok(tempstr[0],"orgfamily_higher_frequency: ");
//                 token = strtok(NULL,"orgfamily_higher_frequency: ");
//                 user_config->mfcc_higher_frequency = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"orgfamily_scale:"))
//             {
//                 if(strcasestr(lines,"mel"))
//                     user_config->mel_enabled = 1;

//             }

//             if(strcasestr(lines,"scf_filterbanks"))
//             {
//                 token = strtok(tempstr[0],"scf_filterbanks: ");
//                 token = strtok(NULL,"scf_filterbanks: ");
//                 user_config->scf_filterbanks = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"scf_cepstral_coeff"))
//             {
//                 token = strtok(tempstr[0],"scf_cepstral_coeff: ");
//                 token = strtok(NULL,"scf_cepstral_coeff: ");
//                 user_config->scf_cepstral_coeff = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"scf_lower_frequency"))
//             {
//                 token = strtok(tempstr[0],"scf_lower_frequency: ");
//                 token = strtok(NULL,"scf_lower_frequency: ");
//                 user_config->scf_lower_frequency = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"scf_higher_frequency"))
//             {
//                 token = strtok(tempstr[0],"scf_higher_frequency: ");
//                 token = strtok(NULL,"scf_higher_frequency: ");
//                 user_config->scf_higher_frequency = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"scf_scale:"))
//             {
//                 if(strcasestr(lines,"mel"))
//                     user_config->scf_mel_enabled = 1;
//                 else
//                 {
//                     user_config->scf_mel_enabled = 0;
//                 }
//             }
//             if(strcasestr(lines,"mfcc_filterbanks"))
//             {
//                 token = strtok(tempstr[0],"mfcc_filterbanks: ");
//                 token = strtok(NULL,"mfcc_filterbanks: ");
//                 user_config->optmfcc_filterbanks = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"mfcc_cepstral_coeff"))
//             {
//                 token = strtok(tempstr[0],"mfcc_cepstral_coeff: ");
//                 token = strtok(NULL,"mfcc_cepstral_coeff: ");
//                 user_config->optmfcc_filterorder = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"mfcc_lower_frequency"))
//             {
//                 token = strtok(tempstr[0],"mfcc_lower_frequency: ");
//                 token = strtok(NULL,"mfcc_lower_frequency: ");
//                 user_config->optmfcc_lower_frequency = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"mfcc_higher_frequency"))
//             {
//                 token = strtok(tempstr[0],"mfcc_higher_frequency: ");
//                 token = strtok(NULL,"mfcc_higher_frequency: ");
//                 user_config->optmfcc_higher_frequency = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"mfcc_ceplifter"))
//             {
//                 token = strtok(tempstr[0],"mfcc_ceplifter: ");
//                 token = strtok(NULL,"mfcc_ceplifter: ");
//                 user_config->optmfcc_ceplifter = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"mfcc_scale:"))
//             {
//                 if(strcasestr(lines,"mel"))
//                     user_config->optmfcc_mel_enabled = 1;
//                 else
//                 {
//                     user_config->optmfcc_mel_enabled = 0;
//                 }
//             }
//             if(strcasestr(lines,"region_based_fe"))
//             {
//                 token = strtok(tempstr[0],"region_based_fe: ");
//                 token = strtok(NULL,"region_based_fe: ");
//                 user_config->region_fe = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"number_of_lsp"))
//             {
//                 token = strtok(tempstr[0],"number_of_lsp: ");
//                 token = strtok(NULL,"number_of_lsp: ");
//                 user_config->lsp_filter_order = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"sma_filt_length"))
//             {
//                 token = strtok(tempstr[0],"sma_filt_length: ");
//                 token = strtok(NULL,"sma_filt_length: ");
//                 user_config->SmoothFilterLength = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"slopeL"))
//             {
//                 char * rest;
//                 token = strtok(tempstr[0],"slopeL: -");
//                 low_level_fe->specslope_params.slopesL[0] = strtol(token,&rest,10);
//                 while(token!=NULL){
//                 token = strtok(NULL,"slopeL: -");
//                 if(token)
//                 low_level_fe->specslope_params.slopesL[1] = strtol(token,&rest,10);}
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"slopeH"))
//             {
//                 char * rest;
//                 token = strtok(tempstr[0],"slopeH: -");
//                 low_level_fe->specslope_params.slopesH[0] = strtol(token,&rest,10);
//                 while(token!=NULL){
//                 token = strtok(NULL,"slopeH: -");
//                 if(token)
//                 low_level_fe->specslope_params.slopesH[1] = strtol(token,&rest,10);}
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//         }
//         if(strstr(lines,"vtc") && features_config->vtc == 1 )
//         {
//             vtc_config_flag =1;
//             continue;
//         }
//         if (vtc_config_flag)
//         {

//             if((p=strstr(lines,"config:")) && p[-1] == 32 )
//             {
//                 fprintf(stderr,"\n	Parsing VTC parameters ... ");
//                 continue;
//             }
//             if(strcasestr(lines,"num_corrpoints"))
//             {
//                 token = strtok(tempstr[0],"num_corrpoints: ");
//                 token = strtok(NULL,"num_corrpoints: ");
//                 vtc_params->num_corrPoints  = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"winsize"))
//             {
//                 token = strtok(tempstr[0],"winsize: ");
//                 token = strtok(NULL,"winsize: ");
//                 vtc_params->winsize = atof(token);
//                 //fprintf(stderr,"\n%lf\n",atof(token));
//             }
//             if(strcasestr(lines,"num_scale0"))
//             {
//                 token = strtok(tempstr[0],"num_scale0: ");
//                 token = strtok(NULL,"- num_scale0: ");
//                 vtc_params->num_scale[0]  = atof(token);
//                 //fprintf(stderr,"\n%s",a);
//             }
//             if(strcasestr(lines,"num_scale1"))
//             {
//                 token = strtok(tempstr[0],"num_scale1: ");
//                 token = strtok(NULL,"- num_scale1: ");
//                 vtc_params->num_scale[1]  = atof(token);
//                 //fprintf(stderr,"\n%s",a);
//             }
//             if(strcasestr(lines,"num_scale2"))
//             {
//                 token = strtok(tempstr[0],"num_scale2: ");
//                 token = strtok(NULL,"- num_scale2: ");
//                 vtc_params->num_scale[2]  = atof(token);
//                 //fprintf(stderr,"\n%s",a);
//             }
//             if(strcasestr(lines,"num_scale3"))
//             {
//                 token = strtok(tempstr[0],"num_scale3: ");
//                 token = strtok(NULL,"- num_scale3: ");
//                 vtc_params->num_scale[3]  = atof(token);
//                 //fprintf(stderr,"\n%s",a);
//                 //break;
//             }
//         }

//         if((p=strstr(lines,"audio_filters:")) && features_config->smoothfilter == 0 && p[-1]!=32) //isalpha(p[-1]) - checks for preceding character in given string
//         {                                                                                // e.g for deltamfcc, isalpha(p[-1]) will point to "a" and condition will not satisfy
//             char* search ="-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer,lines);                           // copy lines array in buffer array to operate as strtok modifies original string
//                 if(strtok(buffer, search))                      // find ":" & "-" in buffer to search "-sma"
//                 {
//                     if((strstr(lines,"sma")))
//                     {
//                        features_config->smoothfilter = 1;
//                        break;
//                     }
//                     if (!strcmp(strtok(buffer, search),lines))   // iterate till empty line
//                         break;
//                 }
//             }
//             continue;                                            // parsing for this section is completed, jump to outer loop
//         }

//         if((p=strstr(lines,"mfcc:")) && features_config->smoothfilter == 1 && !isalpha(p[-1])) //isalpha(p[-1]) - checks for preceding character in given string
//         {                                                                                // e.g for deltamfcc, isalpha(p[-1]) will point to "a" and condition will not satisfy
//             char* search ="-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer,lines);                           // copy lines array in buffer array to operate as strtok modifies original string
//                 if(strstr(buffer,"audio_filters"))
//                     sma_config->mfcc_sma = -1;
//                 if(strtok(buffer, search))                      // find ":" & "-" in buffer to search "-sma"
//                 {
//                     if(strstr(lines,"sma") && features_config->optmfcc == 1 && sma_config->mfcc_sma == -1)
//                     {
//                        sma_config->mfcc_sma = 1;
//                        sma_config->total_sma_count = fill_sma_array(sma_config->sma_array,e_mfcc_sma);
//                        break;
//                     }
//                     if (!strcmp(strtok(buffer, search),lines))   // iterate till empty line
//                         break;
//                 }
//             }
//             continue;                                            // parsing for this section is completed, jump to outer loop
//         }

//         if( strstr(lines,"lsp:") && features_config->smoothfilter == 1 && !(strstr(lines,"-")) )
//         {
//             char* search ="-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer,lines);
//                 if(strstr(buffer,"audio_filters"))
//                     sma_config->lsp_sma = -1;
//                 if(strtok(buffer, search))
//                 {
//                     if(strstr(lines,"sma") && features_config->sonde_lsp == 1 && sma_config->lsp_sma == -1)
//                     {
//                        sma_config->lsp_sma = 1;
//                        sma_config->total_sma_count = fill_sma_array(sma_config->sma_array,e_lsp_sma);
//                        break;
//                     }
//                     if (!strcmp(strtok(buffer, search),lines))
//                         break;
//                 }
//             }
//             continue;
//         }
//         if((p=strstr(lines,"fmt:")) && features_config->smoothfilter == 1 && !isalpha(p[-1])) //isalpha(p[-1]) - checks for preceding character in given string
//         {                                                                                // e.g for deltamfcc, isalpha(p[-1]) will point to "a" and condition will not satisfy
//             char* search ="-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer,lines);
//                 if(strstr(buffer,"audio_filters"))
//                     sma_config->fmt_sma = -1;                            // copy lines array in buffer array to operate as strtok modifies original string
//                 if(strtok(buffer, search))                      // find ":" & "-" in buffer to search "-sma"
//                 {
//                     if( strstr(lines,"sma") && features_config->sonde_formant == 1 && sma_config->fmt_sma == -1 )
//                     {
//                        sma_config->fmt_sma = 1;
//                        sma_config->total_sma_count = fill_sma_array(sma_config->sma_array,e_fmt_sma);
//                        break;
//                     }
//                     if (!strcmp(strtok(buffer, search),lines))   // iterate till empty line
//                         break;
//                 }
//             }
//             continue;                                            // parsing for this section is completed, jump to outer loop
//         }

//         if( strcasestr(lines,"deltadeltamfcc:") && features_config->smoothfilter == 1 )
//         {
//             char* search ="-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer,lines);
//                 strcpy(buffer,lines);
//                 if(strstr(buffer,"audio_filters"))
//                     sma_config->ddmfcc_sma = -1;
//                 if(strtok(buffer, search))
//                 {
//                     if((strstr(lines,"sma") && features_config->optdd_mfcc == 1 && sma_config->ddmfcc_sma == -1 ))
//                     {
//                        sma_config->ddmfcc_sma = 1;
//                        sma_config->total_sma_count = fill_sma_array(sma_config->sma_array,e_ddmfcc_sma);
//                        break;
//                     }
//                     if (!strcmp(strtok(buffer, search),lines))
//                     break;
//                 }
//             }
//             continue;
//         }
//         if( (p=strcasestr(lines,"deltamfcc:")) && features_config->smoothfilter == 1 && !isalpha(p[-1]) )
//         {
//             char* search ="-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer,lines);
//                 if(strstr(buffer,"audio_filters"))
//                     sma_config->dmfcc_sma = -1;
//                 if(strtok(buffer, search))
//                 {
//                     if((strstr(lines,"sma") && features_config->optdelta_mfcc == 1 && sma_config->dmfcc_sma == -1))
//                     {
//                        sma_config->dmfcc_sma = 1;
//                        sma_config->total_sma_count = fill_sma_array(sma_config->sma_array,e_dmfcc_sma);
//                        break;
//                     }
//                     if (!strcmp(strtok(buffer, search),lines))
//                     break;
//                 }
//             }
//             continue;
//         }
//         if( (p=strcasestr(lines,"f0:")) && features_config->smoothfilter == 1 && !isalpha(p[-1]) )
//         {
//             char* search ="-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer,lines);
//                 if(strstr(buffer,"audio_filters"))
//                     sma_config->f0_sma = -1;
//                 if(strtok(buffer, search))
//                 {
//                     if((strstr(lines,"sma") && features_config->f0 == 1 && sma_config->f0_sma == -1 ))
//                     {
//                        sma_config->f0_sma = 1;
//                        sma_config->total_sma_count = fill_sma_array(sma_config->sma_array,e_f0_sma);
//                        break;
//                     }
//                     if (!strcmp(strtok(buffer, search),lines))
//                     break;
//                 }
//             }
//             continue;
//         }
//         if((p=strstr(lines,"amplitude:")) && features_config->smoothfilter == 1 && !isalpha(p[-1])) //isalpha(p[-1]) - checks for preceding character in given string
//         {                                                                                // e.g for deltamfcc, isalpha(p[-1]) will point to "a" and condition will not satisfy
//             char* search ="-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer,lines);                           // copy lines array in buffer array to operate as strtok modifies original string
//                 if(strstr(buffer,"audio_filters"))
//                     sma_config->ampl_sma = -1;
//                 if(strtok(buffer, search))                      // find ":" & "-" in buffer to search "-sma"
//                 {
//                     if(strstr(lines,"sma") && features_config->ampl == 1 &&  sma_config->ampl_sma == -1 )
//                     {
//                        sma_config->ampl_sma = 1;
//                        sma_config->total_sma_count = fill_sma_array(sma_config->sma_array,e_ampl_sma);
//                        break;
//                     }
//                     if (!strcmp(strtok(buffer, search),lines))   // iterate till empty line
//                         break;
//                 }
//             }
//             continue;                                            // parsing for this section is completed, jump to outer loop
//         }
//         if( (p=strcasestr(lines,"jitter:")) && features_config->smoothfilter == 1 && !isalpha(p[-1]) )
//         {
//             char* search ="-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer,lines);
//                 if(strstr(buffer,"audio_filters"))
//                     sma_config->jitter_sma = -1;
//                 if(strtok(buffer, search))
//                 {
//                     if( strstr(lines,"sma") && features_config->jitter == 1 && sma_config->jitter_sma == -1 )
//                     {
//                        sma_config->jitter_sma = 1;
//                        sma_config->total_sma_count = fill_sma_array(sma_config->sma_array,e_jitter_sma);
//                        break;
//                     }
//                     if (!strcmp(strtok(buffer, search),lines))
//                     break;
//                 }
//             }
//             continue;
//         }
//         if((p=strstr(lines,"spectralslope:")) && features_config->smoothfilter == 1 && !isalpha(p[-1])) //isalpha(p[-1]) - checks for preceding character in given string
//         {                                                                                // e.g for deltamfcc, isalpha(p[-1]) will point to "a" and condition will not satisfy
//             char* search ="-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer,lines);                           // copy lines array in buffer array to operate as strtok modifies original string
//                 if(strstr(buffer,"audio_filters"))
//                     sma_config->spect_slope_sma = -1;
//                 if(strtok(buffer, search))                      // find ":" & "-" in buffer to search "-sma"
//                 {
//                     if(strstr(lines,"sma") && features_config->spectral_slope == 1 && sma_config->spect_slope_sma == -1)
//                     {
//                        sma_config->spect_slope_sma = 1;
//                        sma_config->total_sma_count = fill_sma_array(sma_config->sma_array,e_spectralslope_sma);
//                        break;
//                     }
//                     if (!strcmp(strtok(buffer, search),lines))   // iterate till empty line
//                         break;
//                 }
//             }
//             continue;                                            // parsing for this section is completed, jump to outer loop
//         }
//         if((p=strstr(lines,"jitterddp:")) && features_config->smoothfilter == 1 && !isalpha(p[-1])) //isalpha(p[-1]) - checks for preceding character in given string
//         {                                                                                // e.g for deltamfcc, isalpha(p[-1]) will point to "a" and condition will not satisfy
//             char* search ="-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer,lines);                           // copy lines array in buffer array to operate as strtok modifies original string
//                 if(strstr(buffer,"audio_filters"))
//                     sma_config->jitter1_sma = -1;
//                 if(strtok(buffer, search))                      // find ":" & "-" in buffer to search "-sma"
//                 {
//                     if(strstr(lines,"sma") && features_config->jitter1 == 1 && sma_config->jitter1_sma == -1)
//                     {
//                        sma_config->jitter1_sma = 1;
//                        sma_config->total_sma_count = fill_sma_array(sma_config->sma_array,e_jitterddp_sma);
//                        break;
//                     }
//                     if (!strcmp(strtok(buffer, search),lines))   // iterate till empty line
//                         break;
//                 }
//             }
//             continue;                                            // parsing for this section is completed, jump to outer loop
//         }
//         if( (p=strcasestr(lines,"scf:")) && features_config->smoothfilter == 1 && !isalpha(p[-1]) )
//         {
//             char* search ="-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer,lines);
//                 if(strstr(buffer,"audio_filters"))
//                     sma_config->optscf_sma = -1;
//                 if(strtok(buffer, search))
//                 {
//                     if(strstr(lines,"sma") && features_config->optscf == 1 && sma_config->optscf_sma == -1)
//                     {
//                        sma_config->optscf_sma = 1;
//                        sma_config->total_sma_count = fill_sma_array(sma_config->sma_array,e_scf_sma);
//                        break;
//                     }
//                     if (!strcmp(strtok(buffer, search),lines))  //if empty line found break inner for loop
//                     break;
//                 }
//             }
//             continue;
//         }
//         if( (p=strcasestr(lines,"shimmer:")) && features_config->smoothfilter == 1 && !isalpha(p[-1]) )
//         {
//             char* search ="-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer,lines);
//                 if(strstr(buffer,"audio_filters"))
//                     sma_config->shimmer_sma = -1;
//                 if(strtok(buffer, search))
//                 {
//                     if( strstr(lines,"sma") && features_config->shimmer == 1 && sma_config->shimmer_sma == -1 )
//                     {
//                        sma_config->shimmer_sma = 1;
//                        sma_config->total_sma_count = fill_sma_array(sma_config->sma_array,e_shimmer_sma);
//                        break;
//                     }
//                     if (!strcmp(strtok(buffer, search),lines))  //if empty line found break inner for loop
//                     break;
//                 }
//             }
//             continue;
//         }
//         if ((p = strcasestr(lines, "orgscf:")) && features_config->smoothfilter == 1 && !isalpha(p[-1]))
//         {
//             char *search = "-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer, lines);
//                 if (strstr(buffer, "audio_filters"))
//                     sma_config->scf_sma = -1;
//                 if (strtok(buffer, search))
//                 {
//                     if (strstr(lines, "sma") && features_config->spec_centroid == 1 && sma_config->scf_sma == -1)
//                     {
//                         sma_config->scf_sma = 1;
//                         sma_config->total_sma_count = fill_sma_array(sma_config->sma_array, e_orgscf_sma);
//                         break;
//                     }
//                     if (!strcmp(strtok(buffer, search), lines)) //if empty line found break inner for loop
//                         break;
//                 }
//             }
//             continue;
//         }
//         if ((p = strcasestr(lines, "orgmfcc:")) && features_config->smoothfilter == 1 && !isalpha(p[-1]))
//         {
//             char *search = "-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer, lines);
//                 if (strstr(buffer, "audio_filters"))
//                     sma_config->mfcc_sma = -1;
//                 if (strtok(buffer, search))
//                 {
//                     if (strstr(lines, "sma") && features_config->sonde_mfcc == 1 && sma_config->mfcc_sma == -1)
//                     {
//                         sma_config->mfcc_sma = 1;
//                         sma_config->total_sma_count = fill_sma_array(sma_config->sma_array, e_orgmfcc_sma);
//                         break;
//                     }
//                     if (!strcmp(strtok(buffer, search), lines)) //if empty line found break inner for loop
//                         break;
//                 }
//             }
//             continue;
//         }
//         if ((p = strcasestr(lines, "orgdeltamfcc:")) && features_config->smoothfilter == 1 && !isalpha(p[-1]))
//         {
//             char *search = "-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer, lines);
//                 if (strstr(buffer, "audio_filters"))
//                     sma_config->dmfcc_sma = -1;
//                 if (strtok(buffer, search))
//                 {
//                     if (strstr(lines, "sma") && features_config->deltamfcc == 1 && sma_config->dmfcc_sma == -1)
//                     {
//                         sma_config->dmfcc_sma = 1;
//                         sma_config->ddmfcc_sma = 1;
//                         sma_config->total_sma_count = fill_sma_array(sma_config->sma_array, e_orgdeltamfcc_sma);
//                         sma_config->total_sma_count = fill_sma_array(sma_config->sma_array, e_orgddmfcc_sma);
//                         break;
//                     }
//                     if (!strcmp(strtok(buffer, search), lines)) //if empty line found break inner for loop
//                         break;
//                 }
//             }
//             continue;
//         }
//         if ((p = strcasestr(lines, "orgdeltadeltamfcc:")) && features_config->smoothfilter == 1 && !isalpha(p[-1]))
//         {
//             char *search = "-:";
//             for (; (fgets(lines, 50, input_yaml)) != NULL; i++)
//             {
//                 char buffer[100] = {0};
//                 strcpy(buffer, lines);
//                 if (strstr(buffer, "audio_filters"))
//                     sma_config->ddmfcc_sma = -1;
//                 if (strtok(buffer, search))
//                 {
//                     if (strstr(lines, "sma") && features_config->deltadeltamfcc == 1 && sma_config->ddmfcc_sma == -1)
//                     {
//                         sma_config->ddmfcc_sma = 1;
//                         sma_config->total_sma_count = fill_sma_array(sma_config->sma_array, e_ddmfcc_sma);
//                         break;
//                     }
//                     if (!strcmp(strtok(buffer, search), lines)) //if empty line found break inner for loop
//                         break;
//                 }
//             }
//             continue;
//         }
//     memset(lines,32,100); //initialize lines array with space character
//     }
// }

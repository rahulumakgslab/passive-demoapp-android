/*
* @brief sonde sp-fe function prototypes and structures are defined here.
*
* @author Swapnil Warkar
*
*
*/

#ifndef _SONDE_SPFE_FUNCTION_DEF_H_
#define _SONDE_SPFE_FUNCTION_DEF_H_
#include "../global_h/sonde_datatypes.h"
#include "../h/sonde_spfe_define.h"
#include "../h/sonde_spfe_vtc_define.h"
#include "../h/sonde_sp_elck_define.h"
#include <time.h>

#include <android/log.h>
#define  LOG_TAG    "!! sp_detection ->"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// #define TIME_S begin = clock();
// #define TIME_D clock_t begin, end; 
// #define TIME_E(fun) {\
//     end = clock();\
//     double time_spent = (double)(end - begin) / CLOCKS_PER_SEC;\
//     fprintf(stderr,"\n\t%s runtime of -> %f\n", fun, time_spent);\
// }
s_elck_structs * elck_init( t_int8 * input_filename, t_int8 * config_file, t_int8 * output_dir);
int elck_vad_calc( s_elck_vad_structs * elck_vad_structs, s_elck_meta * elck_meta );
int elck_ahh_calc( s_elck_vad_structs * elck_ahh_structs, s_elck_meta * elck_meta );
int elck_em_calc( s_elck_vad_structs * elck_ahh_structs, s_elck_meta * elck_meta );
void set_paths( s_paths_and_meta * paths_and_meta, t_int8 * audio,
t_int8 * config, t_int8 * output_files_dir );
void validate_version(char *version);
void write_json( t_int8 * input_json, t_int8 * output_dir );
t_int32 read_threshold_config_file(s_config * config, s_paths_and_meta * paths_and_meta, s_elck_user_thresholds * );
int print_elck_thresholds( s_elck_user_thresholds * elck_user_thresholds );
t_int32 sonde_sp_elck(t_int8 * input_filename, t_int8 * config_file, t_int8 * output_dir );
void dbg_fsum(t_float *src_, t_int32 ndst_);
t_float * ahh_detection( s_paths_and_meta * , s_shared_params * shared_params, s_fileinfo * file_parameters);
void fprint_log_string_int( t_int8 * string, t_int32 arg );
void fprint_log_string_float( t_int8 * string, t_float arg );
void fprint_log_string( t_int8 * string );
void fprint_log_string_str( t_int8 * string, t_int8 * arg );
void fprint_log_string_int_fd( t_int8 * string, t_int32 arg );
void fprint_log_string_float_fd( t_int8 * string, t_float arg );
void fprint_log_string_fd( t_int8 * string );
void fprint_log_string_str_fd( t_int8 * string, t_int8 * arg );
t_int8 * derive_path_resources( t_int8 * config_file );
t_int32 get_ahh_response( t_float * ahh_output_arr, t_float * thresholds );
void elck_write_output( s_paths_and_meta  * paths_and_meta, s_elck_meta *, t_int8 * );
t_float * energy_meter( s_paths_and_meta * paths_and_meta, s_shared_params * shared_params, s_fileinfo * file_parameters, t_float * );
t_int32 get_vad_response( t_int32 total_frames, t_int32 voice_frames_count, t_float threshold_percent );
t_int8 * form_json( s_elck_out_response  elck_output, t_int8 * );
static int validate_thresholds( t_float * th_array );
void memcpy_padding_vad_gen(  t_float * src, t_float * dst, t_int32 count );
t_int8 *default_json(s_elck_out_response elck_out_response, t_int8 * version);
void elck_destruct( s_elck_structs * );
t_int8 * elck_dump( s_elck_vad_structs *elck_calc_structs, s_elck_meta *elck_meta, t_int8 *output_dir );
t_int32 elck_raise_error( t_int32 error_code );

// file ./src/delta_mfcc/sonde_sfe_delta_mfcc.c:
t_int32 apply_optdeltadeltamfcc(s_features *, s_shared_params *, t_int32, s_user_config *);
t_int32 calculate_optdd_mfcc(t_int32, t_int32, t_int32, t_double, s_deltamfcc *);
t_int32 calculate_optdeltamfcc(t_int32, t_int32, t_int32, t_double, s_deltamfcc *);
t_int32 optmfcc_padding(t_double **, t_int32, t_int32, s_deltamfcc *);

// file ./src/f0_jitter_shimmer/sonde_sfe_f0_jitter_shimmer.c:
t_int32 adjust_f0(t_int32, t_double *);
t_int32 autocorr(t_double *, s_jitter_shimmer *, t_int32);
t_double calcC(t_double *, t_int32, t_int32);
t_int32 calc_f0(t_double *, t_int32, t_int32, t_double *, t_int32 *, s_jitter_shimmer *, t_int32);
t_int32 calc_jitt_shimm(t_double *, t_double *, t_double *, t_double *, s_jitter_shimmer *, s_features_config *);
t_int32 calc_jitt_shimm2(t_double *, t_double *, t_double *, t_double *, s_jitter_shimmer *, s_fileinfo *, t_double *, s_shared_params *);
t_int32 check_f0(t_int32 *, t_double *, t_int32, t_double *, t_int32, t_double *, s_jitter_shimmer *, t_int32);
t_int32 clipping(t_double *, t_int32, t_double, s_jitter_shimmer *, t_int32);
t_int32 filter_input(t_int32, t_double *, t_int32, t_double *, float *, t_int32);
t_double peak(t_int16 *, t_int32, t_int32);
t_int32 pitch(s_jitter_shimmer *, s_fileinfo *, s_paths_and_meta *, s_shared_params *, s_features_config *);
t_double power(t_double *, t_int32);
t_int32 save_lastpartofreport(s_jitter_shimmer *, t_int32);
t_int32 save_report_csv(t_double *, t_double *, t_int32, t_double *, t_double *, s_jitter_shimmer *, t_int32, t_double *);
t_int32 signo(t_double);

// file ./src/signal/sonde_sfe_fft.c
int proc_fft_fbf( s_features * low_level_fe, s_paths_and_meta * paths_and_meta, s_shared_params * shared_params, s_fileinfo * file_parameters );
t_int8 * get_output_path_gen( char * output_path );

// file ./src/fmt/sonde_sfe_eigen_roots.c:
t_int32 balance_matrix(t_double **, t_int32);
t_int32 calc_roots(const t_float *, t_double *, t_long);
t_double complex_abs(t_double, t_double);
void complex_division(t_double, t_double, t_double, t_double, t_double *, t_double *);
void complex_into_unit_circle(t_double *, t_int32);
t_int32 convert_hessenberg_matrix(t_double **, t_int32);
s_fmt_polynomial *fmt_polynomial_allocate(t_long);
t_int32 fmt_polynomial_complex_solve(const t_double *, t_long, s_fmt_polynomial *, t_double *);
void fmt_polynomial_free(s_fmt_polynomial *);
t_int32 merge_roots(t_double *, t_double *, t_double *, t_int32);
t_int32 qr_hessenberg_matrix(t_double **, t_int32, t_double *, t_double *);
void set_cmatrix(const t_double *, t_long, t_double **);

// file ./src/fmt/sonde_sfe_fmt.c:
t_double **calc_fmt(s_fileinfo *, s_shared_params *, s_user_config *);
static t_int32 alloc_formants(t_int32, t_int32, t_int32, t_int32, t_int32, s_fmt *);
static t_int32 cal_formants_from_roots(t_double *, t_int32, t_double *, t_int32, t_double, t_double, t_double);
static void free_formants(s_fmt *);
static t_int32 roots_to_formants(t_double *, t_double *, t_int32, t_int32, t_int32, t_double, t_double);

// file ./src/fmt/sonde_sfe_lpc.c:
t_int32 process_lpc(const t_float *, t_float *, t_long, t_long);
static void calc_lpc(const t_float *, t_long, t_float *, t_long, t_float *);
static void do_autocorr(const t_float *, const t_int32, t_float *, t_int32);
static t_int32 getlpc_withacf(t_float *, t_float *, t_int32, t_float *);

// file ./src/lmk/sonde_sfe_landmarks.c:
t_int32 Hilbert(t_double *, s_landmarks *, t_int32);
t_int32 adjust_f0_L(t_int32, t_double *);
void autocorr_L(t_double *, s_landmarks *, t_int32);
t_double calcC_L(t_double *, t_int32, t_int32);
void calc_consonant_lndmarks(s_shared_params *, s_landmarks *, s_fileinfo *, FILE *, FILE *);
void calc_dim_fractal(s_landmarks *, s_fileinfo *, s_shared_params *);
void calc_f0_L(t_double *, t_int32, t_int32, t_double *, t_int32 *, s_landmarks *, t_int32, t_double *);
t_int32 check_f0_L(t_int32 *, t_double *, t_int32, t_double *, t_int32, t_int16 *, t_double *, s_landmarks *, t_int32, t_double *, t_double *);
void clipping_L(t_double *, t_int32, t_double, s_landmarks *, t_int32);
t_int32 fftw(t_double *, t_double *, t_int32, t_int32, t_int32);
void filter_input_L(t_int32, t_double *, t_int32, t_int16 *);
t_double peak_L(t_int16 *, t_int32, t_int32);
t_double pendiente(t_double *, t_double *, t_int32);
t_double power_L(t_double *, t_int32);
void save_landmarks_csv(s_landmarks *, t_int32, t_int8 *, t_int8 *);
void save_voiced_landmarks(t_int32, s_landmarks *, t_double *, t_double *);
t_int32 signo_L(t_double);
void smooth(t_double *, t_double *, t_int32, t_int32);
int compute_landmarks(s_paths_and_meta * paths_and_meta, s_features_config * features_config, s_shared_params * shared_params, s_landmarks *lndmks, s_fileinfo *file_parameters );

// file ./src/lsp/sonde_sfe_lsp_proc.c:
t_double G(t_double, t_double *, s_features *, t_int32);
t_int32 LPC(s_features *, t_double, s_shared_params *);
t_int32 LSF8(s_features *, t_int32, t_int32);
t_int32 bloques_corr(s_fileinfo *, s_features *, s_shared_params *);
t_int32 compute_lsp_coeff(s_fileinfo *, s_features *, s_user_config *, s_shared_params *);
t_int32 correlacion(s_features *, t_int32, s_shared_params *);
t_int32 myLPC(s_features *, t_int32);
t_int32 sign(t_double, t_int32 *);

// file ./src/main/sonde_sfe_main.c:
t_int32 main(t_int32, t_int8 **);

// file ./src/mfcc/sonde_sfe_mfcc.c:
t_int32 compute_optmfcc(s_fileinfo *, s_features *, s_shared_params *, t_int32, s_user_config *);
t_int32 dct_liftering(t_float *, t_double *, t_int32, t_int32);
static t_int32 alloc_mfcc();
static t_int32 apply_filtercoeff(const t_float *, t_float *, t_long, t_long);
static t_float coeffnum_to_freq(t_long, t_float);
static t_int32 compute_filtercoeff(s_features *, t_double, t_int32, t_int32, s_user_config *);
static t_int32 compute_logspectrum(const t_float *, t_float *, t_long, t_long, t_int32);
static t_long freq_to_coeffnum(t_float, t_float);
static t_int32 init_trignometric_tables(t_long, t_int32, t_int32, t_int32);
static void *mfcc_alloc();

// file ./src/misc/sonde_sfe_debug.c:
void file_dump_d2d(t_double **, t_int32, t_int32, const t_int8 *);
void file_dump_double(t_double *, t_int32, const t_int8 *, t_int8);
void file_dump_float(t_float *, t_int32, const t_int8 *, t_int8);
int get_defaults_ll( s_paths_and_meta * paths_and_meta );
int get_defaults_hl( s_paths_and_meta * paths_and_meta );
void print_d2d(t_double **, t_int32, t_int32);
void print_double(t_double *, t_int32);
void print_float(t_float *, t_int32);
t_int32 print_log_string( t_int8 * string, t_int8 * val );
void report_error( t_int8 * message, t_int32 code );
void copy_data( t_int32 row, t_int32 col, t_double src[][col], t_double ** dst );

// file ./src/misc/sonde_sfe_region_fe.c:
t_int32 region_based_fe(s_shared_params *, s_vad *, s_config *, s_paths_and_meta *);

t_float * resample( s_shared_params * shared_params, s_fileinfo * file_parameters, s_user_config * );
void copy_float_to_double( t_int32 count, t_float * src, t_double * dst );

// file ./src/org_family/delta_mfcc/sonde_sfe_orgdelta_mfcc.c:
t_int32 apply_deltadeltamfcc(s_features_config *, s_features *, s_shared_params *, s_user_config *);
t_int32 calculate_delta_delta_mfcc(t_int32, t_int32, t_int32, t_double, s_orgddmfcc *);
t_int32 calculate_deltamfcc(t_int32, t_int32, t_int32, t_double, s_orgddmfcc *);
t_int32 mfcc_padding(t_double **, t_int32, t_int32, s_orgddmfcc *);

// file ./src/org_family/fmt/sonde_sfe_orgfmt.c:
void DoCubicFit(s_formant *);
t_double Dp(t_int32, t_int32, s_formant *);
void FourierTransform_fmt(t_int32, t_uint8, s_formant *);
t_int8 IsPowerOfTwo(t_long);
t_uint8 NumberOfBitsNeeded(t_int32);
t_int32 ReverseBits(t_int32, t_uint8);
void formant_processing(s_fileinfo *, s_formant *, s_shared_params *, s_user_config *);

// file ./src/org_family/mfcc/sonde_sfe_orgmfcc.c:
t_int32 bloques_fourier(s_features *, s_shared_params *);
t_int32 bloques_fourier_scf(s_features *, s_fileinfo *, s_shared_params *);
t_int32 compute_mfcc_coeff(s_config *, s_fileinfo *, s_features *, s_shared_params *, s_paths_and_meta *);
t_int32 compute_scf(s_features *, s_shared_params *, s_fileinfo *, s_user_config *);
t_int32 dct(s_features *, s_shared_params *, s_user_config *);
void get_filter_path(t_int8 *, t_int8 *, t_int8 *);
t_int32 multiply_matrix_log(s_features *, s_shared_params *, s_user_config *);
t_int32 trifbank(s_features *, t_int32, s_user_config *, s_shared_params *, t_int32);
t_int32 trifbank_scf(s_features *, s_user_config *, s_shared_params *, t_int32);
t_double ** compute_scf_filtercoeff( s_user_config * user_config, t_int32, t_int32 sampling_rate );

// file ./src/org_family/mfcc/sonde_sfe_orgmfcc_fft.c:
t_int32 FFTW(s_features *, t_int32, t_int32, t_int32, t_int32);
t_int32 PFFT(s_features *, s_shared_params *);
t_double f2m(t_double);
t_double m2f(t_double);

// file ./src/preprocessing/sonde_sfe_config_fileread.c:
t_int32 configure_enum(s_features_config *, s_sma_config *);
t_int8 *insert(t_int8 *);
t_int32 print_config(s_features_config *, s_user_config *);
t_int32 read_config_file(s_config *, s_fileinfo *, s_paths_and_meta *);
void remove_chars(t_int8 *, t_int8);
t_int8 *set_feature_set(void *);
t_int8 *set_sma_config(t_int8 *, void *);
t_int8 *set_user_config(void *);
t_int8 *set_vtc_config(void *);
t_int8 *trim_string(t_int8 *);
t_int32 validate_size(t_int32, t_int32);

// file ./src/preprocessing/sonde_sfe_mem_init.c:
t_int32 init_fe_dynamic_memory(s_fileinfo *, s_config *, s_shared_params *, s_vad *);
void **double_memory_alloc(void **, t_int32, t_int32, t_int32, t_int32);
void *single_memory_alloc(void *, t_int32, t_int32, t_int32);

// file ./src/preprocessing/sonde_sfe_preprocessing.c:
t_int32 check_input_existence(s_paths_and_meta *);
t_int32 get_vad_status(t_int32 *, t_int32);
t_int32 init_algorithm_param(s_paths_and_meta *, s_features_config *, s_user_config *, s_shared_params *, s_fileinfo *);
t_int32 interdependent_features_config(s_features_config *);
t_int32 pre_emphasis_filter(s_fileinfo *, s_shared_params *);
t_int32 set_algo_params(s_shared_params *, s_user_config *, t_int32, t_int32);
t_int32 set_audio_path(s_paths_and_meta *);
t_int32 set_feature_array(s_features_config *);
t_int32 sort_features_array(t_int32 *, t_int32);
t_int32 swap(t_int32 *, t_int32 *);
t_int32 copy_org_algo_params( s_shared_params * shared_params, s_user_config * user_config, int samplerate, int actualsamples );
t_int8 * get_output_path( s_paths_and_meta * paths_and_meta );
t_int8 * get_audio_name( const t_int8 * input_path );
t_int8 *erase_ext(t_int8 *filename) ;

// file ./src/preprocessing/sonde_sfe_waveread.c:
t_int32 handle_stereo(s_fileinfo *, s_shared_params *);
t_int32 raise_exception(s_fileinfo *);
t_int32 read_wave_file(s_fileinfo *, s_paths_and_meta *, s_shared_params *, s_user_config *);
t_int32 stereo_channel_processing(t_int16 *, t_int16 *, t_int32, s_shared_params *);

// file ./src/qos/sonde_sfe_qos.c:
t_int32 calculate_avg_amplitude(t_int32, s_features *, t_float *);
t_int32 calculate_avg_amplitudenorm(t_int32, s_features *);
t_int32 calculate_max_value_forchunk(t_float *, t_int32, s_features *);
t_int32 calculate_min_max_amplitude(t_float *, t_int32, s_features *);
t_int32 calculate_rms_amplitude(t_int32, s_features *, t_float *);
t_int32 calculate_rms_amplitudenorm(t_int32, s_features *);
t_int32 calculate_volume(s_shared_params *, s_fileinfo *, s_features *);
t_int32 normalised_samples(t_int16 *, t_int32, s_features *);

// file ./src/scf/sonde_sfe_scf.c:
t_int32 compute_optscf( t_double**, s_features *, t_float *, t_int32, t_int32 , t_int32, s_user_config *);

// file ./src/signal/sonde_sfe_optfft.c:
void bitrv2(t_int32, t_int32 *, t_float *);
void bitrv208(t_float *);
void bitrv208neg(t_float *);
void bitrv216(t_float *);
void bitrv216neg(t_float *);
void bitrv2conj(t_int32, t_int32 *, t_float *);
void calc_rdft(t_int32, t_float *);
void cftb040(t_float *);
void cftb1st(t_int32, t_float *, t_float *);
void cftf040(t_float *);
void cftf081(t_float *, t_float *);
void cftf082(t_float *, t_float *);
void cftf161(t_float *, t_float *);
void cftf162(t_float *, t_float *);
void cftf1st(t_int32, t_float *, t_float *);
void cftfsub(t_int32, t_float *, t_int32 *, t_int32, t_float *);
void cftfx41(t_int32, t_float *, t_int32, t_float *);
void cftleaf(t_int32, t_int32, t_float *, t_int32, t_float *);
void cftmdl1(t_int32, t_float *, t_float *);
void cftmdl2(t_int32, t_float *, t_float *);
void cftrec4(t_int32, t_float *, t_int32, t_float *);
t_int32 cfttree(t_int32, t_int32, t_int32, t_float *, t_int32, t_float *);
void cftx020(t_float *);
void makect(t_int32, t_int32 *, t_float *);
void makeipt(t_int32, t_int32 *);
void makewt(t_int32, t_int32 *, t_float *);
void rftfsub(t_int32, t_float *, t_int32, t_float *);
void resample_rdft(int n, int isgn, float *a);

// file ./src/signal/sonde_sfe_optsigproc.c:
t_double complex_to_double(t_double, t_double);
t_int32 complex_to_real(const t_float *, t_float *, t_long);
t_int32 compute_windowing(t_int32, s_shared_params *);
t_double filter_scale_inverse(t_double, t_int32);
t_double filter_scale_select(t_double, t_int32);
void memset_double(t_double *, t_int32);
void memset_float(t_float *, t_int32);
void memset_int(t_int32 *, t_int32);
void pad_asymmetric(t_float *, t_float *, t_int32, t_int32);
void pad_symmetric(t_float *, t_float *, t_int32, t_int32);
t_float *pre_emphasis_gen(t_float *, t_float *, t_int32);
t_float scaleconv_freqtomel(t_long, t_float, t_int32);
t_int32 pcm_data_normalisation_gen( t_int32, t_int16 * in_pcm, t_float * out_norm );
void init_inverse_dft( t_long fftsize, t_long irdft_outsamples, t_double trig_denom, s_fmt *pfmt ); 
void calc_inverse_dft( t_float *in_irdftbuf, t_float *out_irdftbuf, s_fmt *pfmt );

// file ./src/signal/sonde_sfe_sma_filter.c:
t_int32 smooth_filter_for_feature(s_shared_params *, s_user_config *, t_int32, t_double *);
t_int32 smooth_filter_for_features(s_shared_params *, s_user_config *, t_int32, t_double **);

// file ./src/spectral_slope/sonde_sfe_spectral_slope.c:
t_int32 calc_magnitude(t_double *, t_float *, t_int32);
t_int32 spectral_slope_calc(s_shared_params *, s_features *, t_double *, s_user_config *);
t_int32 spectral_slope_preproc(s_fileinfo *, s_features *, s_shared_params *, s_user_config *);

// file ./src/vad/sonde_sfe_vad_mathops.c:
t_double ERFC(t_double);

// file ./src/vad/sonde_sfe_vad_proc.c:
t_int32 compute_vad(s_paths_and_meta *, s_fileinfo *, s_user_config *, s_vad *, s_shared_params *, s_features_config *);
t_int32 custom_envelope_detector_overlap(t_int32, t_int32, t_float *, s_fileinfo *, s_vad *);
t_int32 equalize_gain(s_shared_params *);
static t_int32 Output_csv(s_vad *, s_shared_params *, s_paths_and_meta *, s_fileinfo *);
static t_int32 compute_filter_coefficient(t_int32, s_vad *, s_shared_params *);
static t_int32 ftoa(t_double, t_int8 *, t_int32);
static t_int32 handle_general_duration_timing(s_vad *, s_fileinfo *);
static t_int32 intToStr(t_int32, t_int8 *, t_int32);
static t_int32 lp_filter_durations(s_vad *);
static t_int32 no_filter(s_vad *);
static t_int32 process_envelope_burst_patt(s_shared_params *, s_user_config *, s_vad *, s_fileinfo *);
static t_int32 reverse(t_int8 *, t_int32);

// file ./src/vtc/sonde_sfe_vtc_eigenspectrum.c:
t_int32 balance_real_matrix(s_vtc *, t_int32);
t_int32 convert_nonsymm_hessenberg_matrix(s_vtc *, t_int32);
t_int32 process_qr_hessenberg_matrix(s_vtc *, t_int32, t_double *, t_double *);

// file ./src/vtc/sonde_sfe_vtc_meminit.c:
t_int32 flush_arrays(s_vtc *);
t_int32 init_fe_dynamic_memory_vtc(s_vtc *, s_shared_params *);
t_int8 * store_output_at( t_int8 * output_dir_path, t_int8 * input_audio_path, t_int8 * token );

// file ./src/vtc/sonde_sfe_vtc_preprocessing.c:
t_double *compute_mean(t_double **, t_int32, t_int32);
t_double *compute_std_deviation(t_double **, t_double *, t_int32, t_int32);
t_int32 compute_vtc(s_paths_and_meta *, s_vtc *, s_config *, s_shared_params *);
t_double **compute_znorm(t_double **, t_double *, t_double *, t_int32, t_int32);
t_double mean(t_double *, t_int32);
t_double **read_csv(t_int8 *, t_int32 *, t_int32 *);
t_int32 set_equations(s_vtc *, t_int32, t_int32);
t_double std_deviation(t_double *, t_int32, t_double);
t_double *vtc_double_malloc1d(t_int32);
t_double **vtc_double_malloc2d(t_int32, t_int32);
t_double ***vtc_double_malloc3d(t_int32, t_int32, t_int32);
t_int32 write_output( s_paths_and_meta * paths_and_meta, s_vtc* vtc_params,t_int32 num_win, t_double * eigen_spectrum_fbf[num_win] );
t_int32 znorm(t_double **, t_double *, t_int32, t_double, t_double);
t_int32 vtc_double_free2d(t_double ** , t_int32 , t_int32);
t_int32 vtc_double_free1d( t_double * out_buf);
t_int32 flip_cols(t_int32, t_double *in_crbuf, t_double *out_crflip);

// file ./src/vtc/sonde_sfe_vtc_proc.c:
t_double * calculate_correlation_matrix(s_vtc *vtc_params, t_double ** in_znormbuf );
t_int32 convert_cellto_matrix(s_vtc *, t_double ***, t_double **);
t_int32 cross_correlation(s_vtc *, t_int32, t_int32, t_double **, t_double **);
t_int32 select_corrpoints_by_tscale( s_vtc * vtc_params, t_int32 tscale, t_double * in_crbuf, t_double * out_crflipbuf );
t_int32 flip_crflip(t_double *, t_double *);
t_int32 fliplr(s_vtc *, t_double **, t_double ***);
t_int32 vector_to_matrix( s_vtc * vtc_params, t_double * in_crflipbuf, t_double ** out_matrixbuf );
t_int32 cell_to_matrix( t_int32 dim, t_int32 corr_size, t_double ** in_cells[][dim], t_double ** out_matrix );
long double calc_determinant(t_double ** Mat, t_int32 size);
long double abs_log_of_determinant( t_double ** in_matrix, t_int32 dim );
t_double trace_log( t_double ** in_matbuf, t_int32 dim ) ;
t_int32 cross_covariance(s_vtc *vtc_params, t_int32 maxdelay, t_int32 cols, t_double ** in_corrbuf, t_double ** out_covbuf );
t_double ** vtc_feature_permutation( s_vtc * vtc_params, t_double ** in_znormbuf, t_double ** out_eigen_spectrum );
t_double ** vtc_feature_pair( s_vtc * vtc_params, t_int32, t_double ** in_znormbuf, t_double ** out_eigen_spectrum, t_int32 * windows );
t_int32 calculate_eigen_value( s_vtc *vtc_params, t_int32 in_dim, t_double ** in_single_corr_matrix, t_double * out_eigen_spectrum );
t_int32 create_eigen_spectrum( s_vtc * vtc_params,t_int32 in_dim, t_double ** in_single_corr_matrix, t_double * out_eigen_spectrum );
t_int32 insert_trace_determinant( t_int32 tscale, t_int32 spectrum_size, t_double in_trace, t_double in_det, t_double * out_merged_eigen_spectrum );
t_int32 abs_eigen_value( t_int32 in_matdim, t_double * in_out_eigen_spectrum );
t_int32 merge_eigen_spectrum( t_int32 in_tscale_counter, t_int32 in_spectrum_size, t_double * in_abs_eigen_spect, t_double * out_merged_eigen_spectrum );
t_int32 write_output_feature_permutation( s_paths_and_meta *, t_int32 numscale_count, t_int32 spectrum_size, t_int32 num_win, t_double ** in_eigspect );
t_int32 write_file( t_int8 * out_filename, t_int32 rows, t_int32 cols, t_double ** in_buffer );
t_int32 write_output_feature_pair( t_int8 * out_dir, t_int32 numscale_count, t_int32 spectrum_size, t_int32 num_win, t_int32 pair_num, t_double ** in_eigspect );
t_int32 print_log_int( t_int8 * string, t_int32 num );
t_int32 print_log_double( t_int8 * string, t_double num );

// file ./src/write_csv/sonde_sfe_write_csv.c:
static void get_path(t_int8 *, t_int8 *, t_int8 *, t_int8 *);
t_int32 write_csv(s_shared_params *, s_paths_and_meta *, s_config *, s_features *, s_fileinfo *, s_jitter_shimmer *, s_qos *, s_vad *);
void write_header_for_features(FILE *, t_int8 *, t_int32, t_int32, t_int32);
static t_int32 write_data_for_features(t_int32, t_double **, t_int32, t_int32, FILE **);
static t_int32 write_data_for_features_int(t_int32, t_int32 *, t_int32, FILE *);

#endif /* _SONDE_SPFE_FUNCTION_DEF_H_ */

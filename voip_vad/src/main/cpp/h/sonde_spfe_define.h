/*
* @brief sonde sp-fe strcture enumerators and definitions.
*
* @author Swapnil Warkar
*
*/

#ifndef _SONDE_SPFE_DEFINE_H_
#define _SONDE_SPFE_DEFINE_H_

#include "sonde_datatypes.h"

/* ENABLE DEBUG DATA */

#define RADIX 2.0
#define SQR(a) ((a)*(a))
#define SIGN(a,b) ((b) >= 0.0 ? fabs(a) : -fabs(a))
#define SWAP(a,b) do { double t = (a); (a) = (b); (b) = t; } while (0)

#define SCALE_LINEAR   0
#define SCALE_MEL      1

#define ENABLE_DEBUG 0 /* 1 for ALL Data, 0 for Default */

#define dump 0
#define FD stderr
#define VAD_PADDING 10000
#define VAD_PADDING2 20000
#define MaxFiles 1000
#define MaxGroupCount 300
//#define MaxIntervals 110000
#define MaxIntervals 20000
#define SONDE_PI 3.14159265358979
#define TRUE 1
#define FALSE 0
#define DecBufSize 11000000
#define FilterWidth 19
#define DecimationOrderAlarm 8
#define UseUniformCoefficients FALSE
#define UseLowPassFilter TRUE
#define EnvDecimation 4
#define PostFiltNumRequiredPoints 6
#define QualInterval 3
#define MinSamplesDurationBurst 7
#define MinConfthresh 0.68
#define PataKaOnly FALSE
#define BwindowSize 51
#define padding_offset 50
#define DEBUG_FLAG 0
#define PERFORMANCE_ANALYSIS 0
#define FILTER_BANK_DUMP 0
/* COLOR FOR PRINTF */
#define RESET "\033[0m"
#define BLACK "\033[30m"			  /* Black */
#define RED "\033[31m"				  /* Red */
#define GREEN "\033[32m"			  /* Green */
#define YELLOW "\033[33m"			  /* Yellow */
#define BLUE "\033[34m"				  /* Blue */
#define MAGENTA "\033[35m"			  /* Magenta */
#define CYAN "\033[36m"				  /* Cyan */
#define WHITE "\033[37m"			  /* White */
#define BOLDBLACK "\033[1m\033[30m"   /* Bold Black */
#define BOLDRED "\033[1m\033[31m"	 /* Bold Red */
#define BOLDGREEN "\033[1m\033[32m"   /* Bold Green */
#define BOLDYELLOW "\033[1m\033[33m"  /* Bold Yellow */
#define BOLDBLUE "\033[1m\033[34m"	/* Bold Blue */
#define BOLDMAGENTA "\033[1m\033[35m" /* Bold Magenta */
#define BOLDCYAN "\033[1m\033[36m"	/* Bold Cyan */
#define BOLDWHITE "\033[1m\033[37m"   /* Bold White */

enum _config_error
{
    E_SUCCESS 	        = 0,
	E_UNSUPPORTED_SAMPLE_RATE = 10,
	E_NOT_A_RIFF        = 11,
	E_ZERO_SAMPLES      = 12,
	E_LESS_SAMPLES      = 13,
	E_ZERO_RMS			= 14,
	E_NOT_A_WAV         = 15,
	E_NOT_A_16_BIT_PCM  = 16,
	E_COMPRESSED_WAV    = 17,
    E_FILE_NOT_FOUND    = 18,
	E_ALLOCATION_FAILED = 19,
	E_INVALID_INPUT		= 20
};

typedef enum _config_error error_t;

// typedef struct 
// {
// 	t_float * vad_output_arr;
// 	t_float * ahh_output_arr;
// 	t_float * em_output_arr;
// 	t_int32 vad_response;
// 	t_int32 ahh_response;
// 	t_int32 em_response;
// }s_elck_output;

typedef struct 
{
	t_float vad_thresh_arr[2];
	t_float ahh_thresh_arr[2];
	t_float em_thresh_arr[2];
	t_float lmk_threshold;
}s_elck_user_thresholds;

typedef struct 
{
	t_int32 sonde_vad;
	t_int32 sonde_lsp;
	t_int32 sonde_formant;
	t_int32 smoothfilter;
	t_int32 vad_annotation;
	t_int32 jitter;
	t_int32 jitter1;
	t_int32 shimmer;
	t_int32 f0;
	t_int32 landmarks;
	t_int32 vtc;
	t_int32 ampl;
	t_int32 aqs_rms;
	t_int32 aqs_min;
	t_int32 aqs_avg;
	t_int32 aqs_avgnorm;
	t_int32 aqs_rmsnorm;
	t_int32 spectral_slope;
	t_int32 aqs_max;
	t_int32 mfcc;
	t_int32 delta_mfcc;
	t_int32 dd_mfcc;
	t_int32 scf;
	t_int32 orgmfcc;
	t_int32 org_deltamfcc;
	t_int32 org_ddmfcc;
	t_int32 org_scf;
	t_int32 orgfmt;
	t_int32 feature_fft;
	int features_array[50];
	int raw_features_array[50];
	int feature_count;
	int ahh_enable;
	int energy_meter_enable;

}s_features_config;

typedef struct 
{
	int mfcc_sma;
	int dmfcc_sma;
	int ddmfcc_sma;
	int lsp_sma;
	int jiiter_sma;
	int scf_sma;
	int ampl_sma;
	int spect_slope_sma;
	int fmt_sma;
	int shimmer_sma;
	int f0_sma;
	int jitter_sma;
	int jitter1_sma;
	int orgmfcc_sma;
	int orgscf_sma;
	int orgdeltamfcc_sma;
	int orgddmfcc_sma;
	int orgfmt_sma;
	int sma_array[50];
	int total_sma_count;
}s_sma_config;

typedef struct 
{
	char summary_ip[500];
	char summary_op[500];
	char version[100];
	char algo_name[30];
	char audio_name[200];
	t_int8 input_filename[500];
	t_int8 sonde_config_filename[500];
	t_int8 output_path_vad_1[500];
	t_int8 output_path_fe_2[500];
	t_int8 resources[500];
	t_int8 region_ip[500];
	t_int8 csv_name[500];
	char algo_version[10];
	char * threshold_config_path;

}s_paths_and_meta;

typedef struct{
	
	int sample_rate;
	int sample_count;
	t_int32 frame_samplerate_size;
	t_int32 frame_samplerate_step;

}org_wave_header_meta;


typedef struct 
{
	t_int32 Jdex;
	t_int32 FilterMidPoint;
	t_int32 number_of_frames;
	t_int32 sample_rate;
	t_double *temp_array;
	t_int16 memory_deallocation_flag;
	t_int16 frame_samplerate_size;
	t_int32 frame_samplerate_step;
	t_double *ham; 
	t_int32 SampleCount;
	t_int16 *pcm_data;
	t_float *resampled_pcm_data;
	t_float *eq_pcm;  
	float *emphasis_pcm;
	float *normalised_pcm;
	double* FirCoef;
	int fft_size;
	int fft_size2;
	org_wave_header_meta org_wave_params;
	int voice_frames;
	int reduced_number_of_frames;
	int number_of_frames_vad;
}s_shared_params;


typedef struct /* module structure starts*/
{
	t_int32 BlockSize;
	t_int32 DecimationOrder;
	t_int32 FilterLength;
	t_int32 OverlapRatio;
	t_int32 DecimDex;
	t_int32 SmoothFilterLength;
	t_int32 sonde_framesize;
	t_int32 sonde_framestep;
	t_int32 mfcc_filterbanks;
	t_int32 mfcc_filterorder;
	t_int32 mfcc_lower_frequency;
	t_int32 mfcc_higher_frequency;
	t_int32 mfcc_ceplifter;
	t_int32 lsp_filter_order;
	float pre_emphasis_factor;
	//t_double *ham;
	t_int32 threshold;
	t_int32 mfcc_mel_enabled;
	t_int32 scf_filterbanks;
	t_int32 scf_cepstral_coeff;
	t_int32 scf_lower_frequency; 
	t_int32 scf_higher_frequency;
	t_int32 scf_mel_enabled;
	t_int32 orgmfcc_filterorder;
	t_int32 orgmfcc_filterbanks;
	t_int32 orgmfcc_lower_frequency;
	t_int32 orgmfcc_higher_frequency;
	t_int32 orgmfcc_mel_enabled;
	t_int32 orgmfcc_ceplifter;
	t_int32 spect_slope_low0;
	t_int32 spect_slope_low1;
	t_int32 spect_slope_high0;
	t_int32 spect_slope_high1;
	t_int32 region_fe;
	t_int32 nfmt;
	t_int32 fmt_resample_freq;
	t_int32 lpc_order;
	t_int32 fmt_min_freq;
	t_int32 fmt_max_freq;
	t_int32 target_samling_freq;
} s_user_config;

typedef struct File
{

	t_int16 *PCM;
	t_int32 actual_samples;

	struct WaveHeader
	{
		t_uint8 riffID[5];   // "riff"
		t_uint32 size;		 //
		t_uint8 wavID[4];	// "WAVE"
		t_uint8 fmtID[4];	// "fmt "
		t_uint32 fmtSize;	//
		t_uint16 format;	 //
		t_uint16 channels;   //
		t_uint32 sampleRate; //
		t_uint32 bytePerSec; //
		t_uint16 blockSize;  //
		t_uint16 bitDepth;
		t_uint8 dataID[4];
		t_uint32 dataSize;
	} WaveHeader;

	t_int32 LastOffsetflag;
	t_int32 LastOnsetflag;
	t_double SegmentStart[3 * MaxGroupCount + 1];
	t_double SegmentEnd[3 * MaxGroupCount + 1];
	t_double SNR[3 * MaxGroupCount + 1];
	t_double VoiceConfidance[3 * MaxGroupCount + 1];
	t_int32 SegmentType[3 * MaxGroupCount + 1];

} s_fileinfo;

typedef struct 
{
	t_int32 duration_count;
	t_int32 * vad_annotation_array;
	t_int32 peak_bin;
	t_double DurationAr[MaxIntervals + 1];
	t_double Duration_FiltAr[MaxIntervals + 1];
	t_int32 blockdex;
	t_int32 number_of_blocks;
	int BlockSizeAlarm;
		
}s_vad;


typedef struct segments
{
	t_double StartTime;
	t_double EndTime;
	t_double Snr;
	t_int32 Classification;
	t_int8 *Description;
	
} s_segments;

typedef struct DELTADELTAMFCC_PARAMETERS
{
	double **delta_mfcc;
	double **ddmfcc;
	double **padded_mfcc;

}s_orgddmfcc;

typedef struct 
{
	float * filter_bank;
	double ** opt_mfcc_output;
	double ** opt_scf;
	float *fft_op;

}s_mfcc;


typedef struct
{
  long n_col;
  t_double *mat;
  t_double ** eigen_mat;
} s_fmt_polynomial; 

typedef struct 
{
	t_double ** fmt_out;
	t_float *cos_table; // K*res_fft matrix
	t_float *sin_table;
	t_long fft_size;    // framesize
	t_long k_max; // MIN(framesize,fftbins)
	t_long irdft_outsamples;    
	t_double *roots;
	t_double *lpc;
	t_float  *acf;
	t_float * current_lpcoeff;
	t_float * next_lpcoeff;
	t_double ** formant_out;
	t_float * out_irdft;
	t_float * out_clpc;

}s_fmt;

typedef struct 
{
	double **optdelta_mfcc;
	double **optdd_mfcc;
	double **padded_mfcc;

} s_deltamfcc;

typedef struct 
{
	t_double *	spec_slope_array;
	t_double ** FFT_spect;
	t_int32		slopesL[2];
	t_int32		slopesH[2];
	double      rdft_op[1024];

}s_spectral_slope; 

typedef struct MFCC_PARAMETERS
{
	t_double **ceps;
	t_double **ceps_spect;
	float *ham;
	t_double *inputv;
	double **FFT;
	t_double **MR3;
	t_double **H;
	t_double **H_scf;
	t_double *lifter;
	t_double **khasil;
	t_double **FCC;
	t_double **delta_mfcc;
	t_double **ddmfcc;
	t_double **scf;

} s_orgmfcc;

typedef struct LSP_PARAMETERS
{
	short FrS;
	t_int32 lsp_filter_order;
	long double **corr;
	t_double *ham;
	t_double **LPC;
	t_double **MLsF;
	t_int32 Nroots;

} s_lsp;


typedef struct JITTER_SHIMMER
{
	t_double *jitter;
	t_double *shimmer;
	t_double *ampl;
	t_double *f0;
	t_int16 *samples;
	t_double* output_autocorr;
	t_double* output_clipped;
	t_int32 f0_start;
	t_int32 f0_end;
	t_int32 past_position;
	t_int32 cuenta;
	t_int32 cuenta2;
	t_int32 state;
	t_int32 frame_past;
	t_int32 frame;
	t_int32 frame_count;
	t_int32 frame_count_past;
	t_double jitt;
	t_double sumpitchs;
	t_double shimm;
	t_double sumAmp;
	t_double *jitterddp;
	
} s_jitter_shimmer;

typedef struct auto_qos
{
	t_double *rms_amplitude_norm;
	t_double *rms_amplitude;
	t_double *min_output_buffer;
	t_double *avg_output_norm;
	t_double *max_output_buffer;
	t_double *avg_output_buffer;
	t_double  max_sample_value;
	t_double  min;
	t_double  max; 
	t_double  max_threshold;
	t_double  min_threshold;
	t_double  rms_value;
	t_double  rms_value_norm;
	t_double  avg_value;
	t_double  avg_value_norm; 
	t_double  *normalised_samples;
	
} s_qos;

typedef struct 
{
	short idx_s ;
	short idx_b ;
	short idx_v ;
	short idx_f ;
	short idx_p ;
	short idx_g ;
	int lmk_onset_ofset[7];
	float g_lmk[150];
	float b_lmk[150];
	float v_lmk[150];
	float f_lmk[150];
	float s_lmk[150];
	float p_lmk[150];
} s_lmk_op;

typedef struct LANDMARKS_PARAMETERS
{
	t_double *f0;
	t_int16 *samples;
	t_double* output_autocorr;
	t_double* output_clipped;
	t_int32 f0_start;
	t_int32 f0_end;
	t_int32 past_position;
	t_int32 cuenta;
	t_int32 cuenta2;
	t_int32 state;
	t_int32 frame_past;
	t_int32 frame;
	t_int32 frame_count;
	t_int32 frame_count_past;	

	t_int32 num;
	t_double *event_on;
	t_double *event_off;
	t_double *on;
	t_double *off;
	t_int32 *final_on; 
	t_int32 *final_off;
	t_int32 *final_type;
	t_double *final_strength;

	t_double *strength_on ;
	t_double* strength_off; 
	t_double* engy; 
	t_double longitud; 
	
	t_double* filter_b;
	t_double* resample_filter;
	
	t_double* input_filtered;
	t_double* input_resampled;
	t_double* energy_lndm; 
	t_double* hilbert_transf;
	t_int32 ws;
	t_int32 wm;
	t_double max_strength;
	// Hilbert & fft
	t_double * filter ;
	t_double * input;
	t_double * pitchcont;
	int 	 * voiced;
	double* f;
	double* x;
	t_int32 MAXPeaks;
	s_lmk_op lmk_op;
	
} s_landmarks;

typedef struct FORMANT_PARAMETERS
{
	double DefaultFormantFreq[7];
	int DefaultFormantK[7];
	int FormantCount;

	double FormantKpeak[6][30001];
	int **FormantKDex;
	double PolyFormant[6][4];
	double FormantRegress[6][30001];
	double LikelyFormantK[6];
	double FormantMeans[7];
	double ComputedFormantFreq[6];
	double** ParallelMag;
	double* Periodogram;
	double* IntegAuto;
	double Xpmax;
	double Xpmin;
	double Xprange;
	double Xpmean;
	double* Xmag;
	short StopProcessing;
	double** formant_output;
	double *XReal;
	double* XImag;
	double* OutReal;
	double* OutImag;

	t_int32 NumberFitPoints;
	t_uint8 CubicOkay;
	t_double* CubicSet;
	t_double* CalibX;
	t_double* CalibFx;

} s_formant;

typedef struct 
{
	s_jitter_shimmer  	js_params;
	s_orgddmfcc	     	orgddmfcc_params;
	s_lsp        		lsp_params;
	s_orgmfcc	    	orgmfcc_params;
	s_landmarks 		landmarks_params;	
	s_formant 			formant_params;
	s_spectral_slope  	specslope_params;
	s_qos  				qos_params;
	s_vad 				vad_params;
	s_fmt	 			fmt_params;
	s_deltamfcc			delta_params;
	s_mfcc			    mfcc_params;

}s_features;

typedef struct 
{
 	s_features_config * features_config;
	s_sma_config	  * sma_config;
	s_user_config	  * user_config; 
	s_fileinfo		  file_parameters;
}s_config; 

 enum {
	e_mfcc=1, 
	e_lsp, 
	e_fmt, 
	e_dmfcc, 
	e_ddmfcc, 
	e_va, 
	e_scf, 
	e_f0, 
	e_ampl, 
	e_jitter, 
	e_jitterddp, 
	e_shimmer, 
	e_spectralslope, 
	e_landmarks, 
	e_rmsamp, 
	e_rmsamp_norm, 
	e_min, 
	e_max, 
	e_avgamp, 
	e_avgamp_norm, 
	e_vad, 	
	e_orgmfcc, 
	e_orgdeltamfcc, 
	e_orgdeltadeltamfcc, 
	e_orgscf,
	e_orgfmt,
	e_feature_fft,
	e_feat_enum_pad
};


enum {
	e_mfcc_sma=1,
	e_lsp_sma,
	e_fmt_sma,
	e_dmfcc_sma,
	e_ddmfcc_sma,
	e_scf_sma,
	e_f0_sma,
	e_ampl_sma,
	e_jitter_sma,
	e_jitterddp_sma,
	e_shimmer_sma,
	e_spectralslope_sma,
	e_orgmfcc_sma,
	e_orgdeltamfcc_sma,
	e_orgddmfcc_sma,
	e_orgscf_sma,
	e_orgfmt_sma,
	e_sma_enum_pad
};

enum {
	vad_annotation_fmt=-1,
	mfcc_family=-3,
	js_family=-4,
	auto_qos_family=-5,
	orgmfcc_family=-6
	};

#endif /* _SONDE_SPFE_DEFINE_H_ */

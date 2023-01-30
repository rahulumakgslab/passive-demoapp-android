#ifndef _SONDE_SP_ELCK_DEFINE_H_
#define _SONDE_SP_ELCK_DEFINE_H_
#include "../h/sonde_spfe_define.h"

#define EDGE 1
#define LAMBDA 0
#define RESEARCH 1

typedef struct 
{
	s_fileinfo 			* file_parameters;
	s_config			* config;		  
	s_paths_and_meta    * paths_and_meta; 
	s_shared_params 	* shared_params; 
	s_features_config   * features_config; 
	s_user_config		* user_config;	  
	s_sma_config 		* sma_config;	 
	s_vad 				* vad_params; 

}s_other_structs;

/************************************************************/

typedef struct // binary response
{
	t_int32 elck_vad_response;	
	t_int32 elck_ahh_response;	
	t_int32 elck_em_response;

}s_elck_out_response;

typedef struct // intermediate output like active voice frames, total frames etc.
{
	t_float elck_vad_results[10];
	t_float * elck_ahh_results;
	t_float * elck_em_results;

}s_elck_write_out;

typedef struct 
{
	s_elck_out_response elck_out_response;	// binary response
	s_elck_write_out elck_write_out;	// intermediate output

}s_elck_out;

typedef struct // parent struct for accomodating all the outputs
{
	s_elck_user_thresholds elck_user_thresholds; 
	s_elck_out elck_output;
	t_int8 * version;
}s_elck_meta; 

/************************************************************/

typedef struct 
{ 
	s_paths_and_meta * paths_and_meta;
	s_fileinfo * file_parameters;
	s_user_config * user_config;
	s_vad * vad_params;
	s_shared_params * shared_params;
	s_features_config * features_config;

}s_elck_vad_structs;

typedef struct 
{ 
	s_paths_and_meta * paths_and_meta;
	s_fileinfo * file_parameters;
	s_shared_params * shared_params; 

}s_elck_ahh_structs;

typedef struct 
{ 	
	s_paths_and_meta * paths_and_meta;
	s_fileinfo * file_parameters;
	s_shared_params * shared_params; 

}s_elck_em_structs;

typedef struct 
{ 
	s_elck_vad_structs * elck_calc_structs;
	s_elck_meta * elck_meta;
	s_other_structs * other_structs; // keep it here to free intermediate structs

}s_elck_structs;

#endif

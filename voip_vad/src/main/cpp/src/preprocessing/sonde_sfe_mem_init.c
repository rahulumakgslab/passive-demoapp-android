/*
* @brief defination of dynamic memory allocation 
*
* @author Swapnil Warkar
*
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <unistd.h>
#include "../h/sonde_spfe_function_def.h"


/******************************************************************************
 * Function Name: init_fe_dynamic_memory
 *
 * Description: handles dynamic memory
 *
 * Param:
 *   (in) *s_fileinfo             - Pointer of file related arrays
 *   (in) *features             - Pointer of all low level features 
 *   (in) *context_structure    - Pointer of common parameters read from config
 *   (in) *s_jitter_shimmer       - Pointer of jitter shimmer related variables 
 * 
 * Return:  SUCCESS
 *
 *****************************************************************************/
t_int32 init_fe_dynamic_memory
(   
    s_fileinfo *fileread, 
    s_config * config,
    s_shared_params * shared_params,
    s_vad * vad_params
)
{
    s_features_config * features_config = config->features_config;
    s_user_config * user_config = config->user_config;
    
    t_int32 dealoc_flg = shared_params->memory_deallocation_flag;
    t_int32 frame_count = shared_params->number_of_frames;
    t_int32 dtype_dbl = sizeof(t_double);
    t_int32 fmt_count = 10; // 5 for fmt and 5 for bandwirdth
    t_int32 fft_size = shared_params->fft_size;
    t_int32 fft_size2 = shared_params->fft_size2;

    if ( shared_params->memory_deallocation_flag == 0)
    {
        /********************** Declarations ************************/
        /*user_config->orgmfcc_filterbanks = user_config->mfcc_filterbanks;
        user_config->orgmfcc_filterorder = user_config->mfcc_filterorder;
        low_level_fe->orgmfcc_params.mfcc_lower_frequency = user_config->mfcc_lower_frequency;
        low_level_fe->orgmfcc_params.mfcc_sampling_rate = fileread->WaveHeader.sampleRate;
        low_level_fe->orgmfcc_params.mfcc_higher_frequency = user_config->mfcc_higher_frequency;*/
        long int i;
        /************************************************************/

        /**************** file related allocations ******************/
        shared_params->emphasis_pcm       = (t_float*)single_memory_alloc( NULL, (fileread->actual_samples ),dtype_dbl,dealoc_flg);
        shared_params->normalised_pcm     = (t_float*)single_memory_alloc( NULL, (fileread->actual_samples ),sizeof(t_float),dealoc_flg);
        vad_params->vad_annotation_array = (t_int32*)single_memory_alloc( NULL, (frame_count + 50 ),sizeof(t_int32),dealoc_flg);
        shared_params->FirCoef           =  (t_double*)single_memory_alloc( NULL, ((user_config->FilterLength+2)  ),dtype_dbl,dealoc_flg);
        shared_params->ham          =  (t_double*)single_memory_alloc( NULL, (shared_params->frame_samplerate_size ),dtype_dbl,dealoc_flg);
        shared_params->temp_array   =  (t_double*)single_memory_alloc( NULL, (frame_count + (user_config->SmoothFilterLength - 1)),dtype_dbl,dealoc_flg); 
       
        /**************** mfcc related allocations ******************/
        
        // if( features_config->mfcc == 1 || features_config->delta_mfcc == 1 || features_config->dd_mfcc == 1 || features_config->scf == 1 )
        // {
        //     t_int32 num_pad = 4;
        //     t_int32 total_padding = frame_count + num_pad;
        //     t_int32 dtype_dbl = sizeof(t_double);
        //     low_level_fe->mfcc_params.filter_bank = (t_float*)single_memory_alloc( NULL, (user_config->mfcc_filterbanks),sizeof(t_float*),dealoc_flg);
        //     low_level_fe->mfcc_params.fft_op = (t_float*)single_memory_alloc( NULL, (fft_size + 1),sizeof(t_float *),dealoc_flg);
        //     low_level_fe->mfcc_params.opt_mfcc_output = (t_double**)double_memory_alloc( NULL, frame_count , user_config->mfcc_filterbanks , dtype_dbl, dealoc_flg );
        //     low_level_fe->mfcc_params.opt_scf = (t_double**)double_memory_alloc( NULL, frame_count , user_config->mfcc_filterorder , dtype_dbl, dealoc_flg );
        //     low_level_fe->orgmfcc_params.H_scf     = (t_double**)double_memory_alloc( NULL, user_config->mfcc_filterbanks ,fft_size2, dtype_dbl, dealoc_flg );

        //     if(features_config->delta_mfcc == 1 || features_config->dd_mfcc == 1){
        //         low_level_fe->delta_params.optdelta_mfcc     = (t_double**)double_memory_alloc( NULL, frame_count , user_config->mfcc_filterbanks, dtype_dbl, dealoc_flg );
        //         low_level_fe->delta_params.optdd_mfcc      = (t_double**)double_memory_alloc( NULL, frame_count , user_config->mfcc_filterbanks, dtype_dbl, dealoc_flg );
        //         low_level_fe->delta_params.padded_mfcc     = (t_double**)double_memory_alloc( NULL, total_padding , user_config->mfcc_filterorder, dtype_dbl, dealoc_flg );
        //     }
        // }
        /********************* LSP Allocs **********************/
        // if ( features_config->sonde_lsp == 1)
        // {
        //     low_level_fe->lsp_params.corr     = (long double**)double_memory_alloc( NULL, frame_count , user_config->lsp_filter_order + 1, dtype_dbl, dealoc_flg );
        //     //low_level_fe->lsp_params.inputv   = (t_double**)double_memory_alloc( NULL, frame_count , shared_params->frame_samplerate_size + 1, dtype_dbl, dealoc_flg );
        //     low_level_fe->lsp_params.MLsF     = (t_double**)double_memory_alloc( NULL, frame_count , user_config->lsp_filter_order, dtype_dbl, dealoc_flg );
        //     low_level_fe->lsp_params.LPC      = (t_double**)double_memory_alloc( NULL, frame_count , user_config->lsp_filter_order, dtype_dbl, dealoc_flg );
        // }
        // /*************** jitter-shimmer allocs *****************/
        // if( features_config->jitter == 1 || features_config->shimmer == 1 || features_config->f0 == 1 || features_config->ampl == 1 )
        // {
        //     js_params->jitter       =  (t_double*)single_memory_alloc( NULL, (frame_count),dtype_dbl,dealoc_flg); 
        //     js_params->shimmer      =  (t_double*)single_memory_alloc( NULL, (frame_count),dtype_dbl,dealoc_flg); 
        //     js_params->jitterddp    =  (t_double*)single_memory_alloc( NULL, (frame_count),dtype_dbl,dealoc_flg); 
        //     js_params->f0           =  (t_double*)single_memory_alloc( NULL, (frame_count),dtype_dbl,dealoc_flg); 
        //     js_params->ampl         =  (t_double*)single_memory_alloc( NULL, (frame_count),dtype_dbl,dealoc_flg);           
        // }
        // /*********************************************************/

        // /********************* auto_qos allocs *******************/
        // if (  features_config->aqs_avg == 1 || features_config->aqs_max == 1 || features_config->aqs_min == 1 || features_config->aqs_avgnorm == 1 || features_config->aqs_rmsnorm == 1 || features_config->aqs_rms == 1 ) 
        // {
        //     low_level_fe->qos_params.normalised_samples  =  (t_double*)single_memory_alloc( NULL, (shared_params->frame_samplerate_step),dtype_dbl,dealoc_flg); 
        //     low_level_fe->qos_params.rms_amplitude       =  (t_double*)single_memory_alloc( NULL, (frame_count),dtype_dbl,dealoc_flg); 
        //     low_level_fe->qos_params.rms_amplitude_norm   =  (t_double*)single_memory_alloc( NULL, (frame_count),dtype_dbl,dealoc_flg); 
        //     low_level_fe->qos_params.min_output_buffer    =  (t_double*)single_memory_alloc( NULL, (frame_count),dtype_dbl,dealoc_flg); 
        //     low_level_fe->qos_params.max_output_buffer    =  (t_double*)single_memory_alloc( NULL, (frame_count),dtype_dbl,dealoc_flg); 
        //     low_level_fe->qos_params.avg_output_buffer    =  (t_double*)single_memory_alloc( NULL, (frame_count),dtype_dbl,dealoc_flg); 
        //     low_level_fe->qos_params.avg_output_norm      =  (t_double*)single_memory_alloc( NULL, (frame_count),dtype_dbl,dealoc_flg); 
        // }

        // /*************  delta & deltadelta mfcc allocs  ***********/
        // if ( features_config->org_ddmfcc == 1 || features_config->org_deltamfcc ==1 )
        // {
        //     t_int32 num_pad = 4;
        //     t_int32 total_padding = frame_count + num_pad;
        //     low_level_fe->orgddmfcc_params.delta_mfcc     = (t_double**)double_memory_alloc( NULL, frame_count , user_config->orgmfcc_filterbanks, dtype_dbl, dealoc_flg );
        //     low_level_fe->orgddmfcc_params.ddmfcc      = (t_double**)double_memory_alloc( NULL, frame_count , user_config->orgmfcc_filterbanks, dtype_dbl, dealoc_flg );
        //     low_level_fe->orgddmfcc_params.padded_mfcc     = (t_double**)double_memory_alloc( NULL, total_padding , user_config->orgmfcc_filterorder, dtype_dbl, dealoc_flg );
        // }
    }
    else if ( dealoc_flg == 1)
    {
        // if ( features_config->sonde_formant ) double_memory_alloc((void**) low_level_fe->fmt_params.fmt_out , frame_count, fmt_count, dtype_dbl, dealoc_flg );
        // if( features_config->mfcc == 1 || features_config->delta_mfcc == 1 || features_config->dd_mfcc == 1 || features_config->scf == 1 )
        // {
        //     t_int32 num_pad = 4;
        //     t_int32 total_padding = frame_count + num_pad;
        //     low_level_fe->mfcc_params.filter_bank = (t_float*)single_memory_alloc((void*)low_level_fe->mfcc_params.filter_bank, (user_config->mfcc_filterbanks),sizeof(t_float*),dealoc_flg);
        //     low_level_fe->mfcc_params.fft_op = (t_float*)single_memory_alloc((void*)low_level_fe->mfcc_params.fft_op, (fft_size + 1),sizeof(t_float *),dealoc_flg);
        //     low_level_fe->mfcc_params.opt_mfcc_output = (t_double**)double_memory_alloc((void**)low_level_fe->mfcc_params.opt_mfcc_output , frame_count , user_config->mfcc_filterbanks , dtype_dbl, dealoc_flg );
        //     low_level_fe->mfcc_params.opt_scf = (t_double**)double_memory_alloc((void**)low_level_fe->mfcc_params.opt_scf, frame_count , user_config->mfcc_filterorder , dtype_dbl, dealoc_flg );
        //     low_level_fe->orgmfcc_params.H_scf     = (t_double**)double_memory_alloc((void**)low_level_fe->orgmfcc_params.H_scf, user_config->mfcc_filterbanks ,fft_size2, dtype_dbl, dealoc_flg );

        //     if(features_config->dd_mfcc == 1 || features_config->delta_mfcc == 1){
        //         low_level_fe->delta_params.optdelta_mfcc     = (t_double**)double_memory_alloc((void**)low_level_fe->delta_params.optdelta_mfcc, frame_count , user_config->mfcc_filterbanks, dtype_dbl, dealoc_flg );
        //         low_level_fe->delta_params.optdd_mfcc      = (t_double**)double_memory_alloc((void**)low_level_fe->delta_params.optdd_mfcc, frame_count , user_config->mfcc_filterbanks, dtype_dbl, dealoc_flg );
        //         low_level_fe->delta_params.padded_mfcc     = (t_double**)double_memory_alloc((void**)low_level_fe->delta_params.padded_mfcc, total_padding , user_config->mfcc_filterorder, dtype_dbl, dealoc_flg );
        //     }
        // }
        // if( features_config->orgmfcc == 1 || features_config->org_deltamfcc == 1 || features_config->org_ddmfcc == 1 || features_config->org_scf == 1 )
        // {
        //     double_memory_alloc( (void**)low_level_fe->orgmfcc_params.FCC, frame_count , user_config->orgmfcc_filterorder,__SIZEOF_FLOAT__, dealoc_flg );
        //     double_memory_alloc( (void**)low_level_fe->orgmfcc_params.MR3, frame_count , user_config->orgmfcc_filterbanks , dtype_dbl, dealoc_flg );
        //     //double_memory_alloc( (void**)low_level_fe->orgmfcc_params.ceps, frame_count , shared_params->fft_size2 , sizeof(low_level_fe->orgmfcc_params.ceps), dealoc_flg );
        //     double_memory_alloc( (void**)low_level_fe->orgmfcc_params.H, user_config->orgmfcc_filterbanks , shared_params->fft_size2, sizeof(low_level_fe->orgmfcc_params.H), dealoc_flg );
        //     single_memory_alloc( (void**)low_level_fe->orgmfcc_params.inputv, (shared_params->frame_samplerate_size  ),sizeof(low_level_fe->orgmfcc_params.inputv),dealoc_flg);
        //     double_memory_alloc( (void**)low_level_fe->orgmfcc_params.khasil, frame_count , user_config->orgmfcc_filterbanks , sizeof(low_level_fe->orgmfcc_params.khasil), dealoc_flg );
        //     double_memory_alloc( (void**)low_level_fe->orgmfcc_params.FFT,frame_count ,2 * fft_size + 1, __SIZEOF_DOUBLE__, dealoc_flg );
        //     single_memory_alloc( (void**)low_level_fe->orgmfcc_params.lifter, (user_config->orgmfcc_filterorder),sizeof(low_level_fe->orgmfcc_params.lifter),dealoc_flg);
        //     double_memory_alloc( (void**)low_level_fe->orgmfcc_params.H_scf,user_config->scf_filterbanks  ,shared_params->fft_size2, dtype_dbl, dealoc_flg );
        //     double_memory_alloc( (void**)low_level_fe->orgmfcc_params.scf,frame_count ,user_config->orgmfcc_filterorder, dtype_dbl, dealoc_flg );
        // }
        // if( features_config->spectral_slope==1 )
        // {
        //     single_memory_alloc( (void*)low_level_fe->specslope_params.spec_slope_array, (frame_count ),dtype_dbl,dealoc_flg);
        // }

        // /********************* Common Allocs **********************/
        single_memory_alloc( (void*)shared_params->ham , (shared_params->frame_samplerate_size ),dtype_dbl,dealoc_flg);
        single_memory_alloc( (void*)shared_params->temp_array , (frame_count + (user_config->SmoothFilterLength - 1)),dtype_dbl,dealoc_flg); 
        
        single_memory_alloc( (void*)shared_params->emphasis_pcm, (fileread->actual_samples ),dtype_dbl,dealoc_flg);
        single_memory_alloc( (void*)shared_params->normalised_pcm, (fileread->actual_samples ),sizeof(t_float),dealoc_flg);
        single_memory_alloc( (void*)vad_params->vad_annotation_array, (frame_count + 50 ),sizeof(t_int32),dealoc_flg);
        single_memory_alloc( (void*)shared_params->FirCoef, ((user_config->FilterLength+2)  ),dtype_dbl,dealoc_flg);       
        // /********************* LSP Allocs **********************/
        // if ( features_config->sonde_lsp == 1)
        // {
        //     double_memory_alloc( (void**)low_level_fe->lsp_params.corr, frame_count, user_config->lsp_filter_order + 1, dtype_dbl, dealoc_flg );
        //     //double_memory_alloc( (void**)low_level_fe->lsp_params.inputv, frame_count , shared_params->frame_samplerate_size + 1, dtype_dbl, dealoc_flg );
        //     double_memory_alloc( (void**)low_level_fe->lsp_params.MLsF, frame_count , user_config->lsp_filter_order, dtype_dbl, dealoc_flg );
        //     double_memory_alloc( (void**)low_level_fe->lsp_params.LPC, frame_count , user_config->lsp_filter_order, dtype_dbl, dealoc_flg );
        // }
        // // /*******************************************************/

        // /*************** jitter-shimmer allocs *****************/
        // if( features_config->jitter == 1 || features_config->shimmer == 1 || features_config->f0 == 1 || features_config->ampl == 1 )
        // {
        //     single_memory_alloc( (void*)js_params->jitter, (frame_count),dtype_dbl,dealoc_flg); 
        //     single_memory_alloc( (void*)js_params->shimmer, (frame_count),dtype_dbl,dealoc_flg); 
        //     single_memory_alloc( (void*)js_params->jitterddp, (frame_count),dtype_dbl,dealoc_flg); 
        //     single_memory_alloc( (void*)js_params->f0, (frame_count),dtype_dbl,dealoc_flg); 
        // }
        // /*********************************************************/

        // /********************* auto_qos allocs *******************/

        // if (  features_config->aqs_avg == 1 || features_config->aqs_max == 1 || features_config->aqs_min == 1 || features_config->aqs_avgnorm == 1 || features_config->aqs_rmsnorm == 1 || features_config->aqs_rms == 1 ) 
        // {
        //     single_memory_alloc( (void*)low_level_fe->qos_params.rms_amplitude_norm, (shared_params->frame_samplerate_step),dtype_dbl,dealoc_flg); 
        //     single_memory_alloc( (void*)low_level_fe->qos_params.min_output_buffer, (frame_count),dtype_dbl,dealoc_flg); 
        //     single_memory_alloc( (void*)low_level_fe->qos_params.max_output_buffer, (frame_count),dtype_dbl,dealoc_flg); 
        //     single_memory_alloc( (void*)low_level_fe->qos_params.avg_output_buffer, (frame_count),dtype_dbl,dealoc_flg); 
        //     single_memory_alloc( (void*)low_level_fe->qos_params.avg_output_norm, (frame_count),dtype_dbl,dealoc_flg); 
        // }

        // /*************  delta & deltadelta mfcc allocs  ***********/
        // if ( features_config->org_ddmfcc == 1 || features_config->org_deltamfcc ==1 )
        // {
        //     t_int32 num_pad = 4;
        //     t_int32 total_padding = frame_count + num_pad;
        //     double_memory_alloc( (void**)low_level_fe->orgddmfcc_params.delta_mfcc, frame_count , user_config->orgmfcc_filterbanks, dtype_dbl, dealoc_flg );
        //     double_memory_alloc((void**)low_level_fe->orgddmfcc_params.ddmfcc, frame_count , user_config->orgmfcc_filterbanks, dtype_dbl, dealoc_flg );
        //     double_memory_alloc( (void**)low_level_fe->orgddmfcc_params.padded_mfcc, total_padding , user_config->orgmfcc_filterorder, dtype_dbl, dealoc_flg );
        // }
    } 
    return E_SUCCESS;
}

/******************************************************************************
 * Function Name: double_memory_alloc
 *
 * Description: handles dynamic memory allocation/deallocation for 2d arrays
 *
 * Param:
 *   (in) void ** dealoc_ptr    - Pointer to be freed 
 *   (in) t_int32 rows              - rows for 2d array
 *   (in) t_int32 columns           - columns for 2d array
 *   (in) t_int32 dtype             - tells functions about data type to be used for calloc
 *   (in) t_int32 dealoc_flg        - 1 - deallocate dealoc_ptr 0 - allocate and return
 * 
 * Return:  (out) void ** partition_ptr - allocated pointer / NULL if dealoc_flg=1
 *
 *****************************************************************************/
void ** double_memory_alloc( void ** dealoc_ptr, t_int32 rows, t_int32 columns, t_int32 dtype, t_int32 dealoc_flg )
{
    void ** partition_ptr = NULL;

    if (dealoc_flg == 1)
    {
        if( dealoc_ptr == NULL ) return NULL;
        for(t_int32 i = 0; i < rows; i++ )
        {
            if(dealoc_ptr[i] != NULL) 
                free(dealoc_ptr[i]);
        }
        free(dealoc_ptr);
    }
    else if ( rows <= 0 || columns <= 0 ) return NULL;
    else
    {
        partition_ptr = calloc ((rows) , sizeof(void *));
        if ( partition_ptr == NULL )
        {
            fprintf(stderr, "Error in malloc\n");
            exit(E_ALLOCATION_FAILED);
        }
        for (t_int32 i = 0; i < rows; i++)
        {
            partition_ptr[i] = calloc (( columns + padding_offset ) , dtype);
            if ( partition_ptr[i] == NULL )
            {
                fprintf(stderr, "Errro in malloc\n");
                exit(E_ALLOCATION_FAILED);
            }
        }
    }
    return partition_ptr;
}

/******************************************************************************
 * Function Name: single_memory_alloc
 *
 * Description: handles dynamic memory allocation/deallocation for 1d arrays
 *
 * Param:
 *   (in) void * dealoc_ptr     - Pointer to be freed 
 *   (in) t_int32 size              - elements to be allocated in bytes
 *   (in) t_int32 dtype             - tells functions about data type to be used for calloc
 *   (in) t_int32 dealoc_flg        - 1 - deallocate dealoc_ptr 0 - allocate and return
 * 
 * Return:  (out) void * aloc_ptr - allocated pointer / NULL if dealoc_flg=1
 *
 *****************************************************************************/
void * single_memory_alloc( void * dealoc_ptr, t_int32 size, t_int32 dtype, t_int32 dealoc_flg)
{
    void * aloc_ptr = NULL;
    if (dealoc_flg == 1){ if( dealoc_ptr != NULL ) free(dealoc_ptr); }
    else if ( size <= 0 ) return NULL;
    else
    {
        aloc_ptr = calloc(size+padding_offset,dtype);
    }
    return aloc_ptr;
}

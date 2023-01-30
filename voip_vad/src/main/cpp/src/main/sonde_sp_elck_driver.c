#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <libgen.h>
#include <stdarg.h>
#include <math.h>
#include <unistd.h>
#include "sonde_spfe_function_def.h"

t_int32 sp_elck(t_int8 *input_filename, t_int8 *config_file, t_int8 *output_dir)
{
    s_elck_structs *elck_structs = NULL;
    t_int32 err1, err2, err3, retVal = 0;
    t_int8 * json = NULL;
    elck_structs = elck_init(input_filename, config_file, output_dir);
    elck_vad_calc(elck_structs->elck_calc_structs, elck_structs->elck_meta);
    err2 = elck_ahh_calc(elck_structs->elck_calc_structs, elck_structs->elck_meta);
    err3 = elck_em_calc(elck_structs->elck_calc_structs, elck_structs->elck_meta);
    // if( err1 || err2 || err3 )
    // {
    //     return default_json(elck_structs->elck_meta->elck_output.elck_out_response, elck_structs->elck_meta->version );
    // }
    // json = elck_dump(elck_structs->elck_calc_structs, elck_structs->elck_meta, output_dir);
   
    if (access(elck_structs->elck_calc_structs->paths_and_meta->output_path_vad_1, F_OK)== 0){
        retVal = 1;
    }
    else{
        retVal = 0;
    }
    
    elck_destruct(elck_structs);
    return retVal;
}

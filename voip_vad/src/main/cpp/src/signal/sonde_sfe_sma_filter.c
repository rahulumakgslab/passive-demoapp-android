/*
* 
* @brief defination of all require mathematical equations
*
* @author Swapnil Warkar
*
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <unistd.h>
#include "sonde_spfe_function_def.h"


int smooth_filter_for_features( s_shared_params * shared_params,s_user_config * user_config, int filter_order, double ** input_array)
{
    static int col;
    static int row;
    static int i, j, k, index, limit;
    static int size;
    row = shared_params->number_of_frames;
    col = filter_order;
    size = row + (user_config->SmoothFilterLength - 1);
    index = user_config->SmoothFilterLength/2;
    limit = index + row;

    for(k=0; k<col; k++)
    {
        memset(shared_params->temp_array, 0, size);
        for(i=0; i<user_config->SmoothFilterLength/2; i++)
        {
            shared_params->temp_array[i] = input_array[0][k];
        }

        for(j=0; j<row; j++)
        {
            shared_params->temp_array[i] = input_array[j][k];
            i++;
        }

        i--;
        j--;
        for(; i<size; i++)
        {
            shared_params->temp_array[i] = input_array[j][k];
        }

        for (i=index; i<limit; i++)
        {
            for (j=1; j<=user_config->SmoothFilterLength/2; j++)
            {
                shared_params->temp_array[i] += shared_params->temp_array[i-j];
                shared_params->temp_array[i] += shared_params->temp_array[i+j];
            }
            shared_params->temp_array[i] /= (t_double)user_config->SmoothFilterLength;
        }

        j=index;
        for(i=0; i<row; i++)
        {
            if(j < limit)
            {
                input_array[i][k] = shared_params->temp_array[j];
                //memcpy(low_level_fe->mfcc_params.MR3[i], shared_params->temp_array, size);
                j++;
            }
        }
    }
    return E_SUCCESS;
}


int smooth_filter_for_feature( s_shared_params *shared_params,s_user_config * user_config, int filter_order, double * input_array )

{
    static int col;
    static int row;
    static int i, j, k, index, limit;
    static int size;
    row = shared_params->number_of_frames;
    col = filter_order;

    size = row + (user_config->SmoothFilterLength - 1);

    index = user_config->SmoothFilterLength/2;
    limit = index + row;

    for(k=0; k<col; k++)
    {
        memset(shared_params->temp_array, 0, size);
        for(i=0; i<user_config->SmoothFilterLength/2; i++)
        {
            shared_params->temp_array[i] = input_array[0];
        }

        for(j=0; j<row; j++)
        {
            shared_params->temp_array[i] = input_array[j];
            i++;
        }

        i--;
        j--;
        for(; i<size; i++)
        {
            shared_params->temp_array[i] = input_array[j];
        }

        for (i=index; i<limit; i++)
        {
            for (j=1; j<=user_config->SmoothFilterLength/2; j++)
            {
                shared_params->temp_array[i] += shared_params->temp_array[i-j];
                shared_params->temp_array[i] += shared_params->temp_array[i+j];
            }
            shared_params->temp_array[i] /= (t_double)user_config->SmoothFilterLength;
        }

        j=index;
        for(i=0; i<row; i++)
        {
            if(j < limit)
            {
                input_array[i] = shared_params->temp_array[j];
                //memcpy(low_level_fe->mfcc_params.MR3[i], shared_params->temp_array, size);
                j++;
            }
        }
    }
    return E_SUCCESS;
}

/*
t_int32 smooth_filter_mfcc(features * low_level_fe, )
{
    static int col;
    static int row;
    static int i, j, k, index, limit;
    static int size;
    row = shared_params->number_of_frames;
    col = low_level_fe->mfcc_params.mfcc_filter_order;
    size = row + (user_config->SmoothFilterLength - 1);
    index = user_config->SmoothFilterLength/2;
    limit = index + row;

    for(k=0; k<col; k++)
    {
        memset(shared_params->temp_array, 0, size);
        for(i=0; i<user_config->SmoothFilterLength/2; i++)
        {
            shared_params->temp_array[i] = low_level_fe->mfcc_params.MR3[0][k];
        }

        for(j=0; j<row; j++)
        {
            shared_params->temp_array[i] = low_level_fe->mfcc_params.MR3[j][k];
            i++;
        }

        i--;
        j--;
        for(; i<size; i++)
        {
            shared_params->temp_array[i] = low_level_fe->mfcc_params.MR3[j][k];
        }

        for (i=index; i<limit; i++)
        {
            for (j=1; j<=user_config->SmoothFilterLength/2; j++)
            {
                shared_params->temp_array[i] += shared_params->temp_array[i-j];
                shared_params->temp_array[i] += shared_params->temp_array[i+j];
            }
            shared_params->temp_array[i] /= (t_double)user_config->SmoothFilterLength;
        }

        j=index;
        for(i=0; i<row; i++)
        {
            if(j < limit)
            {
                low_level_fe->mfcc_params.MR3[i][k] = shared_params->temp_array[j];
                //memcpy(low_level_fe->mfcc_params.MR3[i], shared_params->temp_array, size);
                j++;
            }
        }
    }
    return E_SUCCESS;
}

t_int32 smooth_filter_lsp(features * low_level_fe, )
{
    static int col;
    static int row;
    static int i, j, k, index, limit;
    static int size;
    row = shared_params->number_of_frames;
    col = low_level_fe->lsp_params.lsp_filter_order;

    size = row + (user_config->SmoothFilterLength - 1);

    index = user_config->SmoothFilterLength/2;
    limit = index + row;

    for(k=0; k<col; k++)
    {
        for(i=0; i<user_config->SmoothFilterLength/2; i++)
        {
            shared_params->temp_array[i] =low_level_fe->lsp_params.MLsF[0][k];
        }

        for(j=0; j<row; j++)
        {
            shared_params->temp_array[i] = low_level_fe->lsp_params.MLsF[j][k];
            i++;
        }

        i--;
        j--;
        for(; i<size; i++)
        {
            shared_params->temp_array[i] = low_level_fe->lsp_params.MLsF[j][k];
        }
    
        for (i=index; i<limit; i++)
        {
            for (j=1; j<=user_config->SmoothFilterLength/2; j++)
            {
                shared_params->temp_array[i] += shared_params->temp_array[i-j];
                shared_params->temp_array[i] += shared_params->temp_array[i+j];
            }
            shared_params->temp_array[i] /= (t_double)user_config->SmoothFilterLength;
        }

        j=index;
        for(i=0; i<row; i++)
        {
            if(j < limit)
            {
             low_level_fe->lsp_params.MLsF[i][k] = shared_params->temp_array[j];
                j++;
            }
        }
    }
    return E_SUCCESS;
}

void save_clubbed(t_int8 *header1, t_int32 col1, t_int8 *header2, t_int32 col2,
                     t_int32 samplerate, t_double** matrix1, t_double **matrix2, t_int8 *filename, mfcc_proc *low_level_fe->mfcc_params)
{
    FILE *fp;
    static char values[20], index[20], temp[10], col[30];
    fp = fopen(filename, "w");
    fprintf(fp, "frameTime");
    for(int i=0; i<col1; i++)
    {
        sprintf(temp, "%d", i);
        strcpy(col, header1);
        strcat(col, temp);
        fprintf(fp, ",%s", col);
    }
    for(int i=0; i<col2; i++)
    {
        sprintf(temp, "%d", i);
        strcpy(col, header2);
        strcat(col, temp);
        fprintf(fp, ",%s", col);
    }
    fprintf(fp,"\n");

    for(int i=0; i<(shared_params->number_of_frames); i++)
    {
        sprintf(index,"%d", abs((t_double)i * 1000 * low_level_fe->mfcc_params.mfcc_frame_samplerate_step / samplerate));
        fprintf(fp,"%s", index);
        for(int j=0; j<col1; j++)
        {
            sprintf(temp,"%.6f", matrix1[i][j]);
            if(!strcmp(temp,"0.000000"))
            {
                sprintf(values,"%.8e", matrix1[i][j]);    
            }
            else
            {
                sprintf(values,"%.15lf", matrix1[i][j]);
            }
            
            fprintf(fp,",%s",values);   
        }
        for(int j=0; j<col2; j++)
        {
            sprintf(temp,"%.6f", matrix2[i][j]);
            if(!strcmp(temp,"0.000000"))
            {
                sprintf(values,"%.8e", matrix2[i][j]);    
            }
            else
            {
                sprintf(values,"%.15lf", matrix2[i][j]);
            }
            
            fprintf(fp,",%s",values); 
        }
        fprintf(fp,"\n");
    }
    fclose(fp);
}

*/




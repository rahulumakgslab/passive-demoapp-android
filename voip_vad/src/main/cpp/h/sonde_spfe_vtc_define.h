
#include "sonde_datatypes.h"

typedef struct vtc
{
	t_int32 fpairs;
	t_int8 ** fpairs_features;
	t_int32 fpairs_features_count;
	t_double ** input_array;
		//double Fea[ROWS][COLS];
	//double tmp_feas_1[40][2]      //40x2   - Input from .csv file.

	double **Fea;                   //20x2   - calculate_vtc()
	//double **corr_infe;				//20x2   - calculate_correlation_matrix()   //correlation_counter //increment here. 
	double **cr;					//29x1   - cross_correlation()              
	double **cr_flip;				//29x1   - flip_rows()						

	double **matrix_buffer;			//15x15   - vector_to_matrix_crflip(), vector_to_matrix_cr()
	double ***tmp_corrMatrix;		//4x15x15 - fliplr()						//correlation_counter  
	double **single_corr_matrix;	//30x30   - convert_cellto_matrix()			//correlation_counter //use here.

	double **in_colvector;          //30x1
	                                //4x30x1  - nscale*30*1 - calculate_eigen_vector()
	double **merged_eigen_spectrum;           //1x30    - num_win_loopcounter * (NumcorrPoints(15) * num_scale(1) * Number of features(2) = 30) //num_win*120
	
	double **lg_flip;//1x29
	double **lg;//1x29

	int num_scale[4];
	int num_corr_points; //15
	int win_rowsize; //20      		- WinSize      
	int win_colsize; //2       		- num_of_features
	int feature_corr_count;//4      - num_of_features*2
	int corr_size;

	int tscale_counter;
	int num_win_loopcounter;

	int eigenmatrix_dimension; //30  - num_corr_points*2
	int eigspectrum_colsize; //120
	int eigspect_colsize_fpair; //120
	int eigspectrum_rowsize; //2   - num_win
	double mean_lowfe1;
	double mean_lowfe2;
	double sigma_fe1 ;
    double sigma_fe2 ;
	int csv_rows;
	int csv_cols;
	int max_num_scale;
	int	winsize;
	int numscale_count;
	//t_int32 vtc_frame_count;
	t_int32 num_win;
    char output_filename[300];;
	
}s_vtc;


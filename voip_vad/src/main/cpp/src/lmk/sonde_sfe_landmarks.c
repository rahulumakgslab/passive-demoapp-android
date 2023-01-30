
#include "math.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <ctype.h>
#include <libgen.h>
#include "sonde_spfe_function_def.h"

/////config
#define N_filters 10
double pendiente(double* x,double* y,int N);
double my_max(short* x,int start,int end);
double my_min(short* x,int start,int end);
void calc_dim_fractal(s_landmarks *lndmks,s_fileinfo *file_parameters, s_shared_params *);
t_int32 adjust_f0_L(int cuenta, t_double *f0);

int compute_landmarks(s_paths_and_meta * paths_and_meta, s_features_config * features_config, s_shared_params * shared_params, s_landmarks *lndmks, s_fileinfo *file_parameters )
{
	fprintf(stderr,"\n	start : landmarks processing\n" );
	//FILE* filePtr_wav;
	//filePtr_wav= fopen(archivo, "rb");
	int get_cnt = 0;
    char output_filename[300];
	
	int fs = shared_params->org_wave_params.sample_rate;
	int sample_count = shared_params->org_wave_params.sample_count;
	
	//int fs = file_parameters->WaveHeader.sampleRate;

	/******** output file handling *********/
	char vad_buffer[300],wav_buffer[300];
	strcpy(vad_buffer,paths_and_meta->output_path_vad_1);
    strcpy(output_filename,dirname(vad_buffer));
    strcat(output_filename,"/");
	strcpy(wav_buffer,paths_and_meta->input_filename);
    strcat(output_filename,basename(wav_buffer));
    output_filename[strlen(output_filename) - 4] = 0;
    strcat(output_filename,"_landmarks.csv");
    /***************************************/
	/******** summary file handling *********/
	char summary_output_filename[300];
	strcpy(vad_buffer,paths_and_meta->output_path_vad_1);
    strcpy(summary_output_filename,dirname(vad_buffer));
    strcat(summary_output_filename,"/");
	strcpy(wav_buffer,paths_and_meta->input_filename);
    strcat(summary_output_filename,basename(wav_buffer));
    summary_output_filename[strlen(summary_output_filename) - 4] = 0;
    strcat(summary_output_filename,"_landmarks_summary.csv");
    /***************************************/
	//FILE* filePtr_csv = fopen(output_filename, "w");
	FILE* file_filterbank;
	FILE* file_resample_filter;
	/******** filter file handling *********/
    char filter_filename[300];
	strcpy(filter_filename,paths_and_meta->resources);
	char resample_filter_filename[300]; 
	strcpy(resample_filter_filename,paths_and_meta->resources);
	strcat(resample_filter_filename,"/resample_filter44_11k.bin");
    /***************************************/
	
    /***************************************/

	if (fs==8000)
		strcat(filter_filename,"/gamatonefilters_10ch_8k.bin");
	else if(fs==16000)
		strcat(filter_filename,"/gamatonefilters_10ch_16k.bin");
	else if(fs==44100 || fs==48000)
		strcat(filter_filename,"/gamatonefilters_10ch_11k.bin");
	else 
		printf("\nError: sample rate not supported by landmarks algorithm");
	
	if ((access(filter_filename,F_OK)) == -1 )
    {
        perror("\n	ERROR : input filters does not exist, exiting landmarks FE\n");
		
        features_config->landmarks = 0;
		return -1;
    }
	else
	{
		file_filterbank = fopen(filter_filename,"rb");
		file_resample_filter = fopen(resample_filter_filename,"rb");
	}
	//FILE *filePtr;

	int N 			= fs*.03; // 330;
	int L 			= fs*.01; // 110;
	int MaxLength 	= 3000;
	// minlenght is 330 x 8 = 2640
	int MaxFR 		= 100;
	int length 		= MaxLength;// /4;
	int fr 			= (length - N) / L + 1;
	length 			= (fr - 1) * L + N;
	int real_length = length; // *4;
	lndmks->num=0;
	int chunk_size = 2048;

	///////////////////////////////////////////////
	/////////////////// Memory Allocation

	//WaveHeader_t header;
	//get_cnt = fread(&header, sizeof(header), 1, filePtr_wav);
	//WaveData_t *data = (WaveData_t*)malloc(sizeof(WaveData_t));
	//wavRead(header,data);
	//data->samples = (t_int16*)malloc(real_length* sizeof(t_int16));

	t_double *filter = NULL;// 			= (t_double *)malloc(sizeof(t_double) * 466);
	t_double *input 			= (t_double *)(malloc(sizeof(t_double) * length));
	t_double *pitchcont 		= (t_double *)(malloc(sizeof(t_double) * fr));
	t_double *pitch_engy 		= (t_double *)(malloc(sizeof(t_double) * fr));
	//t_double* peaks = (t_double*)(malloc(sizeof(t_double)* fr));
	int *voiced 				= (int *)malloc(sizeof(int) * fr);

	lndmks->output_autocorr 	= (t_double *)malloc(sizeof(t_double) * 1.5*L-10); //L
	lndmks->output_clipped 		= (t_double *)malloc(sizeof(t_double) * N);
	t_double *f0 				= (t_double *)(malloc(sizeof(t_double) * MaxFR));
	t_double *f0_engy			= (t_double *)(malloc(sizeof(t_double) * MaxFR));
	t_double *ampl 				= (t_double *)(malloc(sizeof(t_double) * MaxFR));
	long int global 			= 0;
	short *data_samples			= NULL;
	lndmks->longitud = ceil( (double)sample_count * (1000.0 /(double) fs));
	lndmks->MAXPeaks = (t_int32) lndmks->longitud / 20.0;
	//printf (" longi %f maxpeaks %d",lndmks->longitud,lndmks->MAXPeaks);
	lndmks->event_on = (double*)(malloc(sizeof(double) * lndmks->MAXPeaks));
	lndmks->event_off= (double*)(malloc(sizeof(double) * lndmks->MAXPeaks));
	lndmks->final_on = (int*)malloc(sizeof(int) * lndmks->MAXPeaks);
	lndmks->final_off = (int*)malloc(sizeof(int) * lndmks->MAXPeaks);
	lndmks->final_type = (int*)malloc(sizeof(int) * lndmks->MAXPeaks);
	lndmks->final_strength= (double*)(malloc(sizeof(double) * lndmks->MAXPeaks));

	lndmks->strength_on = (double*)(malloc(sizeof(double) * lndmks->MAXPeaks));
	lndmks->strength_off = (double*)(malloc(sizeof(double) * lndmks->MAXPeaks));
	lndmks->engy = (double*)(malloc(sizeof(double) * lndmks->MAXPeaks));
	
	lndmks->input_filtered = (double*)(malloc(sizeof(double)*sample_count));
	lndmks->input_resampled = (double*)(malloc(sizeof(double)*sample_count/4));
	lndmks->filter_b = (double*)(malloc(sizeof(double)*400));
	//double D_ik;
	lndmks->on = (double*)(malloc(sizeof(double) * lndmks->longitud));
	lndmks->off = (double*)(malloc(sizeof(double) * lndmks->longitud));
	lndmks->energy_lndm = (double*)(malloc(sizeof(double) * lndmks->longitud));
	lndmks->resample_filter = (double*)(malloc(sizeof(double) * 81));
	lndmks->ws = 20 * fs / 1000;
	lndmks->wm = 20;
	lndmks->num=0;	
	
	lndmks->hilbert_transf = (double*)malloc(sizeof(double) * sample_count);
	lndmks->f = (double*)malloc(sizeof(double) *2 *chunk_size);
	lndmks->x = (double*)malloc(sizeof(double) *2 *chunk_size);


	//current		= fopen("data_samples.bin","wb");
	//refer		= fopen("pcm_data.bin","wb");
	/*
	filePtr = fopen(file_parameters -> bin_file, "rb"); 
	//int counter = 0;
	if (filePtr == NULL)
	{
		perror("Unable to open filter file");
		return;
	}
	get_cnt = fread(filter, sizeof(t_double), 466, filePtr);
	*/
	//get_cnt = fread(data->samples, sizeof(t_int16), real_length, filePtr_wav);

		
		int position = 0;
		while (position+length <= sample_count)
		{
			//printf("state %d ", state);

			if (lndmks->state == 0 || lndmks->state == 1)
			{
				///// wav reading
				//get_cnt = fread(data->samples, sizeof(t_int16), real_length, filePtr_wav);
				//////////// Filtering
				data_samples = &shared_params->pcm_data[global];
				filter_input_L(length, input, real_length, data_samples);

				//////////// F0 calc
				//position += real_length - 880;
				position += real_length - 2*L;
				//printf("to -> %d\twe -> %d\n",data->samples[global],data_samples[global]);
				calc_f0_L(input, L, fr, pitchcont, voiced, lndmks,fs,pitch_engy);

				lndmks->frame_count += fr;
				//fseek(filePtr_wav, -1760, SEEK_CUR);
				//global += 1760;
				global += length - 2*L;// 1760;				
			}

			lndmks->state = check_f0_L(voiced, pitchcont, fr, f0, L, data_samples, ampl, lndmks,fs,pitch_engy,f0_engy);
			//printf("\nstate = %d",lndmks->state);
			if (lndmks->state == 0 || lndmks->state == 1)
				continue;
			
			if (lndmks->state == 2)
				lndmks->state = 2;
			else if (lndmks->state == 3)
				lndmks->state = 1;

			//// csv file report
			save_voiced_landmarks(fr, lndmks,f0_engy,f0);
		}
		//save_lastpartofreport(js_params,p_context);

		calc_consonant_lndmarks(shared_params,lndmks,file_parameters,file_filterbank,file_resample_filter);
		calc_dim_fractal(lndmks,file_parameters,shared_params);
		#if RESEARCH
		save_landmarks_csv(lndmks,shared_params->number_of_frames,output_filename,summary_output_filename);
		#endif
		//////////// Memory Liberation
		free(input);
		free(pitchcont);
		free(pitch_engy);
		free(voiced);
		free(lndmks->output_autocorr);
		free(lndmks->output_clipped);
		free(f0);
		free(f0_engy);
		free(ampl);
		free(lndmks->event_on);
		free(lndmks->event_off);
		free(lndmks->final_on);
		free(lndmks->final_off);
		free(lndmks->final_type);
		free(lndmks->final_strength);
		free(lndmks->strength_on);
		free(lndmks->strength_off);
		free(lndmks->engy);
		free(lndmks->input_filtered);
		free(lndmks->input_resampled);
		free(lndmks->on);
		free(lndmks->off);
		free(lndmks->energy_lndm);
		free(lndmks->resample_filter);
		free(lndmks->hilbert_transf);
		free(lndmks->f);
		free(lndmks->x);
		free(lndmks->filter_b);
		fclose(file_filterbank);
		fclose(file_resample_filter);
		fprintf(stderr,"	done  : landmarks processing\n" );
		return E_SUCCESS;	
}


void smooth(double* source,double* target, int length, int width ){
	int limit = width/2;
	for(int i = 0;i<length;i++){
		target[i]=0;
		if( i>=limit && i<length-limit)
			for(int j=i-limit;j<i+limit;j++)
				target[i] += source[j];
		target[i]/=width;	
		//printf("%f ",target[i]);
	}
}

void calc_consonant_lndmarks(s_shared_params * shared_params, s_landmarks *lndmks,s_fileinfo *file_parameters,FILE* file_filterbank,FILE* file_resample_filter)
{
	
	int i,j,k,n,n3,m;
	int get_cnt = 0;
	int on_peaks=0,off_peaks=0;
	double D_ik;
	int fs = shared_params->org_wave_params.sample_rate;
	int sample_count = shared_params->org_wave_params.sample_count;
	//fprintf(stderr,"STARTING ...");

	for ( n = 0; n < lndmks->longitud; n++) {
			lndmks->on[n] = 0;
			lndmks->off[n] = 0;
			lndmks->energy_lndm[n] = 0;
		}
	
	// Resampling
	int SampleCount =  sample_count;
	if ( fs==44100 || fs==48000 ){
		//fprintf(stderr,"Resampling ...");
		SampleCount = SampleCount/4;
		fs = 11025;
		lndmks->ws = 20 * fs / 1000;
		get_cnt = fread(lndmks->resample_filter, sizeof(double), 81, file_resample_filter);
		for (i=0;i<SampleCount;i++){
			lndmks->input_resampled[i] = 0;
			for (j=0;j<81 && 4*i+j < sample_count;j++)
				lndmks->input_resampled[i] += (double)shared_params->pcm_data[4*i+j]/32767 * lndmks->resample_filter[j];
		}
	}
	
	int filter_size=400;
	for ( k = 0; k < N_filters; k++) {  // Channels 
	    
		//printf(" filter size = %d ",filter_size);
		get_cnt = fread(lndmks->filter_b, sizeof(double), filter_size, file_filterbank);
		
		if(fs == 11025)
		for ( i = 0; i < SampleCount; i++) {
			lndmks->input_filtered[i] = 0;
           	    for ( j = 0; j < filter_size && i + j < SampleCount; j++)
					if (fabs(lndmks->filter_b[j])>0.0001)
						lndmks->input_filtered[i] += lndmks->input_resampled[i+j]* lndmks->filter_b[j];				
		    }
		else
		for ( i = 0; i < SampleCount; i++) {
			lndmks->input_filtered[i] = 0;
           	    for ( j = 0; j < filter_size && i + j < SampleCount; j++)
					if (fabs(lndmks->filter_b[j])>0.0001)
						lndmks->input_filtered[i] += (double)shared_params->pcm_data[i+j]/3276* lndmks->filter_b[j];				
		    }
			

			Hilbert(lndmks->input_filtered, lndmks,SampleCount);
			
			for ( n = lndmks->wm; n + lndmks->wm < lndmks->longitud; n++) {
				double comon = 0;
				double restando = 0;
				for ( m = 0; m < lndmks->ws; m++) {
					comon += lndmks->hilbert_transf[n*(fs/1000) + m];
					restando += lndmks->hilbert_transf[n*(fs/1000) + m - lndmks->ws];
				}
				D_ik = 20 * log(comon) - 20 * log(restando);
				lndmks->energy_lndm[n] += comon + restando;
				if (D_ik > 0)
					lndmks->on[n] += D_ik;
				else
					lndmks->off[n] -= D_ik;
			}
		

	}// filter bands
	

		for ( n = 0; n < lndmks->longitud; n++) {
			lndmks->on[n] /= N_filters;
			lndmks->off[n] /= N_filters;
			lndmks->energy_lndm[n] /= N_filters;
		}

		

		int local_num = 0;
		for ( n = 0; n < lndmks->MAXPeaks; n++) {
			lndmks->event_on[n] = 0;
			lndmks->event_off[n] = 0;
			//lndmks->final_on[n] = 0;
			//lndmks->final_off[n] = 0;
		}

		double maxx = 0;
		int flag = 1;
		for ( n = 0; n < lndmks->longitud; n++) {
			if (lndmks->on[n] < 3)
				flag = 1;
			if (lndmks->on[n] > maxx)
				maxx = lndmks->on[n];
			else if (lndmks->on[n] < maxx && maxx>4.7 && flag==1) { //&& flag
				//printf("%d ", n);
				if (local_num<lndmks->MAXPeaks){
					lndmks->event_on[local_num] = n;
					lndmks->strength_on[local_num] = maxx;
					lndmks->engy[local_num] = lndmks->energy_lndm[n]*44100/fs;
					local_num++;
				}
				else {
					printf("\nMax num of peaks achieved");
				}
				
				maxx = 0;
				flag = 0;
			}
		}
		//printf("longitud %d\n ",longitud);
		maxx = 0;
		flag = 1;
		on_peaks=local_num;
		local_num = 0;
		for ( n = 0; n < lndmks->longitud; n++) {
			if (lndmks->off[n] < 0.1)
				flag = 1;
			if (lndmks->off[n] > maxx)
				maxx = lndmks->off[n];
			else if (lndmks->off[n] < maxx && maxx>5.15 && flag==1) {
				//printf("%d ", n);
				if (local_num<lndmks->MAXPeaks){
					lndmks->event_off[local_num] = n;
					lndmks->strength_off[local_num] = maxx;
					local_num++;
					}
				else {
					printf("\nMax num of peaks achieved");
				}
				
				maxx = 0;
				flag = 0;
			}
		}

		off_peaks=local_num;

		flag = 0;
		
		//printf("\n on peaks %d  off peaks %d ",on_peaks,off_peaks);
		
		//num = 0;
		int past_on = -100;
		int past_off = -100;
		lndmks->max_strength=0;
		for ( n = 0; n < on_peaks; n++) {
			int ev_on = lndmks->event_on[n];
			if (ev_on < past_on + 50 ||ev_on<past_off)
				continue;

			if (ev_on == 0 && n > 0)
				break;
			int n2 = 0;
			while (lndmks->event_off[n2] < ev_on && n2<off_peaks-1) {
				n2++;
			}
			if (n2 >= off_peaks-1)
				break;
			int ev_off = lndmks->event_off[n2];
			if (ev_off == 0)
				break;
			//printf("\nposible landmark %d %d\n",ev_on,ev_off);
			

			for ( n3 = 0; n3 < lndmks->num; n3++) {
				//printf(" %d ",lndmks->final_on[n3] );
				if (ev_off < lndmks->final_on[n3]){ //+10
					
					if (n3 > 0){
						if (ev_on > lndmks->final_off[n3 - 1] - 10) 
						    if (ev_off > ev_on && ev_off < ev_on + 50 && lndmks->engy[n]>0.3){
							//printf("C+ %d C- %d strength %f engy %f\n", ev_on, ev_off,lndmks->strength_on[n] + lndmks->strength_off[n2], lndmks->engy[n]);
							lndmks->final_type[lndmks->num] = 1;
							lndmks->final_on[lndmks->num] = ev_on;
							lndmks->final_off[lndmks->num] = ev_off;
							lndmks->final_strength[lndmks->num] = lndmks->strength_on[n];// + lndmks->strength_off[n2]; 
							past_on = ev_on;
							past_off = ev_off;
							if(lndmks->final_strength[lndmks->num]>lndmks->max_strength)
								lndmks->max_strength=lndmks->final_strength[lndmks->num];
							lndmks->num++;
							
							break;
						}
					}
					else
						if (ev_off > ev_on && ev_off < ev_on + 50 && lndmks->engy[n]>0.3) {
							//printf("C+ %d C- %d strength %f engy %f\n", ev_on, ev_off, lndmks->strength_on[n] + lndmks->strength_off[n2],lndmks->engy[n]);
							lndmks->final_type[lndmks->num] = 1;
							lndmks->final_on[lndmks->num] = ev_on;
							lndmks->final_off[lndmks->num] = ev_off;
							lndmks->final_strength[lndmks->num] = lndmks->strength_on[n];// + lndmks->strength_off[n2]; 
							past_on = ev_on;
							past_off = ev_off;
							if(lndmks->final_strength[lndmks->num]>lndmks->max_strength)
								lndmks->max_strength=lndmks->final_strength[lndmks->num];
							lndmks->num++;
							break;
						}
				}
				
				else if (ev_on > lndmks->final_on[n3] + 50 && ev_off < lndmks->final_off[n3] - 50 
					&& ev_off<ev_on +100 && lndmks->engy[n]>0.3 && lndmks->final_type[n3]==0) {
					//printf("S+ %d S- %d strength %f engy %f\n", ev_on, ev_off,lndmks->strength_on[n] + lndmks->strength_off[n2],lndmks->engy[n]);
					lndmks->final_type[lndmks->num] = 2;
					lndmks->final_on[lndmks->num] = ev_on;
					lndmks->final_off[lndmks->num] = ev_off;
					lndmks->final_strength[lndmks->num] = lndmks->strength_on[n];// + lndmks->strength_off[n2]; 
					past_on = ev_on;
					past_off = ev_off;
					if(lndmks->final_strength[lndmks->num]>lndmks->max_strength)
								lndmks->max_strength=lndmks->final_strength[lndmks->num];
					lndmks->num++;
					break;
				}
			}
		
		}
		//printf("max_strength %f " ,lndmks->max_strength);
		
}

void save_voiced_landmarks( int fr,s_landmarks *lndmks,double* f0_engy,double* pitchcont)
{
	int i;
	double strength=0;
	//printf(" SAVE %d %d ", lndmks->num , lndmks->MAXPeaks);
	
	lndmks->frame = lndmks->frame_past + lndmks->f0_end;
	if (lndmks->num < lndmks->MAXPeaks){
		//printf("v+ %d  v- %d\n",(lndmks->frame_count - lndmks->f0_end)*10,lndmks->frame_count*10);
		for(i=0;i<lndmks->f0_end;i++)
			strength+=f0_engy[i];
		strength = strength / (lndmks->f0_end*250);

		lndmks->final_on[lndmks->num] = (lndmks->frame_count - lndmks->f0_end-fr+lndmks->past_position)*10;
		lndmks->final_off[lndmks->num] = (lndmks->frame_count-fr+lndmks->past_position)*10;
		lndmks->final_type[lndmks->num] = 0;
		lndmks->final_strength[lndmks->num] = strength;
		//printf("%d v+ %d  v- %d\n",lndmks->num,lndmks->final_on[lndmks->num],lndmks->final_off[lndmks->num]);
		lndmks->num++;
		
		//p+
		int flag=0;
		
		double ratio = pitchcont[1] / pitchcont[0];
		
		if(ratio<1.1 && ratio>0.9){
			lndmks->final_on[lndmks->num] = (lndmks->frame_count - lndmks->f0_end-fr+lndmks->past_position)*10;
			lndmks->final_type[lndmks->num] = 3;
			flag=1;
			for (i=2;i<lndmks->f0_end;i++){
				ratio = pitchcont[i] / pitchcont[i-1];
				if((ratio>1.1 || ratio<0.9) && flag==1){
					lndmks->final_off[lndmks->num] = (i + lndmks->frame_count- lndmks->f0_end-fr+lndmks->past_position)*10;
					lndmks->num++;
					flag=0;
				}
				if((ratio<1.1 && ratio>0.9) && flag==0){
					lndmks->final_on[lndmks->num] = (i + lndmks->frame_count - lndmks->f0_end-fr+lndmks->past_position)*10;
					lndmks->final_type[lndmks->num] = 3;
					flag=1;
				}
			}
			if(flag==1){
				lndmks->final_off[lndmks->num] = (lndmks->frame_count-fr+lndmks->past_position)*10;
				lndmks->num++;
			}
		}
		else 
			for (i=2;i<lndmks->f0_end;i++){
				ratio = pitchcont[i] / pitchcont[i-1];
				if(ratio<1.1 && ratio>0.9 && flag==0){
					lndmks->final_on[lndmks->num] = (i+lndmks->frame_count - lndmks->f0_end-fr+lndmks->past_position)*10;
					lndmks->final_type[lndmks->num] = 3;
					flag=1;
				}
				if((ratio>1.1 || ratio<0.9) && flag==1){
					lndmks->final_off[lndmks->num] = (i + lndmks->frame_count- lndmks->f0_end-fr+lndmks->past_position)*10;
					lndmks->num++;
					flag=0;
				}
			}
			if(flag==1){
				lndmks->final_off[lndmks->num] = (lndmks->frame_count-fr+lndmks->past_position)*10;
				lndmks->num++;
			}
	}
		
	lndmks->frame_past = lndmks->frame;
	lndmks->frame_count_past = lndmks->frame_count;
}

void calc_dim_fractal(s_landmarks *lndmks,s_fileinfo *file_parameters, s_shared_params * shared_params){
	int nn = 32, p=5;
	int sample_rate = shared_params->org_wave_params.sample_rate;
	int sample_count = shared_params->org_wave_params.sample_count;
	if(sample_rate ==16000)
	{nn=16;p=4;}
	else if(sample_rate ==8000)
	{nn=8;p=3;}
	
	double* scale = (double*)malloc(sizeof(double)*p);
	double* count = (double*)malloc(sizeof(double)*p);
	double* dimf1 = (double*)malloc(sizeof(double)*lndmks->longitud);
	double* dimf2 = (double*)malloc(sizeof(double)*lndmks->longitud);
	
	for(int k=0;k<lndmks->longitud;k++){
		int xc = k*(sample_rate/1000);
		short* x = &shared_params->pcm_data[k*(sample_rate/1000)]; 
		if (k == lndmks->longitud-1)
			x = &shared_params->pcm_data[k*(sample_rate/1000)-1]; 
		
		for(int i=0;i<p;i++){
			double sf = pow(2,i+1);
			int n=nn/sf;
			int n_cajas=0;
			for(int j=0;j<sf;j++){
				int start = j*n;
				int end = (j+1)*n;
				if( xc + end  >= sample_count ) break;
				double mayor = my_max(x,start,end);
				double menor = my_min(x,start,end);
				n_cajas = n_cajas+(mayor-menor);
			}
			scale[i] = sf;
			count[i] = n_cajas*sf;
		}
		dimf1[k] = pendiente(scale,count,p);
	}
	smooth(dimf1,dimf2,lndmks->longitud,30);
	
	int start=0,end=0,flag=0;
	for (int i=0;i< lndmks->longitud;i++){
		if (dimf2[i]>1.62 && dimf2[i]< 1.68 && flag ==0){
			start=i;
			flag=1;
		}
		if ((dimf2[i]<1.62 || dimf2[i]>1.67) && flag ==1){
			end=i;
			flag=0;
			if(end-start > 60 ){
				lndmks->final_on[lndmks->num] = (start);
				lndmks->final_off[lndmks->num] = (end);
				lndmks->final_strength[lndmks->num] = 1;
				lndmks->final_type[lndmks->num] = 4;
				lndmks->num++;
			}
		}
	}
	if(flag==1){
		end=lndmks->longitud;
		flag=0;
		lndmks->final_on[lndmks->num] = (start);
		lndmks->final_off[lndmks->num] = (end);
		lndmks->final_strength[lndmks->num] = 1;
		lndmks->final_type[lndmks->num] = 4;
		lndmks->num++;
	}
	
	//printf(" pow %f",pow(2,10) );
	free(scale);
	free(count);
	free(dimf1);
	free(dimf2);
}

double pendiente(double* x,double* y,int N){
	double sumx=0,sumy=0,sumxy=0,sumx2=0;
	for(int i=0;i<N;i++){
		if (y[i]==0)
			return 0.0;
		sumx+=log(x[i]);
		sumy+=log(y[i]);
		sumxy+=log(x[i])*log(y[i]);
		sumx2+=log(x[i])*log(x[i]);
	}
	double m = (N*sumxy-sumx*sumy) / (N*sumx2-sumx*sumx);
	return m;
}

double my_max(short* x,int start,int end) {
	double m = 0;
	for(int i=start;i<=end;i++)
		if(x[i]>m)
			m=x[i];
	return m;
}

double my_min(short* x,int start,int end) {
	double m = 35000;
	for(int i=start;i<=end;i++)
		if(x[i]<m)
			m=x[i];
	return m;
}

void __attribute__((optimize("O0"))) save_landmarks_csv(s_landmarks *lndmks,int number_of_frames ,char * lmks,char * summary)
{
	int i;
	int nv=0,nb=0,ns=0,ng=0,np=0,nf=0;
	
	for(i=0;i<lndmks->num;i++)
	{
		if (lndmks->final_type[i]==0 && lndmks->final_strength[i]>0)
			nv++;
		if (lndmks->final_type[i]==0 && lndmks->final_strength[i]>.005)
			ng++;
		if (lndmks->final_type[i]==1)
			nb++;
		if (lndmks->final_type[i]==2)
			ns++;
		if (lndmks->final_type[i]==4)
			nf++;
		if (lndmks->final_type[i]==3)
			np++;	
	}
	
	/*if (!nv&&!nb&&!ns&&!ng&&!np&&!nf)
	{
		fprintf(stderr,"\terror : no landmarks found in file, exiting landmarks process\n");
		return;
	}*/
	FILE* filePtr_csv = fopen(lmks, "w");
	FILE* filePtr = fopen(summary,"w");
	fprintf(filePtr,"lmk_time_duration_inms,lmk_v_onset_count,lmk_v_offset_count,lmk_v_bigram_count");
	fprintf(filePtr,",lmk_b_onset_count,lmk_b_offset_count,lmk_b_bigram_count");
	fprintf(filePtr,",lmk_s_onset_count,lmk_s_offset_count,lmk_s_bigram_count");
	fprintf(filePtr,",lmk_g_onset_count,lmk_g_offset_count,lmk_g_bigram_count");
	fprintf(filePtr,",lmk_p_onset_count,lmk_p_offset_count,lmk_p_bigram_count");
	fprintf(filePtr,",lmk_f_onset_count,lmk_f_offset_count,lmk_f_bigram_count\n");
	fprintf(filePtr,"%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d\n",number_of_frames*10,nv,nv,nv, nb,nb,nb,ns,ns,ns,ng,ng,ng,np,np,np,nf,nf,nf);
	
	fprintf(filePtr_csv,"lmk_type,lmk_onset,lmk_offset,lmk_strength,");
	fprintf(filePtr_csv,"lmk_bigram_deltatime\n");

	lndmks->lmk_op.lmk_onset_ofset[0] = number_of_frames*10;
	lndmks->lmk_op.lmk_onset_ofset[1] = nv;
	lndmks->lmk_op.lmk_onset_ofset[2] = nb;
	lndmks->lmk_op.lmk_onset_ofset[3] = ns;
	lndmks->lmk_op.lmk_onset_ofset[4] = ng;
	lndmks->lmk_op.lmk_onset_ofset[5] = np;
	lndmks->lmk_op.lmk_onset_ofset[6] = nf;

	for(i=0;i<lndmks->num;i++)
	{
		if (lndmks->final_type[i]==0 && lndmks->final_strength[i]>.005)
		{
			fprintf(filePtr_csv,"g,");
			fprintf(filePtr_csv,"%lf,%lf,%lf,",(double)lndmks->final_on[i]/1000,(double)lndmks->final_off[i]/1000,1.0);
			fprintf(filePtr_csv,"%lf\n",(double)(lndmks->final_off[i]-lndmks->final_on[i])/1000);
			lndmks->lmk_op.g_lmk[lndmks->lmk_op.idx_g++] = (float)(lndmks->final_off[i]-lndmks->final_on[i])/1000;
			ng++;
		}
	}
	//fclose(filePtr_csv);
	volatile int wr_flg = 0;
	for(i=0;i<lndmks->num;i++)
	{
		if (lndmks->final_type[i]==0 && lndmks->final_strength[i]>0)
		{
			fprintf(filePtr_csv,"v,");nv++;
			wr_flg = 1;
		}
		if (lndmks->final_type[i]==1 && lndmks->final_strength[i]>0)
		{
			fprintf(filePtr_csv,"b,");nb++;
			wr_flg = 1;
		}
		if (lndmks->final_type[i]==2 && lndmks->final_strength[i]>0 )
		{
			fprintf(filePtr_csv,"s,");ns++;
			wr_flg = 1;
		}
		if (lndmks->final_type[i]==4 && lndmks->final_strength[i]>0 )
		{
			fprintf(filePtr_csv,"f,");nf++;
			wr_flg = 1;
		}
		if (lndmks->final_type[i]==3 && lndmks->final_strength[i]>0)
		{
			fprintf(filePtr_csv,"p,");
			lndmks->final_strength[i]=1;
			wr_flg = 1;
			np++;
		}

		if (lndmks->final_type[i]==1 || lndmks->final_type[i]==2 )
			lndmks->final_strength[i] /= (lndmks->max_strength*.7);
		if(lndmks->final_strength[i] > 1)
			lndmks->final_strength[i]=1;

		if(lndmks->final_strength[i]>0 && wr_flg )//&& (double)lndmks->final_on[i]/1000>0 && (double)lndmks->final_off[i]/1000)
		{
			wr_flg = 0;
			fprintf(filePtr_csv,"%lf,%lf,%lf,",(double)lndmks->final_on[i]/1000,(double)lndmks->final_off[i]/1000,lndmks->final_strength[i] );
			fprintf(filePtr_csv,"%lf\n",(double)(lndmks->final_off[i]-lndmks->final_on[i])/1000);
			fflush(filePtr_csv);
			
			float data = (float)(lndmks->final_off[i]-lndmks->final_on[i])/1000;
			if (lndmks->final_type[i]==0)
			{
				lndmks->lmk_op.v_lmk[lndmks->lmk_op.idx_v++] = data;
			}
			if (lndmks->final_type[i]==1)
			{
				lndmks->lmk_op.b_lmk[lndmks->lmk_op.idx_b++] = data;
			}
			if (lndmks->final_type[i]==2)
			{
				lndmks->lmk_op.s_lmk[lndmks->lmk_op.idx_s++] = data;
			}
			if (lndmks->final_type[i]==4)
			{
				lndmks->lmk_op.f_lmk[lndmks->lmk_op.idx_f++] = data;
			}
			if (lndmks->final_type[i]==3)
			{
				lndmks->lmk_op.p_lmk[lndmks->lmk_op.idx_p++] = data;
			}		
		}
	}
	fclose(filePtr_csv);
	fclose(filePtr);
	//printf("\n%d landmarks",lndmks->num);
}

void filter_input_L(int length, t_double *input, int size, short *data_samples)
{
	int i;
	for (i = 0; i < length; i++)
	{
		input[i] = (t_double)data_samples[i] / 32767;
/*
		for (j = 0; j < 466 && i * 4 + j < size; j++)
		{
			input[i] += (t_double)data_samples[i * 4 + j] / 32767 * filter[j];
		}
*/
	}
}

void calc_f0_L(t_double *input, int L, int fr, t_double *pitchcont, int *voiced, s_landmarks *lndmks, int fs,double* pitch_engy)
{
	int i;

	for (i = 0; i < fr; i++)
	{
		int index = i * L;
		t_double cl = 0;
		//for (int j = 0; j < N; j++, index++)
		cl = calcC_L(input, index,L);
		clipping_L(input, index, cl, lndmks,3*L);
		autocorr_L(lndmks->output_clipped, lndmks,L);
		t_double en = power_L(lndmks->output_clipped,L);
		t_double maxi = 0;
		int mark = 0;
		int j = 0;
		for (j = 0; j < (1.5*L-10); j++) 	//160
			if (lndmks->output_autocorr[j] > maxi)
			{
				maxi = lndmks->output_autocorr[j];
				mark = j;
			}
		t_double f0 = fs / (t_double)(mark + 20);
		pitchcont[i] = f0;
		pitch_engy[i] = en * 44100/fs;

		if (maxi <= .75 * en)
			voiced[i] = 0;
		else
			voiced[i] = 1;
		//printf("%d ", voiced[i]);
	}
}

int check_f0_L(int *voiced, t_double *pitchcont, int fr, t_double *f0, int L, t_int16 *data_samples, t_double *ampl,s_landmarks *lndmks,int fs,double* pitch_engy,double* f0_engy)
{
	int i = lndmks->past_position;
	lndmks->past_position = 0;
	while (i < fr)
	{
		//printf("v %d ", voiced[i]);
		if (lndmks->state == 0 || lndmks->state == 2)
		{
			while (i < fr)
			{
				if (voiced[i] == 1)
					break;
				i++;
			}
			if (i == fr)
			{
				lndmks->past_position = 0;
				return voiced[i - 1];
			}
			lndmks->f0_start = i;
			lndmks->cuenta = 0;
			lndmks->cuenta2 = 0;
		}
		while (i < fr)
		{
			if (voiced[i] == 0)
				break;
			f0[lndmks->cuenta] = pitchcont[i];
			f0_engy[lndmks->cuenta] = pitch_engy[i];
			//ampl[js_params->cuenta] = peak(data_samples, i * L, (int)(fs / f0[js_params->cuenta]));
			lndmks->cuenta++;
			lndmks->cuenta2++;
			i++;
			if (lndmks->cuenta >= 100)
			{
				adjust_f0_L(100, f0);

				lndmks->state = 3;
				lndmks->f0_end = lndmks->cuenta;
				lndmks->cuenta = 0;
				lndmks->past_position = i;
				return 3;
			}
		}

		if (i < fr && (lndmks->cuenta2 < 5 || lndmks->cuenta == 0))
		{
			lndmks->cuenta = 0;
			lndmks->cuenta2 = 0;
			lndmks->state = 0;
			continue;
		}
		else if (i < fr && lndmks->cuenta2 >= 5 && lndmks->cuenta > 0)
		{
			adjust_f0_L(lndmks->cuenta2, f0);
			lndmks->state = 2;
			lndmks->f0_end = lndmks->cuenta;
			lndmks->cuenta = 0;
			lndmks->cuenta2 = 0;
			lndmks->past_position = i;
			return 2;
		}
		else if (i == fr)
		{
			lndmks->past_position = 0;
			return voiced[i - 1];
		}
	}
	lndmks->past_position = 0;
	return E_SUCCESS;
}

t_int32 adjust_f0_L(int cuenta, t_double *f0)
{
	int k, h;
	for (k = 0; k < cuenta && k < 100; k++)
	{
		int npar = 0;
		int ndoble = 0;
		int nmit = 0;
		int ntrip = 0;
		int n23 = 0;
		int n32 = 0;
		for (h = 0; h < cuenta; h++)
		{
			if (k != h)
			{
				t_double ratio = f0[h] / f0[k];
				if (ratio > 2.75)
					ntrip++;
				else if (ratio > 1.75)
					ndoble++;
				else if (ratio > 1.3)
					n32++;
				else if (ratio > 0.8)
					npar++;
				else if (ratio > 0.6)
					n23++;
				else
					nmit++;
			}
		}

		if (ntrip > ndoble && ntrip > n32 && ntrip > npar && ntrip > n23 && ntrip > nmit)
			f0[k] = f0[k] * 3;
		else if (ndoble > ntrip && ndoble > n32 && ndoble > npar && ndoble > n23 && ndoble > nmit)
			f0[k] = f0[k] * 2;
		else if (n32 > ntrip && n32 > ndoble && n32 > npar && n32 > n23 && n32 > nmit)
			f0[k] = f0[k] * 3 / 2;
		else if (n23 > ndoble && n23 > n32 && n23 > npar && n23 > ntrip && n23 > nmit)
			f0[k] = f0[k] * 2 / 3;
		else if (nmit > ndoble && nmit > n32 && nmit > npar && nmit > n23 && nmit > ntrip)
			f0[k] = f0[k] / 2;
	}
	return E_SUCCESS;
}

t_double peak_L(t_int16 *audio, int index, int periodo)
{
	int i;
	int pico = 0;
	for (i = index; i < index + periodo; i++) // +10
	{
		if (abs(audio[i]) > pico)
			pico = abs(audio[i]);
	}
	return (t_double)pico / 32767;
}


t_int32 signo(t_double num)
{
	if (num < 0)
		return -1;
	else if (num > 0)
		return 1;
	else
		return E_SUCCESS;
}

t_double calcC_L(t_double *input, int index,int L)
{
	int i;
	int diffpast = signo(2 * input[index + 1] - input[index] - input[index + 2]);
	int diff;
	t_double min = 1;
	for (i = index + 2; i < index + L; i++)
	{
		diff = signo(2 * input[i] - input[i + 1] - input[i - 1]);
		if (diff != diffpast || diff == 0)
			if (fabs(input[i]) > 0.0001 && fabs(input[i]) < min)
				min = fabs(input[i]);
	}
	diffpast = signo(2 * input[index + 2*L] - input[index + 2*L-1] - input[index + 2*L+1]);
	for (i = index + 2*L+1; i < index + 3*L-1; i++)
	{
		diff = signo(2 * input[i] - input[i + 1] - input[i - 1]);
		if (diff != diffpast || diff == 0)
			if (fabs(input[i]) > 0.0001 && fabs(input[i]) < min)
				min = fabs(input[i]);
	}

	return min * 0.68;
}

int signo_L(t_double num)
{
	if (num < 0)
		return -1;
	else if (num > 0)
		return 1;
	else
		return E_SUCCESS;
}

void autocorr_L(t_double *signal, s_landmarks *lndmks,int L)
{
	int i, j;

	for (i = 0; i < 1.5*L-10; i++)  //160
	{
		lndmks->output_autocorr[i] = 0;
		for (j = 0; j < 1.5*L-10; j++)  //150
			lndmks->output_autocorr[i] += signal[i + j + 20] * signal[j];
	}
}

t_double power_L(t_double *signal,int L)
{
	int i;
	t_double p = 0;
	for (i = 0; i < 1.5*L-10; i++)
		p += signal[i] * signal[i];
	return p;
}

void clipping_L(t_double *input, int index, t_double cl, s_landmarks *lndmks,int N)
{
	int i;

	int j = 0;
	for (i = index; i < index + N; i++, j++)
	{
		if (fabs(input[i]) > 2 * cl)
			lndmks->output_clipped[j] = input[i];
		else
			lndmks->output_clipped[j] = 0;
	}
}

t_int32 Hilbert(double* input, s_landmarks* lndmks, int length) {
	int chunk_size = 2048;
	int frames = length / chunk_size;
	
	double real, imag;
	int i,fr,n;
	for ( fr = 0; fr < frames; fr++) {
		fftw(&input[fr*chunk_size], lndmks->f,chunk_size, 1, 0);

		lndmks->f[0] = 1;
		lndmks->f[1] = 1;
		lndmks->f[chunk_size] = 1;
		lndmks->f[chunk_size+1] = 1;
		for ( i = 1; i < chunk_size/2; i++) {
			lndmks->f[i * 2] *= 2;
			lndmks->f[i * 2 + 1] *= 2;
			lndmks->f[i * 2 + chunk_size] = 0;
			lndmks->f[i * 2 + chunk_size+1] = 0;
		}
		fftw(lndmks->f,lndmks->x, 2*chunk_size, -1, 1);
		for ( n = 0; n < chunk_size; n++) {
			real = /*input[fr * chunk_size+n] -*/ lndmks->x[2 * n + 1]/chunk_size;
			imag = lndmks->x[2 * n]/chunk_size;
			lndmks->hilbert_transf[fr * chunk_size + n] = sqrt(real*real + imag * imag);
		}
	}
	return E_SUCCESS;
	//return e;
	
}

t_int32 fftw(double* input,double* data, int length, int isign,int imag) 
{

	int nn = length;
	//double* data;
	int i;
	if (isign == 1 && imag == 0) {
		nn = length;
		//data = (double*)malloc(sizeof(double) * 2 * length);
		for ( i = 0; i<length; i++) {
			data[2 * i] = input[i];
			data[2 * i + 1] = 0;
		}
	}
	else {
		//data = (double*)malloc(sizeof(double) * length);
		nn = length / 2;
		for ( i = 0; i<length; i++) 
			data[i] = input[i];
	}

	unsigned long n, mmax, m, j, istep;
	double wtemp, wr, wpr, wpi, wi, theta;
	double tempr, tempi;

	// reverse-binary reindexing

	n = nn << 1;
	j = 1;
	for (i = 1; i<n; i += 2) {
		if (j>i) {
			tempr = data[j - 1]; data[j-1]= data[i - 1] ; data[i - 1] = tempr;
			tempr = data[j]; data[j] = data[i]; data[i] = tempr;
		}
		m = nn;
		while (m >= 2 && j>m) {
			j -= m;
			m >>= 1;
		}
		j += m;
	}

	mmax = 2;
	while (n>mmax) {
		istep = 2 * mmax;
		theta = (2 * SONDE_PI*isign / mmax);//////// -
		wtemp = sin(0.5*theta);
		wpr = -2.0*wtemp*wtemp;
		wpi = sin(theta);
		wr = 1.0;
		wi = 0.0;
		for (m = 1; m < mmax; m += 2) {
			for (i = m; i <= n; i += istep) {
				j = i + mmax;
				tempr = wr*data[j - 1] - wi*data[j];
				tempi = wr * data[j] + wi*data[j - 1];

				data[j - 1] = data[i - 1] - tempr;
				data[j] = data[i] - tempi;
				data[i - 1] += tempr;
				data[i] += tempi;
			}
			wtemp = wr;
			wr += wr*wpr - wi*wpi;
			wi += wi*wpr + wtemp*wpi;
		}
		mmax = istep;
	}
	return E_SUCCESS;
}


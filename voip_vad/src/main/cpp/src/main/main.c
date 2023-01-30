
#include <stdio.h>
#include <stdlib.h>
#include "sonde_sp_api.h"
#define __DEBUG 0

int main( int argc, char  **argv )
{
        int response = 0;

        if ( argc == 5 )
        {
                char * audio = argv[1];   // percentage threshold with all trimmed frames to active voice frames
                char * config = argv[2];   // all frames percentage to trimmed frames percentage difference ( in % )
                char * tconfig = argv[3];
                char * out_dir = argv[4];
                response = sp_elck( audio, config, out_dir );
                printf("\t%d\n", response);
        }
        else
        {
                fprintf(stderr,"\n\tInsufficient command line arguments, Please run sonde_sp_algorithm followed by\n\tbelow arguments separated by space \n\n");
                fprintf(stderr,"\t1. Audio file path \n");
                fprintf(stderr,"\t2. manifest file path\n");
                fprintf(stderr,"\t3. resources/ dir path \n");
                fprintf(stderr,"\t4. vad th1 \n");
                fprintf(stderr,"\t5. lmk th1\n");
                fprintf(stderr,"\t6. ahh th1 (v1)\n");
                fprintf(stderr,"\t7. ahh th2 (v5)\n");
                fprintf(stderr,"\t8. energy meter th1\n");
                fprintf(stderr,"\t9. energy meter th2\n\n");

		#if __DEBUG
                char * audio = "/home/samir/Desktop/all/wav/demo.wav";
                char * config = "./manifest.yaml";
                char * tconfig = "./thresholds.yaml";
                char * out_dir = "here/";
                float vad_th1 = atof("30");
                float vad_th2 = atof("30");
                float lmk_th1 = atof("0");
                float ahh_th1 = atof("1e-4");
                float ahh_th2 = atof("1");
                float em_th1 = atof("25");
                float em_th2 = atof("32");
                float threshold_array[] = { vad_th1, vad_th2, lmk_th1, ahh_th1, ahh_th2, em_th1, em_th2 };
                response = sp_elck( audio, config, tconfig, out_dir );
                printf("\t%s\n", response);
                #endif 
                return E_ARG_ERR;
        }
}

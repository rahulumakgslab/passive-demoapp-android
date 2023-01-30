/*
* @brief extraction of vad(voice activity detection) feature.
*
* @author Swapnil Warkar
*
*/

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <stdarg.h>
#include "sonde_spfe_function_def.h"


t_int32 compute_vad( s_paths_and_meta * paths_and_meta, s_fileinfo *file_parameters,s_user_config * user_config, s_vad * vad_params, s_shared_params * shared_params, s_features_config * features_config )
{
	fprint_log_string("\n	start : vad processing");
    vad_params->BlockSizeAlarm = shared_params->frame_samplerate_size;
    switch (user_config->BlockSize)
    {
    case 256:
        vad_params->blockdex = 0;
        break;
    case 512:
        vad_params->blockdex = 1;
        break;
    case 1024:
        vad_params->blockdex = 2;
        break;
    case 2048:
        vad_params->blockdex = 3;
        break;
    case 4096:
        vad_params->blockdex = 4;
        break;
    case 8192:
        vad_params->blockdex = 5;
        break;
    case 16384:
        vad_params->blockdex = 6;
        break;
    case 32768:
        vad_params->blockdex = 7;
        break;
    case 65536:
        vad_params->blockdex = 8;
        break;
    case 131072:
        vad_params->blockdex = 9;
        break;
    default:
        user_config->BlockSize = 32768;
        vad_params->blockdex = 7;
        break;
    }

    switch (user_config->DecimationOrder)
    {
    case 1:
        user_config->DecimDex = 0;
        break;
    case 2:
        user_config->DecimDex = 1;
        break;
    case 4:
        user_config->DecimDex = 2;
        break;
    case 8:
        user_config->DecimDex = 3;
        break;
    case 16:
        user_config->DecimDex = 4;
        break;
    case 32:
        user_config->DecimDex = 5;
        break;
    case 64:
        user_config->DecimDex = 6;
        break;
    case 128:
        user_config->DecimDex = 7;
        break;
    case 256:
        user_config->DecimDex = 8;
        break;
    case 512:
        user_config->DecimDex = 9;
        break;
    case 1024:
        user_config->DecimDex = 10;
        break;
    case 2048:
        user_config->DecimDex = 11;
        break;
    case 4096:
        user_config->DecimDex = 12;
        break;
    default:
        user_config->DecimDex = 1;
        break;
    }

    compute_filter_coefficient(user_config->FilterLength,vad_params,shared_params);
    process_envelope_burst_patt(shared_params, user_config, vad_params,file_parameters);

    if (UseLowPassFilter)
    {
        lp_filter_durations(vad_params);
    }
    else
    {
        no_filter(vad_params);
    }

    handle_general_duration_timing(vad_params, file_parameters);
    if( features_config->sonde_vad == 1 ) Output_csv(vad_params,shared_params,paths_and_meta, file_parameters);

    fprint_log_string("\n	done : vad processing");
    return E_SUCCESS;
}

 t_int32 compute_filter_coefficient(t_int32 FilterLength, s_vad * vad_params, s_shared_params * shared_params)
{
     t_double MeanVar = 0;
     t_int32 Jdex = 0;
    

    // COMPUTE COEFFICIENTS  FOR THE LOW PASS FILTER

    if (FilterLength % 2 == 0)
    {
        shared_params->FilterMidPoint = (t_int32)((t_double)FilterLength / 2);
    }
    else
    {
        shared_params->FilterMidPoint = (t_int32)((t_double)(FilterLength - 1) / 2 + 1);
    }

    MeanVar = 0;
    Jdex = (t_int32)(-shared_params->FilterMidPoint);

    while (Jdex < shared_params->FilterMidPoint + 1)
    {
        shared_params->FirCoef[Jdex + shared_params->FilterMidPoint] = 2 * shared_params->FilterMidPoint - fabs(Jdex);
        MeanVar += shared_params->FirCoef[Jdex + shared_params->FilterMidPoint];
        Jdex++;
    }

    MeanVar /= FilterLength;

    //second pass on coefficients for anti-podal properties
    Jdex = (t_int32)(-shared_params->FilterMidPoint);

    while (Jdex < shared_params->FilterMidPoint + 1)
    {
        shared_params->FirCoef[Jdex + shared_params->FilterMidPoint] /= FilterLength;
        Jdex++;
    }
    return E_SUCCESS;
}

t_int32 custom_envelope_detector_overlap(t_int32 Dex, t_int32 ShiftDex, t_float *pcm_buffer , s_fileinfo * file_parameters, s_vad * vad_params)
{
    t_int32 InitDex        = 0;
    t_double MsqRaw        = 0.0;
    t_double StandDevRaw   = 0.0;   

    t_int32 SeqDex         = 0;
    t_double MaxRMS        = 0.0;
    t_int32 DecDex         = 0;
    t_int32 LastMin        = 0;
    t_double MinRMS        = 0.0;
    t_int32 PeakBinCount   = 0;
    t_double ActiveThresh  = 1200;
    t_double DecBuf[31000] = {0.0};
    t_int32 tempval = 0;

    InitDex                = 0;
    DecDex                 = 0;
     t_int32 DeltaBin  = 0;
    PeakBinCount = 0;
     t_int32 InterValHist[10000];
     t_double IntegEnv = 0;
     t_double FiltEnv  = 0;
    MsqRaw                   = 0;
    StandDevRaw              = 0;
    MinRMS                   = 100000000000.0;
    MaxRMS                   = 0.0;
    tempval                  = vad_params->BlockSizeAlarm * (Dex + 1) + ShiftDex;

    SeqDex = Dex * vad_params->BlockSizeAlarm + ShiftDex;

    while (SeqDex < tempval)
    {
        if (tempval > (file_parameters -> actual_samples + VAD_PADDING2) )
                    break;
        MsqRaw += pow(pcm_buffer[SeqDex], 2);
        SeqDex++;
        
    }
    MsqRaw /= (t_double)(vad_params->BlockSizeAlarm - 1);
    StandDevRaw = sqrt(MsqRaw);
    ActiveThresh = 800.0 + StandDevRaw / 4.0;

    InitDex = Dex * vad_params->BlockSizeAlarm + ShiftDex;

    vad_params->duration_count = 0;
    SeqDex = InitDex;

    while (SeqDex < (vad_params->BlockSizeAlarm + InitDex))
    {
        //square signal
        if ( SeqDex > (file_parameters -> actual_samples + VAD_PADDING2)  )
                    break;
        DecBuf[DecDex] = (t_double)(sqrt(pow(pcm_buffer[SeqDex], 2)));

        if (DecBuf[DecDex] > ActiveThresh)
        {
            vad_params->duration_count++;
        }
        else
        {
            vad_params->duration_count--;
        }
        if (DecDex > FilterWidth)
        {
            IntegEnv -= DecBuf[DecDex - FilterWidth];
        }
        FiltEnv /= (t_double)FilterWidth;

        //determine local peak
        IntegEnv += DecBuf[DecDex];
        if (IntegEnv > MaxRMS)
        {
            MaxRMS = IntegEnv;
        }

        //determine local minimum
        if (IntegEnv < MinRMS)
        {
            MinRMS = IntegEnv;

            DeltaBin = DecDex - LastMin;
            if (DeltaBin > 0)
            {
                if (DeltaBin < 5000) //10000
                {
                    InterValHist[DeltaBin]++;
                }
                if (InterValHist[DeltaBin] > PeakBinCount)
                {
                    PeakBinCount = InterValHist[DeltaBin];
                    vad_params->peak_bin = DeltaBin;
                }
                LastMin = DecDex;
            }
        }
        DecDex++;
        SeqDex += DecimationOrderAlarm;
    }
    return E_SUCCESS;
    //printf("Function Exit : custom_envelope_detector_overlap\n");
}

 t_int32 process_envelope_burst_patt( s_shared_params * shared_params, s_user_config * user_config, s_vad * vad_params, s_fileinfo * file_parameters)
{

    t_double MaxFodDuration = 0;
    t_double MeanDuration = 0;
     t_double BurstLength = 0;
     t_double PauseLength = 0.0;
     t_int32 EventCount = 0;
     t_double FodDuration = 0;
     t_int32 PhaseDex = 0;
     t_double Interp = 0;
     t_int32 PreviousDurationCount = 0;
     t_int32 SubDex = 0;
     t_int32 MinLimit = 0;
     t_int32 MaxLimit = 0;
     t_int32 PhaseInterval = 0;
     t_int32 CumSamples = 0;

    MinLimit = (t_int32)((t_double)user_config->BlockSize / EnvDecimation);
    MaxLimit = (t_int32)((t_double)user_config->BlockSize / EnvDecimation * 0.75);
    PhaseInterval = (t_int32)((t_double)user_config->BlockSize / 4.0);
    MaxFodDuration = 0;
    MeanDuration = 0;
    vad_params->number_of_blocks = (t_int32)((t_double)(shared_params->SampleCount + VAD_PADDING2)  / vad_params->BlockSizeAlarm);
    t_double MinY = 2000.0;
    t_double MaxY = -2000.0;
    vad_params->duration_count = 0;
    vad_params->blockdex = 0;

    while (vad_params->blockdex < vad_params->number_of_blocks - 1)
    {
        PhaseDex = 0;
        while (PhaseDex < 4)
        {
            SubDex = vad_params->blockdex * 4 + PhaseDex;

            custom_envelope_detector_overlap(vad_params->blockdex, ((PhaseDex) * (PhaseInterval)), shared_params->eq_pcm,file_parameters, vad_params);

            CumSamples += vad_params->peak_bin * EnvDecimation;
            vad_params->DurationAr[SubDex] = vad_params->duration_count;

            if (vad_params->duration_count > MaxY)
            {
                MaxY = vad_params->duration_count;
            }
            if (vad_params->duration_count < MinY)
            {
                MinY = vad_params->duration_count;
            }

            MeanDuration += vad_params->duration_count;

            if (vad_params->blockdex > 0)
            {
                FodDuration = fabs(vad_params->DurationAr[SubDex] - vad_params->DurationAr[SubDex - 1]);
                if (FodDuration > MaxFodDuration)
                {
                    MaxFodDuration = FodDuration;
                }
            }
            Interp = ((t_double)(vad_params->duration_count + PreviousDurationCount)) / 2.0;

            if (Interp > 0)
            {
                if (PauseLength > 0)
                {
                    
                    
                    PauseLength = 0;
                    EventCount++;
                }
                BurstLength += Interp / MaxLimit;
            }
            else
            {
                if (BurstLength > 0)
                {
                    BurstLength = 0;
                    EventCount++;
                }
                PauseLength += fabs(Interp / (t_double)MinLimit);
            }

            PreviousDurationCount = vad_params->duration_count;
            PhaseDex++;
        }
        vad_params->blockdex++;
    }
    return E_SUCCESS;
}

 t_int32 lp_filter_durations( s_vad * vad_params )
{
    //printf("Function Entry : lp_filter_durations\n");
     t_double Filtsum = 0;
     t_int32 Wdex = 0;
     t_int32 HalfB = 0;
     t_int32 NormCount = 0;
     t_int32 blockdex = 0;

    if ((BwindowSize) % 2 == 0)
    {
        HalfB = (t_int32)((t_double)(BwindowSize) / 2);
    }
    else
    {
        HalfB = (t_int32)((t_double)(BwindowSize - 1) / 2);
    }

    while (blockdex < 4 * vad_params->number_of_blocks - 1)
    {
        Filtsum = 0;

        Wdex = (t_int32)(-HalfB);
        NormCount = 0;
        while (Wdex < HalfB + 1)
        {
            if (((blockdex + Wdex) > -1) && ((blockdex + Wdex) < (vad_params->number_of_blocks * 4 - 1)))
            {

                if (UseUniformCoefficients)
                {
                    Filtsum += vad_params->DurationAr[blockdex + Wdex];
                }
                else
                {
                    Filtsum += (2 * HalfB - fabs(Wdex)) * vad_params->DurationAr[blockdex + Wdex];
                }
                NormCount++;
            }
            Wdex++;
        }

        if (NormCount > 0)
        {
            vad_params->Duration_FiltAr[blockdex] = Filtsum / NormCount;
        }
        else
        {
            vad_params->Duration_FiltAr[blockdex] = 0;
        }

        blockdex++;
    }

    blockdex = 0;
    while (blockdex < 4 * vad_params->number_of_blocks - 1)
    {
        vad_params->DurationAr[blockdex] = vad_params->Duration_FiltAr[blockdex];
        blockdex++;
    }
    return E_SUCCESS;
    //printf("Function Exit : lp_filter_durations\n");
}

 t_int32 no_filter( s_vad * vad_params )
{
    //reassign duration values as filtered ones:
     t_int32 blockdex = 0;

    while (blockdex < 4 * vad_params->number_of_blocks - 1)
    {
        vad_params->Duration_FiltAr[blockdex] = vad_params->DurationAr[blockdex];
        blockdex++;
    }
    return E_SUCCESS;
}

 t_int32 handle_general_duration_timing( s_vad * vad_params, s_fileinfo * file_parameters )
{
    //printf("Function Entry : handle_general_duration_timing\n");
     t_double MaxD    = -500;
     t_double MinD    = 500;
    t_int32 blockdex = 0;
    file_parameters->LastOffsetflag = -1;
    file_parameters->LastOnsetflag = -1;

    while (blockdex < vad_params->number_of_blocks - 1)
    {
        if (vad_params->DurationAr[blockdex] > MaxD)
        {
            MaxD = vad_params->DurationAr[blockdex];
        }
        if (vad_params->DurationAr[blockdex] < MinD)
        {
            MinD = vad_params->DurationAr[blockdex];
        }
        blockdex++;
    }

     t_int32 Halfscale    = 0;
     t_int32 Midpoint     = 0;
     t_int32 Onsetblock   = -1;
     t_int32 Offsetblock  = -1;
     t_double SampleInterval = (t_double)((t_double)vad_params->BlockSizeAlarm / 4.0 / (t_double)file_parameters->WaveHeader.sampleRate);
    //int Idex = 0;
     t_int32 ReqPoints = 0;
     t_uint8 Pass = FALSE;

    file_parameters->SegmentStart[0]    = 0;
    file_parameters->SegmentEnd[0]      = 0;
    Halfscale = (t_int32)((MaxD - MinD) * 0.5);
    Midpoint = (t_int32)(Halfscale + MinD);
    blockdex = 0;

    while (blockdex < ((vad_params->number_of_blocks * 4) - 1))
    {
        ReqPoints = 0;

        //look for exceedance condition using fail condition
        Pass = TRUE;
        while (ReqPoints < PostFiltNumRequiredPoints)
        {
            if (vad_params->DurationAr[blockdex + ReqPoints * QualInterval] < Midpoint)
            {
                Pass = FALSE;
                break;
            }
            ReqPoints++;
        }

        if (vad_params->DurationAr[blockdex] > Midpoint && Pass && Onsetblock == -1)
        {
            Onsetblock = blockdex;
            file_parameters->LastOnsetflag = 0;
            file_parameters->SegmentStart[file_parameters->LastOnsetflag] = blockdex * SampleInterval;
        }
        else
        {
            if (vad_params->DurationAr[blockdex] > Midpoint && Pass)
            {
                if (file_parameters->LastOffsetflag == file_parameters->LastOnsetflag)
                {
                    file_parameters->LastOnsetflag++;
                    file_parameters->SegmentStart[file_parameters->LastOnsetflag] = blockdex * SampleInterval;
                }
            }
        }

        //Look for decedance condition using fail condition
        if (!Pass)
        {
            ReqPoints = 0;
            Pass = TRUE;
            while (ReqPoints < PostFiltNumRequiredPoints)
            {
                if (vad_params->DurationAr[blockdex + ReqPoints * QualInterval] > Midpoint)
                {
                    Pass = FALSE;
                    break;
                }
                ReqPoints++;
            }
        }
        else
        {
            Pass = FALSE;
        }

        if (vad_params->DurationAr[blockdex] < Midpoint && Pass && (file_parameters->LastOnsetflag > file_parameters->LastOffsetflag) && (blockdex - Offsetblock > MinSamplesDurationBurst))
        {
            Offsetblock = blockdex;

            file_parameters->SegmentEnd[file_parameters->LastOnsetflag] = blockdex * SampleInterval;
            file_parameters->SegmentType[file_parameters->LastOnsetflag] = 1;

            file_parameters->LastOffsetflag = file_parameters->LastOnsetflag;
        }
        blockdex++;
    }
    return E_SUCCESS;
    //printf("Function Exit : handle_general_duration_timing\n");
}


t_int32 equalize_gain(s_shared_params * shared_params)
{
    //printf("Function Entry : equalize_gain\n");
     t_double RMSsignal = 0;
     t_double CorrectionFactor = 0;
     t_int32 tInt = 0;
     t_int32 Jdex = 0;

    while (Jdex < ((t_double)shared_params->SampleCount + VAD_PADDING2) / 2)
    {
        RMSsignal += pow(shared_params->eq_pcm[Jdex], 2);
        Jdex++;
    }
    RMSsignal /= (t_double)(shared_params->SampleCount + VAD_PADDING2)  / 2;

    RMSsignal = sqrt(RMSsignal);
    CorrectionFactor = 3000 / RMSsignal;
    Jdex = 0;

    if (RMSsignal == 0)
    {
        fprintf(stderr, "\n\t Error : invalid input file, Zero RMS amplitude present!\n");
        exit(E_ZERO_RMS);
    }

    while (Jdex < (shared_params->SampleCount + VAD_PADDING2) )
    {
        tInt = lrint(CorrectionFactor * shared_params->eq_pcm[Jdex]);
        if (tInt > 32767)
        {
            tInt = 32767;
        }
        else
        {
            if (tInt < -32767)
            {
                tInt = -32767;
            }
        }
        shared_params->eq_pcm[Jdex] = (t_int16)tInt;

        if (shared_params->eq_pcm[Jdex] > 3000)
        {
            Jdex += 0;
        }

        Jdex++;
    }

    return E_SUCCESS;
}

 int vad_get_reduced_frame_count( t_int32 *  va_array, t_int32 size )
{
    t_int32 front = 0;
    t_int32 end = 0;
    for( int f = 0; f < size; f++ )
    {
        if( va_array[f] == 1 )
        {
            break;
        }
        else
        {
            front++;
        }
    }
    for( int e = size - 1; e >= 0; e-- )
    {
        if( va_array[e] == 1 )
        {
            break;
        }
        else
        {
            end++;
        }
    }
    return abs( size - (front + end) );
}

 t_int32 Output_csv(s_vad * vad_params, s_shared_params * shared_params, s_paths_and_meta * paths_and_meta  , s_fileinfo *file_parameters)
{
    //printf("Function Entry : Output_csv\n");
     t_int8 str[20];
     FILE *fp, *fpheader, *fp_pcmdata;
     s_segments SegmentAr[MaxIntervals + 1];
     t_int32 i;

    int write_csv = 0;
    
    #if RESEARCH
    write_csv = 1;
    #endif

    if (ENABLE_DEBUG)
    {
        fp = fopen(paths_and_meta->output_path_vad_1, "w");
        fpheader = fopen("../testbench/sonde_c_output/debug/debug_header.csv", "w");
        fp_pcmdata = fopen("../testbench/sonde_c_output/debug/debug_pcm.csv", "w");

        if (fp == NULL)
        {
            perror("Error opening output file:\n");
            return -1;
        }
        else if (fpheader == NULL)
        {
            perror("Error opening debug_header.csv file:\n");
            return -1;
        }
        else if (fp_pcmdata == NULL)
        {
            perror("Error opening debug_eq_pcm.csv file");
            return -1;
        }
        fprintf(fp, "Segment,Onset,Offset,Description\n");
        fprintf(fp_pcmdata, "Samples,eq_pcm,Gained_eq_pcm,Durations\n");
        fprintf(fpheader, "Parameters,Values\n");
        fprintf(fpheader, "RIFF,");
        for (i = 0; i < 4; i++)
        {
            fprintf(fpheader, "%c", file_parameters->WaveHeader.riffID[i]);
        }
        fprintf(fpheader, "\n");
        intToStr(file_parameters->WaveHeader.size, str, 0);
        fprintf(fpheader, "FileSize,%s\n", str);
        fprintf(fpheader, "FileType-WavID,");
        for (i = 0; i < 4; i++)
        {
            fprintf(fpheader, "%c", file_parameters->WaveHeader.wavID[i]);
        }
        fprintf(fpheader, "\n");
        fprintf(fpheader, "FmtID,");
        for (i = 0; i < 4; i++)
        {
            fprintf(fpheader, "%c", file_parameters->WaveHeader.fmtID[i]);
        }
        fprintf(fpheader, "\n");
        intToStr(file_parameters->WaveHeader.fmtSize, str, 0);
        fprintf(fpheader, "FmtSize,%s\n", str);
        intToStr(file_parameters->WaveHeader.format, str, 0);
        fprintf(fpheader, "FormatType,%s\n", str);
        intToStr(file_parameters->WaveHeader.channels, str, 0);
        fprintf(fpheader, "Channels,%s\n", str);
        intToStr(file_parameters->WaveHeader.sampleRate, str, 0);
        fprintf(fpheader, "file_parameters->WaveHeader.sampleRate,%s\n", str);
        intToStr(file_parameters->WaveHeader.bytePerSec, str, 0);
        fprintf(fpheader, "BytePerSec,%s\n", str);
        intToStr(file_parameters->WaveHeader.blockSize, str, 0);
        fprintf(fpheader, "BlockSize,%s\n", str);
        intToStr(file_parameters->WaveHeader.bitDepth, str, 0);
        fprintf(fpheader, "BitDepth,%s\n", str);
        intToStr(file_parameters->WaveHeader.dataSize, str, 0);
        fprintf(fpheader, "DataID,");
        for (i = 0; i < 4; i++)
        {
            fprintf(fpheader, "%c", file_parameters->WaveHeader.dataID[i]);
        }
        fprintf(fpheader, "\n");
        fprintf(fpheader, "Datasize,%s\n", str);

        for (i = 0; i < (shared_params->SampleCount + VAD_PADDING2) ; i++)
        {
            if (i <= 30000)
            {
                fprintf(fp_pcmdata, "%d,%d,%f,%lf\n", i, file_parameters->PCM[i], shared_params->eq_pcm[i], vad_params->DurationAr[i]);
            }
            else
            {
                fprintf(fp_pcmdata, "%d,%d,%f\n", i, file_parameters->PCM[i], shared_params->eq_pcm[i]);
            }
        }
        //printf("\n3 DEBUG FILES GENERATED\n");
    }
    else if( write_csv )
    {
        t_int8 * vad_op = store_output_at( paths_and_meta->output_path_vad_1, paths_and_meta->input_filename, "_vad.csv" );   
        fp = fopen(vad_op, "w");
        if (fp == NULL)
        {
            perror("Error opening output file:\n");
            return -1;
        }
        fprintf(fp, "Segment,Onset,Offset,Description\n");
        strcpy(paths_and_meta->output_path_vad_1 , vad_op);
        //printf("\nVAD CSV FILE GENERATED\n");
    }

     t_int32 StartSample = 0;
     t_int32 EndSample = 0;
     t_int32 MsqNoise = 0;
     t_double MsqSignalPlusNoise = 0;
     t_int32 SDEX = 0;
     t_int32 Segdex = 0;
     t_double SNR[MaxGroupCount + 1];
     t_double zScore = 0;
     t_double tConfidence = 0;
     t_double Confidence[MaxGroupCount + 1];
     t_int8 startTime_s[25];
     t_int8 endTime_s[25];
     t_int8 Segdex_s[10];
    int frame_count = 0;
    int loop_counter = 0;

    SegmentAr[Segdex].StartTime = 0;

    //assume zero mean  PCM data values
     t_int32 Gdex = 0;

    while (Gdex < (file_parameters->LastOffsetflag + 1))
    {
        //Linelog += "Voice_onset(" + Gdex.ToString() + "): " + String.Format("{0:0.00} ms", Module1.RecordingGeneral.SegmentStart[Gdex] * 1000) + "\t" + "Voice_offset(" + Gdex.ToString() + "): " + String.Format("{0:0.00} ms", Module1.RecordingGeneral.SegmentEnd[Gdex] * 1000) + "\r\n";
        //Linelog += '\t' + "\t" + "Fmt0" + "\t" + "Fmt1" + "\t" + "Fmt2" + "\t" + "Fmt3" + "\t" + "Fmt4" + "\r\n";

        Confidence[Segdex] = 0.5;

        if (Gdex == 0)
        {
            StartSample = EndSample;
            EndSample = (t_int32)(file_parameters->SegmentStart[Gdex] * (t_double)file_parameters->WaveHeader.sampleRate);
        }
        else
        {
            StartSample = (t_int32)(file_parameters->SegmentEnd[Gdex - 1] * (t_double)file_parameters->WaveHeader.sampleRate);
            EndSample = (t_int32)(file_parameters->SegmentStart[Gdex] * (t_double)file_parameters->WaveHeader.sampleRate);
        }

        SegmentAr[Segdex].StartTime = (t_double)((t_double)StartSample / (t_double)file_parameters->WaveHeader.sampleRate);
        SegmentAr[Segdex].EndTime = (t_double)((t_double)EndSample / (t_double)file_parameters->WaveHeader.sampleRate);
        SegmentAr[Segdex].Classification = 0;
        SegmentAr[Segdex].Description = "noise";

        double start_time_inms = ((double)SegmentAr[Segdex].StartTime * 100000 * shared_params->frame_samplerate_step / file_parameters->WaveHeader.sampleRate);
        double end_time_inms = ((double)SegmentAr[Segdex].EndTime * 100000 * shared_params->frame_samplerate_step / file_parameters->WaveHeader.sampleRate);

        for (loop_counter = (int)start_time_inms; loop_counter < end_time_inms;)
        {
            vad_params->vad_annotation_array[frame_count] = 0;
            frame_count++;
            loop_counter = frame_count * 10;
        }

        //.SegmentAr[Segdex].Confidence = 0.5;

        //SegmentLog += Segdex.ToString() + "\t" + String.Format("{0:0.0000}", Module1.SegmentAr[Segdex].StartTime) + "\t" + String.Format("{0:0.0000}", Module1.SegmentAr[Segdex].EndTime) + "\t" + String.Format("{0:0.00%}", Module1.SegmentAr[Segdex].Confidence) + "\t" + Module1.SegmentAr[Segdex].Description + "\r\n";
        sprintf(Segdex_s, "%d", Segdex);
        sprintf(startTime_s, "%.10lf", SegmentAr[Segdex].StartTime);
        sprintf(endTime_s, "%.10lf", SegmentAr[Segdex].EndTime);
        if( write_csv )
        fprintf(fp, "%s,%s,%s,%s\n", Segdex_s, startTime_s, endTime_s, SegmentAr[Segdex].Description);

        Segdex++;

        StartSample = (t_int32)(file_parameters->SegmentStart[Gdex] * (t_double)file_parameters->WaveHeader.sampleRate);
        EndSample = (t_int32)(file_parameters->SegmentEnd[Gdex] * (t_double)file_parameters->WaveHeader.sampleRate);

        MsqSignalPlusNoise = 0;
        SDEX = StartSample;

        while (SDEX < EndSample + 1.0)
        {
            MsqSignalPlusNoise += pow(shared_params->eq_pcm[SDEX], 2.0);
            SDEX++;
        }
        MsqSignalPlusNoise /= (t_double)((t_double)EndSample - (t_double)StartSample);
        SNR[Gdex] = 10.0 * log10(MsqSignalPlusNoise / MsqNoise);
        //Linelog += "SNR(" + Gdex.ToString() + ") = " + String.Format("{0:0.00} dB", SNR[Gdex]) + "\r\n";

        zScore = MsqSignalPlusNoise / MsqNoise - 1;
        if (zScore < 0)
        {
            zScore = 0;
        }
        else
        {
            zScore = sqrt(zScore) / 4;
        }

        tConfidence = 0.5 + 0.5 * ERFC(zScore);
        Confidence[Gdex] = tConfidence;
        //Linelog += "Confidence(" + Gdex.ToString() + ") = " + String.Format("{0:0.00%}", tConfidence) + "\r\n" + "\r\n" + "\r\n";

        SegmentAr[Segdex].StartTime = (t_double)StartSample / (t_double)file_parameters->WaveHeader.sampleRate;
        SegmentAr[Segdex].EndTime = (t_double)EndSample / (t_double)file_parameters->WaveHeader.sampleRate;
        SegmentAr[Segdex].Snr = SNR[Gdex];
        if (Confidence[Gdex] < MinConfthresh)
        {
            SegmentAr[Segdex].Classification = 2;
            SegmentAr[Segdex].Description = "Unwanted/artifact";
        }
        else
        {
            SegmentAr[Segdex].Classification = 1;
            SegmentAr[Segdex].Description = "Active voice";
        }

        start_time_inms = ((double)SegmentAr[Segdex].StartTime * 100000 * shared_params->frame_samplerate_step / file_parameters->WaveHeader.sampleRate);
        end_time_inms = ((double)SegmentAr[Segdex].EndTime * 100000 * shared_params->frame_samplerate_step / file_parameters->WaveHeader.sampleRate);
        
        for (loop_counter = (int)start_time_inms; loop_counter < end_time_inms;)
        {
            vad_params->vad_annotation_array[frame_count] = 1;
            shared_params->voice_frames++;
            frame_count++;
            loop_counter = frame_count * 10;
        }

        //.SegmentAr[Segdex].Confidence = tConfidence;
        //SegmentLog += Segdex.ToString() + "\t" + String.Format("{0:0.0000}", Module1.SegmentAr[Segdex].StartTime) + "\t" + String.Format("{0:0.0000}", Module1.SegmentAr[Segdex].EndTime) + "\t" + String.Format("{0:0.00%}", Module1.SegmentAr[Segdex].Confidence) + "\t" + Module1.SegmentAr[Segdex].Description + "\r\n";

        sprintf(Segdex_s, "%d", Segdex);
        sprintf(startTime_s, "%.10lf", SegmentAr[Segdex].StartTime);
        sprintf(endTime_s, "%.10lf", SegmentAr[Segdex].EndTime);
        if( write_csv )
        fprintf(fp, "%s,%s,%s,%s\n", Segdex_s, startTime_s, endTime_s, SegmentAr[Segdex].Description);

        Segdex++;
        Gdex++;
    }

    if( shared_params->voice_frames == 0 )
    {
        shared_params->reduced_number_of_frames = 0; 
    }
    else
    {
        shared_params->reduced_number_of_frames = vad_get_reduced_frame_count( vad_params->vad_annotation_array, shared_params->number_of_frames_vad );
    }

    if (ENABLE_DEBUG)
    {
        fclose(fp);
        fclose(fp_pcmdata);
        fclose(fpheader);
    }
    else
    {
        if( write_csv )
        fclose(fp);
    }
    return E_SUCCESS;
}

 t_int32 reverse(t_int8 *str, t_int32 len)
{
    t_int32 i = 0, j = len - 1, temp;
    while (i < j)
    {
        temp = str[i];
        str[i] = str[j];
        str[j] = temp;
        i++;
        j--;
    }
    return E_SUCCESS;
}

 t_int32 intToStr(t_int32 x, t_int8 *str, t_int32 d)
{
    t_int32 i = 0;
    while (x)
    {
        str[i++] = (x % 10) + '0';
        x = x / 10;
    }

    while (i < d)
        str[i++] = '0';

    reverse(str, i);
    str[i] = '\0';
    return i;
}

// Converts a floating point number to string.
 t_int32 ftoa(t_double n, t_int8 *res, t_int32 afterpoint)
{

    t_int32 ipart = (t_int32)n;

    t_double fpart = n - (t_double)ipart;

    t_int32 i = intToStr(ipart, res, 0);

    if (afterpoint != 0)
    {
        res[i] = '.';

        fpart = fpart * pow(10, afterpoint);

        intToStr((t_int32)fpart, res + i + 1, afterpoint);
    }
    return E_SUCCESS;
}

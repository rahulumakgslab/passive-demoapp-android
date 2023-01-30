/*
* @brief extraction of vad(voice activity detection) feature - mathematical opreation.
*
* @author Swapnil Warkar
*
*/

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "sonde_spfe_function_def.h"

t_double ERFC(t_double X)
{
    t_double returnValue = 0;
    t_double t = 0;
    t_double u = 0;
    t_double Y = 0;
    t_double V = 0;

    V = X / (sqrt(2.0));

    t = 3.97886080735226 / (fabs(V) + 3.97886080735226);
    u = t - 0.5;
    Y = (((((((((0.00127109764952614 * u + 0.000119314022838341) * u - 0.00396385097360514) * u - 0.000870779635317296) * u + 0.00773672528313527) * u + 0.00383335126264887) * u - 0.0127223813782123) * u - 0.013382364453346) * u + 0.0161315329733252) * u + 0.0390976845588484) * u + 0.00249367200053503;

    Y = ((((((((((((Y * u - 0.0838864557023002) * u - 0.119463959964325) * u + 0.0166207924969367) * u + 0.357524274449531) * u + 0.805276408752911) * u + 1.18902982909273) * u + 1.37040217682338) * u + 1.31314653831023) * u + 1.07925515155857) * u + 0.774368199119539) * u + 0.490165080585318) * u + 0.275374741597377) * t * exp(-V * V);

    if (V < 0.0)
    {
        returnValue = Y - 1;
    }
    else
    {
        returnValue = 1 - Y;
    }

    return returnValue;
}
/*
t_uint8 NumberOfBitsNeeded(t_int32 PowerOfTwo)
{
    t_uint8 returnValue = 0;
    t_int8 I = 0;
    t_int32 Iarg = 0;

    returnValue = (t_int8)0;
    for (I = 0; I <= 16; I++)
    {
        Iarg = (t_uint32)(pow(2, I));
        if ((PowerOfTwo & Iarg) != 0)
        {
            returnValue = I;
            return returnValue;
        }
    }
    return returnValue;
}

t_int8 IsPowerOfTwo(t_long X)
{
    t_int8 returnValue = FALSE;
    if (X < 2)
    {
        returnValue = FALSE;
        return returnValue;
    }
    returnValue = TRUE;
    return returnValue;
}

t_int32 ReverseBits(t_int32 Index, t_uint8 NumBits)
{
    t_int32 returnValue = 0;
    t_uint8 I = 0;
    t_int32 Rev = 0;
    t_uint8 nLim = 0;

    nLim = (t_uint8)(NumBits - 1);

    for (I = 0; I <= nLim; I++)
    {
        Rev = (t_int32)((Rev * 2) | (Index & 1));
        Index = Index / 2;
    }

    returnValue = Rev;
    return returnValue;
}

void FourierTransform(t_int32 NumSamples, t_double RealIn[], t_double ImageIn[], t_double RealOut[], t_double ImagOut[], t_uint8 InverseTransform)
{
    t_double AngleNumerator = 0;
    t_uint8 NumBits = 0;
    t_int32 I = 0;
    t_int32 J = 0;
    t_int32 K = 0;
    t_int32 N = 0;
    t_int32 BlockSize = 0;
    t_int32 BlockEnd = 0;
    t_double DeltaAngle = 0;
    t_double DeltaAr = 0;
    t_double Alpha = 0;
    t_double Beta = 0;
    t_double TR = 0;
    t_double TI = 0;
    t_double AR = 0;
    t_double AI = 0;

    if (InverseTransform)
    {
        AngleNumerator = -2.0 * SONDE_PI;
    }
    else
    {
        AngleNumerator = 2.0 * SONDE_PI;
    }

    if ((IsPowerOfTwo(NumSamples) == FALSE) || (NumSamples < 2))
    {
        printf("Error in procedure Fourier: \r\n NumSamples is %d, which is not a positive integer power of two. Error!", NumSamples);
        return -1;
    }

    NumBits = NumberOfBitsNeeded(NumSamples);
    for (I = 0; I <= (NumSamples - 1); I++)
    {
        J = ReverseBits(I, NumBits);
        RealOut[J] = RealIn[I];
        ImagOut[J] = ImageIn[I];
    }

    BlockEnd = 1;
    BlockSize = 2;

    while (BlockSize <= NumSamples)
    {
        DeltaAngle = AngleNumerator / BlockSize;
        Alpha = sin(0.5 * DeltaAngle);
        Alpha = 2.0 * Alpha * Alpha;
        Beta = sin(DeltaAngle);

        I = 0;
        while (I < NumSamples)
        {
            AR = 1.0;
            AI = 0.0;

            J = I;
            for (N = 0; N <= BlockEnd - 1; N++)
            {
                K = J + BlockEnd;
                TR = AR * RealOut[K] - AI * ImagOut[K];
                TI = AI * RealOut[K] + AR * ImagOut[K];
                RealOut[K] = RealOut[J] - TR;
                ImagOut[K] = ImagOut[J] - TI;
                RealOut[J] = RealOut[J] + TR;
                ImagOut[J] = ImagOut[J] + TI;
                DeltaAr = Alpha * AR + Beta * AI;
                AI = AI - (Alpha * AI - Beta * AR);
                AR = AR - DeltaAr;
                J++;
            }

            I = I + BlockSize;
        }

        BlockEnd = BlockSize;
        BlockSize = BlockSize * 2;
    }

    if (InverseTransform)
    {
        //Normalize the resulting time samples...
        for (I = 0; I <= NumSamples - 1; I++)
        {
            RealOut[I] = RealOut[I] / NumSamples;
            ImagOut[I] = ImagOut[I] / NumSamples;
        }
    }
}

t_double FrequencyOfIndex(t_int32 NumberOfSamples, t_int32 Index)
{
    t_double returnValue = 0;
    //Based on IndexToFrequency().  This name makes more sense to me.

    if (Index >= NumberOfSamples)
    {
        returnValue = 0.0;
        return returnValue;
    }
    else if (Index <= (t_double)NumberOfSamples / 2)
    {
        returnValue = (Index) / NumberOfSamples;
        return returnValue;
    }
    else
    {
        returnValue = -(NumberOfSamples - Index) / NumberOfSamples;
        return returnValue;
    }
}

void CalcFrequency(t_int32 NumberOfSamples, t_int32 FrequencyIndex, t_double RealIn[], t_double ImagIn[], t_double RealOut, t_double ImagOut)
{

    int K = 0;
    t_double Cos1 = 0;
    t_double Cos2 = 0;
    t_double Cos3 = 0;
    t_double Theta = 0;
    t_double Beta = 0;
    t_double Sin1 = 0;
    t_double Sin2 = 0;
    t_double Sin3 = 0;

    Theta = 2 * SONDE_PI * FrequencyIndex / NumberOfSamples;
    Sin1 = sin(-2 * Theta);
    Sin2 = sin((t_double)(-Theta));
    Cos1 = cos(-2 * Theta);
    Cos2 = cos((t_double)(-Theta));
    Beta = 2 * Cos2;

    for (K = 0; K <= NumberOfSamples - 2; K++)
    {
        //Update trig values
        Sin3 = Beta * Sin2 - Sin1;
        Sin1 = Sin2;
        Sin2 = Sin3;

        Cos3 = Beta * Cos2 - Cos1;
        Cos1 = Cos2;
        Cos2 = Cos3;

        RealOut = RealOut + RealIn[K] * Cos3 - ImagIn[K] * Sin3;
        ImagOut = ImagOut + ImagIn[K] * Cos3 + RealIn[K] * Sin3;
    }
}

void DoCubicFit()
    {
        t_int32 NumberFitPoints;
        t_uint8 CubicOkay;
        t_double CubicSet[4];
        t_double CalibX[2];
        t_double CalibFx[2];
        //Stan Smith 10/6/2001

        t_double TermAA = 0;
        t_double TermAB = 0;
        t_double TermAC = 0;
        t_double TermAD = 0;
        t_double TermAE = 0;
        t_double TermAF = 0;
        t_double TermAG = 0;
        t_double TermAH = 0;

        t_double TermBA = 0;
        t_double TermBB = 0;
        t_double TermBC = 0;
        t_double TermCA = 0;
        t_double TermCB = 0;
        t_double TermCD = 0;

        t_double CommonZero = 0;
        t_double Minimum[2];

        //8/8/2006
        CubicOkay = FALSE;

        if (NumberFitPoints == 1 && CalibX[0] != 0.0)
        {
            CubicSet[0] = 0.0;
            CubicSet[2] = 0.0;
            CubicSet[3] = 0.0;
            CubicSet[1] = CalibFx[0] / CalibX[0];
            CubicOkay = TRUE;
            return -1;
        }
        else
        {
            if (NumberFitPoints == 2 && CalibX[0] != CalibX[1])
            {
                CubicSet[1] = (CalibFx[0] - CalibFx[1]) / (CalibX[0] - CalibX[1]);
                CubicSet[0] = CalibFx[0] - CubicSet[1] * CalibX[0];
                CubicSet[2] = 0.0;
                CubicSet[3] = 0.0;
                CubicOkay = TRUE;
                return -1;
            }
        }

        CommonZero = Dp(0, 0);

        //compute CubicSet(3)

        CubicSet[0] = 0.0;
        CubicSet[1] = 0.0;
        CubicSet[2] = 0.0;
        CubicSet[3] = 0.0;
        if (CommonZero == 0.0)
        {
            return -1;
        }


        TermAH = Dp(1, 2) - Dp(0, 2) * Dp(1, 0) / CommonZero;
        TermAA = Dp(1, 1) - Dp(0, 1) * Dp(1, 0) / CommonZero;
        TermAB = Dp(0, 1) * Dp(2, 0) / CommonZero - Dp(2, 1);
        TermAC = Dp(0, 1) * Dp(3, 0) / CommonZero - Dp(3, 1);
        TermAD = Dp(-1, 1) - Dp(0, 1) * Dp(-1, 0) / CommonZero;
        TermAE = Dp(2, 2) - Dp(0, 2) * Dp(2, 0) / CommonZero;
        TermAF = Dp(3, 2) - Dp(0, 2) * Dp(3, 0) / CommonZero;
        TermAG = Dp(-1, 2) - Dp(0, 2) * Dp(-1, 0) / CommonZero;

        if (TermAA == 0)
        {
            return -1;
        }

        TermBA = Dp(1, 3) * TermAB / TermAA + Dp(2, 3) - Dp(0, 3) * Dp(1, 0) * TermAB / CommonZero / TermAA - Dp(2, 0) * Dp(0, 3) / CommonZero / CommonZero;
        TermBB = Dp(1, 3) * TermAC / TermAA + Dp(3, 3) - Dp(0, 3) * Dp(1, 0) * TermAC / CommonZero / TermAA - Dp(0, 3) * Dp(3, 0) / CommonZero;
        TermBC = Dp(-1, 3) - Dp(0, 3) / CommonZero * (Dp(-1, 0) - Dp(1, 0) * TermAD / TermAA) - Dp(1, 3) * TermAD / TermAA;

        TermCA = TermAH * TermAB / TermAA + TermAE;
        TermCB = TermAH * TermAC / TermAA + TermAF;
        TermCD = TermAH * TermAD / TermAA - TermAG;
        if (TermCA == 0.0)
        {
            return -1;
        }

        //7/2/2002 Original
        CubicSet[3] = (TermCD + TermCA * TermBC / TermBA) / (TermCA * TermBB / TermBA - TermCB);
        if (NumberFitPoints < 4)
        {
            CubicSet[3] = 0.0;
        }
        CubicSet[2] = (TermBC - TermBB * CubicSet[3]) / TermBA;

        //optional solution for CubicSet(2)
        CubicSet[2] = ((TermAG - TermAH * TermAD / TermAA) - CubicSet[3] * (TermAH * TermAC / TermAA + TermAF)) / (TermAH * TermAB / TermAA + TermAE);
        CubicSet[1] = (TermAB * CubicSet[2] + TermAC * CubicSet[3] + TermAD) / TermAA;
        CubicSet[0] = (Dp(-1, 0) - Dp(1, 0) * CubicSet[1] - Dp(2, 0) * CubicSet[2] - Dp(3, 0) * CubicSet[3]) / CommonZero;
        CubicOkay = TRUE;

    }

t_double Dp(t_int32 Phi1, t_int32 Phi2)
    {
        t_double returnValue = 0;
        //10/5/2001 Stan Smith

        int K = 0;
        t_double OrthogFunctionDP = 0;


        OrthogFunctionDP = 0.0;


        if (NumberFitPoints > 1)
        {
            K = 0;
            while (K < NumberFitPoints)
            {
                if (Phi1 < 0)
                {
                    OrthogFunctionDP = OrthogFunctionDP + (CalibFx[K]) * (pow(CalibX[K], Phi2));
                }
                else
                {
                    OrthogFunctionDP = OrthogFunctionDP + (pow(CalibX[K], Phi1)) * (pow(CalibX[K], Phi2));
                }
                K++;
            }
        }
        else
        {
            K = 0;
            while (K < NumberFitPoints + 1)
            {
                if (Phi1 < 0)
                {
                    OrthogFunctionDP = OrthogFunctionDP + (CalibFx[K]) * (pow(CalibX[K], Phi2));
                }
                else
                {
                    OrthogFunctionDP = OrthogFunctionDP + (pow(CalibX[K], Phi1)) * (pow(CalibX[K], Phi2));
                }
                K++;
            }
        }

        returnValue = OrthogFunctionDP;

        return returnValue;
    }
    */
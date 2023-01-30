
#ifndef _sonde_spfe_filter
#define _sonde_spfe_filter

int generate_low_pass(float Fs, float Fc,
              int order, float coeffs[]);
int generate_high_pass(float Fs, float Fc,
               int order, float coeffs[]);
int generate_band_pass(float Fs, float Fcl, float Fch,
               int order, float coeffs[]);
int generate_band_stop(float Fs, float Fcl, float Fch,
               int order, float coeffs[]);

void butter (int n, float fcf, float ** b, float ** a);

void filter (int ord, float * a, float * b,
            int np, const float * x, float * y);

void filtfilt (int ord, float * a, float * b,
            int np, float * x, float * y);

#endif

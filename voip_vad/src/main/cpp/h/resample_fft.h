
#pragma once

#include <complex.h>
#include <stdbool.h>
#include <stddef.h>

typedef float complex cplx;
typedef double complex cplx_d;

#ifdef __cplusplus
extern "C" {
#endif

bool resample_fft(cplx vec[], size_t n, bool inverse);
bool transform_radix(cplx vec[], size_t n, bool inverse);
bool transform_bluestein(cplx vec[], size_t n, bool inverse);
bool fft_convolve(const cplx xvec[], const cplx yvec[], cplx outvec[], size_t n);

#ifdef __cplusplus
}
#endif

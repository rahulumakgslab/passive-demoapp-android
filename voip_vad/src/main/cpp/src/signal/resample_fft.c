
#include <math.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include "../../h/resample_fft.h"

typedef float complex cplx;
#define SONDE_PI 3.14159265358979
static size_t reverse_bits(size_t val, int width);
static void *memdup(const void *src, size_t n);

bool resample_fft(cplx vec[], size_t n, bool inverse)
{
	if (n == 0)
		return true;
	else if ((n & (n - 1)) == 0) // Is power of 2
		return transform_radix(vec, n, inverse);
	else // More complicated algorithm for arbitrary sizes
		return transform_bluestein(vec, n, inverse);
}

bool transform_radix(cplx vec[], size_t n, bool inverse) 
{
	// Length variables
	int levels = 0; // Compute levels = floor(log2(n))

	for (size_t temp = n; temp > 1U; temp >>= 1)
		levels++;

	if ((size_t)1U << levels != n)
		return false; // n is not a power of 2

	// Trignometric tables
	if (SIZE_MAX / sizeof(cplx) < n / 2)
		return false;

	cplx *exptable = malloc((n / 2) * sizeof(cplx));
	if (exptable == NULL)
		return false;

	for (size_t i = 0; i < n / 2; i++)
		exptable[i] = cexp((inverse ? 2 : -2) * SONDE_PI * i / n * I);

	// Bit-reversed addressing permutation
	for (size_t i = 0; i < n; i++)
	{
		size_t j = reverse_bits(i, levels);

		if (j > i)
		{
			cplx temp = vec[i];
			vec[i] = vec[j];
			vec[j] = temp;
		}
	}

	// Cooley-Tukey decimation-in-time radix-2 FFT
	for (size_t size = 2; size <= n; size *= 2)
	{
		size_t halfsize = size / 2;
		size_t tablestep = n / size;
		for (size_t i = 0; i < n; i += size)
		{
			for (size_t j = i, k = 0; j < i + halfsize; j++, k += tablestep)
			{
				size_t l = j + halfsize;
				cplx temp = vec[l] * exptable[k];
				vec[l] = vec[j] - temp;
				vec[j] += temp;
			}
		}
		if (size == n) // Prevent overflow in 'size *= 2'
			break;
	}

	free(exptable);
	return true;
}

bool transform_bluestein( cplx vec[], size_t n, bool inverse )
{
	bool status = false;

	// Find a power-of-2 convolution length m such that m >= n * 2 + 1
	size_t m = 1;

	while (m / 2 <= n)
	{
		if (m > SIZE_MAX / 2)
			return false;
		m *= 2;
	}

	// Allocate memory
	if (SIZE_MAX / sizeof(cplx) < n || SIZE_MAX / sizeof(cplx) < m)
		return false;
	cplx *exptable = malloc(n * sizeof(cplx));
	cplx *avec = calloc(m, sizeof(cplx));
	cplx *bvec = calloc(m, sizeof(cplx));
	cplx *cvec = malloc(m * sizeof(cplx));
	if (exptable == NULL || avec == NULL || bvec == NULL || cvec == NULL)
		goto cleanup;

	// Trignometric tables
	for (size_t i = 0; i < n; i++)
	{
		uintmax_t temp = ((uintmax_t)i * i) % ((uintmax_t)n * 2);
		double angle = (inverse ? SONDE_PI : -SONDE_PI) * temp / n;
		exptable[i] = cexp(angle * I);
	}

	// Temporary vectors and preprocessing
	for (size_t i = 0; i < n; i++)
		avec[i] = vec[i] * exptable[i];
	bvec[0] = exptable[0];
	for (size_t i = 1; i < n; i++)
		bvec[i] = bvec[m - i] = conj(exptable[i]);

	// Convolution
	if (!fft_convolve(avec, bvec, cvec, m))
		goto cleanup;

	// Postprocessing
	for (size_t i = 0; i < n; i++)
		vec[i] = cvec[i] * exptable[i];
	status = true;

	// Deallocation
cleanup:
	free(exptable);
	free(avec);
	free(bvec);
	free(cvec);
	return status;
}

bool fft_convolve(const cplx xvec[], const cplx yvec[], cplx outvec[], size_t n)
{
	bool status = false;

	if (SIZE_MAX / sizeof(cplx) < n)
		return false;

	cplx *xv = memdup(xvec, n * sizeof(cplx));
	cplx *yv = memdup(yvec, n * sizeof(cplx));
	if (xv == NULL || yv == NULL)
		goto cleanup;

	if (!resample_fft(xv, n, false))
		goto cleanup;
	if (!resample_fft(yv, n, false))
		goto cleanup;
	for (size_t i = 0; i < n; i++)
		xv[i] *= yv[i];
	if (!resample_fft(xv, n, true))
		goto cleanup;
	for (size_t i = 0; i < n; i++) // Scaling (because this FFT implementation omits it)
		outvec[i] = xv[i] / n;
	status = true;

cleanup:
	free(xv);
	free(yv);
	return status;
}

static size_t reverse_bits(size_t val, int width)
{
	size_t result = 0;

	for (int i = 0; i < width; i++, val >>= 1)
		result = (result << 1) | (val & 1U);

	return result;
}

static void *memdup(const void *src, size_t n)
{
	void *dest = malloc(n);

	if (n > 0 && dest != NULL)
		memcpy(dest, src, n);

	return dest;
}

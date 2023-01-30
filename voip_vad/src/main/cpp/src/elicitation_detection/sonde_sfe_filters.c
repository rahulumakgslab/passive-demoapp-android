
#include <math.h>
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include "sonde_spfe_filter.h"

int generate_low_pass(float Fs, float Fc,
              int order, float coeffs[])
{
  float Ft;
  int n;
  
  assert(order>0 && !(order&0x1));
  assert(Fs>0 && Fc>0 && Fc<Fs/2);
  
  Ft = Fc/Fs;

  for (n=0;n<=order;n++) {
    if (n == order/2) {
      coeffs[n] = 2*Ft;
    } else {
      coeffs[n] = sin(2*M_PI*Ft*(n-order/2))/(M_PI*(n-order/2));
    }
  }
  
  return 0;
}

int generate_high_pass(float Fs, float Fc,
               int order, float coeffs[])
{
  float Ft;
  int n;
  
  assert(order>0 && !(order&0x1));
  assert(Fs>0 && Fc>0 && Fc<Fs/2);
  
  Ft = Fc/Fs;
  
  for (n=0;n<=order;n++) {
    if (n == order/2) {
      coeffs[n] = 1 - 2*Ft;
    } else {
      coeffs[n] = -sin(2*M_PI*Ft*(n-order/2))/(M_PI*(n-order/2));
    }
  }
  return 0;
}

int generate_band_pass(float Fs, float Fcl, float Fch,
               int order, float coeffs[])
{
  float Ftl, Fth;
  int n;
  
  assert(order>0 && !(order&0x1));
  assert(Fs>0 && Fcl>0 && Fcl<Fs/2 && Fch>0 && Fch<Fs/2);
  
  Ftl = Fcl/Fs;
  Fth = Fch/Fs;
  
  for (n=0;n<=order;n++) {
    if (n == order/2) {
      coeffs[n] = 2*(Fth-Ftl);
    } else {
      coeffs[n] = (sin(2*M_PI*Fth*(n-order/2))/(M_PI*(n-order/2))) -
    (sin(2*M_PI*Ftl*(n-order/2))/(M_PI*(n-order/2)));
    }
  }
  return 0;
}


int generate_band_stop(float Fs, float Fcl, float Fch,
               int order, float coeffs[])
{
  float Ftl, Fth;
  int n;
  
  assert(order>0 && !(order&0x1));
  assert(Fs>0 && Fcl>0 && Fcl<Fs/2 && Fch>0 && Fch<Fs/2);
  
  Ftl = Fcl/Fs;
  Fth = Fch/Fs;
  
  for (n=0;n<=order;n++) {
    if (n == order/2) {
      coeffs[n] = 1-2*(Fth-Ftl);
    } else {
      coeffs[n] = (sin(2*M_PI*Ftl*(n-order/2))/(M_PI*(n-order/2))) -
    (sin(2*M_PI*Fth*(n-order/2))/(M_PI*(n-order/2)));
    }
  }
  return 0;
}

static float * binomial_mult (int n, float * p)
{
    int i, j;
    float *a;

    a = (float *)calloc( 2 * n, sizeof(float) );
    if( a == NULL ) return( NULL );

    for( i = 0; i < n; ++i )
    {
    for( j = i; j > 0; --j )
    {
        a[2*j] += p[2*i] * a[2*(j-1)] - p[2*i+1] * a[2*(j-1)+1];
        a[2*j+1] += p[2*i] * a[2*(j-1)+1] + p[2*i+1] * a[2*(j-1)];
    }
    a[0] += p[2*i];
    a[1] += p[2*i+1];
    }
    return( a );
}

static float * ccof_bwlp (int n)
{
    float * ccof;
    int m, i;

    ccof = (float*)calloc(n + 1, sizeof(float));
    if (!ccof) {
        return NULL;
    }

    ccof[0] = 1.0;
    ccof[1] = (float)n;
    m = n/2;
    for (i = 2; i <= m; ++i) {
        ccof[i] = (float)((n - i + 1) * ccof[i-1] / i);
        ccof[n-i]= ccof[i];
    }

    ccof[n-1] = (float)n;
    ccof[n] = 1.0;

    return ccof;
}

static float * dcof_bwlp (int n, float fcf)
{
    int k;            // loop variables
    float theta;     // M_PI * fcf / 2.0
    float st;        // sine of theta
    float ct;        // cosine of theta
    float parg;      // pole angle
    float sparg;     // sine of the pole angle
    float cparg;     // cosine of the pole angle
    float a;         // workspace variable
    float *rcof;     // binomial coefficients
    float *dcof;     // dk coefficients

    rcof = (float *)calloc(2 * n, sizeof(float));
    if (!rcof) {
        return NULL;
    }

    theta = M_PI * fcf;
    st = sin(theta);
    ct = cos(theta);

    for (k = 0; k < n; ++k) {
        parg = M_PI * (float)(2*k+1)/(float)(2*n);
        sparg = sin(parg);
        cparg = cos(parg);
        a = 1.0 + st*sparg;
        rcof[2*k] = -ct/a;
        rcof[2*k+1] = -st*cparg/a;
    }

    dcof = binomial_mult(n, rcof);
    free(rcof);

    dcof[1] = dcof[0];
    dcof[0] = 1.0;
    for (k = 3; k <= n; ++k) {
        dcof[k] = dcof[2*k-2];
    }

    return dcof;
}

static float sf_bwlp (int n, float fcf)
{
    int k;         // loop variables
    float omega;     // M_PI * fcf
    float fomega;    // function of omega
    float parg0;     // zeroth pole angle
    float sf;        // scaling factor

    omega = M_PI * fcf;
    fomega = sin(omega);
    parg0 = M_PI / (float)(2*n);

    sf = 1.0;
    for (k = 0; k < n/2; ++k) {
        sf *= 1.0 + fomega * sin((float)(2*k+1)*parg0);
    }

    fomega = sin(omega / 2.0);

    if (n % 2) {
        sf *= fomega + cos(omega / 2.0);
    }

    sf = pow(fomega, n) / sf;

    return sf;
}

void butter (int n, float fcf, float ** b, float ** a)
{
    int i = 0;
    float sf;

    /* get numerator coffs */
    *b = ccof_bwlp(n);
    if (!*b) {
        fprintf(stderr, "Error calculating numerator coeffs\n");
        return;
    }

    /* get denominator coffs */
    *a = dcof_bwlp(n, fcf);
    if (!*a) {
        fprintf(stderr, "Error calculating denominator coeffs\n");
        return;
    }
    

    sf = sf_bwlp(n, fcf);

    // scale the scaling factor for numerator coffs
    for (i = 0; i < n + 1; i++) {
        (*b)[i] *= sf;
    }

}

void filter (int ord, float * a, float * b,
             int np, const float * x, float * y)
{
    int i,j;

    y[0] = b[0] * x[0];

    for (i = 1; i < ord + 1; i++) {

        y[i] = 0.0;

        for (j = 0; j < i + 1; j++) {
            y[i] += b[j] * x[i - j];
        }

        for (j = 0; j < i; j++) {
            y[i] -=  a[j + 1] * y[i - j - 1];
        }
    }

    /* end of initial part */
    for (i = ord + 1; i < np + 1; i++) {

        y[i] = 0.0;

        for (j = 0; j < ord + 1; j++) {
            y[i] += b[j] * x[i - j];
        }

        for (j = 0; j < ord; j++) {
            y[i] -= a[j + 1] * y[i - j - 1];
        }
    }
}

void filtfilt (int ord, float * a, float * b,
               int np, float * x, float * y)
{

    int i;

    filter(ord, a, b, np, x, y);

    /* reverse the series */
    for (i = 0; i < np; i++) {
        x[i] = y[np - i - 1];
    }

    filter(ord, a, b, np, x, y);

    /* put it back */
    for (i = 0; i < np; i++) {
        x[i] = y[np - i - 1];
    }

    for (i = 0; i < np; i++) {
        y[i] = x[i];
    }
}

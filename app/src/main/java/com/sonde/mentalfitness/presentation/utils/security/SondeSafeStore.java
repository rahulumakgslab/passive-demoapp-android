package com.sonde.mentalfitness.presentation.utils.security;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.crypto.KeyGenerator;
import javax.security.auth.x500.X500Principal;


public class SondeSafeStore {
    private static final String TAG = SondeSafeStore.class.getSimpleName();

    private static final String KEY_ALGORITHM_AES = "AES";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final int SECRET_KEY_LENGTH_BITS = 256;

    private KeyStore keyStore;

    private String mAlias;
    private Context mContext;

    public SondeSafeStore(Context context, String mAlias) {
        this.mContext = context;
        this.mAlias = mAlias;
        initKeyStore();
    }

    private void initKeyStore() {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        createKeyIfNotExist();
    }

    private void createKeyIfNotExist() {
        try {
            // Create new key if needed
            if (!keyStore.containsAlias(mAlias)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(mAlias,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setKeySize(SECRET_KEY_LENGTH_BITS)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build();

                    final KeyGenerator keyGenerator = KeyGenerator
                            .getInstance(KEY_ALGORITHM_AES, ANDROID_KEY_STORE);

                    keyGenerator.init(keyGenParameterSpec);
                    keyGenerator.generateKey();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Calendar start = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    end.add(Calendar.YEAR, 1);
                    KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(mContext)
                            .setAlias(mAlias)
                            .setSubject(new X500Principal("CN=Sample Name, O=Android Authority"))
                            .setSerialNumber(BigInteger.ONE)
                            .setStartDate(start.getTime())
                            .setEndDate(end.getTime())
                            .setKeySize(SECRET_KEY_LENGTH_BITS)
                            .build();
                    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                    generator.initialize(spec);

                    generator.generateKeyPair();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    public Key getSecretKey() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(mAlias, null);
                return secretKeyEntry.getSecretKey();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(mAlias, null);
                return privateKeyEntry.getPrivateKey();
            }
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public void deleteAlias() {
        try {
            if (keyStore.containsAlias(mAlias))
                keyStore.deleteEntry(mAlias);
        } catch (KeyStoreException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}

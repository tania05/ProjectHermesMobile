package ca.projecthermes.projecthermes.util;

import android.util.Log;

import org.spongycastle.crypto.AsymmetricBlockCipher;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.encodings.PKCS1Encoding;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.engines.RSAEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PKCS7Padding;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.util.PrivateKeyFactory;
import org.spongycastle.crypto.util.PublicKeyFactory;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.KeyGenerator;

import ca.projecthermes.projecthermes.data.HermesDbHelper;


public class Encryption {

    private static final int keySize = 2048;
    private static final String TAG = "Encryption";

    public static byte[] encryptString(byte[] data, byte[] publicKeyData) {

        AsymmetricBlockCipher encryptionCipher = new PKCS1Encoding(new RSAEngine());
        byte[] encryptedData = null;
        try{
            encryptionCipher.init(true, PublicKeyFactory.createKey(publicKeyData));
            encryptedData = encryptionCipher.processBlock(data, 0, data.length);
        } catch (Exception e) {
            Log.e("hermes", e.toString());
        }

        return encryptedData;
    }

    public static byte[] decryptString(byte[] encryptedData, byte[] privateKeyData) {
        AsymmetricBlockCipher decryptionCipher = new PKCS1Encoding(new RSAEngine());
        try {
            decryptionCipher.init(false, PrivateKeyFactory.createKey(privateKeyData));
            byte[] decryptedData = decryptionCipher.processBlock(encryptedData, 0, encryptedData.length);
            return decryptedData;
        } catch (Exception e) {
            Log.e("hermes", e.toString());
        }
        return null;
    }

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(keySize);
            KeyPair keyPair = keyGenerator.generateKeyPair();
            Log.d(TAG, keyPair.getPublic().toString());
            Log.d(TAG, keyPair.getPrivate().toString());
            return keyPair;
        } catch (Exception e) {
            Log.e("hermes", e.toString());
        }
        return null;
    }

    public static byte[] getEncodedPublicKey(KeyPair keyPair) {
        return  keyPair.getPublic().getEncoded();
    }

    public static byte[] getEncodedPrivateKey(KeyPair keyPair) {
        return keyPair.getPrivate().getEncoded();
    }

    public static byte[] generateAESKey() {
        int keySize = 256; //Must be 128, 192, or 256

        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(keySize);
            Key key = keyGenerator.generateKey();
            byte[] keyData = key.getEncoded();
            return keyData;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] encryptUnderAes(byte[] key, byte[] data) {
        try {
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
            cipher.init(true, new KeyParameter(key));
            byte[] encryptedData = new byte[cipher.getOutputSize(data.length)];
            int bytesProcessed = 0;
            bytesProcessed += cipher.processBytes(data, 0, data.length, encryptedData, 0);
            cipher.doFinal(encryptedData, bytesProcessed);

            return encryptedData;
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }

        return new byte[] {};
    }

    public static byte[] decryptUnderAes(byte[] key, byte[] encryptedData) {
        try {
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
            cipher.init(false, new KeyParameter(key));
            byte[] decryptedData = new byte[cipher.getOutputSize(encryptedData.length)];
            int bytesProcessed = 0;
            bytesProcessed += cipher.processBytes(encryptedData, 0, encryptedData.length, decryptedData, 0);
            bytesProcessed += cipher.doFinal(decryptedData, bytesProcessed);

            decryptedData = Arrays.copyOf(decryptedData, bytesProcessed); //Strips away any padding that is leftover
            return decryptedData;
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }

        return new byte[] {};
    }
}

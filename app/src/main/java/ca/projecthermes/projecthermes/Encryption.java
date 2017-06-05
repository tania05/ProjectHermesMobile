package ca.projecthermes.projecthermes;

import android.util.Log;

import org.spongycastle.crypto.AsymmetricBlockCipher;
import org.spongycastle.crypto.encodings.PKCS1Encoding;
import org.spongycastle.crypto.engines.RSAEngine;
import org.spongycastle.crypto.util.PrivateKeyFactory;
import org.spongycastle.crypto.util.PublicKeyFactory;

import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;


public class Encryption {

    private static final int keySize = 2048;

    public static byte[] encryptString(String dataString, byte[] publicKeyData) {
        byte[] data = dataString.getBytes(Charset.forName("UTF-8"));

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

    public static String decryptString(byte[] encryptedData, byte[] privateKeyData) {
        AsymmetricBlockCipher decryptionCipher = new PKCS1Encoding(new RSAEngine());
        String decryptedString = "";
        try {
            decryptionCipher.init(false, PrivateKeyFactory.createKey(privateKeyData));
            byte[] decryptedData = decryptionCipher.processBlock(encryptedData, 0, encryptedData.length);
            decryptedString = new String(decryptedData, Charset.forName("UTF-8"));
        } catch (Exception e) {
            Log.e("hermes", e.toString());
        }
        return decryptedString;
    }

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(keySize);
            KeyPair keyPair = keyGenerator.generateKeyPair();
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
}

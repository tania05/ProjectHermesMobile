package ca.projecthermes.projecthermes;

import org.junit.Test;
import org.spongycastle.crypto.AsymmetricBlockCipher;
import org.spongycastle.crypto.BlockCipher;
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

import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.Assert.*;

public class EncryptionUnitTests {

    @Test
    public void testRSAKeyGeneration() throws Exception {

        int[] keySizes = {512, 768, 1024, 1280, 1536, 1792, 2048};

        for (int keySize : keySizes) {
            System.out.println("Testing key size " + keySize);

            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(keySize);

            //Generate 3 unique public/private key pairs
            KeyPair keyPair1 = keyGenerator.generateKeyPair();
            PublicKey publicKey1 = keyPair1.getPublic();
            PrivateKey privateKey1 = keyPair1.getPrivate();
            assertNotNull("Should be able get a byte array from the public key", publicKey1.getEncoded());
            assertNotNull("Should be able get a byte array from the private key", privateKey1.getEncoded());

            KeyPair keyPair2 = keyGenerator.generateKeyPair();
            PublicKey publicKey2 = keyPair2.getPublic();
            PrivateKey privateKey2 = keyPair2.getPrivate();
            assertNotNull("Should be able get a byte array from the public key", publicKey2.getEncoded());
            assertNotNull("Should be able get a byte array from the private key", privateKey2.getEncoded());

            KeyPair keyPair3 = keyGenerator.generateKeyPair();
            PublicKey publicKey3 = keyPair3.getPublic();
            PrivateKey privateKey3 = keyPair3.getPrivate();
            assertNotNull("Should be able get a byte array from the public key", publicKey3.getEncoded());
            assertNotNull("Should be able get a byte array from the private key", privateKey3.getEncoded());

            assertNotEquals("Public and private key from same pair should be different", Arrays.toString(publicKey1.getEncoded()), Arrays.toString(privateKey1.getEncoded()));
            assertNotEquals("Public and private key from same pair should be different", Arrays.toString(publicKey2.getEncoded()), Arrays.toString(privateKey2.getEncoded()));
            assertNotEquals("Public and private key from same pair should be different", Arrays.toString(publicKey3.getEncoded()), Arrays.toString(privateKey3.getEncoded()));

            assertNotEquals("Public keys from different pairs should be different", Arrays.toString(publicKey1.getEncoded()), Arrays.toString(publicKey2.getEncoded()));
            assertNotEquals("Public keys from different pairs should be different", Arrays.toString(publicKey2.getEncoded()), Arrays.toString(publicKey3.getEncoded()));
            assertNotEquals("Public keys from different pairs should be different", Arrays.toString(publicKey1.getEncoded()), Arrays.toString(publicKey3.getEncoded()));

            assertNotEquals("Private keys from different pairs should be different", Arrays.toString(privateKey1.getEncoded()), Arrays.toString(privateKey2.getEncoded()));
            assertNotEquals("Private keys from different pairs should be different", Arrays.toString(privateKey2.getEncoded()), Arrays.toString(privateKey3.getEncoded()));
            assertNotEquals("Private keys from different pairs should be different", Arrays.toString(privateKey1.getEncoded()), Arrays.toString(privateKey3.getEncoded()));

            //Cast the public and private keys to RSA public and private keys (so that we can do further testing)
            RSAPublicKey rsaPublicKey1 = (RSAPublicKey) publicKey1;
            RSAPublicKey rsaPublicKey2 = (RSAPublicKey) publicKey2;
            RSAPublicKey rsaPublicKey3 = (RSAPublicKey) publicKey3;

            RSAPrivateKey rsaPrivateKey1 = (RSAPrivateKey) privateKey1;
            RSAPrivateKey rsaPrivateKey2 = (RSAPrivateKey) privateKey2;
            RSAPrivateKey rsaPrivateKey3 = (RSAPrivateKey) privateKey3;

            assertEquals("Public and private keys from same pair should have same modulus", rsaPublicKey1.getModulus(), rsaPrivateKey1.getModulus());
            assertEquals("Public and private keys from same pair should have same modulus", rsaPublicKey2.getModulus(), rsaPrivateKey2.getModulus());
            assertEquals("Public and private keys from same pair should have same modulus", rsaPublicKey3.getModulus(), rsaPrivateKey3.getModulus());

            assertNotEquals("Modulus from different pairs should be different", rsaPublicKey1.getModulus(), rsaPublicKey2.getModulus());
            assertNotEquals("Modulus from different pairs should be different", rsaPublicKey2.getModulus(), rsaPublicKey3.getModulus());
            assertNotEquals("Modulus from different pairs should be different", rsaPublicKey1.getModulus(), rsaPublicKey3.getModulus());

            assertNotEquals("Public and private keys from same pair should have different exponents", rsaPublicKey1.getPublicExponent(), rsaPrivateKey1.getPrivateExponent());
            assertNotEquals("Public and private keys from same pair should have different exponents", rsaPublicKey2.getPublicExponent(), rsaPrivateKey2.getPrivateExponent());
            assertNotEquals("Public and private keys from same pair should have different exponents", rsaPublicKey3.getPublicExponent(), rsaPrivateKey3.getPrivateExponent());
        }
    }

    @Test
    public void testRSAEncryptionDecryption() throws Exception {

        int keySize = 2048;

        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(keySize);
        KeyPair keyPair = keyGenerator.generateKeyPair();

        byte[] publicKeyData = keyPair.getPublic().getEncoded();
        byte[] privateKeyData = keyPair.getPrivate().getEncoded();
        byte[] data = { 0, 1, 5, 3, 13, -20, -20, 0, 121, 2, -3, -32, 11, -32, 0, 5, 20 };

        AsymmetricBlockCipher encryptionCipher = new PKCS1Encoding(new RSAEngine());
        encryptionCipher.init(true, PublicKeyFactory.createKey(publicKeyData));
        byte[] encryptedData = encryptionCipher.processBlock(data, 0, data.length);
        assertNotEquals("Encrypted data should be different from original data", Arrays.toString(data), Arrays.toString(encryptedData));

        AsymmetricBlockCipher decryptionCipher = new PKCS1Encoding(new RSAEngine());
        decryptionCipher.init(false, PrivateKeyFactory.createKey(privateKeyData));
        byte[] decryptedData = decryptionCipher.processBlock(encryptedData, 0, encryptedData.length);
        assertEquals("Decrypted data should be the same as the original data", Arrays.toString(data), Arrays.toString(decryptedData));
    }

    @Test
    public void testRSAEncryptionOfAESKey() throws Exception {

        //The purpose of this test is to demonstrate that we can encrypt an AES key using RSA.
        //Although RSA has a maximum data size that can be encrypted, AES does not. So the idea is that
        //the message we send across the P2P network will consist of the following:
        // - an AES key that is encrypted under RSA using user B's public key
        // - the actual message to be sent that is encrypted under the AES key
        //
        // When user B receives the message, he will decrypt the AES key using his private key.
        // User B can then decrypt the actual message using the AES key that has now been decrypted.
        // This lets User A send large amounts of data instead of being limited to sending no more than
        // the length of the RSA key.

        int keySize = 2048;

        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(keySize);
        KeyPair keyPair = keyGenerator.generateKeyPair();

        int aesKeySize = 256; //Must be 128, 192, or 256

        KeyGenerator aesKeyGenerator = KeyGenerator.getInstance("AES");
        aesKeyGenerator.init(aesKeySize);
        Key key = aesKeyGenerator.generateKey();
        byte[] aesKeyData = key.getEncoded();

        byte[] publicKeyData = keyPair.getPublic().getEncoded();
        byte[] privateKeyData = keyPair.getPrivate().getEncoded();

        AsymmetricBlockCipher encryptionCipher = new PKCS1Encoding(new RSAEngine());
        encryptionCipher.init(true, PublicKeyFactory.createKey(publicKeyData));
        byte[] encryptedData = encryptionCipher.processBlock(aesKeyData, 0, aesKeyData.length);
        assertNotEquals("Encrypted data should be different from original AES key", Arrays.toString(aesKeyData), Arrays.toString(encryptedData));

        AsymmetricBlockCipher decryptionCipher = new PKCS1Encoding(new RSAEngine());
        decryptionCipher.init(false, PrivateKeyFactory.createKey(privateKeyData));
        byte[] decryptedData = decryptionCipher.processBlock(encryptedData, 0, encryptedData.length);
        assertEquals("Decrypted data should be the same as the original AES key", Arrays.toString(aesKeyData), Arrays.toString(decryptedData));
    }

    @Test
    public void testRSAEncryptionDecryptionStrings() throws Exception {

        int keySize = 2048;

        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(keySize);
        KeyPair keyPair = keyGenerator.generateKeyPair();

        String dataString = "This is some message that should be kept secret";
        byte[] data = dataString.getBytes(Charset.forName("UTF-8"));

        byte[] publicKeyData = keyPair.getPublic().getEncoded();
        byte[] privateKeyData = keyPair.getPrivate().getEncoded();

        AsymmetricBlockCipher encryptionCipher = new PKCS1Encoding(new RSAEngine());
        encryptionCipher.init(true, PublicKeyFactory.createKey(publicKeyData));
        byte[] encryptedData = encryptionCipher.processBlock(data, 0, data.length);
        assertNotEquals("Encrypted data should be different from original data", Arrays.toString(data), Arrays.toString(encryptedData));

        AsymmetricBlockCipher decryptionCipher = new PKCS1Encoding(new RSAEngine());
        decryptionCipher.init(false, PrivateKeyFactory.createKey(privateKeyData));
        byte[] decryptedData = decryptionCipher.processBlock(encryptedData, 0, encryptedData.length);
        String decryptedString = new String(decryptedData, Charset.forName("UTF-8"));
        assertEquals("Decrypted data should be the same as the original data", Arrays.toString(data), Arrays.toString(decryptedData));
        assertEquals("Decrypted string should be the same as the original string", dataString, decryptedString);
    }

    @Test
    public void testRSAEncryptionDecryptionBadPrivateKey() throws Exception {

        int keySize = 2048;

        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(keySize);
        KeyPair goodKeyPair = keyGenerator.generateKeyPair();

        keyGenerator.initialize(keySize);
        KeyPair badKeyPair = keyGenerator.generateKeyPair();

        byte[] publicKeyData = goodKeyPair.getPublic().getEncoded();
        byte[] privateKeyData = badKeyPair.getPrivate().getEncoded();
        byte[] data = { 0, 1, 5, 3, 13, -20, -20, 0, 121, 2, -3, -32, 11, -32, 0, 5, 20 };

        AsymmetricBlockCipher encryptionCipher = new PKCS1Encoding(new RSAEngine());
        encryptionCipher.init(true, PublicKeyFactory.createKey(publicKeyData));
        byte[] encryptedData = encryptionCipher.processBlock(data, 0, data.length);
        assertNotEquals("Encrypted data should be different from original data", Arrays.toString(data), Arrays.toString(encryptedData));


        try {
            AsymmetricBlockCipher decryptionCipher = new PKCS1Encoding(new RSAEngine());
            decryptionCipher.init(false, PrivateKeyFactory.createKey(privateKeyData));
            byte[] decryptedData = decryptionCipher.processBlock(encryptedData, 0, encryptedData.length);

            //Following assert probably shouldn't happen because an exception would be thrown, but just in case
            //the decryption is "successful", we will assert that the decrypted data does not actually match the orignal
            assertNotEquals("Decrypted data using invalid private key should not result in original data", Arrays.toString(data), Arrays.toString(decryptedData));
        }
        catch(InvalidCipherTextException e) {
            //Makes sense that this would happen
        }
    }

    @Test
    public void testAESEncryptionDecryption() throws Exception {
        int keySize = 256; //Must be 128, 192, or 256

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySize);
        Key key = keyGenerator.generateKey();
        byte[] keyData = key.getEncoded();

        //1MB of data to encrypt/decrypt
        byte[] data = new byte[1024*1024];
        for(int i = 0; i < data.length; ++i) {
            data[i] = (byte) i;
        }

        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
        cipher.init(true, new KeyParameter(keyData));
        byte[] encryptedData = new byte[cipher.getOutputSize(data.length)];
        int bytesProcessed = 0;
        bytesProcessed += cipher.processBytes(data, 0, data.length, encryptedData, 0);
        cipher.doFinal(encryptedData, bytesProcessed);

        //byte[] encryptedData = cipher.doFinal(data);
        assertNotEquals("Encrypted data should be different from original data", Arrays.toString(data), Arrays.toString(encryptedData));

        cipher.init(false, new KeyParameter(keyData));
        byte[] decryptedData = new byte[cipher.getOutputSize(encryptedData.length)];
        bytesProcessed = 0;
        bytesProcessed += cipher.processBytes(encryptedData, 0, encryptedData.length, decryptedData, 0);
        bytesProcessed += cipher.doFinal(decryptedData, bytesProcessed);

        decryptedData = Arrays.copyOf(decryptedData, bytesProcessed); //Strips away any padding that is leftover

        assertEquals("Decrypted data should be the same as the original data", Arrays.toString(data), Arrays.toString(decryptedData));
    }

    @Test
    public void testAESEncryptionDecryptionBadKey() throws Exception {
        int keySize = 256; //Must be 128, 192, or 256

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySize);

        Key goodKey = keyGenerator.generateKey();
        byte[] goodKeyData = goodKey.getEncoded();

        Key badKey = keyGenerator.generateKey();
        byte[] badKeyData = badKey.getEncoded();

        //1MB of data to encrypt/decrypt
        byte[] data = new byte[1024*1024];
        for(int i = 0; i < data.length; ++i) {
            data[i] = (byte) i;
        }

        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), new PKCS7Padding());
        cipher.init(true, new KeyParameter(goodKeyData));
        byte[] encryptedData = new byte[cipher.getOutputSize(data.length)];
        int bytesProcessed = 0;
        bytesProcessed += cipher.processBytes(data, 0, data.length, encryptedData, 0);
        cipher.doFinal(encryptedData, bytesProcessed);

        assertNotEquals("Encrypted data should be different from original data", Arrays.toString(data), Arrays.toString(encryptedData));

        cipher.init(false, new KeyParameter(badKeyData));
        byte[] decryptedData = new byte[cipher.getOutputSize(encryptedData.length)];
        bytesProcessed = 0;

        try {
            bytesProcessed += cipher.processBytes(encryptedData, 0, encryptedData.length, decryptedData, 0);
            bytesProcessed += cipher.doFinal(decryptedData, bytesProcessed);

            decryptedData = Arrays.copyOf(decryptedData, bytesProcessed); //Strips away any padding that is leftover

            //Following assert probably shouldn't happen because an exception would be thrown, but just in case
            //the decryption is "successful", we will assert that the decrypted data does not actually match the orignal
            assertNotEquals("Decrypted data using invalid key should not result in original data", Arrays.toString(data), Arrays.toString(decryptedData));
        } catch(InvalidCipherTextException e) {
            //Expected
        }
    }
}

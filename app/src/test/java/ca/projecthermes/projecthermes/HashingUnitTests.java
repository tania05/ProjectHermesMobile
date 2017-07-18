package ca.projecthermes.projecthermes;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class HashingUnitTests {

    private static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    //This is primarily a demo of what the smart contract expects in terms of hashing
    //message IDs combined with nonces.
    @Test
    public void testSha256() throws Exception {

        UUID messageId = UUID.randomUUID();
        byte[] messageIdBytes = getBytesFromUUID(messageId);

        assertEquals("Nonce should be 16 bytes", 16, messageIdBytes.length);

        UUID nonce = UUID.randomUUID();
        byte[] nonceBytes = getBytesFromUUID(nonce);

        assertEquals("Nonce should be 16 bytes", 16, nonceBytes.length);

        byte[] messageIdWithNonceBytes = new byte[32];

        //Copy messageIdBytes into first 16 positions of messageIdWithNonceBytes
        System.arraycopy(messageIdBytes, 0, messageIdWithNonceBytes, 0, 16);

        //Copy nonceBytes into last 16 positions of messageIdWithNonceBytes
        System.arraycopy(nonceBytes, 0, messageIdWithNonceBytes, 16, 16);

        assertArrayEquals(messageIdBytes, Arrays.copyOfRange(messageIdWithNonceBytes, 0, 16));
        assertArrayEquals(nonceBytes, Arrays.copyOfRange(messageIdWithNonceBytes, 16, 32));


        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(messageIdWithNonceBytes);
        byte[] hashedMessageIdWithNonce = md.digest();


        //When you call newMessage(), which is part of the smart contract,
        //you will send the messageIdBytes hashedMessageIdWithNonce (there are actually 2
        //hashedMessageIdWithNonce values you will send: 1 that used the public nonce and
        //the other that uses the private nonce)

        //When you call addHop(), which is part of the smart contract,
        //you will send the messageIdBytes and nonceBytes (where nonceBytes is the public nonce).
        //The smart contract will use these two values in order to compute the SHA-256 hash and compare
        //with the hash that was sent as part of newMessage()

        //When you call receiveMessage(), which is part of the smart contract,
        //you will send the messageIdBytes and nonceBytes (where nonceBytes is the
        //private nonce). The smart contract will use these two values in order to
        //compute the SHA-256 hash and compare with the hash that was sent as part of newMessage()
    }

    //Tests that the hash computed in Android matches what Solidity computes
    @Test
    public void testSha256MatchesSolidity() throws Exception {

        byte[] messageId = {
                0x12, 0x3e, 0x45, 0x67,
                (byte) 0xe8, (byte) 0x9b, 0x12, (byte) 0xd3,
                (byte) 0xa4, 0x56, 0x42, 0x66,
                0x55, 0x44, 0x00, 0x00
        };

        byte[] nonce = {
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00
        };

        byte[] messageIdWithNonce = new byte[32];

        byte[] hashFromSolidity = {
                (byte) 0xad, 0x28, (byte) 0xa8, 0x23,
                (byte) 0x9d, 0x37, 0x49, (byte) 0x89,
                0x3a, (byte) 0xb9, (byte) 0x8b, (byte) 0xf5,
                0x52, 0x0a, (byte) 0xf8, 0x5f,
                (byte) 0xbd, 0x1b, (byte) 0x96, (byte) 0xe4,
                0x6f, (byte) 0xb1, 0x6a, 0x68,
                0x0c, 0x76, (byte) 0xe0, 0x3b,
                0x6c, 0x4f, (byte) 0x9e, 0x0d
        };

        //Copy messageIdBytes into first 16 positions of messageIdWithNonceBytes
        System.arraycopy(messageId, 0, messageIdWithNonce, 0, 16);

        //Copy nonceBytes into last 16 positions of messageIdWithNonceBytes
        System.arraycopy(nonce, 0, messageIdWithNonce, 16, 16);

        assertArrayEquals(messageId, Arrays.copyOfRange(messageIdWithNonce, 0, 16));
        assertArrayEquals(nonce, Arrays.copyOfRange(messageIdWithNonce, 16, 32));



        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(messageIdWithNonce);
        byte[] hashedMessageIdWithNonce = md.digest();

        assertArrayEquals(hashFromSolidity, hashedMessageIdWithNonce);
    }
}
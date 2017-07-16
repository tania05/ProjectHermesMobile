package ca.projecthermes.projecthermes.networking.payload;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

import static ca.projecthermes.projecthermes.util.Encryption.encryptString;

public class Message implements IPayload {
    private static final Charset CHARSET = Charset.forName("UTF-16");
    public static final byte[] VALID_VERIFIER = "CAFEBABE-DEADBEEF".getBytes(CHARSET);

    public byte[] identifier;
    public byte[] verifier;
    public byte[] key;
    public byte[] body;

    public Message(byte[] identifier, byte[] verifier, byte[] key, byte[] body) {
        this.identifier = identifier;
        this.verifier = verifier;
        this.key = key;
        this.body = body;
    }

    public static byte[] generateIdentifier() {
        return UUID.randomUUID().toString().getBytes(CHARSET);
    }

    public static byte[] getValidVerifier(byte[] publicKey) {
        return  encryptString(VALID_VERIFIER, publicKey);
    }

    @Override
    public String toString() {
        return "{Message [identifier]: " + Arrays.toString(identifier) + " [verifier.len] : " + verifier.length + " [body.length] " + body.length;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Message)) {
            return false;
        }

        Message otherMessage = (Message) other;

        if (!Arrays.equals(this.identifier, otherMessage.identifier)) {
            return false;
        }
        if (!Arrays.equals(this.verifier, otherMessage.verifier)) {
            return false;
        }
        if (!Arrays.equals(this.key, otherMessage.key)) {
            return false;
        }
        if (!Arrays.equals(this.body, otherMessage.body)) {
            return false;
        }

        return true;
    }

}

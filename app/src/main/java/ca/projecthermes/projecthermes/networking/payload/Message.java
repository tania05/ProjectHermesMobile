package ca.projecthermes.projecthermes.networking.payload;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

public class Message implements IPayload {
    private static final Charset CHARSET = Charset.forName("UTF-16");
    private static final byte[] VALID_VERIFIER = "CAFEBABE-DEADBEEF".getBytes(CHARSET);

    public byte[] identifier;
    public byte[] verifier;
    public byte[] body;

    public Message(byte[] identifier, byte[] verifier, byte[] body) {
        this.identifier = identifier;
        this.verifier = verifier;
        this.body = body;
    }

    public static byte[] generateIdentifier(byte[] body) {
//        byte[] identifier = new byte[256];
//        SHA3Digest md = new SHA3Digest(256);
//        md.update(body, 0, body.length);
//        md.doFinal(identifier, 0);
//        Log.d("From Message.class", new String(identifier));
//        return identifier;
        return UUID.randomUUID().toString().getBytes(Charset.forName("US-ASCII"));
    }

    public static byte[] getValidVerifier(byte[] publicKey) {
        //XXX
        return VALID_VERIFIER;
//        return  encryptString(VALID_VERIFIER, publicKey);
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
        if (!Arrays.equals(this.body, otherMessage.body)) {
            return false;
        }

        return true;
    }

}

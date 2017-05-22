package ca.projecthermes.projecthermes.networking.payload;

import java.util.Arrays;

public class Message implements IPayload {
    public byte[] identifier;
    public byte[] verifier;
    public byte[] body;

    public Message(byte[] identifier, byte[] verifier, byte[] body) {
        this.identifier = identifier;
        this.verifier = verifier;
        this.body = body;
    }

    @Override
    public String toString() {
        return "{Message [identifier]: " + Arrays.toString(identifier) + " [verifier.len] : " + verifier.length + " [body.length] " + body.length;
    }
}

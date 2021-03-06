package ca.projecthermes.projecthermes.data;

import java.util.ArrayList;

import ca.projecthermes.projecthermes.networking.payload.Message;

public interface IMessageStore {
    ArrayList<byte[]> getStoredMessageIdentifiers();
    Message getMessageForIdentifier(byte[] identifier);
    void storeMessage(Message m);
}
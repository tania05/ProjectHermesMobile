package ca.projecthermes.projecthermes.Ethereum;

import android.content.Context;
import android.util.Log;

import org.ethereum.geth.Account;
import org.ethereum.geth.BigInt;

import java.math.BigInteger;
import java.util.Timer;
import java.util.TimerTask;

import ca.projecthermes.projecthermes.util.Encryption;
import io.ethmobile.ethdroid.EthDroid;
import io.ethmobile.ethdroid.KeyManager;
import io.ethmobile.ethdroid.model.Balance;

/**
 * Created by abc on 2017-07-20.
 */

public class Ethereum {
    public static EthDroid eth;
    public static KeyManager keyManager;
    private Account account;
    private final String TAG = this.getClass().getSimpleName();
    private static Ethereum instance;
    private final String SmartContractAddress = "0xAc7d48eb7Ca5bcd18a03c3C517EA1238D80D1cf4";
    private final BigInt gasAmount = new BigInt(2000000);
    private final long msgCost = (long) 1e17;

    private Ethereum(Context context) {
        try {
            String datadir = context.getFilesDir().getAbsolutePath();

            keyManager = KeyManager.newKeyManager(datadir);
            if (keyManager.getAccounts().size() == 0) {
                keyManager.newAccount("password");
            }
            account = keyManager.getAccounts().get(0);

            eth = new EthDroid.Builder(datadir)
                    .onRinkeby()
                    .withKeyManager(keyManager)
                    .build();

            eth.setMainAccountAtIndex(0);
            eth.start();

            Log.d(TAG, "Wallet Address: " + account.getAddress().getHex());


        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Ethereum class couldn't initialize");
        }
    }

    public static Ethereum getInstance(Context context) {
        if (instance == null) {
            instance = new Ethereum(context);
        }
        return instance;
    }

    public void newMessage(byte[] msgId, byte[] publicNonce, byte[] privateNonce) {
        try {

            Log.e(TAG, "msgId: " + String.format("%32x", new BigInteger(1, msgId)));
            Log.e(TAG, "publicNonce: " + String.format("%32x", new BigInteger(1, publicNonce)));
            Log.e(TAG, "privateNonce: " + String.format("%32x", new BigInteger(1, privateNonce)));

            byte[] hashedPublicNonce = Encryption.hashNonceWithMsgId(msgId, publicNonce);
            byte[] hashedPrivateNonce = Encryption.hashNonceWithMsgId(msgId, privateNonce);

            Log.e(TAG, "hashedPrivateNonce: " + String.format("%64x", new BigInteger(1, hashedPrivateNonce)));
            Log.e(TAG, "hashedPublicNonce: " + String.format("%64x", new BigInteger(1, hashedPublicNonce)));


            String encodedData = "0x324120650000000000000000000000000000000000000000000000000000000000000060"
                    + String.format("%-64x", new BigInteger(1, hashedPublicNonce)).replace(' ', '0')
                    + String.format("%-64x", new BigInteger(1, hashedPrivateNonce)).replace(' ', '0')
                    + String.format("%064x", 32)
                    + hexToString(msgId);

            Log.e(TAG, "New Message, Encoded data: " + encodedData);
            callContractFunction(encodedData, msgCost);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "new Message failed");
        }
    }

    public static String hexToString(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
        //String textStr = String.format("%32x", new BigInteger(1, text));

        //StringBuilder textSb = new StringBuilder();
        //for (int i=0; i<textStr.length(); i++) {
        //    textSb.append(Integer.toHexString((int) textStr.charAt(i)));
        //}
        //return textSb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public void addHop(final byte[] msgId, final byte[] publicNonce) {

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        String encodedData = "0x79a90dfd0000000000000000000000000000000000000000000000000000000000000040"
                                + "0000000000000000000000000000000000000000000000000000000000000080"
                                + String.format("%064x", 32)
                                + hexToString(msgId)
                                + String.format("%064x", 32)
                                + hexToString(publicNonce);
                        Log.e(TAG, "Encoded data: " + encodedData);

                        callContractFunction(encodedData, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "addHop failed");
                    }
                }
            }, 200000);


    }
    public void receiveMessage(byte[] msgId, byte[] privateNonce) {
        try {
            String encodedData = "0x96e03dd30000000000000000000000000000000000000000000000000000000000000040"
                    + "0000000000000000000000000000000000000000000000000000000000000080"
                    + String.format("%064x", 32)
                    + hexToString(msgId)
                    + String.format("%064x", 32) + hexToString(privateNonce);

            callContractFunction(encodedData, 0);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "receiveMessage failed");
        }
    }


    private void callContractFunction(String encodedData, long value) throws Exception {

            keyManager.unlockAccount(account, "password"); //TODO: ask user for password
            eth.newTransaction()
                    .to(SmartContractAddress)
                    .gasAmount(gasAmount)
                    .value(value)
                    .data(encodedData)
                    .send();
    }

    public Balance getBalance() throws Exception {
        return eth.getBalance();
    }

}

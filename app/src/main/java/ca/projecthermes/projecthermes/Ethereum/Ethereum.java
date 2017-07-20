package ca.projecthermes.projecthermes.Ethereum;

import android.content.Context;
import android.util.Log;

import org.ethereum.geth.Account;
import org.ethereum.geth.BigInt;

import java.math.BigInteger;

import io.ethmobile.ethdroid.EthDroid;
import io.ethmobile.ethdroid.KeyManager;

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
    private final BigInt gasAmount = new BigInt(400000);
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

    public void newMessage(String msgId, int privateNonce, int publicNonce) {
        try {
            String encodedData = "0x324120650000000000000000000000000000000000000000000000000000000000000060"
                    + String.format("%-64x", publicNonce).replace(' ', '0')
                    + String.format("%-64x", privateNonce).replace(' ', '0')
                    + String.format("%64x", 32).replace(' ', '0')
                    + String.format("%32x", new BigInteger(1, msgId.replaceAll("-", "").getBytes("US-ASCII")));

            keyManager.unlockAccount(account, "password"); //TODO: ask user for password
            eth.newTransaction()
                    .to(SmartContractAddress)
                    .gasAmount(gasAmount)
                    .value(msgCost)
                    .data(encodedData)
                    .send();
            Log.e(TAG, "MSG UUID: " + msgId);
            Log.e(TAG, "privateNonce : " + privateNonce);
            Log.e(TAG, "publicNonce: " + publicNonce);

            Log.e(TAG, "Encoded data: " + encodedData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "new Message failed");
        }
    }
    public void addHop(String msgId, int publicNonce) {}
    public void receiveMessage(String msgId, int privateNonce) {}

}

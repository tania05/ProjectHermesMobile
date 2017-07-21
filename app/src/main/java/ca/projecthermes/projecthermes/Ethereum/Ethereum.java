package ca.projecthermes.projecthermes.Ethereum;

import android.content.Context;
import android.util.Log;

import org.ethereum.geth.Account;

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

            Log.d(TAG, "Address: " + account.getAddress().getHex());


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

    public void newMessage(String msgId, int privateNonce, int publicNonce) {}
    public void addHop(String msgId, int publicNonce) {}
    public void receiveMessage(String msgId, int privateNonce) {}

    public Balance getBalance() throws Exception {
        return eth.getBalance();
    }

}

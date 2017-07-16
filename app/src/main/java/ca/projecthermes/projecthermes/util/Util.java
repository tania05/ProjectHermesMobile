package ca.projecthermes.projecthermes.util;

import ca.projecthermes.projecthermes.exceptions.InvokerFailException;

public class Util {
    public static boolean equal(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }

    public static void sleepRetryInvoker(int timeout, int tries, IInvokerCallback callback) throws InvokerFailException {
        for (int i = 0; i < tries; i++) {
            try {
                callback.call();
                return;
            } catch (Exception e) {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException ignored) {
                }
            }
        }

        throw new InvokerFailException();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public interface IInvokerCallback {
        void call() throws Exception;
    }

}

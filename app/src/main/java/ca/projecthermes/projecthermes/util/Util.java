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

    public interface IInvokerCallback {
        void call() throws Exception;
    }

}

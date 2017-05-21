package ca.projecthermes.projecthermes.util;

import ca.projecthermes.projecthermes.IHermesLogger;

/**
 * Created by Brandon Mabey on 2017-05-21.
 */

public class SystemLogger implements IHermesLogger {
    @Override
    public void v(String msg) {
        System.out.println("v: " + msg);
    }

    @Override
    public void d(String msg) {
        System.out.println("d: " + msg);
    }

    @Override
    public void i(String msg) {
        System.out.println("i: " + msg);
    }

    @Override
    public void w(String msg) {
        System.out.println("w: " + msg);
    }

    @Override
    public void e(String msg) {
        System.out.println("e: " + msg);
    }

    @Override
    public void wtf(String msg) {
        System.out.println("wtf: " + msg);
    }

    @Override
    public IHermesLogger withTag(String newTag) {
        return null;
    }
}

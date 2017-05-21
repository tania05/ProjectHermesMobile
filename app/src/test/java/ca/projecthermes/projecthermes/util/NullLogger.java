package ca.projecthermes.projecthermes.util;

import ca.projecthermes.projecthermes.IHermesLogger;

/**
 * Created by brand_000 on 2017-05-17.
 */

public class NullLogger implements IHermesLogger {
    @Override
    public void v(String msg) {

    }

    @Override
    public void d(String msg) {

    }

    @Override
    public void i(String msg) {

    }

    @Override
    public void w(String msg) {

    }

    @Override
    public void e(String msg) {

    }

    @Override
    public void wtf(String msg) {

    }

    @Override
    public IHermesLogger withTag(String newTag) {
        return this;
    }
}

package ca.projecthermes.projecthermes;

import android.util.Log;

public class HermesLogger implements IHermesLogger {
    private final String _tag;

    public HermesLogger(String tag) {
        _tag = tag;
    }

    @Override
    public void v(String msg) {
        Log.v(_tag, msg);
    }

    @Override
    public void d(String msg) {
        Log.d(_tag, msg);
    }

    @Override
    public void i(String msg) {
        Log.i(_tag, msg);
    }

    @Override
    public void w(String msg) {
        Log.w(_tag, msg);
    }

    @Override
    public void e(String msg) {
        Log.e(_tag, msg);
    }

    @Override
    public void wtf(String msg) {
        Log.wtf(_tag, msg);
    }

    @Override
    public IHermesLogger withTag(String newTag) {
        return new HermesLogger(newTag);
    }
}

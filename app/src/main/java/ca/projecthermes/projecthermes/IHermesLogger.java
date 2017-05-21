package ca.projecthermes.projecthermes;

public interface IHermesLogger {
    void v(String msg);
    void d(String msg);
    void i(String msg);
    void w(String msg);
    void e(String msg);
    void wtf(String msg);

    /**
     * Gets a new logger instance with a different tag.
     * @param newTag The new tag to use.
     * @return A new IHermesLogger that has the new tag.
     */
    IHermesLogger withTag(String newTag);
}

package cm.aptoide.ptdev.parser;

import java.util.concurrent.FutureTask;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 23-10-2013
 * Time: 11:04
 * To change this template use File | Settings | File Templates.
 */
public abstract class RunnableWithPriority implements Runnable {


    public Integer getPriority() {
        return priority;
    }

    private final int priority;

    public RunnableWithPriority(int priority){
        this.priority = priority;
    }

    @Override  public abstract void run();
}

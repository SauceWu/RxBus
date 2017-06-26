package me.sauce;


/**
 * Created by Android on 2016/6/8.
 */
public enum EventThread {
    /**
     * 主线程
     */
    MAIN_THREAD,
    /**
     * 新的线程
     */
    NEW_THREAD,
    /**
     * 读写线程
     */
    IO,
    /**
     * 计算工作默认线程
     */
    COMPUTATION,
    /**
     * 在当前线程中按照队列方式执行
     */
    TRAMPOLINE;



    public static String getScheduler(EventThread threadMode) {
        String scheduler;
        switch (threadMode) {
            case MAIN_THREAD:
                scheduler = "io.reactivex.android.schedulers.AndroidSchedulers.mainThread()";
                break;
            case NEW_THREAD:
                scheduler = "io.reactivex.schedulers.Schedulers.newThread()";
                break;
            case IO:
                scheduler = " io.reactivex.schedulers. Schedulers.io()";
                break;
            case COMPUTATION:
                scheduler = "io.reactivex.schedulers.Schedulers.computation()";
                break;
            case TRAMPOLINE:
                scheduler = "io.reactivex.schedulers.Schedulers.trampoline()";
                break;
            default:
                scheduler = "io.reactivex.schedulers.AndroidSchedulers.mainThread()";
        }
        return scheduler;
    }
}

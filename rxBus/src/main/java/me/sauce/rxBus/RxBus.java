package me.sauce.rxBus;

import android.support.annotation.NonNull;


import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import me.sauce.RxBusTag;
import me.sauce.rxBus.pojo.Message;

/**
 * Created by sauce on 2017/5/15.
 * Version 1.0.0
 */
public class RxBus {

    private static RxBus instance;

    public static RxBus getInstance() {
        if (instance == null) {
            synchronized (RxBus.class) {
                if (instance == null) {
                    instance = new RxBus();
                }
            }
        }
        return instance;
    }

    //TAG默认值



    //TAG-class
    private static Map<Class, Integer> tag4Class;
    //发布者
    private final Subject<Object> bus;

    //存放订阅者信息
    protected Map<Object, CompositeDisposable> subscriptions = new HashMap<>();

    /**
     * PublishSubject 创建一个可以在订阅之后把数据传输给订阅者Subject
     */
    public RxBus() {
        bus = PublishSubject.create().toSerialized();
    }

    public void post(int code) {
        post(code, new Object());
    }

    public void post(@NonNull Object obj) {
        post(RxBusTag.TAG_DEFAULT, obj);
    }

    /**
     * 发布事件
     *ad
     * @param code 值使用RxBus.getInstance().getTag(class,value)获取
     * @param obj  为需要被处理的事件
     */
    public void post(int code, @NonNull Object obj) {
        bus.onNext(new Message(code, obj));
    }


    public <T> Observable<T> toObservable(Class<T> eventType) {
        return toObservable(RxBusTag.TAG_DEFAULT, eventType);
    }

    /**
     * 订阅事件
     *
     * @return
     */

    public Observable<Object> toObservable(final int code) {
        return bus.ofType(Message.class)//判断接收事件类型
                .filter(new Predicate<Message>() {
                    @Override
                    public boolean test(Message Message) throws Exception {
                        return Message.code == code;
                    }
                })
                .map(new Function<Message, Object>() {
                    @Override
                    public Object apply(Message Message) throws Exception {
                        return Message.object;
                    }
                });
    }

    public <T> Observable<T> toObservable(final int code, final Class<T> eventType) {
        return bus.ofType(Message.class)//判断接收事件类型
                .filter(new Predicate<Message>() {
                    @Override
                    public boolean test(Message Message) throws Exception {
                        return Message.code == code;
                    }
                })
                .map(new Function<Message, Object>() {
                    @Override
                    public Object apply(Message Message) throws Exception {
                        return Message.object;
                    }
                })
                .cast(eventType);
    }


    /**
     * 添加订阅者到map空间来unRegister
     *
     * @param subscriber 订阅者
     * @param disposable 订阅者 Subscription
     */
    protected void putSubscriptionsData(Object subscriber, Disposable disposable) {
        CompositeDisposable subs = subscriptions.get(subscriber);
        if (subs == null) {
            subs = new CompositeDisposable();
        }
        subs.add(disposable);
        subscriptions.put(subscriber, subs);
    }


    /**
     * 解除订阅者
     *
     * @param subscriber 订阅者
     */
    public void unRegister(Object subscriber) {
        CompositeDisposable compositeDisposable;
        if (subscriber != null) {
            compositeDisposable = subscriptions.get(subscriber);
            if (compositeDisposable != null) {
                compositeDisposable.dispose();
                subscriptions.remove(subscriber);
            }
        }
    }
}

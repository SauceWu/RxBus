package me.sauce.rxBus.pojo;


public class Message {
    public int code;
    public Object object;

    public Message(int code, Object object){
        this.code = code;
        this.object = object;
    }

}

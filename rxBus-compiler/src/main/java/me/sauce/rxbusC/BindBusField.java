package me.sauce.rxbusC;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import me.sauce.EventThread;
import me.sauce.Subscribe;

public class BindBusField {

    private ExecutableElement mFieldElement;
    private int mTag;
    private final EventThread mThread;

    public BindBusField(Element element) throws IllegalArgumentException {
        if (element.getKind() != ElementKind.METHOD) {
            throw new IllegalArgumentException(
                    String.format("Only fields can be annotated with @%s", Subscribe.class.getSimpleName()));
        }

        mFieldElement = (ExecutableElement) element;
        Subscribe bindView = mFieldElement.getAnnotation(Subscribe.class);
        mTag = bindView.tag();
        mThread = bindView.thread();
    }

    int getTag() {
        return mTag;
    }

    TypeMirror getFieldType() {
        return mFieldElement.asType();
    }

    Name getFieldName() {
        return mFieldElement.getSimpleName();
    }


    List<? extends VariableElement> getParameters() {
        return mFieldElement.getParameters();
    }

    public String getFieldThread() {
        return EventThread.getScheduler(mThread);
    }
}

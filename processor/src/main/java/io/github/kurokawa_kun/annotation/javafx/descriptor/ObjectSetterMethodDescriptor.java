package io.github.kurokawa_kun.annotation.javafx.descriptor;

public class ObjectSetterMethodDescriptor extends MethodDescriptor
{
    public ObjectSetterMethodDescriptor(String targetMethodName)
    {
        super();
        this.setFormat(Format.makeSetterInstance());
        this.logic = Logic.makeVoidObjectLogic();
        this.logic.setTargetMethodName(targetMethodName);
        this.setLogic(this.logic);
    }
}

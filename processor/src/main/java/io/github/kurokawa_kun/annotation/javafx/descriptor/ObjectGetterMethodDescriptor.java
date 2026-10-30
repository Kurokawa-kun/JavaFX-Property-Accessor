package io.github.kurokawa_kun.annotation.javafx.descriptor;

public class ObjectGetterMethodDescriptor extends MethodDescriptor
{
    public ObjectGetterMethodDescriptor(String targetMethodName)
    {
        super();
        this.setFormat(Format.makeGetterInstance());
        this.logic = Logic.makeReturnObjectLogic();
        this.logic.setTargetMethodName(targetMethodName);
        this.setLogic(this.logic);
    }
}

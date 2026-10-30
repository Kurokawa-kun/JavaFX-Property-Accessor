package io.github.kurokawa_kun.annotation.javafx.descriptor;

public class ObjectBooleanGetterMethodDescriptor extends MethodDescriptor
{
    public ObjectBooleanGetterMethodDescriptor(String targetMethodName)
    {
        super();
        this.setFormat(Format.makeGetterBooleanInstance());
        this.logic = Logic.makeReturnObjectLogic();
        this.logic.setTargetMethodName(targetMethodName);
        this.setLogic(this.logic);
    }
}

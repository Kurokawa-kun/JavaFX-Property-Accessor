package io.github.kurokawa_kun.annotation.javafx.descriptor;

public class PrimitiveSetterMethodDescriptor extends MethodDescriptor
{
    public PrimitiveSetterMethodDescriptor(String targetMethodName)
    {
        super();
        this.setFormat(Format.makeSetterInstance());
        this.logic = Logic.makeVoidPrimitiveLogic();
        this.logic.setTargetMethodName(targetMethodName);
        this.setLogic(this.logic);
    }
}

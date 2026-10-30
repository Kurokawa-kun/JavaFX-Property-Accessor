package io.github.kurokawa_kun.annotation.javafx.descriptor;

public class PrimitiveGetterMethodDescriptor extends MethodDescriptor
{
    public PrimitiveGetterMethodDescriptor(String targetMethodName)
    {
        super();
        this.setFormat(Format.makeGetterInstance());
        this.logic = Logic.makeReturnPrimitiveLogic();
        this.logic.setTargetMethodName(targetMethodName);
        this.setLogic(this.logic);
    }
}

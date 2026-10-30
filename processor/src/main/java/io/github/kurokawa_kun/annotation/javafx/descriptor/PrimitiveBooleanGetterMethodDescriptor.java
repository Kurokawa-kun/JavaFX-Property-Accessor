package io.github.kurokawa_kun.annotation.javafx.descriptor;

public class PrimitiveBooleanGetterMethodDescriptor extends MethodDescriptor
{
    public PrimitiveBooleanGetterMethodDescriptor(String targetMethodName)
    {
        super();
        this.setFormat(Format.makeGetterBooleanInstance());
        this.logic = Logic.makeReturnPrimitiveLogic();
        this.logic.setTargetMethodName(targetMethodName);
        this.setLogic(this.logic);
    }
}

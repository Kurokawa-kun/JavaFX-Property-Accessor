package io.github.kurokawa_kun.annotation.javafx.descriptor;

public class GenericObjectGetterMethodDescriptor extends MethodDescriptor
{
    public GenericObjectGetterMethodDescriptor(String targetMethodName, String genericTypeNames)
    {
        super();
        this.setFormat(Format.makeGetterInstance());
        this.logic = Logic.makeReturnGenericObjectLogic();
        this.logic.setTargetMethodName(targetMethodName);
        this.logic.setGenericTypeNames(genericTypeNames);
    }
}

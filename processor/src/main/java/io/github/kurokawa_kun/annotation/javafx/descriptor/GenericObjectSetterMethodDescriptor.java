package io.github.kurokawa_kun.annotation.javafx.descriptor;

public class GenericObjectSetterMethodDescriptor extends MethodDescriptor
{
    public GenericObjectSetterMethodDescriptor(String targetMethodName, String genericTypeNames)
    {
        super();
        this.setFormat(Format.makeSetterInstance());
        this.logic = Logic.makeVoidGenericObjectLogic();
        this.logic.setTargetMethodName(targetMethodName);
        this.logic.setGenericTypeNames(genericTypeNames);
    }
}

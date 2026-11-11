package io.github.kurokawa_kun.annotation.javafx.descriptor;

public class GenericObjectReadOnlyPropertyMethodDescriptor extends MethodDescriptor
{
    public GenericObjectReadOnlyPropertyMethodDescriptor(String genericTypeNames)
    {
        super();
        this.setFormat(Format.makePropertyInstance());
        this.logic = Logic.makeReturnGenericPropertyLogic();
        this.logic.setGenericTypeNames(genericTypeNames);
    }
}

package io.github.kurokawa_kun.annotation.javafx.descriptor;

public class GenericObjectPropertyMethodDescriptor extends MethodDescriptor
{
    public GenericObjectPropertyMethodDescriptor(String genericTypeNames)
    {
        super();
        this.setFormat(Format.makePropertyInstance());
        this.logic = Logic.makeReturnGenericPropertyLogic();
        this.logic.setGenericTypeNames(genericTypeNames);
    }
}

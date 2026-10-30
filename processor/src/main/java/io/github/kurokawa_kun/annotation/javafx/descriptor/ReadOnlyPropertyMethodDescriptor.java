package io.github.kurokawa_kun.annotation.javafx.descriptor;

public class ReadOnlyPropertyMethodDescriptor extends MethodDescriptor
{
    public ReadOnlyPropertyMethodDescriptor(String readOnlyProperty)
    {
        super();
        this.setFormat(Format.makePropertyInstance());
        this.logic = Logic.makeReturnReadOnlyPropertyLogic();
        this.logic.setElementType(readOnlyProperty);        
    }
}

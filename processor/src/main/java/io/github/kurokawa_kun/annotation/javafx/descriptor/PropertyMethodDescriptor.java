package io.github.kurokawa_kun.annotation.javafx.descriptor;

public class PropertyMethodDescriptor extends MethodDescriptor
{
    public PropertyMethodDescriptor()
    {
        super();
        this.setFormat(Format.makePropertyInstance());
        this.setLogic(Logic.makeReturnPropertyLogic());
    }
}

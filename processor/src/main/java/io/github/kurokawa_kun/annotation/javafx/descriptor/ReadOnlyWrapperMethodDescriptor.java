package io.github.kurokawa_kun.annotation.javafx.descriptor;

public class ReadOnlyWrapperMethodDescriptor extends MethodDescriptor
{
    public ReadOnlyWrapperMethodDescriptor()
    {
        super();
        this.setFormat(Format.makePropertyInstance());
        this.logic = Logic.makeReturnReadOnlyWrapperLogic();
        this.logic.setTargetMethodName("getReadOnlyProperty");
    }
}

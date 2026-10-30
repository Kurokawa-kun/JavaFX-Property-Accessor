package io.github.kurokawa_kun.annotation.javafx.type;
import lombok.*;

@RequiredArgsConstructor
public enum ParameterType
{
    NO_PARAMETER            (false),
    PRIMITIVE_PARAMETER     ( true),
    OBJECT_PARAMETER        ( true);
    
    private final boolean parameter; //  メソッド呼び出しの際、何かしらのパラメータを渡す
    
    public boolean hasParameter()
    {
        return this.parameter;
    }
}

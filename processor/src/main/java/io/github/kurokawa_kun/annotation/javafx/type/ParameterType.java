package io.github.kurokawa_kun.annotation.javafx.type;
import lombok.*;

@RequiredArgsConstructor
public enum ParameterType
{
    NO_PARAMETER                    (false, false),
    PRIMITIVE_PARAMETER             ( true, false),
    OBJECT_PARAMETER                ( true, false),
    GENERIC_OBJECT_PARAMETER        ( true,  true);
    
    private final boolean parameter; //  メソッド呼び出しの際、何かしらのパラメータを渡す
    private final boolean hasGenericTypeInfo;   //  型情報を持っているか
    
    public boolean hasParameter()
    {
        return this.parameter;
    }
    public boolean hasGenericTypeInfo()
    {
        return this.hasGenericTypeInfo;
    }
}

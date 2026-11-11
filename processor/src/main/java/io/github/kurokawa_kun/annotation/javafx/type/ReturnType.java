package io.github.kurokawa_kun.annotation.javafx.type;
import lombok.*;

@RequiredArgsConstructor
public enum ReturnType
{
    VOID                              (false,  true, false),
    RETURN_FIELD                      ( true, false, false),
    RETURN_PRIMITIVE_VALUE            ( true,  true, false),
    RETURN_OBJECT_VALUE               ( true,  true, false),
    RETURN_GENERIC_OBJECT_VALUE       ( true,  true,  true),
    RETURN_PROPERTY                   ( true, false, false),
    RETURN_GENERIC_PROPERTY           ( true, false,  true),
    RETURN_READONLY_PROPERTY          ( true, false, false),
    RETURN_GENERIC_READONLY_PROPERTY  ( true, false,  true),
    RETURN_READONLY_WRAPPER           ( true,  true, false),
    RETURN_GENERIC_READONLY_WRAPPER   ( true,  true,  true);

    private final boolean returnsValue; //  何かしらの値を返却する
    private final boolean callsMethod;  //  true…メソッドを呼び出す。false…フィールドを参照する
    private final boolean hasGenericTypeInfo;   //  型情報を持っているか
    public boolean returnsValue()
    {
        return this.returnsValue;
    }
    public boolean callsMethod()
    {
        return this.callsMethod;
    }
    public boolean hasGenericTypeInfo()
    {
        return this.hasGenericTypeInfo;
    }
}

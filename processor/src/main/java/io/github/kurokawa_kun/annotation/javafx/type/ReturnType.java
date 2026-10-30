package io.github.kurokawa_kun.annotation.javafx.type;
import lombok.*;

@RequiredArgsConstructor
public enum ReturnType
{
    VOID                        (false,  true),
    RETURN_FIELD                ( true, false),
    RETURN_PRIMITIVE_VALUE      ( true,  true),
    RETURN_OBJECT_VALUE         ( true,  true),
    RETURN_PROPERTY             ( true, false),
    RETURN_READONLY_PROPERTY    ( true, false),
    RETURN_READONLY_WRAPPER     ( true,  true);

    private final boolean returnsValue; //  何かしらの値を返却する
    private final boolean callsMethod;  //  true…メソッドを呼び出す。false…フィールドを参照する
    public boolean returnsValue()
    {
        return this.returnsValue;
    }
    public boolean callsMethod()
    {
        return this.callsMethod;
    }
}

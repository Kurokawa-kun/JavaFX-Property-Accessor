package io.github.kurokawa_kun.annotation.javafx;
import io.github.kurokawa_kun.annotation.javafx.type.ValueType;
import io.github.kurokawa_kun.annotation.javafx.type.Access;
import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface FxSetter
{
    String prefix() default "";
    String suffix() default "";
    String parameterName() default "value";
    ValueType valueType() default ValueType.DEFAULT;
    Access access() default Access.DEFAULT;
}

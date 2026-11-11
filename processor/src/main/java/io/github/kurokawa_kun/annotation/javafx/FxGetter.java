package io.github.kurokawa_kun.annotation.javafx;
import io.github.kurokawa_kun.annotation.javafx.type.ValueType;
import io.github.kurokawa_kun.annotation.javafx.type.Access;
import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface FxGetter
{
    String prefix() default "";
    String suffix() default "";
    ValueType valueType() default ValueType.DEFAULT;
    Access access() default Access.DEFAULT;
}

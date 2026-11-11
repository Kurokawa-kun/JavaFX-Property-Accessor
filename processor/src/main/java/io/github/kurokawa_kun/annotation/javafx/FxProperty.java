package io.github.kurokawa_kun.annotation.javafx;
import io.github.kurokawa_kun.annotation.javafx.type.Access;
import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface FxProperty
{
    String prefix() default "";
    String suffix() default "Property";
    boolean readOnly() default false;
    Access access() default Access.DEFAULT;
}

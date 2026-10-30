package io.github.kurokawa_kun.annotation.javafx.type;
import com.sun.tools.javac.code.Flags;
import javax.lang.model.element.VariableElement;
import lombok.*;

@RequiredArgsConstructor
public enum Access 
{
    PUBLIC          ( 400,    Flags.PUBLIC),
    PACKAGE         ( 300,              0L),
    PROTECTED       ( 200, Flags.PROTECTED),
    PRIVATE         ( 100,   Flags.PRIVATE),
    DEFAULT         (   1,           9999L);
    
    @Getter
    private final int accessLevel;
    @Getter
    private final long flag;
    
    public static Access convertFromVariableElement(Object annotationAttribute)
    {
        Access access = null;
        
        if (annotationAttribute instanceof VariableElement)
        {
            VariableElement variableElement = (VariableElement) annotationAttribute;
            String name = variableElement.getSimpleName().toString();
            return Access.valueOf(name);
        }
        else if (annotationAttribute instanceof String)
        {
            //  もし文字列表現などで入っていた場合の処理
            return Access.valueOf((String) annotationAttribute);
        }        
        return null;
    }    
}

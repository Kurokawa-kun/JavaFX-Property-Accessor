package io.github.kurokawa_kun.annotation.javafx.type;

import javax.lang.model.element.VariableElement;

public enum ValueType 
{
    DEFAULT,
    PRIMITIVE,
    OBJECT;
    
    public static ValueType convertFromVariableElement(Object annotationAttribute)
    {
        ValueType valueType = null;

        if (annotationAttribute instanceof VariableElement)
        {
            VariableElement variableElement = (VariableElement) annotationAttribute;
            String name = variableElement.getSimpleName().toString();
            return ValueType.valueOf(name);
        }
        else if (annotationAttribute instanceof String)
        {
            //  もし文字列表現が入っていた場合の処理
            return ValueType.valueOf((String) annotationAttribute);
        }        
        return null;
    }
}

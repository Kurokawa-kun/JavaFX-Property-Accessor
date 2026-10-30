package io.github.kurokawa_kun.annotation.javafx;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeKind;

/**
 *   AST要素のチェックだけを専門に行うクラス
 */
public class Validators 
{
    // 指定したアノテーションがついていればそれを返す。最初にマッチしたものを返す。見つからなければnull
    public static String getAnnotation(AnnotationMirror annotationMirror, String... annotations)
    {
        Element element = annotationMirror.getAnnotationType().asElement();
        Name qualifiedName = ((TypeElement) element).getQualifiedName();
        
        return Stream.of(annotations).filter(annotation -> qualifiedName.contentEquals(annotation)).findFirst()
                .orElse(null); // マッチするものがなければ null を返す
    }
    
    //  指定したアノテーションがついているかチェックする。1つでも該当すればtrue
    public static boolean hasAnyAnnotation(AnnotationMirror annotationMirror, String... annotations)
    {
        Element element = annotationMirror.getAnnotationType().asElement();
        Name qualifiedName = ((TypeElement) element).getQualifiedName();
        return Stream.of(annotations).anyMatch(annotation -> qualifiedName.contentEquals(annotation));
    }
    
    //  プリミティブ型かどうかを返す
    public static boolean isPrimitive(String typeName)
    {
        if (Arrays.stream(TypeKind.values()).filter(TypeKind::isPrimitive).anyMatch(typeKind -> typeKind.name().equals(typeName.toUpperCase())))
        {
            return true;
        }
        return false;
    }
    
    //  public修飾子が指定されているかを返す
    public static boolean isPublic(Element element)
    {
        return element.getModifiers().stream().anyMatch(modifier -> modifier.equals(modifier.PUBLIC));
    }    
    
    //  フィールド名のチェック（小文字で始まっているか）
    public static boolean checkStartsWithLowerCase(String fieldName)
    {
        char firstChar = fieldName.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z')
        {
            return false;
        }
        return true;
    }
    
    //  フィールド名のチェック（末尾がPropertyで終わっていないか）
    public static boolean checkEndsWithoutProperty(String name)
    {
        return (!name.endsWith("Property") && !name.endsWith("property"));
    }
}

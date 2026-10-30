package io.github.kurokawa_kun.annotation.javafx;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.*;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.*;

/**
 *   ユーティリティクラス
 */
public class Utils 
{
    //  定数
    public static final String FX_GETTER   = "io.github.kurokawa_kun.annotation.javafx.FxGetter";
    public static final String FX_SETTER   = "io.github.kurokawa_kun.annotation.javafx.FxSetter";
    public static final String FX_PROPERTY = "io.github.kurokawa_kun.annotation.javafx.FxProperty";
    
    public static final String[] ALL_TARGET_ANNOTATIONS = {FX_GETTER, FX_SETTER, FX_PROPERTY};
    
    //  プリミティブ型プロパティ
    public static final String[] primitiveProperties = 
    {
        "javafx.beans.property.BooleanProperty",
        "javafx.beans.property.DoubleProperty",
        "javafx.beans.property.FloatProperty",
        "javafx.beans.property.IntegerProperty",
        "javafx.beans.property.LongProperty"
    };
    
    //  読み込み専用プロパティ
    public static final String readOnlyProperty = "javafx.beans.property.ReadOnlyProperty";
    
    //  論理値。getter作成時、メソッドの名前を変えるときに必要
    public static final String[] booleanValues =
    {
        "boolean",
        "java.lang.Boolean"
    };
    
    //  ReadOnlyPropertyクラス
    public static final String[] readOnlyProperties = 
    {
        "javafx.beans.property.ReadOnlyBooleanProperty", 
        "javafx.beans.property.ReadOnlyDoubleProperty", 
        "javafx.beans.property.ReadOnlyFloatProperty", 
        "javafx.beans.property.ReadOnlyIntegerProperty", 
        "javafx.beans.property.ReadOnlyListProperty", 
        "javafx.beans.property.ReadOnlyLongProperty", 
        "javafx.beans.property.ReadOnlyMapProperty", 
        "javafx.beans.property.ReadOnlyObjectProperty", 
        "javafx.beans.property.ReadOnlySetProperty", 
        "javafx.beans.property.ReadOnlyStringProperty"
    };
    
    //  Propertyクラス
    public static final String[] properties = 
    {
        "javafx.beans.property.BooleanProperty", 
        "javafx.beans.property.DoubleProperty", 
        "javafx.beans.property.FloatProperty", 
        "javafx.beans.property.IntegerProperty", 
        "javafx.beans.property.ListProperty", 
        "javafx.beans.property.LongProperty", 
        "javafx.beans.property.MapProperty", 
        "javafx.beans.property.ObjectProperty", 
        "javafx.beans.property.SetProperty", 
        "javafx.beans.property.StringProperty"
    };
    
    //  ReadOnlyWrapperクラス
    public static final String[] readOnlyWrappers = 
    {
        "javafx.beans.property.ReadOnlyBooleanWrapper", 
        "javafx.beans.property.ReadOnlyDoubleWrapper", 
        "javafx.beans.property.ReadOnlyFloatWrapper", 
        "javafx.beans.property.ReadOnlyIntegerWrapper", 
        "javafx.beans.property.ReadOnlyListWrapper", 
        "javafx.beans.property.ReadOnlyLongWrapper", 
        "javafx.beans.property.ReadOnlyMapWrapper", 
        "javafx.beans.property.ReadOnlyObjectWrapper", 
        "javafx.beans.property.ReadOnlySetWrapper", 
        "javafx.beans.property.ReadOnlyStringWrapper"
    };
    
    //  getの代わりにisのgetterを持つクラス
    public String[] UsingIsGetter = 
    {
        "javafx.beans.property.ReadOnlyBooleanProperty", 
        "javafx.beans.property.BooleanProperty", 
        "javafx.beans.property.ReadOnlyBooleanWrapper"
    };
    
    public static final Map<Modifier, Long> modifierFlags = new HashMap<>();
    public static final Map<String, TypeTag> primitiveTags = new HashMap<>();
    public static final Map<String, String> primitiveValues = new HashMap<>();
    public static final Map<String, String> valueTypes = new HashMap<>();
    
    static
    {
        //  ※ 拾ってはならないフラグはコメントアウトしてある
        
        //modifierFlags.put(Modifier.ABSTRACT,         (long) Flags.ABSTRACT);
        //modifierFlags.put(Modifier.DEFAULT,          (long) Flags.DEFAULT);
        //modifierFlags.put(Modifier.FINAL,            (long) Flags.FINAL);
        //modifierFlags.put(Modifier.NATIVE,           (long) Flags.NATIVE);
        //modifierFlags.put(Modifier.NON_SEALED,       (long) Flags.NON_SEALED);
        modifierFlags.put(Modifier.PRIVATE,          (long) Flags.PRIVATE);
        modifierFlags.put(Modifier.PROTECTED,        (long) Flags.PROTECTED);
        modifierFlags.put(Modifier.PUBLIC,           (long) Flags.PUBLIC);
        //modifierFlags.put(Modifier.SEALED,           (long) Flags.SEALED);
        modifierFlags.put(Modifier.STATIC,           (long) Flags.STATIC);
        //modifierFlags.put(Modifier.STRICTFP,         (long) Flags.STRICTFP);
        //modifierFlags.put(Modifier.SYNCHRONIZED,     (long) Flags.SYNCHRONIZED);
        //modifierFlags.put(Modifier.TRANSIENT,        (long) Flags.TRANSIENT);
        //modifierFlags.put(Modifier.VOLATILE,         (long) Flags.VOLATILE);
        
        primitiveValues.put("javafx.beans.property.ReadOnlyBooleanProperty", "boolean");
        primitiveValues.put("javafx.beans.property.ReadOnlyDoubleProperty", "double");
        primitiveValues.put("javafx.beans.property.ReadOnlyFloatProperty", "float");
        primitiveValues.put("javafx.beans.property.ReadOnlyIntegerProperty", "int");
        primitiveValues.put("javafx.beans.property.ReadOnlyLongProperty", "long");
        
        valueTypes.put("javafx.beans.property.ReadOnlyBooleanProperty", "java.lang.Boolean");
        valueTypes.put("javafx.beans.property.ReadOnlyDoubleProperty", "java.lang.Double");
        valueTypes.put("javafx.beans.property.ReadOnlyFloatProperty", "java.lang.Float");
        valueTypes.put("javafx.beans.property.ReadOnlyIntegerProperty", "java.lang.Integer");
        valueTypes.put("javafx.beans.property.ReadOnlyListProperty", "java.lang.List");
        valueTypes.put("javafx.beans.property.ReadOnlyLongProperty", "java.lang.Long");
        valueTypes.put("javafx.beans.property.ReadOnlyMapProperty", "java.lang.Map");
        valueTypes.put("javafx.beans.property.ReadOnlyObjectProperty", "java.lang.Object");
        valueTypes.put("javafx.beans.property.ReadOnlySetProperty", "java.lang.Set");
        valueTypes.put("javafx.beans.property.ReadOnlyStringProperty", "java.lang.String");
        
        primitiveTags.put("boolean", TypeTag.BOOLEAN);
        primitiveTags.put("double", TypeTag.DOUBLE);
        primitiveTags.put("float", TypeTag.FLOAT);
        primitiveTags.put("int", TypeTag.INT);
        primitiveTags.put("long", TypeTag.LONG);
    }
    
    public static class TraceMessager
    {
        private static TraceMessager instance;
        private ProcessingEnvironment processingEnv;
        private boolean outputFlag;
        
        private TraceMessager(ProcessingEnvironment processingEnv, boolean outputFlag)
        {
            this.processingEnv = processingEnv;
            this.outputFlag = outputFlag;
        }
        public static TraceMessager getInstance(ProcessingEnvironment processingEnv, boolean outputFlag)
        {
            if (TraceMessager.instance == null) 
            {
                TraceMessager.instance = new TraceMessager(processingEnv, outputFlag);
            }            
            return instance;
        }
        public static TraceMessager getInstance()
        {
            if (TraceMessager.instance == null) throw new NullPointerException("The instance of TraceMessager has not initialized yet.");
            return instance;
        }
         
        public void showMessage(String message)
        {
            if (!this.outputFlag) return;
            message.lines().filter(line -> !line.equals("")).forEach(line -> processingEnv.getMessager().printMessage(Kind.OTHER, line));            
        }
    }
    
    //  指定したアノテーションの指定したアトリビュートの値を取り出す
    //  変更されていない場合はデフォルト値を返す
    public static Object getCurrentAnnotationAttribute(Elements elementUtils, AnnotationMirror annotationMirror, String attributeName)
    {
        Object annotationAttributeMirror = getAnnotationAttributeMirror(annotationMirror, attributeName);
        Object defaultAttribute = getDefaultAttributeValue(elementUtils, annotationMirror, attributeName);
        
        if (annotationAttributeMirror != null)
        {
            return annotationAttributeMirror;
        }
        else
        {
            return defaultAttribute;
        }
    }
    
    //  ミラー上にある指定したアノテーションの指定したアトリビュートの値を取り出す
    //  ※ 変更していないアトリビュートを参照するとnullが返却される
    public static Object getAnnotationAttributeMirror(AnnotationMirror annotationMirror, String attributeName)
    {
        Map<? extends ExecutableElement, ? extends AnnotationValue> m = annotationMirror.getElementValues();
        
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : m.entrySet())
        {
            // キーのメソッド名が一致するか確認
            if (entry.getKey().getSimpleName().toString().equals(attributeName))
            {
                return entry.getValue().getValue();
            }
        }        
        return null; // 該当する属性がない場合
    }
    
    //  指定したアノテーションの指定したアトリビュートのデフォルト値を取り出す
    public static Object getDefaultAttributeValue(Elements elementUtils, AnnotationMirror annotationMirror, String attributeName)
    {
        // 明示的な値とデフォルト値を結合したマップを取得
        Map<? extends ExecutableElement, ? extends AnnotationValue> defaultsMap = elementUtils.getElementValuesWithDefaults(annotationMirror);
        
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : defaultsMap.entrySet())
        {
            if (entry.getKey().getSimpleName().toString().equals(attributeName))
            {
                return entry.getValue().getValue();
            }
        }
        return null;
    }
    
    //  指定したアノテーションの指定したアトリビュートが変更されているか返す
    public static boolean hasAttributeChanged(Elements elementUtils, AnnotationMirror annotationMirror, String attributeName)
    {
        Object currentAttribute = getAnnotationAttributeMirror(annotationMirror, attributeName);
        Object defaultAttribute = getDefaultAttributeValue(elementUtils, annotationMirror, attributeName);
        
        if (currentAttribute != null && currentAttribute.equals(defaultAttribute))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    //  指定したクラスが指定したクラスを継承、または指定したインターフェースを実装しているか返す。ジェネリクスの型情報は無視する。1つでもマッチすればtrue
    public static boolean anyDelivered(Elements elementUtils, Types typeUtils, String typeName, String[] parentNames)
    {
        System.err.println(typeName);
        try
        {
            for (String parentName : parentNames)
            {
                if (isDelivered(elementUtils, typeUtils, typeName, parentName)) return true;
            }
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    //  指定したクラスが指定したクラスを継承、または指定したインターフェースを実装しているか返す。ジェネリクスの型情報は無視する。すべてにマッチすればtrue
    //  （※ 複数のクラスを指定しないこと。trueになりません）
    public static boolean allDelivered(Elements elementUtils, Types typeUtils, String typeName, String[] parentNames)
    {
        boolean delivered = false;
        for (String parentName : parentNames)
        {
            if (isDelivered(elementUtils, typeUtils, typeName, parentName)) return true;
        }
        return false;
    }
        
    //  指定したクラスが指定したクラスを継承、または指定したインターフェースを実装しているか返す。ジェネリクスの型情報は無視される。
    public static boolean isDelivered(Elements elementUtils, Types typeUtils, String typeName, String parentName)
    {
        typeName = eraseTypeInfo(typeName);
        
        //  比較対象の型要素のミラーを取得
        TypeElement propertyElement = elementUtils.getTypeElement(parentName);
        
        //// ジェネリクスを消去（Raw Type化）して比較可能にする        
        TypeMirror propertyType = typeUtils.erasure(propertyElement.asType());
        
        //  入力された型の型要素を取得する
        //  他のメソッドを経てこの段階でクラスが存在することの確認は取れているので存在チェックは行わない
        TypeElement inputElement = elementUtils.getTypeElement(typeName);
        
        //  指定された型のミラーを取得する
        TypeMirror inputType = typeUtils.erasure(inputElement.asType());
        
        //  型の継承・実装関係を判定
        return typeUtils.isSubtype(inputType, propertyType);
    }
    
    //  型情報だけを取り除く
    public static String eraseTypeInfo(String typeName)
    {
        int index = typeName.indexOf('<');
        
        if (index != -1)
        {
            typeName = typeName.substring(0, index);
        }
        return typeName;
    }    
    
    //  FQCN形式のクラス名をJCExpression型に変換する
    public static JCTree.JCExpression makeQualifiedIdentifier(TreeMaker treeMaker, Names names, String fullyQualifiedClassName)
    {
        String[] parts = fullyQualifiedClassName.split("\\.");
        
        JCTree.JCExpression expression = treeMaker.Ident(names.fromString(parts[0]));
        for (int i = 1; i < parts.length; i++)
        {
            expression = treeMaker.Select(expression, names.fromString(parts[i]));
        }
        
        return expression;
    }
}

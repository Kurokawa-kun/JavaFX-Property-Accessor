package io.github.kurokawa_kun.annotation.javafx.descriptor;
import io.github.kurokawa_kun.annotation.javafx.type.MemberType;
import io.github.kurokawa_kun.annotation.javafx.type.Access;
import io.github.kurokawa_kun.annotation.javafx.type.ReturnType;
import io.github.kurokawa_kun.annotation.javafx.type.ParameterType;
import lombok.*;

//  FormatクラスはAST構築に必要な情報を保持した、いわばメソッドの「内側」の情報を格納するクラス
public class Logic implements DebugInfo
{
    @Getter
    private final String tag;   //  デバッグ用の識別子
    @Getter @Setter
    private String annotation;  //  このクラスが処理しようとしているアノテーション
    @Getter
    private ReturnType returnType;  //  値返却の有無。返却する場合、プリミティブ型かオブジェクト型か
    @Getter
    private ParameterType parameterType = null; //  パラメータの型がプリミティブ型かオブジェクト型か
    @Getter @Setter
    private String baseClassName = null;  //  このプロパティそのものの型
    @Getter @Setter
    private String genericTypeNames = null;  //  このプロパティが持つ型情報
    @Getter @Setter
    private String propertyType = null;  //  このプロパティが持つ値の型
    
    //  後から変更していいメンバー
    @Getter
    private Access access = null;  //  作成するメソッドのアクセス権    
    @Getter @Setter
    private MemberType memberType = null;  //  インスタンスメンバーかクラスメンバーか。staticが指定されていたときに変更が必要    
    @Getter @Setter
    private String targetMethodName;     //  作成したメソッドの中で呼び出すメソッド。メソッドを呼び出す代わりにフィールドを参照する場合はnullにすること
    
    //  外部から呼び出しできないコンストラクタ
    private Logic(String tag, ReturnType returnType, ParameterType parameterType)
    {
        this.tag = tag;
        this.returnType = returnType;
        this.parameterType = parameterType;
        this.access = Access.PUBLIC;  //  後から変更可能
        this.memberType = MemberType.INSTANCE_MEMBER;  //  後から変更可能
    }
    
    public static Logic makeVoidPrimitiveLogic()
    {
        return new Logic("Logic:VoidPrimitiveLogic", ReturnType.VOID, ParameterType.PRIMITIVE_PARAMETER);
    }
    public static Logic makeVoidObjectLogic()
    {
        return new Logic("Logic:VoidObjectLogic", ReturnType.VOID, ParameterType.OBJECT_PARAMETER);
    }    
    public static Logic makeVoidGenericObjectLogic()
    {
        return new Logic("Logic:VoidGenericObjectLogic", ReturnType.VOID, ParameterType.GENERIC_OBJECT_PARAMETER);
    }    
    public static Logic makeReturnFieldLogic()
    {
        return new Logic("Logic:ReturnFieldLogic", ReturnType.RETURN_FIELD, ParameterType.NO_PARAMETER);
    }
    public static Logic makeReturnPrimitiveLogic()
    {
        return new Logic("Logic:ReturnPrimitiveLogic", ReturnType.RETURN_PRIMITIVE_VALUE, ParameterType.NO_PARAMETER);
    }
    public static Logic makeReturnObjectLogic()
    {
        return new Logic("Logic:ReturnObjectLogic", ReturnType.RETURN_OBJECT_VALUE, ParameterType.NO_PARAMETER);
    }
    public static Logic makeReturnPropertyLogic()
    {
        return new Logic("Logic:ReturnPropertyLogic", ReturnType.RETURN_PROPERTY, ParameterType.NO_PARAMETER);
    }
    public static Logic makeReturnGenericPropertyLogic()
    {
        return new Logic("Logic:ReturnGenericPropertyLogic", ReturnType.RETURN_GENERIC_PROPERTY, ParameterType.NO_PARAMETER);
    }
    public static Logic makeReturnReadOnlyPropertyLogic()
    {
        return new Logic("Logic:ReturnReadOnlyPropertyLogic", ReturnType.RETURN_READONLY_PROPERTY, ParameterType.NO_PARAMETER);
    }
    public static Logic makeReturnReadOnlyWrapperLogic()
    {
        return new Logic("Logic:ReturnReadOnlyWrapperLogic", ReturnType.RETURN_READONLY_WRAPPER, ParameterType.NO_PARAMETER);
    }    
    public static Logic makeReturnGenericObjectLogic()
    {
        return new Logic("Logic:ReturnGenericObjectLogic", ReturnType.RETURN_GENERIC_OBJECT_VALUE, ParameterType.NO_PARAMETER);
    }
    
    
    public String getDebugInfo()
    {
        StringBuilder stringBuilder = new StringBuilder(10000);
        stringBuilder.append(String.format("--  Logic  --\n"));
        stringBuilder.append(String.format("annotation          :%s\n", this.annotation));
        stringBuilder.append(String.format("tag                 :%s\n", this.tag));
        stringBuilder.append(String.format("returnType          :%s\n", this.returnType));
        stringBuilder.append(String.format("parameterType       :%s\n", this.parameterType));
        stringBuilder.append(String.format("baseClassName       :%s\n", this.baseClassName));
        stringBuilder.append(String.format("genericTypeNames    :%s\n", this.genericTypeNames));
        stringBuilder.append(String.format("propertyType        :%s\n", this.propertyType));
        stringBuilder.append(String.format("access              :%s\n", this.access));
        stringBuilder.append(String.format("memberType          :%s\n", this.memberType));
        stringBuilder.append(String.format("targetMethodName    :%s\n", this.targetMethodName));
        
        return stringBuilder.toString();
    }
}

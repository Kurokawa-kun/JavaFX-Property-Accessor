package io.github.kurokawa_kun.annotation.javafx;
import io.github.kurokawa_kun.annotation.javafx.type.ValueType;
import io.github.kurokawa_kun.annotation.javafx.type.MemberType;
import io.github.kurokawa_kun.annotation.javafx.type.Access;
import io.github.kurokawa_kun.annotation.javafx.type.PropertyType;
import io.github.kurokawa_kun.annotation.javafx.descriptor.ReadOnlyWrapperMethodDescriptor;
import io.github.kurokawa_kun.annotation.javafx.descriptor.ObjectSetterMethodDescriptor;
import io.github.kurokawa_kun.annotation.javafx.descriptor.ObjectBooleanGetterMethodDescriptor;
import io.github.kurokawa_kun.annotation.javafx.descriptor.ReadOnlyPropertyMethodDescriptor;
import io.github.kurokawa_kun.annotation.javafx.descriptor.PrimitiveSetterMethodDescriptor;
import io.github.kurokawa_kun.annotation.javafx.descriptor.PrimitiveGetterMethodDescriptor;
import io.github.kurokawa_kun.annotation.javafx.descriptor.PrimitiveBooleanGetterMethodDescriptor;
import io.github.kurokawa_kun.annotation.javafx.descriptor.MethodDescriptor;
import io.github.kurokawa_kun.annotation.javafx.descriptor.ObjectGetterMethodDescriptor;
import io.github.kurokawa_kun.annotation.javafx.descriptor.PropertyMethodDescriptor;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.*;
import lombok.*;

public class MethodDescriptorFactory
{
    /*
        メモ：このクラスがMethodDescriptorを作成するにあたって考慮すべきことは以下：
        ・このエレメントについているアノテーションの種類
        ・boolean型の値を持つプロパティか
        ・プリミティブ型かオブジェクト型か
        ・ReadOnlyが指定されているか、またはReadOnlyWrapperを継承した型のプロパティか
    
        そんなに重要ではないこと（後から変更可能）：
        ・staticが指定されているか
        ・アクセス権が○○に指定されているか
    */
    @Getter
    String annotation = null;
    @Getter
    String elementType = null;
    @Getter
    Modifier[] modifiers = null;
    //  仕様上無理があるのでsourcedValueTypeは使わない（Object型だけ作れるパターンに対応できない）
    //@Getter
    //ValueType sourcedValueType = null;    //  ソースコード上の型情報    
    @Getter
    boolean canMakePrimitive = false;
    @Getter
    boolean canMakeObject = false;
    @Getter
    boolean canMakeReadOnlyProperty = false;
    @Getter
    boolean canMakeProperty = false;
    @Getter
    ValueType annotatedValueType = null;    //  指定された型情報。この通りに作られる保証はない    
    @Getter
    ValueType definitiveValueType = null;
    @Getter
    private boolean sourcedReadOnly;
    @Getter
    private boolean annotatedReadOnly;
    @Getter
    private boolean definitiveReadOnly;    
    @Getter
    private Access sourcedAccessPrivilege = Access.PUBLIC;  //  これは「ソース中に書かれたプロパティの」アクセス権（分かりにくい…そのうち直したい）
    @Getter
    private Access annotatedAccessPrivilege = Access.PUBLIC;  //  これはアノテーションで指定されたアクセス権
    @Getter
    private Access definitiveAccessPrivilege = Access.PUBLIC;    
    @Getter
    private MemberType definitiveMemberType = null;  //  static有無の指定はソースで書かれていることを変更しようがないのでチェック等は行わない    
    @Getter
    private PropertyType propertyType = null;   //  プロパティの種類
    @Getter
    private String baseClassName = null;  //  指定されたプロパティの最も基底となるインターフェース（ReadOnlyPropertyインターフェース）
    @Getter @Setter
    String parameterType = null;    //  プロパティがやり取りする型の情報
    
    //  環境情報
    private Elements elementUtils = null;
    private Types typeUtils = null;
    
    //  コンストラクタ
    public MethodDescriptorFactory(Elements elementUtils, Types typeUtils, String annotation, String elementType)
    {
        //  入力されたパラメータを設定する
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.annotation = annotation;
        this.elementType = elementType;
    }
    
    //  プロパティの種類を設定する
    public boolean setMethodProperty(Modifier[] modifiers, ValueType annotatedValueType, boolean annotatedReadOnly)
    {
        this.annotatedValueType = annotatedValueType;
        this.annotatedReadOnly = annotatedReadOnly;
        setModifiers(modifiers);
        
        //  アノテーションで指定された型（DEFAULTも取り得る）
        this.annotatedValueType = annotatedValueType;
        
        //  どのタイプのプロパティなのかを判定する
        //  クラス階層を下から上に辿る必要があるためelse if文で判定しないこと
        int index = -9999;
        if (Utils.anyDelivered(this.elementUtils, this.typeUtils, this.elementType, Utils.readOnlyProperties))
        {
            index = Arrays.asList(Utils.readOnlyProperties).indexOf(this.elementType);
            if (index > 0)
            {
                this.baseClassName = this.elementType;  //  このクラス自体がReadOnlyPropertyなので変換は不要
                this.propertyType = propertyType.READ_ONLY_PROPERTY;
            }
        }
        if (Utils.anyDelivered(this.elementUtils, this.typeUtils, this.elementType, Utils.properties))
        {
            index = Arrays.asList(Utils.properties).indexOf(this.elementType);
            if (index > 0)
            {
                this.baseClassName = Utils.readOnlyProperties[index];
                this.propertyType = propertyType.PROPERTY;
            }
        }
        if (Utils.anyDelivered(this.elementUtils, this.typeUtils, this.elementType, Utils.readOnlyWrappers))
        {
            index = Arrays.asList(Utils.readOnlyWrappers).indexOf(this.elementType);
            if (index > 0)
            {
                this.baseClassName = Utils.readOnlyProperties[index];
                this.propertyType = propertyType.READ_ONLY_WRAPPER;
            }
        }
        
        //  どのプロパティの型にもマッチしなかった場合
        if (this.propertyType == null) return false;
        
        //  指定された型から作成可能なメソッドを判定する
        switch (annotation)
        {
            case Utils.FX_GETTER:
            case Utils.FX_SETTER:
            {
                this.definitiveReadOnly = false;  //  このフラグは使わない
                this.canMakePrimitive = (Utils.primitiveValues.get(this.baseClassName) != null);
                this.canMakeObject = true;  //  このルートに来ている（クラスのチェックは終わっている）時点で常にtrue
                
                if (this.canMakePrimitive && this.canMakeObject)
                {
                    switch (this.annotatedValueType)
                    {
                        case ValueType.OBJECT:
                        {
                            this.definitiveValueType = ValueType.OBJECT;
                            this.parameterType = Utils.valueTypes.get(this.baseClassName);
                            this.definitiveReadOnly = false;
                            break;
                        }
                        default:
                        {
                            this.definitiveValueType = ValueType.PRIMITIVE;
                            this.parameterType = Utils.primitiveValues.get(this.baseClassName);
                            this.definitiveReadOnly = false;
                            break;
                        }
                    }                    
                }
                else if (this.canMakePrimitive && !this.canMakeObject)
                {
                    switch (this.annotatedValueType)
                    {
                        case ValueType.PRIMITIVE:
                        case ValueType.DEFAULT:
                        {
                            this.definitiveValueType = ValueType.PRIMITIVE;
                            this.parameterType = Utils.primitiveValues.get(this.baseClassName);
                            this.definitiveReadOnly = false;
                            break;
                        }
                        default:
                        {
                            //  エラールート
                            this.definitiveValueType = null;
                            this.definitiveReadOnly = false;
                            break;
                        }
                    }
                }
                else if (!this.canMakePrimitive && this.canMakeObject)
                {
                    switch (this.annotatedValueType)
                    {
                        case ValueType.OBJECT:
                        case ValueType.DEFAULT:
                        {
                            this.definitiveValueType = ValueType.OBJECT;
                            this.parameterType = Utils.valueTypes.get(this.baseClassName);
                            this.definitiveReadOnly = false;
                            break;
                        }
                        default:
                        {
                            //  エラールート
                            this.definitiveValueType = null;
                            this.definitiveReadOnly = false;
                            break;
                        }
                    }                    
                }
                else
                {
                    //  何のフラグも立っていない場合
                    //  エラールート
                    this.definitiveValueType = null;
                    this.definitiveReadOnly = false;
                    break;
                }
                break;
            }
            default:
            {
                //  ※ FX_PROPERTY以外のアノテーションのケースがくることはないので判定は不要
                /*
                    パラメタの状態から作成するメソッド（が返却するクラス）の組み合わせはこうなる。
                
                    |  canReadOnly  |  canPropery   |   ext Wrapper | ReadOnly flg  |    返却するクラス
                    |--------------------------------------------------------------------------------------
                    |      true     |     false     |     false     |       N/A     |  ReadOnlyProperty
                    |      true     |      true     |     false     |      true     |  ReadOnlyProperty
                    |      true     |      true     |     false     |     false     |          Property
                    |       N/A     |       N/A     |      true     |       N/A     |  ReadOnlyProperty
                */

                this.canMakeReadOnlyProperty = this.propertyType.canMakeReadOnlyProperty();                
                this.canMakeProperty = this.propertyType.canMakeProperty();
                boolean extWrapper = (this.propertyType == PropertyType.READ_ONLY_WRAPPER);
                
                if (this.canMakeReadOnlyProperty && !this.canMakeProperty && !extWrapper)
                {
                    //  ReadOnlyPropertyを返却するメソッドを作成する
                    this.definitiveValueType = ValueType.OBJECT;
                    this.parameterType = Utils.readOnlyProperty;
                    this.definitiveReadOnly = true;
                }                
                else if (this.canMakeReadOnlyProperty && this.canMakeProperty && !extWrapper && this.annotatedReadOnly)
                {
                    //  ReadOnlyPropertyを返却するメソッドを作成する
                    this.definitiveValueType = ValueType.OBJECT;
                    this.parameterType = Utils.readOnlyProperty;
                    this.definitiveReadOnly = true;
                }
                else if (this.canMakeReadOnlyProperty && this.canMakeProperty && !extWrapper && !this.annotatedReadOnly)
                {
                    //  Propertyを返却するメソッドを作成する
                    this.definitiveValueType = ValueType.OBJECT;
                    this.parameterType = this.elementType;
                    this.definitiveReadOnly = false;
                }
                else if (extWrapper)
                {
                    //  ReadOnlyPropertyを返却するメソッドを作成する
                    this.definitiveValueType = ValueType.OBJECT;
                    this.parameterType = Utils.readOnlyProperty;
                    this.definitiveReadOnly = true;
                }
                else
                {
                    this.definitiveValueType = null;    //  後続のチェックメソッドで検出される
                }
            }
        }
        
        return true;
    }
    
    //  ユーザー側に呼び出してもらうためのチェック用メソッド
    
    //  ソースに書かれたプロパティのアクセス権とアノテーションに書かれたアクセス権を比較する
    //  問題ない場合はtrueを返却する
    public boolean checkAccessPrivilege()
    {
        return (this.sourcedAccessPrivilege.getAccessLevel() < annotatedAccessPrivilege.getAccessLevel());
    }
    
    //  ReadOnlyプロパティにsetメソッドを呼び出していないかチェックする
    //  ReadOnlyWrapperの場合はsetメソッドがあるためチェックしない。
    public boolean checkSetReadOnlyProperty()
    {
        return !(this.annotation.equals(Utils.FX_SETTER) && (this.propertyType == propertyType.READ_ONLY_PROPERTY));
    }
    
    public void setAccess(Access accessPrivilege)
    {
        this.annotatedAccessPrivilege = accessPrivilege;
    }
    
    private void setModifiers(Modifier[] modifiers)
    {
        this.modifiers = modifiers;
        
        //  修飾子を調べる
        if (Stream.of(modifiers).anyMatch(modifier -> modifier.equals(Modifier.PUBLIC)))
        {            
            this.sourcedAccessPrivilege = Access.PUBLIC;
        }
        else if (Stream.of(modifiers).anyMatch(modifier -> modifier.equals(Modifier.PROTECTED)))
        {
            this.sourcedAccessPrivilege = Access.PROTECTED;
        }
        else if (Stream.of(modifiers).anyMatch(modifier -> modifier.equals(Modifier.PRIVATE)))
        {
            this.sourcedAccessPrivilege = Access.PRIVATE;
        }
        else
        {
            this.sourcedAccessPrivilege = Access.PACKAGE;
        }
        
        if (Stream.of(modifiers).anyMatch(modifier -> modifier.equals(Modifier.STATIC)))
        {
            this.definitiveMemberType = MemberType.CLASS_MEMBER;
        }
        else
        {
            this.definitiveMemberType = MemberType.INSTANCE_MEMBER;
        }
    }
    
    //  MethodDescriptorを作成して返却する
    public MethodDescriptor makeMethodDescriptor()
    {
        MethodDescriptor methodDescriptor = null;
        
        //  テンプレートからMethodDescriptorを作成する
        switch (this.getAnnotation())
        {
            case Utils.FX_GETTER:
            {
                //  Getterパターン
                if (this.definitiveValueType == ValueType.PRIMITIVE)
                {
                    if (Arrays.asList(Utils.booleanValues).stream().anyMatch(booleanValue -> booleanValue.equals(this.parameterType)))
                    {
                        //  boolean型の場合
                        methodDescriptor = new PrimitiveBooleanGetterMethodDescriptor("get");
                    }
                    else
                    {
                        //  それ以外の型の場合
                        methodDescriptor = new PrimitiveGetterMethodDescriptor("get");
                    }
                }
                else
                {
                    if (Arrays.asList(Utils.booleanValues).stream().anyMatch(booleanValue -> booleanValue.equals(this.parameterType)))
                    {
                        //  Boolean型の場合
                        methodDescriptor = new ObjectBooleanGetterMethodDescriptor("getValue");
                    }
                    else
                    {
                        //  それ以外の型の場合
                        methodDescriptor = new ObjectGetterMethodDescriptor("getValue");
                    }
                }
                break;
            }
            case Utils.FX_SETTER:
            {
                //  Setterパターン
                if (this.definitiveValueType == ValueType.PRIMITIVE)
                {
                    methodDescriptor = new PrimitiveSetterMethodDescriptor("set");
                }
                else
                {
                    methodDescriptor = new ObjectSetterMethodDescriptor("setValue");
                }
                break;
            }
            case Utils.FX_PROPERTY:
            {
                //  PRIMITIVE/OBJECT型の判定は不要。プロパティは常にOBJECT型だから
                if (this.definitiveReadOnly)
                {
                    if (this.propertyType == PropertyType.READ_ONLY_PROPERTY)
                    {
                        //  ReadOnlyPropertyパターン
                        methodDescriptor = new ReadOnlyPropertyMethodDescriptor(this.baseClassName);
                    }
                    else
                    {
                        //  ReadOnlyWrapperパターン
                        methodDescriptor = new ReadOnlyWrapperMethodDescriptor();
                    }
                }
                else
                {
                    //  Propertyパターン
                    methodDescriptor = new PropertyMethodDescriptor();
                }
                break;
            }
            default:
            {
                //  あり得ないルート
                System.err.println("Unknown annotation has been set.");
                break;
            }
        }
        
        //  呼び出し側に返却する前に細かな微調整
        this.definitiveAccessPrivilege = this.annotatedAccessPrivilege;
        methodDescriptor.getFormat().setMemberType(this.definitiveMemberType);  //  static指定の有無（メソッドの定義）
        methodDescriptor.getFormat().setAccess(this.definitiveAccessPrivilege);  //  メソッドのアクセス権
        methodDescriptor.getLogic().setMemberType(this.definitiveMemberType);  //  static指定の有無（メソッドの内側）
        methodDescriptor.getLogic().setElementType(this.elementType);  //  プロパティの型情報
        methodDescriptor.getLogic().setPropertyType(this.parameterType);  //  プロパティの値の型情報

        return methodDescriptor;
    }
}

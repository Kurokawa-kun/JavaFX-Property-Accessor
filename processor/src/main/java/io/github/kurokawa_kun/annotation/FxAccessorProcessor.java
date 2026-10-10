package io.github.kurokawa_kun.annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.Optional;
import java.util.Locale;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.element.*;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.lang.model.SourceVersion;
import javax.annotation.processing.*;
import javax.tools.*;
import com.sun.source.util.*;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.MemberEnter;
import java.util.MissingResourceException;

@SupportedSourceVersion(SourceVersion.RELEASE_25)
@SupportedOptions("verbose")
@SupportedAnnotationTypes({"io.github.kurokawa_kun.annotation.FxGetter", "io.github.kurokawa_kun.annotation.FxSetter", "io.github.kurokawa_kun.annotation.FxProperty"})
/**
 *    アノテーションプロセッサの本体
 *    -Averbose=trueを指定すると詳細なメッセージを出力する
 */
public class FxAccessorProcessor extends AbstractProcessor
{
    
    private boolean isVerbose = false;  //  メッセージ出力フラグ
    private TreeMaker treeMaker = null;
    private Names names = null;
    private Trees trees = null;
    private Elements elementUtils = null; 
    private Types typeUtils = null;
    private Attr attribute = null;
    private Enter enter = null;
    private Context context = null;
    
    private ResourceBundle messageBundle = null;
    
    /**
     *   アノテーションプロセッサの初期化処理
     *   @param processingEnv コンパイラが提供する機能を保持した環境
     */
    @Override public void init(ProcessingEnvironment processingEnv) 
    {
        if (isVerbose)
        {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, "initメソッドが呼ばれました。");
        }
        
        super.init(processingEnv);
        
        //  -Averbose=trueオプションが指定されているかチェックする
        this.isVerbose = Optional.ofNullable(processingEnv.getOptions()).map(map -> "true".equals(map.get("verbose"))).orElse(false);
        
        //  メッセージファイルのロード
        messageBundle = ResourceBundle.getBundle("io.github.kurokawa_kun.annotation.i18n.messages", Locale.getDefault(), this.getClass().getClassLoader());
        
        this.trees = Trees.instance(processingEnv);

        // javac 内部のContextとユーティリティを取得
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.attribute = com.sun.tools.javac.comp.Attr.instance(context);        
        this.enter = Enter.instance(context);
        
        if (isVerbose)
        {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, "initメソッドを抜けます。");
        }
    }
    
    /**
     *   見つかったアノテーションを処理する
     *   @param annotations 処理対象となるアノテーション
     *   @param roundEnv ラウンドに関する情報
     *   @return 当該アノテーションがこのメソッドで処理された場合はtrueを返す。その場合、後続のアノテーションプロセッサは呼び出されない。
     */    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (isVerbose)
        {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, "processメソッドが呼ばれました。");
        }
        
        //  最終ラウンドの場合は何もしない
        if (roundEnv.processingOver())
        {
            if (isVerbose)
            {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, "最終ラウンドです。");
                processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, "processメソッドを抜けます。");
            }
            return false;
        }
        
        //  ルート直下のクラスから順番にフィールドを探す
        roundEnv.getRootElements().stream().filter(rootElement -> rootElement.getKind() == ElementKind.CLASS).forEach(rootElement -> 
        {            
            inspectFields(rootElement);
        });
        
        if (isVerbose)
        {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.OTHER, "processメソッドを抜けます。");
        }
        
        return false;
    }
    
    //  指定されたクラス定義のメンバを調べてフィールドかつJavaFXプロパティだった場合、ASTにメソッドを追加する。
    //  内部クラスが見つかった場合、このメソッドを再帰的に呼び出す。
    private void inspectFields(Element classElement)
    {
        JCClassDecl classDecl = (JCClassDecl) trees.getTree(classElement);
        
        classElement.getEnclosedElements().stream().forEach(element ->
        {
            switch (element.getKind())
            {
                case CLASS:
                {
                    inspectFields(element);
                    break;
                }
                case FIELD:
                {
                    //  アノテーションがついているかチェック
                    boolean hasFxGetter = hasFxGetter(element);
                    boolean hasFxSetter = hasFxSetter(element);
                    boolean hasFxProperty = hasFxProperty(element);
                    
                    if (!hasFxGetter && !hasFxSetter && !hasFxProperty) break;
                    
                    String elementType = element.asType().toString();
                    String fieldName = element.getSimpleName().toString();
                    
                    //  プリミティブ型になっていないかチェック
                    if (isPrimitive(elementType))
                    {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, MessageFormat.format(messageBundle.getString("platform.annotation.javafx.error.primitivefield"), elementType, fieldName));
                        break;
                    }
                    
                    //  JavaFXプロパティかチェック
                    if (!isJavaFXProperty(elementType))
                    {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, MessageFormat.format(messageBundle.getString("platform.annotation.javafx.error.notproperty"), elementType));
                        break;
                    }
                    
                    //  フィールド名の書式チェック
                    if (!checkStartsWithLowerCase(fieldName))
                    {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, MessageFormat.format(messageBundle.getString("platform.annotation.javafx.warning.startlowercase"), fieldName));
                    }
                    if (!checkEndsWithoutProperty(fieldName))
                    {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, MessageFormat.format(messageBundle.getString("platform.annotation.javafx.warning.unnecessarysuffix"), fieldName));
                    }
                    
                    //  フィールドがpublicになっていないかチェック
                    if (isPublic(element))
                    {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, MessageFormat.format(messageBundle.getString("platform.annotation.javafx.warning.publicfield"), fieldName));
                    }
                    
                    //  ここまででクラスとフィールドのチェックは終わっている
                    
                    //  プロパティに付与されている型情報を取得する
                    String typeInfo = getPropertyGenericType(elementType, processingEnv).toString();
                    
                    String methodName = null;
                    JCMethodDecl generatedMethod = null;
                    
                    if (hasFxGetter)
                    {
                        //  FxGetterが付与されている場合
                        
                        //  getメソッドが実装されているか確認する
                        if (!isGettable(elementType))
                        {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, MessageFormat.format(messageBundle.getString("platform.annotation.javafx.error.notgetter"), elementType, fieldName));
                        }
                        
                        //  適切なメソッド名を作成する
                        methodName = getPropertyAccessorName(AccessorType.FxGetter, fieldName);
                        //  メソッドを作成する
                        generatedMethod = makeGetter(typeInfo, methodName, fieldName);
                        //  ASTにメソッドを追加する
                        classDecl.defs = classDecl.defs.append(generatedMethod);
                    }
                    if (hasFxSetter)
                    {
                        //  FxSetterが付与されている場合
                        
                        //  setメソッドが実装されているか確認する
                        if (!isSettable(elementType))
                        {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, MessageFormat.format(messageBundle.getString("platform.annotation.javafx.error.notsetter"), elementType, fieldName));
                        }
                        
                        //  適切なメソッド名を作成する
                        methodName = getPropertyAccessorName(AccessorType.FxSetter, fieldName);
                        
                        System.err.println("この機能はまだ作りかけです。This feature is still under development.");                        
                        
                        //  メソッドを作成する
                        //generatedMethod = makeSetter(typeInfo, methodName, fieldName);
                        //  ASTにメソッドを追加する
                        //classDecl.defs = classDecl.defs.append(generatedMethod);
                    }
                    if (hasFxProperty)
                    {
                        //  FxPropertyが付与されている場合
                        
                        System.err.println("この機能はまだ作りかけです。This feature is still under development.");
                        
                    }
                }
            }
        });
          
        //dumpClassDecl(classDecl);
    }
    
    //  @FxGetterアノテーションが付与されているかを返す
    private boolean hasFxGetter(Element element)
    {
        return element.getAnnotation(FxGetter.class) != null;
    }
    
    //  @FxSetterアノテーションが付与されているかを返す
    private boolean hasFxSetter(Element element)
    {
        return element.getAnnotation(FxSetter.class) != null;
    }
    
    //  @FxPropertyアノテーションが付与されているかを返す
    private boolean hasFxProperty(Element element)
    {
        return element.getAnnotation(FxProperty.class) != null;
    }
    
    //  フィールド名のチェック（小文字で始まっているか）
    private boolean checkStartsWithLowerCase(String fieldName)
    {
        char firstChar = fieldName.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z')
        {
            return false;
        }
        return true;
    }
    
    //  フィールド名のチェック（末尾がPropertyで終わっていないか）
    private boolean checkEndsWithoutProperty(String name)
    {
        return (!name.endsWith("Property") && !name.endsWith("property"));
    }
    
    //  ObservableValueインターフェースを実装しているかを返す
    private boolean isGettable(String typeName)
    {
        return implementsInterface(typeName, "javafx.beans.value.ObservableValue");
    }
    
    //  WritableValueインターフェースを実装しているかを返す
    private boolean isSettable(String typeName)
    {
        return implementsInterface(typeName, "javafx.beans.value.WritableValue");
    }
    
    //  Propertyインターフェースを実装しているかを返す
    private boolean isJavaFXProperty(String typeName)
    {
        return implementsInterface(typeName, "javafx.beans.property.Property");
    }
    
    //  指定したクラス（文字列で表す）が指定したインターフェース（文字列で表す）を実装しているか返す。ジェネリクスの型情報は無視する
    private boolean implementsInterface(String typeName, String interfaceName)
    {
        //  比較対象のTypeMirrorを取得
        TypeElement propertyElement = processingEnv.getElementUtils().getTypeElement(interfaceName);
        
        //// ジェネリクスを消去（Raw Type化）して比較可能にする
        TypeMirror propertyType = typeUtils.erasure(propertyElement.asType());
        
        //  入力された型名のTypeElementを取得する
        //  他のメソッドを経てこの段階でクラスが存在することの確認は取れているので存在チェックは行わない
        TypeElement inputElement = elementUtils.getTypeElement(typeName);
        
        //  指定された型のTypeMirrorを取得する
        TypeMirror inputType = typeUtils.erasure(inputElement.asType());
        
        //  型の継承・実装関係を判定
        return typeUtils.isSubtype(inputType, propertyType);
    }
    
    //  プリミティブ型かどうかを返す
    private boolean isPrimitive(String typeName)
    {
        if (Arrays.stream(TypeKind.values()).filter(TypeKind::isPrimitive).anyMatch(typeKind -> typeKind.name().equals(typeName.toUpperCase())))
        {
            return true;
        }
        return false;
    }
    
    //  public修飾子が指定されているかを返す
    private boolean isPublic(Element element)
    {
        return element.getModifiers().stream().anyMatch(modifier -> modifier.equals(modifier.PUBLIC));
    }
    
    //  プロパティの型情報（Property<T>のTのこと）を取得する。アノテーションプロセッサ環境で動作する必要があるため、Classクラスによる判定は行わない
    private TypeMirror getPropertyGenericType(String className, ProcessingEnvironment processingEnv) 
    {
        //  クラス名からTypeElementを取得する
        TypeElement element = processingEnv.getElementUtils().getTypeElement(className);
        if (element == null) 
        {
            return null;
        }

        //  継承階層を探索してPropertyインターフェースの型引数を取得する
        return findPropertyGenericType(element.asType(), processingEnv);
    }
    
    //  Propertyインターフェースの型情報を取得する。getPropertyGenericTypeから内部的に呼び出される。Propertyインターフェースに到達するまで再帰的に呼び出される。
    private static TypeMirror findPropertyGenericType(TypeMirror targetType, ProcessingEnvironment processingEnv) 
    {
        Types typeUtils = processingEnv.getTypeUtils();

        //  対象の型の親クラス・インターフェースを型（DeclaredType）として探索
        java.util.List<? extends TypeMirror> supertypes = typeUtils.directSupertypes(targetType);
        
        for (TypeMirror supertype : supertypes) 
        {
            if (supertype instanceof DeclaredType) 
            {
                DeclaredType declaredType = (DeclaredType) supertype;
                TypeElement element = (TypeElement) declaredType.asElement();

                // Propertyインターフェースに到達したかチェック
                if (element.getQualifiedName().contentEquals("javafx.beans.property.Property")) 
                {
                    java.util.List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();                    
                    if (!typeArguments.isEmpty()) 
                    {
                        // Property<T>の最初の型引数Tを返す
                        return typeArguments.get(0);
                    }
                }

                //  親クラス・親インターフェースを再帰的に探索する
                TypeMirror result = findPropertyGenericType(supertype, processingEnv);
                if (result != null)
                {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    //  作成するアクセサに応じて処理を切り分けるためのenum
    private enum AccessorType
    {
        FxGetter("get", true, false), 
        FxSetter("set", true, false),
        FxProperty("", false, true);
        
        private String prefix;
        private boolean convertToUppercase;
        private boolean addProperty;
        
        private String getPrefix()
        {
            return this.prefix;
        }
        private boolean getConvertToUppercase()
        {
            return this.convertToUppercase;
        }
        private boolean getAddProperty()                
        {
            return this.addProperty;
        }        
        
        private AccessorType(String prefix, boolean convertToUppercase, boolean addProperty)
        {
            this.prefix = prefix;
            this.convertToUppercase = convertToUppercase;
            this.addProperty = addProperty;
        }
    }
    
    //  アクセサの種類に応じてメソッド名を返す
    private String getPropertyAccessorName(AccessorType accessorType, String fieldName)
    {
        String body = accessorType.getConvertToUppercase() ? Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1) : fieldName;
        String suffix = "";
        if (accessorType.getAddProperty())
        {
            suffix = (fieldName.endsWith("Property") || fieldName.endsWith("property")) ? "" : "Property";
        }
        String accessorName = accessorType.getPrefix() + body + suffix;
        
        return accessorName;
    }
    
    //  FQCNからシンプルネームを取り出す
    private JCTree.JCExpression makeQualifiedIdentifier(String fullyQualifiedClassName)
    {
        String[] parts = fullyQualifiedClassName.split("\\.");
        
        JCTree.JCExpression expression = treeMaker.Ident(names.fromString(parts[0]));
        for (int i = 1; i < parts.length; i++)
        {
            expression = treeMaker.Select(expression, names.fromString(parts[i]));
        }
        
        return expression;
    }
    
    //  getterメソッドを作成する
    private JCMethodDecl makeGetter(String returnValue, String methodName, String propertyFieldName)
    {
        //  メソッド名
        com.sun.tools.javac.util.Name name = names.fromString(methodName);
        //  復帰値
        JCTree.JCExpression returnType = makeQualifiedIdentifier(returnValue); 
        //  修飾子
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(com.sun.tools.javac.code.Flags.PUBLIC);
        //  メソッド本体
        JCStatement returnStatement = treeMaker.Return
        (
            treeMaker.Apply
            (
                List.nil(),
                treeMaker.Select(treeMaker.Select(treeMaker.Ident(names.fromString("this")), names.fromString(propertyFieldName)), names.fromString("get")),
                List.nil()
            )
        );
        //  return文の形にする
        JCBlock body = treeMaker.Block(0, List.of(returnStatement));        
        //  JCMethodDeclの形にする
        JCMethodDecl methodDecl = treeMaker.MethodDef
        (
            modifiers,
            name,
            returnType, //  復帰値
            List.nil(), //  型パラメータ
            List.nil(), //  引数リスト
            List.nil(), //  throwsリスト
            body,
            null        //  デフォルト値 (アノテーション用)
        );
        
        return methodDecl;
    }
    
    //  クラス定義がもつASTの情報を表示する
    private void dumpClassDecl(JCClassDecl classDecl)
    {
        classDecl.defs.stream().forEach(c -> 
        {
            System.out.println("--");
            System.out.println(c.getTree().toString());
        });
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //  !!   ここから下にあるソースコードは開発中のものです。まだ正常に動作しません。  !!
    //  !!  The source code below is under development and does not work properly yet.  !!
    //
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    // setterメソッドを作成する
    private JCMethodDecl makeSetter(String parameterType, String methodName, String propertyFieldName) 
    {
        // メソッド名
        com.sun.tools.javac.util.Name name = names.fromString(methodName);
        // 復帰値
        JCTree.JCExpression returnType = treeMaker.TypeIdent(com.sun.tools.javac.code.TypeTag.VOID);
        // 修飾子
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(com.sun.tools.javac.code.Flags.PUBLIC);
        // 引数名の定義
        com.sun.tools.javac.util.Name paramName = names.fromString("value");
        // 引数の定義作成 (修飾子なし, 引数名, 型, 初期値なし)
        JCVariableDecl parameter = treeMaker.VarDef
        (
            treeMaker.Modifiers(com.sun.tools.javac.code.Flags.PARAMETER | com.sun.tools.javac.code.Flags.FINAL),
            paramName,
            makeQualifiedIdentifier(parameterType),
            null
        );

        // メソッド本体
        JCStatement execStatement = treeMaker.Exec
        (
            treeMaker.Apply
            (
                List.nil(),
                treeMaker.Select
                (
                    treeMaker.Select
                    (
                        treeMaker.Ident(names.fromString("this")),
                        names.fromString(propertyFieldName)
                    ),
                    names.fromString("set")
                ),
                List.of(treeMaker.Ident(paramName)) // 引数としてvalueを渡す
            )
        );

        // Blockの形にする
        JCBlock body = treeMaker.Block(0, List.of(execStatement));

        // JCMethodDeclの形にする
        JCMethodDecl methodDecl = treeMaker.MethodDef(
            modifiers,
            name,
            returnType,                  //  復帰値 (void)
            List.nil(),                  //  型パラメータ
            List.of(parameter),          //  引数リスト (定義したparamを追加)
            List.nil(),                  //  throwsリスト
            body,
            null                         //  デフォルト値
        );
        
        return methodDecl;
    }
    
    //  setterメソッドを作成する（シンボル情報も付与したもの）
    private JCMethodDecl makeSetter2(String parameterType, String methodName, String propertyFieldName, com.sun.tools.javac.code.Symbol.ClassSymbol targetClassSymbol) 
    {
        com.sun.tools.javac.util.Name name = names.fromString(methodName);
        JCTree.JCExpression returnType = treeMaker.TypeIdent(com.sun.tools.javac.code.TypeTag.VOID);
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(com.sun.tools.javac.code.Flags.PUBLIC);
        com.sun.tools.javac.util.Name paramName = names.fromString("value");

        //  引数
        JCVariableDecl parameter = treeMaker.VarDef
        (
            treeMaker.Modifiers(com.sun.tools.javac.code.Flags.PARAMETER),
            paramName,
            makeQualifiedIdentifier(parameterType),
            null
        );
        
        //  MethodSymbolとVarSymbolの辻褄を合わせる
        com.sun.tools.javac.code.Symbol.MethodSymbol methodSym = new com.sun.tools.javac.code.Symbol.MethodSymbol
        (
            com.sun.tools.javac.code.Flags.PUBLIC,
            name,
            new com.sun.tools.javac.code.Type.MethodType
            (
                List.nil(), 
                com.sun.tools.javac.code.Type.noType, 
                List.nil(), 
                targetClassSymbol
            ),
            targetClassSymbol
        );        
        
        //  引数のVarSymbolを生成する
        VarSymbol paramSym = new com.sun.tools.javac.code.Symbol.VarSymbol
        (
            com.sun.tools.javac.code.Flags.PARAMETER,
            paramName,
            com.sun.tools.javac.code.Type.noType,
            methodSym
        );
        
        paramSym.flags_field |= com.sun.tools.javac.code.Flags.PARAMETER;
        paramSym.adr = 0;
        parameter.sym = paramSym;
        methodSym.params = List.of(paramSym);
        
        //  メソッド本体
        JCStatement execStatement = treeMaker.Exec
        (
            treeMaker.Apply
            (
                List.nil(),
                treeMaker.Select
                (
                    treeMaker.Select
                    (
                        treeMaker.Ident(names.fromString("this")),
                        names.fromString(propertyFieldName)
                    ),
                    names.fromString("set")
                ),
                List.of(treeMaker.Ident(paramName))
            )
        );

        JCBlock body = treeMaker.Block(0, List.of(execStatement));

        //  メソッドのAST
        JCMethodDecl methodDecl = treeMaker.MethodDef
        (
            modifiers,
            name,
            returnType,
            List.nil(),
            List.of(parameter),
            List.nil(),
            body,
            null
        );
        methodDecl.sym = methodSym;

        return methodDecl;
    }
    
    //  メソッドの作成（テスト用）
    private JCMethodDecl test() 
    {
        /*
            ここで作っているメソッド：
        
            public int addNumbers(int a, int b)
            {
                return a + b;
            }
        */
        JCVariableDecl paramA = treeMaker.VarDef
        (
            treeMaker.Modifiers(com.sun.tools.javac.code.Flags.PARAMETER),
            names.fromString("a"),
            treeMaker.TypeIdent(TypeTag.INT),
            null
        );

        JCVariableDecl paramB = treeMaker.VarDef
        (
            treeMaker.Modifiers(com.sun.tools.javac.code.Flags.PARAMETER),
            names.fromString("b"),
            treeMaker.TypeIdent(TypeTag.INT),
            null
        );

        //  メソッド本体の文 "a + b" を作成
        JCBinary returnExpr = treeMaker.Binary(
            Tag.PLUS,
            treeMaker.Ident(names.fromString("a")),
            treeMaker.Ident(names.fromString("b"))
        );

        //  "return a + b" 文を作成
        JCReturn returnStmt = treeMaker.Return(returnExpr);

        //  メソッドブロック { return a + b; } を作成
        JCBlock methodBody = treeMaker.Block(0, List.of(returnStmt));

        //  メソッド全体の定義を作成
        JCMethodDecl methodDecl = treeMaker.MethodDef(
            treeMaker.Modifiers(com.sun.tools.javac.code.Flags.PUBLIC), // アクセス修飾子 (public)
            names.fromString("addNumbers"),                             // メソッド名
            treeMaker.TypeIdent(TypeTag.INT),                           // 戻り値の型 (int)
            List.nil(),                                                 // 型パラメータ（ジェネリクス: なし）
            List.of(paramA, paramB),                                    // 引数リスト
            List.nil(),                                                 // throws 例外リスト (なし)
            methodBody,                                                 // メソッド本体
            null                                                        // デフォルト値 (アノテーション要素用: なし)
        );
        
        return methodDecl;
    }
}

package io.github.kurokawa_kun.annotation.javafx;
import io.github.kurokawa_kun.annotation.javafx.type.*;
import io.github.kurokawa_kun.annotation.javafx.descriptor.*;
import java.util.*;
import javax.lang.model.util.Elements;
import javax.lang.model.element.*;
import javax.lang.model.util.Types;
import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import java.text.MessageFormat;
import javax.annotation.processing.*;
import com.sun.source.util.*;
import com.sun.tools.javac.processing.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.Context;

@SupportedOptions("verbose")
@SupportedAnnotationTypes({Utils.FX_GETTER, Utils.FX_SETTER, Utils.FX_PROPERTY})
/**
 *    アノテーションプロセッサの本体
 *    -Averbose=trueを指定すると詳細なメッセージを出力する
 */
public class AccessorMain extends AbstractProcessor
{
    /*
    概要：このアノテーションプロセッサがやっていること
    
    動作中のコンパイラのASTの中身を書き換えて、JavaFXのアクセサメソッドを追加する。
    
    ・コンパイラが保持しているASTを取得して、ルートから順に辿りJavaFXプロパティのフィールドを探す。
    ・このAccessorMainクラスからMethodDescriptorFactorクラスを呼び出す。
    　同クラスは指定されたオプションからどの雛形（MethodDescriptor）が相応しいか判定する。
    ・MethodDescriptorの中身を微調整する（作成するメソッド名など）。
    ・MethodGeneratorにMethodDescriptorを渡す。それを元にメソッドを作成する。
    ・作成されたメソッドをASTに追加する。以後ASTの末端まで繰り返し。
    */
    
    private Trees trees = null;
    private TreeMaker treeMaker = null;
    private Context context = null;
    private Names names = null;
    private Elements elementUtils = null;
    private Types typeUtils = null;
    private Messager messager = null;
    private ResourceBundle messageBundle = null;
    
    /**
     *   このアノテーションプロセッサがサポートしているソースコードのバージョン
     *   @return バージョン番号
     */
    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        /*
            バージョンチェックについて補足：
            
            Java 17～26の間で警告メッセージなく動作するようにしたいので、
            動的なバージョンチェック(※)を行っている。

            ※ その実行環境がサポートしている、かつ26を超えない数字をサポートしているものとして返す。

            @SupportedSourceVersionアノテーションに古いバージョン（例えばJava 17）を直に指定すると
            それ以降のバージョンで警告メッセージが表示されてしまう。一方、新しいバージョン（例えばJava 26）を直に指定すると
            古いバージョンの形式でビルドができない（その時点でSourceVersion.RELEASE_26が存在しないから）ためこのようにしている。
            
            Java 16以前ではもともと動作しない。Java 27以降で動作させた場合、警告メッセージが表示される。これは仕様通り。
        */
        SourceVersion latestValidVersion = null;
        
        for (int versionNumber = 17; versionNumber <= 26; versionNumber++)
        {
            try
            {
                latestValidVersion = SourceVersion.valueOf("RELEASE_" + String.valueOf(versionNumber));
            }
            catch (IllegalArgumentException e)
            {
                // 実行中の（古い）JDKがそのバージョンを知らない場合、直前までの有効なバージョンで確定する
                break;
            }
        }
        
        return latestValidVersion;
    }
    
    /**
     *   アノテーションプロセッサの初期化処理
     *   @param processingEnv コンパイラが提供する機能を保持した環境
     */
    @Override
    public void init(ProcessingEnvironment processingEnv) 
    {
        super.init(processingEnv);
        
        //  -Averbose=trueオプションが指定されているかチェックする
        boolean isVerbose = Optional.ofNullable(processingEnv.getOptions()).map(map -> "true".equals(map.get("verbose"))).orElse(false);        
        Utils.TraceMessager trace = Utils.TraceMessager.getInstance(processingEnv, isVerbose);
        
        //  メッセージ出力のためMessagerオブジェクトを取得
        this.messager = processingEnv.getMessager();
        
        trace.showMessage("Entering init()...");
        
        //  メッセージファイルのロード
        try
        {
            this.messageBundle = ResourceBundle.getBundle("io.github.kurokawa_kun.annotation.i18n.messages", Locale.getDefault(), this.getClass().getClassLoader());            
        }
        catch (MissingResourceException e)
        {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to load the message file.");
        }
        
        //  javac内部の実行環境の取得
        this.trees = Trees.instance(processingEnv);
        this.context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        
        trace.showMessage("Leaving init()...");
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
        Utils.TraceMessager trace = Utils.TraceMessager.getInstance();
        trace.showMessage("Entering process()...");
        
        //  最終ラウンドの場合は何もしない
        if (roundEnv.processingOver())
        {
            trace.showMessage("This round is final. Do nothing.");
            trace.showMessage("Leaving process()...");
            return true;
        }
        
        //  ASTのルート直下のクラスから順番に要素を探索する
        roundEnv.getRootElements().stream().filter(rootElement -> rootElement.getKind() == ElementKind.CLASS).forEach(rootElement -> 
        {            
            inspectAstElements(rootElement);
        });
        
        trace.showMessage("Leaving process()...");
        return true;
    }
    
    //  指定されたクラス定義のメンバを調べてフィールドかつJavaFXプロパティだった場合、ASTにメソッドを追加する。
    //  内部クラスが見つかった場合、このメソッドを再帰的に呼び出す。
    private void inspectAstElements(Element classElement)
    {
        JCClassDecl classDecl = (JCClassDecl) this.trees.getTree(classElement);
        
        Utils.TraceMessager trace = Utils.TraceMessager.getInstance();
        trace.showMessage(String.format("Inspecting current element '%s'.\n", classDecl.getSimpleName()));
        
        classElement.getEnclosedElements().stream().forEach(element ->
        {
            switch (element.getKind())
            {
                case CLASS:
                case INTERFACE:
                case ENUM:
                case RECORD:
                case ANNOTATION_TYPE:
                {
                    //  フィールドを持つ可能性のある入れ子の要素を見つけた場合は再帰的に探索する                    
                    inspectAstElements(element);
                    break;
                }
                case FIELD:
                {
                    //  フィールドを見つけた場合
                    
                    //  フィールド名と型、このエレメントのアノテーションミラーを取得
                    String fieldName = element.getSimpleName().toString();
                    String elementType = element.asType().toString();
                    java.util.List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
                    
                    for (AnnotationMirror annotationMirror : annotationMirrors)
                    {
                        //  このクラスでは作成するメソッドのロジック変更に関わることはしない。ロジックの作成はファクトリクラスで行う。
                        //  ただ、要素の表層的な字面やパラメーターをチェックしてエラーメッセージを出すだけ。
                        
                        //  当該アノテーションがついているかチェック
                        String currentAnnotation = Validators.getAnnotation(annotationMirror, Utils.FX_GETTER, Utils.FX_SETTER, Utils.FX_PROPERTY);
                        if (currentAnnotation == null) continue;
                        
                        //  プリミティブ型になっていないかチェック
                        //  ※ この処理はファクトリを呼び出す前に行う必要がある
                        if (Validators.isPrimitive(elementType))
                        {
                            messager.printMessage(Diagnostic.Kind.ERROR, MessageFormat.format(this.messageBundle.getString("platform.annotation.javafx.error.primitivefield"), fieldName));
                            return;
                        }
                        
                        //  ReadOnlyアトリビュートの取得
                        Object readOnly = Utils.getCurrentAnnotationAttribute(this.elementUtils, annotationMirror, "readOnly");
                        
                        //  valueTypeアトリビュートの取得
                        ValueType valueType = ValueType.convertFromVariableElement(Utils.getCurrentAnnotationAttribute(this.elementUtils, annotationMirror, "valueType"));
                        
                        //  ファクトリの作成
                        MethodDescriptorFactory methodDescriptorFactory = new MethodDescriptorFactory(this.elementUtils, this.typeUtils, currentAnnotation, elementType);
                        
                        //  必要なプロパティ情報の設定
                        if (!methodDescriptorFactory.setMethodProperty(element.getModifiers().toArray(Modifier[]::new), valueType, readOnly != null ? (Boolean) readOnly : false))
                        {
                            //  JavaFXのプロパティではない
                            messager.printMessage(Diagnostic.Kind.ERROR, MessageFormat.format(this.messageBundle.getString("platform.annotation.javafx.error.notproperty"), fieldName));
                            return;
                        }
                        if (!methodDescriptorFactory.checkSetReadOnlyProperty())
                        {
                            //  ReadOnlyPropertyにsetterをつけようとした
                            messager.printMessage(Diagnostic.Kind.ERROR, MessageFormat.format(this.messageBundle.getString("platform.annotation.javafx.error.settertoreadonly"), elementType, fieldName));
                            return;
                        }
                        
                        //  accessアトリビュートの取得
                        Access accessPrivilege = Access.convertFromVariableElement(Utils.getCurrentAnnotationAttribute(this.elementUtils, annotationMirror, "access"));
                        //  アトリビュートが変更されていない場合はデフォルト値を設定する
                        if (accessPrivilege == Access.DEFAULT) 
                        {
                            if (methodDescriptorFactory.getPropertyType() == PropertyType.READ_ONLY_WRAPPER &&
                                methodDescriptorFactory.getAnnotation().equals(Utils.FX_SETTER))
                            {
                                accessPrivilege = Access.PROTECTED;
                            }
                            else
                            {
                                accessPrivilege = Access.PUBLIC;
                            }
                        }
                        methodDescriptorFactory.setAccess(accessPrivilege);
                        
                        //  ここでメソッドディスクリプタを作成する
                        MethodDescriptor methodDescriptor = methodDescriptorFactory.makeMethodDescriptor();
                        
                        //  アノテーションについているパラメタを取得してファクトリに渡す
                        String methodPrefix = (String) Utils.getCurrentAnnotationAttribute(this.elementUtils, annotationMirror, "prefix");
                        if (!methodPrefix.equals(""))
                        {
                            methodDescriptor.getFormat().setMethodPrefix(methodPrefix);
                        }
                        String methodSuffix = (String) Utils.getCurrentAnnotationAttribute(this.elementUtils, annotationMirror, "suffix");
                        if (!methodSuffix.equals(""))
                        {
                            methodDescriptor.getFormat().setMethodSuffix(methodSuffix);
                        }
                        
                        methodDescriptor.getFormat().setFieldName(fieldName);
                        methodDescriptor.getFormat().setVariableName((String) Utils.getCurrentAnnotationAttribute(this.elementUtils, annotationMirror, "parameterName"));
                        
                        //  ここから先はメソッドを作ることが確定しているためエラーメッセージではなく警告メッセージを出力する
                        
                        //  プロパティメソッドを作成する、かつミュータブルな値を持つプロパティ、かつ読み取り専用の指定がされているか
                        if (methodDescriptorFactory.getPropertyType() == PropertyType.PROPERTY &&
                            Utils.MUTABLE_PROPERTIES.containsKey(methodDescriptor.getLogic().getBaseClassName()) &&
                            methodDescriptorFactory.isDefinitiveReadOnly())
                        {
                            messager.printMessage(Diagnostic.Kind.WARNING, MessageFormat.format(this.messageBundle.getString("platform.annotation.javafx.warning.mutable"), fieldName));
                        }
                        
                        //  ReadOnlyWrapperかつ、setterメソッドを作成する、かつpublicが指定されているか
                        if (!methodDescriptorFactory.checkSetterOfReadOnlyWrapper() &&
                            methodDescriptorFactory.getAnnotation().equals(Utils.FX_SETTER) && 
                            Access.convertFromVariableElement(Utils.getCurrentAnnotationAttribute(this.elementUtils, annotationMirror, "access")) == Access.PUBLIC)
                        {
                            messager.printMessage(Diagnostic.Kind.WARNING, MessageFormat.format(this.messageBundle.getString("platform.annotation.javafx.warning.publicwrappersetter"), fieldName));
                        }
                        
                        //  フィールド名の書式チェック
                        if (!Validators.checkStartsWithLowerCase(fieldName))
                        {
                            messager.printMessage(Diagnostic.Kind.WARNING, MessageFormat.format(this.messageBundle.getString("platform.annotation.javafx.warning.startlowercase"), fieldName));
                            
                        }
                        
                        //  不要な接尾語がついていないかチェック
                        if (!Validators.checkEndsWithoutProperty(fieldName))
                        {
                            messager.printMessage(Diagnostic.Kind.WARNING, MessageFormat.format(this.messageBundle.getString("platform.annotation.javafx.warning.unnecessarysuffix"), fieldName));
                        }
                        
                        //  フィールドがpublicになっていないかチェック
                        if (!methodDescriptorFactory.checkAccessPrivilege())
                        {
                            if (Validators.isPublic(element))
                            {
                                messager.printMessage(Diagnostic.Kind.WARNING, MessageFormat.format(this.messageBundle.getString("platform.annotation.javafx.warning.publicfield"), fieldName));
                            }
                            else
                            {
                                messager.printMessage(Diagnostic.Kind.WARNING, MessageFormat.format(this.messageBundle.getString("platform.annotation.javafx.warning.loweraccessmethod"), fieldName));
                            }
                        }
                        
                        //trace.showMessage("Now generating a new method...");
                        //trace.showMessage(methodDescriptor.getDebugInfo());
                        
                        //  メソッドを作成する
                        JCMethodDecl generatedMethod = MethodGenerator.generateAccessor(treeMaker, names, classDecl, methodDescriptor);
                        
                        //  ASTにメソッドを追加する
                        classDecl.defs = classDecl.defs.append(generatedMethod);
                    }

                    break;
                }
                default:
                {
                    //  それ以外の要素の場合。何もせず次の要素を探索する。
                    break;
                }
            }
        });
        
        //  ASTの情報を画面に出力する（デバッグ用）
        dumpClassDecl(classDecl);
    }
        
    //  クラス定義が持つASTの情報を表示する
    private void dumpClassDecl(JCClassDecl classDecl)
    {
        Utils.TraceMessager trace = Utils.TraceMessager.getInstance();
        trace.showMessage("The cuurent AST tree info:");
        classDecl.defs.stream().forEach(c -> 
        {
            trace.showMessage("--  " + c.getKind() + "  --");
            trace.showMessage(c.getTree().toString());
        });
    }    
}

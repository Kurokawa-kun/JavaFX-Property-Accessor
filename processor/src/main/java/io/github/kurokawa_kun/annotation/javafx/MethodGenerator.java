package io.github.kurokawa_kun.annotation.javafx;
import com.sun.tools.javac.code.BoundKind;
import io.github.kurokawa_kun.annotation.javafx.descriptor.*;
import io.github.kurokawa_kun.annotation.javafx.type.*;
import static io.github.kurokawa_kun.annotation.javafx.Utils.*;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;

public class MethodGenerator 
{
    private static TreeMaker treeMaker = null;
    private static Names names = null;
    
    public static JCMethodDecl generateAccessor(TreeMaker treeMaker, Names names, JCClassDecl classDecl, MethodDescriptor methodDescriptor)
    {
        //  環境情報の取得
        MethodGenerator.treeMaker = treeMaker;
        MethodGenerator.names = names;
        
        //  作成するメソッドの情報を取得
        Format format = methodDescriptor.getFormat();
        Logic logic = methodDescriptor.getLogic();
        
        //  Posを現在位置に合わせる（超重要）
        treeMaker.at(classDecl.pos);
        
        //  メソッド名
        Name targetMethodName = names.fromString(format.getTargetMethodName());
        
        //  メソッド修飾子
        JCModifiers targetMethodModifiers = treeMaker.Modifiers(format.getFlags());
        
        //  メソッドの復帰値の型を設定する
        JCExpression targetReturnType = null;
        if (logic.getReturnType().returnsValue())
        {
            //  何かしらの復帰値がある場合
            if (logic.getReturnType().callsMethod())
            {
                //  復帰値がある、かつ、メソッドの復帰値を返す場合
                
                if (logic.getReturnType() == ReturnType.RETURN_PRIMITIVE_VALUE)
                {
                    //  プリミティブ型の復帰値を返す場合                    
                    targetReturnType = treeMaker.TypeIdent(PRIMITIVE_TAGS.get(logic.getPropertyType()));
                }
                else
                {
                    //  オブジェクト型の復帰値を返す場合
                    if (logic.getReturnType().hasGenericTypeInfo())
                    {
                        //  ジェネリクス型を持つ復帰値を返す場合
                        
                        //  型情報の作成
                        List<JCExpression> genericReturnValues = List.nil();
                        ObjectNode objectNodes = new ObjectNode(null);
                        objectNodes.makeNodes(logic.getGenericTypeNames(), 0);
                        targetReturnType = treeMaker.TypeApply(Utils.makeQualifiedIdentifier(treeMaker, names, logic.getPropertyType()), objectNodes.getExpressions(objectNodes));
                    }
                    else
                    {
                        //  オブジェクト型単体の復帰値を返す場合
                        targetReturnType = Utils.makeQualifiedIdentifier(treeMaker, names, logic.getPropertyType());                            
                    }
                }
            }
            else
            {
                //  復帰値がある、かつ、フィールドを返す場合                
                if (logic.getReturnType().hasGenericTypeInfo())
                {
                    //  ジェネリクス型を持つフィールドを返す場合
                    List<JCExpression> genericReturnValues = List.nil();
                    ObjectNode objectNodes = new ObjectNode(null);
                    objectNodes.makeNodes(logic.getGenericTypeNames(), 0);                        
                    targetReturnType = treeMaker.TypeApply(Utils.makeQualifiedIdentifier(treeMaker, names, logic.getPropertyType()), objectNodes.getExpressions(objectNodes));
                }
                else
                {
                //  復帰値がある、かつ、プロパティそのものを返す場合
                    targetReturnType = Utils.makeQualifiedIdentifier(treeMaker, names, logic.getPropertyType());
                }
            }
        }
        else
        {
            //  復帰値がない場合
            targetReturnType = treeMaker.TypeIdent(com.sun.tools.javac.code.TypeTag.VOID);
        }
        
        //  メソッドのパラメータ名とパラメータの型を設定する
        Name targetParameterName = null;
        List<JCVariableDecl> parameterDecls = List.nil();
        if (logic.getParameterType() == ParameterType.NO_PARAMETER)
        {
            //  メソッドにパラメータが存在しない場合
            parameterDecls = List.nil();
        }
        else if (logic.getParameterType() == ParameterType.PRIMITIVE_PARAMETER)
        {
            //  メソッドにプリミティブ型のパラメータが存在する場合
            targetParameterName = names.fromString(format.getVariableName());
            parameterDecls = parameterDecls.append(treeMaker.VarDef
            (
                treeMaker.Modifiers(Flags.PARAMETER),                           //  パラメータ属性（PARAMETERの指定がないとコンパイラがクラッシュする）
                targetParameterName,                                            //  パラメータの名前
                treeMaker.TypeIdent(Utils.PRIMITIVE_TAGS.get(logic.getPropertyType())),  //  パラメータの型
                null                                                            //  初期値（nullで問題ない）
            ));
        }
        else
        {
            if (logic.getParameterType().hasGenericTypeInfo())
            {                
                //  ジェネリクス型を持つパラメータが存在する場合
                ObjectNode objectNodes = new ObjectNode(null);
                objectNodes.makeNodes(logic.getGenericTypeNames(), 0);
                targetParameterName = names.fromString(format.getVariableName());
                parameterDecls = parameterDecls.append(treeMaker.VarDef
                (
                    treeMaker.Modifiers(Flags.PARAMETER),                           //  パラメータの属性（PARAMETERの指定がないとコンパイラがクラッシュする）
                    targetParameterName,                                            //  パラメータの名前
                    treeMaker.TypeApply(Utils.makeQualifiedIdentifier(treeMaker, names, logic.getPropertyType()), objectNodes.getExpressions(objectNodes)),  //  パラメータの型
                    null                                                            //  初期値（nullで問題ない）
                ));
            }
            else
            {
                //  メソッドにオブジェクト型のパラメータが存在する場合
                targetParameterName = names.fromString(format.getVariableName());
                parameterDecls = parameterDecls.append(treeMaker.VarDef
                (
                    treeMaker.Modifiers(Flags.PARAMETER),                           //  パラメータの属性（PARAMETERの指定がないとコンパイラがクラッシュする）
                    targetParameterName,                                            //  パラメータの名前
                    Utils.makeQualifiedIdentifier(treeMaker, names, logic.getPropertyType()),   //  パラメータの型
                    null                                                            //  初期値（nullで問題ない）
                ));
            }
        }
        
        //  対象となる識別子を特定する
        JCExpression thisKeyword;
        if (logic.getMemberType() == MemberType.INSTANCE_MEMBER)
        {
            //  インスタンスメソッドの場合
            thisKeyword = treeMaker.Ident(names.fromString("this"));
        }
        else
        {
            //  クラスメソッドの場合
            thisKeyword = treeMaker.Ident(classDecl.getSimpleName());
        }
        
        //  対象プロパティの名前
        Name targetPropertyName = names.fromString(format.getFieldName());
        
        //  呼び出す処理がメソッドなのかプロパティ（フィールド）そのものなのか決定する
        JCExpression methodCall;
        
        if (logic.getReturnType().callsMethod())
        {
            //  getter/setterのメソッドを呼び出す            
            Name targetMethod = names.fromString(logic.getTargetMethodName());
            methodCall = treeMaker.Select(treeMaker.Select(thisKeyword, targetPropertyName), targetMethod);
        }
        else
        {
            //  プロパティそのものを参照する
            methodCall = treeMaker.Select(thisKeyword, targetPropertyName);
        }
        
        //  return文を作成する
        JCStatement returnStatement;
        List<JCExpression> methodArgs = List.nil();
        
        if (logic.getReturnType().returnsValue())
        {
            //  復帰値を持つ処理の呼び出し
            if (logic.getReturnType().callsMethod())
            {
                //  メソッドを呼び出す
                if (logic.getReturnType().hasGenericTypeInfo())
                {
                    //  型情報のついたメソッドを呼び出す
                    
                    List<JCExpression> returnValue = List.nil();
                    JCMethodInvocation methodInvocation = treeMaker.Apply
                    (
                        List.nil(),     //  メソッドの復帰値
                        methodCall,     //  メソッドの本体
                        methodArgs      //  メソッドのパラメータ
                    );
                    returnStatement = treeMaker.Return(methodInvocation);
                }
                else
                {
                    //  型情報のないメソッドを呼び出す
                    JCMethodInvocation methodInvocation = treeMaker.Apply
                    (
                        List.nil(),     //  メソッドの復帰値
                        methodCall,     //  メソッドの本体
                        methodArgs      //  メソッドのパラメータ
                    );
                    returnStatement = treeMaker.Return(methodInvocation);
                }
            }
            else
            {
                //  フィールドを参照する
                returnStatement = treeMaker.Return(methodCall);
            }            
        }
        else
        {
            //  復帰値を持たない処理の呼び出し
            methodArgs = methodArgs.append(treeMaker.Ident(targetParameterName));            
            JCMethodInvocation methodInvocation = treeMaker.Apply
            (
                List.nil(),     //  メソッドの復帰値
                methodCall,     //  メソッドの本体
                methodArgs      //  メソッドのパラメータ
            );
            returnStatement = treeMaker.Exec(methodInvocation);
        }
        
        //  ブロックにする
        JCTree.JCBlock block = treeMaker.Block(0, List.of(returnStatement));        
        
        //  AST Node形式のメソッド定義に組み立てる
        JCMethodDecl methodDecl = treeMaker.MethodDef
        (
            targetMethodModifiers,  //  修飾子
            targetMethodName,       //  メソッド名
            targetReturnType,       //  戻り値の型
            List.nil(),             //  型パラメータ
            parameterDecls,         //  引数の一覧
            List.nil(),             //  例外リスト
            block,                  //  メソッド本文
            null                    //  デフォルト値
        );
        
        return methodDecl;
    }
    
    public static class ObjectNode
    {
        public enum ClassType
        {
            ROOT_NODE,
            CLASS_NAME,
            ARRAY,
            WILDCARD_EXTENDS,
            WILDCARD_SUPER,
        };
        
       //  構文エラーはコンパイラによって事前に弾かれているので、その手の例外処理は行わない
        public StringBuffer className = null;
        public JCStatement statement = null;
        public ObjectNode parentNode = null;
        public java.util.List<ObjectNode> childNodes = new java.util.ArrayList<>(3);
        public ClassType classType = ClassType.CLASS_NAME;
        
        public ObjectNode(ObjectNode parentNode)
        {
            this.className = new StringBuffer(100);
            this.parentNode = parentNode;
        }
        
        private boolean isExtendsWildcard(String className)
        {
            return className.startsWith("? extends ");
        }

        private boolean isSuperWildcard(String className)
        {
            return className.startsWith("? super ");
        }
        
        private boolean isArray(String className)
        {
            return className.endsWith("[]");
        }
        
        //  ワイルドカードが指定されているとき、条件となるクラス名を取得する
        private String getClassName(String className)
        {
            String[] parts = className.split(" ", 3);
            return parts[2];                
        }
        
        private void setClassType()
        {
            if (this.isExtendsWildcard(this.className.toString()))
            {
                String newClassName = this.getClassName(this.className.toString());
                this.className.setLength(0);
                this.className.append(newClassName);
                this.classType = ClassType.WILDCARD_EXTENDS;
            }
            else if (this.isSuperWildcard(this.className.toString()))
            {
                String newClassName = this.getClassName(this.className.toString());
                this.className.setLength(0);
                this.className.append(newClassName);
                this.classType = ClassType.WILDCARD_SUPER;
            }
            else if (this.isArray(this.className.toString()))
            {
                String newClassName = className.substring(0, className.length() - 2);
                this.className.setLength(0);
                this.className.append(newClassName);
                this.classType = ClassType.ARRAY;
            }
        }        
        
        public int makeNodes(String elementType, int pos)
        {
            for ( ; pos < elementType.length(); pos++)
            {
                char currentChar = elementType.charAt(pos);
                switch (currentChar)
                {
                    case '<':
                    {
                        //  自ノードの種類を判定する
                        setClassType();
                        
                        //  子ノードを作る
                        ObjectNode childNode = new ObjectNode(this);
                        this.childNodes.add(childNode);
                        pos = childNode.makeNodes(elementType, pos + 1);
                        continue;
                    }
                    case ',':
                    {
                        //  自ノードの種類を判定する
                        setClassType();
                        
                        //  兄弟ノードを作る
                        ObjectNode childNode = new ObjectNode(this.parentNode);
                        this.parentNode.childNodes.add(childNode);
                        pos = childNode.makeNodes(elementType, pos + 1);
                        return pos;
                    }
                    case '>':
                    {
                        //  自ノードの種類を判定する
                        setClassType();
                        
                        //  終わり（親ノードに探索を続けてもらう）
                        return pos;
                    }
                    default:
                    {
                        //  クラス名を取得
                        this.className.append(currentChar);
                    }
                }
            }
            setClassType();
            return pos;
        }
        
        private List<JCExpression> getExpressions(ObjectNode currentNode)
        {
            com.sun.tools.javac.util.List<JCExpression> currentExpressions = List.nil();
            
            if (currentNode.childNodes.size() == 0)
            {
                //  子ノードを持っていない
                switch (this.classType)
                {
                    case CLASS_NAME:
                    {
                        //  通常のクラスだった場合、自ノードをJCExpressionとして返す
                        JCExpression currentExpression = Utils.makeQualifiedIdentifier(MethodGenerator.treeMaker, MethodGenerator.names, currentNode.className.toString());
                        return List.of(currentExpression);
                    }
                    case WILDCARD_EXTENDS:
                    {
                        JCExpression currentExpression = Utils.makeQualifiedIdentifier(MethodGenerator.treeMaker, MethodGenerator.names, currentNode.className.toString());                    
                        JCExpression wildcard = treeMaker.Wildcard(treeMaker.TypeBoundKind(BoundKind.EXTENDS), currentExpression);
                        return List.of(wildcard);
                    }
                    case WILDCARD_SUPER:
                    {
                        JCExpression currentExpression = Utils.makeQualifiedIdentifier(MethodGenerator.treeMaker, MethodGenerator.names, currentNode.className.toString());                    
                        JCExpression wildcard = treeMaker.Wildcard(treeMaker.TypeBoundKind(BoundKind.SUPER), currentExpression);
                        return List.of(wildcard);
                    }
                    case ARRAY:
                    {
                        JCExpression currentExpression = Utils.makeQualifiedIdentifier(MethodGenerator.treeMaker, MethodGenerator.names, currentNode.className.toString());                    
                        JCExpression array = treeMaker.TypeArray(currentExpression);
                        return List.of(array);
                    }
                    default:
                    {
                        //  あり得ないルート
                        return null;
                    }
                }
            }
            else
            {
                //  子ノードを持っている場合                
                com.sun.tools.javac.util.List<JCExpression> childExpressions = List.nil();
                for (ObjectNode childNode : currentNode.childNodes)
                {
                    //  子ノードの同一メソッドを再帰的に呼び出し、リストに追加する
                    childExpressions = childExpressions.append(childNode.getExpressions(childNode).head);
                }
                
                if (this.parentNode == null)
                {
                    //  ルートノードだった場合、自分自身は無視して子ノードの情報だけを返す
                    return childExpressions;
                }
                else
                {
                    //  ルートノードではない場合
                    //  自分自身をJCTypeArrayとして返す                    
                    JCExpression currentExpression = Utils.makeQualifiedIdentifier(MethodGenerator.treeMaker, MethodGenerator.names, currentNode.className.toString());                    
                    return List.of(MethodGenerator.treeMaker.TypeApply(currentExpression, childExpressions));
                }
            }
        }
    }
}

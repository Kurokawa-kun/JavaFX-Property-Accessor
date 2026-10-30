package io.github.kurokawa_kun.annotation.javafx;
import io.github.kurokawa_kun.annotation.javafx.descriptor.Format;
import io.github.kurokawa_kun.annotation.javafx.descriptor.Logic;
import io.github.kurokawa_kun.annotation.javafx.type.MemberType;
import io.github.kurokawa_kun.annotation.javafx.type.ReturnType;
import io.github.kurokawa_kun.annotation.javafx.type.ParameterType;
import io.github.kurokawa_kun.annotation.javafx.descriptor.MethodDescriptor;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;
import static io.github.kurokawa_kun.annotation.javafx.Utils.primitiveTags;

public class MethodFactory 
{
    public static JCMethodDecl generateAccessor(TreeMaker treeMaker, Names names, JCClassDecl classDecl, MethodDescriptor methodDescriptor)
    {
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
                    targetReturnType = treeMaker.TypeIdent(primitiveTags.get(logic.getPropertyType()));
                }
                else
                {
                    //  オブジェクト型の復帰値を返す場合
                    targetReturnType = Utils.makeQualifiedIdentifier(treeMaker, names, logic.getPropertyType());
                }
            }
            else
            {
                //  復帰値がある、かつ、プロパティそのものを返す場合
                targetReturnType = Utils.makeQualifiedIdentifier(treeMaker, names, logic.getPropertyType());
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
                treeMaker.TypeIdent(Utils.primitiveTags.get(logic.getPropertyType())),  //  パラメータの型
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
}

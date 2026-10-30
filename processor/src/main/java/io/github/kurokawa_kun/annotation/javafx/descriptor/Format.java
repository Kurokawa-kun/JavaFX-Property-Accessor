package io.github.kurokawa_kun.annotation.javafx.descriptor;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.util.List;
import io.github.kurokawa_kun.annotation.javafx.MethodFactory;
import io.github.kurokawa_kun.annotation.javafx.type.Access;
import io.github.kurokawa_kun.annotation.javafx.type.MemberType;
import javax.lang.model.element.Modifier;
import lombok.*;

//  Formatクラスはメソッドを作成する際に必要となる、メソッド名やメソッド名につく接頭語など「外側」の情報を格納するクラス
public class Format implements DebugInfo
{
    @Getter
    private final String tag; //  デバッグ用の識別子
    @Getter
    private MemberType memberType = null;  //  インスタンスメンバーかクラスメンバーか。staticが指定されていたときに変更が必要    
    private boolean prefix; //  接頭語を付けるか
    @Setter @Getter
    private String methodPrefix;//  作成されるメソッドに付く接頭語
    @Setter @Getter
    private String fieldName;//  対象となるフィールドの名前。例えばname,ageなど。
    private boolean suffix; //  接尾語を付けるか
    @Setter @Getter
    private String methodSuffix;  //  作成されるメソッドに付く接尾語
    @Getter @Setter
    private String variableName;    //  メソッドの引数の名前
    private boolean checkSuffixDuplicate;   //  フィールド名の末尾が接頭語と重複しているかチェックする
    
    //  外部には公開しないフラグ
    private long flags = 0;
    
    public void setAccess(Access access)
    {
        flags |= access.getFlag();
    }
    
    public void setMemberType(MemberType memberType)
    {
        flags |= memberType.getFlag();
    }
    
    public long getFlags()
    {
        return this.flags;
    }
    
    //  外部から呼び出しできないコンストラクタ
    private Format(String tag, MemberType memberType, boolean prefix, String methodPrefix, String fieldName, boolean suffix, String methodSuffix, String variableName, boolean checkSuffixDuplicate/*, String targetMethodName*/)
    {
        this.tag = tag;
        this.memberType = memberType;
        this.prefix = prefix;
        this.methodPrefix = methodPrefix;
        this.fieldName = fieldName;
        this.suffix = suffix;
        this.methodSuffix = methodSuffix;
        this.variableName = variableName;
        this.checkSuffixDuplicate = checkSuffixDuplicate;
    }
    
    public static Format makeGetterInstance()
    {
        return new Format("Format:GetterInstance", MemberType.INSTANCE_MEMBER, true, "get", "unnamed", false, "", "value", false);
    }
    public static Format makeGetterBooleanInstance()
    {
        return new Format("Format:GetterBooleanInstance", MemberType.INSTANCE_MEMBER, true, "is", "unnamed", false, "", "value", false);
    }
    public static Format makeSetterInstance()
    {
        return new Format("Format:SetterInstance", MemberType.INSTANCE_MEMBER, true, "set", "unnamed", false, "", "value", false);
    }
    public static Format makePropertyInstance()
    {
        return new Format("Format:PropertyInstance", MemberType.INSTANCE_MEMBER, false, "", "unnamed", true, "", "value", true);
    }
    
    //  メソッドの正式名を返す
    //  ※ targetMethodNameには2つある。Formatクラスが保持するものは作成するメソッドの外部から見た名前、
    //     Logicクラスが保持するものはそのメソッドの内部で呼び出すメソッドの名前
    public String getTargetMethodName()
    {
        //  接頭語が指定されている場合は、フィールド名の先頭1文字を大文字にする
        String body;
        if (this.hasPrefix())
        {
            body = Character.toUpperCase(this.fieldName.charAt(0)) + this.fieldName.substring(1);
        }
        else
        {
            body = this.fieldName;
        }
        
        String modifiedSuffix;
        if (this.shouldCheckSuffixDuplicate())
        {
            //  フィールド名が接尾語で終わっている場合（大文字小文字問わない）は、指定された通りのフィールド名、そうでない場合はフィールド名に接尾語を追記する
            modifiedSuffix = this.fieldName.toUpperCase().endsWith(this.methodSuffix.toUpperCase()) ? "" : this.methodSuffix;
        }
        else
        {
            modifiedSuffix = this.methodSuffix;
        }
        
        return (methodPrefix + body + modifiedSuffix);
    }
    
    public boolean hasPrefix()
    {
        return this.prefix;
    }
    public boolean hasSuffix()
    {
        return this.suffix;
    }
    public boolean shouldCheckSuffixDuplicate()
    {
        return this.checkSuffixDuplicate;
    }
    public void setCheckSuffixDuplicate(boolean checkSuffixDuplicate)
    {
        this.checkSuffixDuplicate = checkSuffixDuplicate;
    }
    
    //  デバッグ情報
    public String getDebugInfo()
    {
        StringBuilder stringBuilder = new StringBuilder(10000);
        stringBuilder.append(String.format("--  Format  --\n"));
        stringBuilder.append(String.format("tag                  :%s\n", this.tag));
        stringBuilder.append(String.format("memberType           :%s\n", memberType));
        stringBuilder.append(String.format("readOnly             :%s\n", this.prefix));
        stringBuilder.append(String.format("methodPrefix         :%s\n", this.methodPrefix));
        stringBuilder.append(String.format("fieldName            :%s\n", this.fieldName));
        stringBuilder.append(String.format("suffix               :%s\n", this.suffix));
        stringBuilder.append(String.format("methodSuffix         :%s\n", this.methodSuffix));
        stringBuilder.append(String.format("variableName         :%s\n", this.variableName));
        stringBuilder.append(String.format("checkSuffixDuplicate :%s\n", this.checkSuffixDuplicate));
        stringBuilder.append(String.format("flags                :%s\n", this.flags));
        stringBuilder.append(String.format("getTargetMethodName():%s\n", this.getTargetMethodName()));
        
        return stringBuilder.toString();
    }
}

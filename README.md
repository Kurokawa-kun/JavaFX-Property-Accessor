# JavaFX-Property-Accessor
## 説明
JavaFXプロパティのアクセサメソッドを自動作成できるアノテーションプロセッサです。実行中のコンパイラのASTを直接書き換える手法をとっています。getterメソッドまでは完成したので一旦公開します。setterメソッドとプロパティメソッドは現在作成中です（技術的に難しい…）。
プロジェクトの構成は以下の通り。
- **JavaFX-Property-Accessor**：プロジェクト全体
- **processor**：アノテーションプロセッサの入ったサブプロジェクト
- **example**：サンプルプログラムの入ったサブプロジェクト


## プロパティについて
JavaFXのプロパティを作成するには以下3つのアクセサメソッドを用意する必要があります。本アノテーションプロセッサを使うことでこれらのコードを自動生成することが可能です。
```
    //  JavaFXプロパティ
    private StringProperty text = new SimpleStringProperty();

    （中略）
    
    //  getterメソッド
    public String getText()
    {
        return this.text.get();
    }
    //  setterメソッド
    public void setText(String text)
    {
        this.text.set(text);
    }    
    //  プロパティメソッド
    public StringProperty textProperty()
    {
        return this.text;
    }
```
# 使用方法
## 設定
ご自分のプロジェクトにサブプロジェクトprocessorを追加し、build.gradleに以下の記述を追加します。
``` build.gradle

dependencies
{
    （中略）
    
    implementation project(':processor')
    annotationProcessor project(':processor')
}

tasks.withType(JavaCompile)
{
    （中略）
    
    options.fork = true
    options.forkOptions.jvmArgs += '--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED'
    options.forkOptions.jvmArgs += '--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED'
    options.forkOptions.jvmArgs += '--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED'
    options.forkOptions.jvmArgs += '--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED'
    options.forkOptions.jvmArgs += '--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED'
    
    options.compilerArgs += '-Averbose=true'  //  詳細メッセージを表示する場合。普段このオプションは指定しなくていい
}
```
プラグインにすることでこれらの手順はたぶん自動化できると思います（気が向いたらやります…）。

## アノテーションの使用
JavaFXのプロパティフィールドに以下のアノテーションをつけてコンパイルしてください。
- @FxGetter
- ~~@FxSetter~~
- ~~@FxProperty~~

``` App1.java
import io.github.kurokawa_kun.annotation.*;
import javafx.beans.property.*;

public class App1
{
    @FxGetter
    private StringProperty name = new SimpleStringProperty("Name property here.");
    
    public App1()
    {
        System.out.println("App1 constructor called.");
        System.out.println(this.getName()); //  ソースコードにないメソッドの呼び出し
    }
    
    public static void main(String[] args)
    {
        System.out.println("App1 main called.");
        new App1();
    }    
}
```

## 制限事項
上述の通り、@FxSetterと@FxPropertyは現在開発中です。

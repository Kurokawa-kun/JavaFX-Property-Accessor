# JavaFX-Property-Accessor
## このプロジェクトの説明
JavaFXプロパティのアクセサメソッドを自動作成できるアノテーションプロセッサです。実行中のコンパイラのASTを直接書き換える手法をとっています。一応の機能は完成したので公開します。バージョン0.9 Betaといったところ。

プロジェクトの構成は以下の通り。
- **JavaFX-Property-Accessor**：ルートプロジェクト
- **processor**：アノテーションプロセッサ本体が格納されたサブプロジェクト
- **plugin**：アノテーションプロセッサをgradleプラグインとして使うためのラッパーが格納されたサブプロジェクト
- ~~**examples**：サンプルプログラムの入ったサブプロジェクト~~

examplesプロジェクトは別プロジェクトに分けました。[リンクはこちら](https://github.com/Kurokawa-kun/JavaFX-Property-Accessor-Examples)。

## アクセサメソッドについて
JavaFXのプロパティを作成するには以下3つのアクセサメソッドを用意する必要があります。本アノテーションプロセッサを使うことでこれらを自動作成することが可能です。
```
    //  JavaFXプロパティ
    private StringProperty text = new SimpleStringProperty();

    （中略）
    
    //  getterメソッド
    public final String getText()
    {
        return this.text.get();
    }
    //  setterメソッド
    public final void setText(String text)
    {
        this.text.set(text);
    }    
    //  プロパティメソッド
    public final StringProperty textProperty()
    {
        return this.text;
    }
```
## サポートしているJavaのバージョン
サポートしているJavaのバージョンは以下の通りです。
| バージョン | サポート | 補足 |
| ---- | :----: | ---- |
| Java 16 以前 | ❌ | バイトコードのバージョンが違うため起動しません。 |
| Java 17 LTS ～25 LTS | ✔ | |
| Java 26（現時点で最新） | ✔ | |
| Java 27 以降（将来のバージョン） | ✔ | 動作未確認です。起動時に警告メッセージが表示されます。多分動くとは思います。 |

## 使用方法
### ビルドとローカルリポジトリへの公開
ルートプロジェクトで以下のコマンドを実行します。
``` 
gradle build
```
ビルドが成功するとpublishToMavenLocalコマンド（ローカルリポジトリへの公開）も自動的に実行され、ローカルリポジトリとして使用できるようになります。

### プラグインの利用
利用する側のプロジェクトのbuild.gradleに以下の記述を追加してください。
```
plugins
{
    id 'io.github.kurokawa_kun.javafx-property-accessor-plugin' version '0.9.0'
}

repositories
{
    mavenLocal()
    gradlePluginPortal()
}
```
デバッグ用の詳細メッセージを出力する場合は build.gradle に以下の記述も追加してください。
```
tasks.withType(JavaCompile)
{
    options.compilerArgs += '-Averbose=true'
}
```
Gradleがローカルのリポジトリからプラグインを探せるよう settings.gradle に以下の記述を追加してください。
```
pluginManagement
{
    repositories 
    {
        mavenLocal()
        gradlePluginPortal()
    }
}
```
Java 9以降のモジュールシステムを利用する場合は module-info.java に以下の記述を追加してください。
```
module yourmodulename
{
    requires JavaFxPropertyAccessor;
}
```
[こちら](https://github.com/Kurokawa-kun/JavaFX-Property-Accessor-Examples) にサンプルコードが入ったプロジェクトを収めたリポジトリがあります。よければお使いください。

### アノテーションの使用
JavaFXのプロパティフィールドに以下のアノテーションをつけてビルドしてください。
- @FxGetter
- @FxSetter
- @FxProperty

```
import io.github.kurokawa_kun.annotation.javafx.*;
import javafx.beans.property.*;

public class App1
{
    @FxSetter
    @FxGetter
    @FxProperty
    private IntegerProperty fontSize = new SimpleIntegerProperty(20);  //  アクセサをつけるプロパティ
    private IntegerProperty boundFontSize = new SimpleIntegerProperty();  //  付加されたプロパティメソッドを用いて値をバインドする
    
    public App1()
    {
        //  ソースコード中に存在しない（App1クラスが保持していない）メソッドを呼び出している
        boundFontSize.bind(this.fontSizeProperty());        
        System.out.println("Current font size:" + this.getFontSize());
        this.setFontSize(30);
        System.out.println("Current font size:" + this.getFontSize());
        System.out.println("Current bound font size:" + this.boundFontSize.get());
    }
    
    public static void main(String[] args)
    {
        System.out.println("App1 main called.");
        new App1();
    }    
}
```

## 対応しているプロパティ
以下の型のインスタンス、または以下の型を継承/実装したクラスのインスタンスに対応しています。
- ReadOnlyPropertyインターフェース（例えばReadOnlyIntegerPropertyインターフェース, ReadOnlyFloatPropertyインターフェースなど）
- Propertyクラス（例えばIntegerPropertyクラス, FloatPropertyクラスなど）
- ReadOnlyWrapperクラス（例えばReadOnlyIntegerWrapperクラス, ReadOnlyFloatWrapperクラスなど）

## 型情報を持つプロパティへの対応
以下のような型情報を持つプロパティにも対応しています。
```
public class MyApp
{
    @FxGetter
    private SetProperty<? extends Long> extendsProperty;
    @FxGetter
    private SetProperty<? super Long> superProperty;
    @FxGetter
    private ListProperty<String[]> arrayProperty;
    @FxGetter
    private ListProperty<Map<Map<String, Integer>, Set<Object>>> complicatedProperty;
    
    （中略）
}
```

## 指定可能なオプション
以下のオプションが指定可能です。
<table>
  <thead>
    <tr>
      <th scope="col" rowspan="2">オプション名</th>
      <th scope="col" colspan="3">対応しているアノテーション</th>
      <th scope="col" rowspan="2">指定可能な値<br>（太字はデフォルト値）</th>
      <th scope="col" rowspan="2">説明</th>
    </tr>
    <tr>
      <th scope="col">@FxGetter</th>
      <th scope="col">@FxSetter</th>
      <th scope="col">@FxProperty</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>valueType</td>
      <td>✔</td>
      <td>✔</td>
      <td></td>
      <td><b>DEFAULT</b><br>PRIMITIVE<br>OBJECT</td>
      <td>IntegerProperty, DoubleProperty, BooleanPropertyなどのプリミティブ型を扱うプロパティの場合、プリミティブ型の引数を取るか、オブジェクト型の引数を取るかを指定します。DEFAULTを指定した場合、優先的にPRIMITIVEになります。<br>PRIMITIVEを指定した場合、オートボクシングの処理が入らないため若干高速です。ただし値がnullを取り得る場合は誤動作を防ぐためOBJECTを指定してください。</td>
    </tr>
    <tr>
      <td>access</td>
      <td>✔</td>
      <td>✔</td>
      <td>✔</td>
      <td><b>DEFAULT</b><br>PUBLIC<br>PACKAGE<br>PROTECTED<br>PRIVATE</td>
      <td>作成されるメソッドのアクセス権を指定します。DEFAULTを指定するとPUBLICになります。ただし、ReadOnlyWrapperのsetterメソッドを作成する場合はデフォルトでPROTECTEDになります。</td>
    </tr>
    <tr>
      <td>readOnly</td>
      <td></td>
      <td></td>
      <td>✔</td>
      <td>true<br><b>false</b></td>
      <td>対象プロパティをそのまま返すのではなく、ReadOnlyProperty型にキャストしてから返します。対象プロパティがReadOnlyPropertyまたはReadOnlyWrapperの場合、指定の有無に関係なく常にreadOnlyです。<br><br>※ Javaの仕様上、利用者がProperty型にキャストした場合、値の書き換えが可能になります。値の書き換えを完全に不可能にしたい場合はReadOnlyWrapperクラスを使用してください。</td>
    </tr>
    <tr>
      <td>prefix</td>
      <td>✔</td>
      <td>✔</td>
      <td>✔</td>
      <td>””（空欄）</td>
      <td>作成されるメソッドにつく接頭語です。<br>getterメソッドの場合、空欄を指定しても自動的にget（boolean型の場合はis）がつきます。<br>setterメソッドの場合、空欄を指定しても自動的にsetがつきます。</td>
    </tr>
    <tr>
      <td>suffix</td>
      <td>✔</td>
      <td>✔</td>
      <td>✔</td>
      <td>FxGetter, Fxsetterの場合は””（空欄）<br>FxProperyの場合は”Property”</td>
      <td>作成されるメソッドにつく接尾語です。<br>propertyメソッドに限り、単語の重複チェックが行われます（例えばプロパティ名がnamePropertyだった場合、作成されるメソッドがnamePropertyPropertyにはなりません）。</td>
    </tr>
    <tr>
      <td>parameterName</td>
      <td></td>
      <td>✔</td>
      <td></td>
      <td>value</td>
      <td>setterメソッドの引数となるパラメータの名前です。</td>
    </tr>
  </tbody>
</table>

## 今後について
今後の予定は以下の通り。手が空いたらやります。
1. リファクタリングの実施。突貫工事と仕様変更を繰り返したためソースが汚いです。冗長な変数や冗長なif文がかなりあります😅
2. 品質確保のためのテストコードの追加
3. メッセージファイルの拡充。日本語と英語以外の追加。
4. Mavenセントラルリポジトリへの登録。ローカルリポジトリへ公開しなくてもプラグインとして使えるようにしたい。

import io.github.kurokawa_kun.annotation.*;
import javafx.beans.property.*;

public class App1
{
    @FxGetter
    private StringProperty name = new SimpleStringProperty("Name property here.");
    
//    @FxGetter
//    private IntegerProperty height = new SimpleIntegerProperty(75);

    //  エラー：Propertyインターフェースを継承していないクラス
    //@FxGetter
    //private String CompanyName;
    
    //  エラー：プリミティブ型のプロパティ
    //@FxGetter
    //private int intValue;

    
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

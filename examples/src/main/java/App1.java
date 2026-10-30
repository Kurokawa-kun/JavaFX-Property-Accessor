import io.github.kurokawa_kun.annotation.javafx.*;
import io.github.kurokawa_kun.annotation.javafx.type.*;
import javafx.beans.property.*;

//  サンプルプログラム
public class App1
{
    @FxGetter
    private IntegerProperty age = new SimpleIntegerProperty();    
    
    @FxGetter(valueType = ValueType.OBJECT)
    private IntegerProperty height = new SimpleIntegerProperty();    
    
    @FxSetter(access = Access.PROTECTED)
    private StringProperty name = new SimpleStringProperty();
    
    @FxGetter
    private double temperature; //  エラー
    
    public App1()
    {
    }
    
    public static void main(String[] args)
    {
        System.out.println("App1 main called.");
        new App1();
    }    
}

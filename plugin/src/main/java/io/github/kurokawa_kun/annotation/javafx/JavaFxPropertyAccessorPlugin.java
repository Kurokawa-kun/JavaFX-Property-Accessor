package io.github.kurokawa_kun.annotation.javafx;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;

public class JavaFxPropertyAccessorPlugin implements Plugin<Project>
{
    @Override
    public void apply(Project project) 
    {
        //  プラグイン適用時に必要な依存関係を追加する
        project.getPlugins().withId("java", javaPlugin -> 
        {
            project.getDependencies().add("compileOnly", "io.github.kurokawa_kun:javafx-property-accessor:0.9.0");
            project.getDependencies().add("implementation", "io.github.kurokawa_kun:javafx-property-accessor:0.9.0");
            project.getDependencies().add("annotationProcessor", "io.github.kurokawa_kun:javafx-property-accessor:0.9.0");
        });
        
        //  JavaCompileタスクの設定を行う
        project.getTasks().withType(JavaCompile.class).configureEach(task -> 
        {
            task.getOptions().setFork(true);
            
            //  jvmArgsに--add-exportsを追加する
            task.getOptions().getForkOptions().getJvmArgs().addAll(java.util.List.of
            (
                "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED"
            ));
        });
    }
}

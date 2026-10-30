package io.github.kurokawa_kun.annotation.javafx.descriptor;
import lombok.*;

public abstract class MethodDescriptor implements DebugInfo
{
    @Getter @Setter
    Format format;
    @Getter @Setter
    Logic logic;
    
    @Override
    public String getDebugInfo()
    {
        StringBuilder stringBuilder = new StringBuilder(10000);
        for (DebugInfo debugInfo : new DebugInfo[] {format, logic})
        {
            stringBuilder.append(debugInfo.getDebugInfo());
        }        
        return stringBuilder.toString();
    }
}

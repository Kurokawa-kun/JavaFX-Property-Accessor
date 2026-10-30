package io.github.kurokawa_kun.annotation.javafx.type;
import lombok.*;

@RequiredArgsConstructor
public enum PropertyType
{
    READ_ONLY_PROPERTY  (true, false, false, true),
    PROPERTY            (true,  true,  true, true),
    READ_ONLY_WRAPPER   (true,  true, false, true);
    
    private final boolean makeGetter;
    private final boolean makeSetter;
    private final boolean makeProperty;    
    private final boolean makeReadOnlyProperty;
    
    public boolean canMakeGetter()
    {
        return this.makeGetter;
    }
    public boolean canMakeSetter()
    {
        return this.makeSetter;
    }
    public boolean canMakeProperty()
    {
        return this.makeProperty;
    }
    public boolean canMakeReadOnlyProperty()
    {
        return this.makeReadOnlyProperty;
    }
}

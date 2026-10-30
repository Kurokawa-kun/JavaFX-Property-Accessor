package io.github.kurokawa_kun.annotation.javafx.type;
import com.sun.tools.javac.code.Flags;
import lombok.*;

@RequiredArgsConstructor
public enum MemberType
{
    INSTANCE_MEMBER             ( true,           0L),  //  0L…何もしない
    CLASS_MEMBER                (false, Flags.STATIC);

    @Getter
    private final boolean isInstanceMember;
    @Getter
    private final long flag;
}

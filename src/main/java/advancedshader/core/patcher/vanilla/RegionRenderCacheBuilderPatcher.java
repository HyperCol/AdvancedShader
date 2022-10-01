package advancedshader.core.patcher.vanilla;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("bum")
public class RegionRenderCacheBuilderPatcher extends Patcher {

    @MethodPatch("<init>()V")
    public void init(MethodNode method) {
        patch("为Tripwire渲染类型添加渲染器", method,
                ByteCode.AAStore(),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetField("bum", "a", "[Lbuk;")),
                inject(ByteCode.Ldc("TRIPWIRE")),
                inject(ByteCode.InvokeStatic("amm", "valueOf", "(Ljava/lang/String;)Lamm;")),
                inject(ByteCode.InvokeVirtual("amm", "ordinal", "()I")),
                inject(ByteCode.New("buk")),
                inject(ByteCode.Dup()),
                inject(ByteCode.Ldc(0x40000)),
                inject(ByteCode.InvokeSpecial("buk", "<init>", "(I)V")),
                inject(ByteCode.AAStore()),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Return());
    }
}

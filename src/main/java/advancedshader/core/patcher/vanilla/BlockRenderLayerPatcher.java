package advancedshader.core.patcher.vanilla;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("amm")
public class BlockRenderLayerPatcher extends Patcher {

    @MethodPatch("<clinit>()V")
    public void clinit(MethodNode method) {
        patch("增加Tripwire渲染类型 第一部分", method,
                remove(ByteCode.IConst(4)),
                inject(ByteCode.IConst(5)),
                ByteCode.NewArray("amm"));

        patch("增加Tripwire渲染类型 第二部分", method,
                inject(ByteCode.Dup()),
                inject(ByteCode.IConst(4)),
                inject(ByteCode.New("amm")),
                inject(ByteCode.Dup()),
                inject(ByteCode.Ldc("TRIPWIRE")),
                inject(ByteCode.IConst(4)),
                inject(ByteCode.Ldc("Tripwire")),
                inject(ByteCode.InvokeSpecial("amm", "<init>", "(Ljava/lang/String;ILjava/lang/String;)V")),
                inject(ByteCode.AAStore()),
                ByteCode.PutStatic("amm", "f", "[Lamm;"));
    }

    @MethodPatch("values()[Lamm;")
    public void values(MethodNode method) {
        patch("高版本渲染机制兼容性修复", method,
                ByteCode.CheckCast("[Lamm;"),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "getBlockRenderLayers", "([Lamm;)[Lamm;")),
                ByteCode.AReturn());
    }
}

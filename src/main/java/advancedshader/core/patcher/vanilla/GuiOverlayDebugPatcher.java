package advancedshader.core.patcher.vanilla;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("bjd")
public class GuiOverlayDebugPatcher extends Patcher {

    @MethodPatch("b()Ljava/util/List;")
    public void call(MethodNode method) {
        patch("增加氛围值调试信息", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.ALoad(8)),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "addPlayerMoodDebug", "(Ljava/util/List;)V")),
                ByteCode.ALoad(8),
                ByteCode.AReturn());
    }
}

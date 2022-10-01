package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.config.MacroState")
public class MacroStatePatcher extends Patcher {

    @MethodPatch("processMacro(Ljava/lang/String;Ljava/lang/String;)V")
    public void processMacro(MethodNode method) {
        LabelNode label = ByteCode.Label();

        patch("修复宏状态机bug 第一部分", method,
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetField("net/optifine/shaders/config/MacroState", "active", "Z")),
                inject(ByteCode.IfZero(label)),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(1),
                ByteCode.Ldc("define"),
                ByteCode.InvokeVirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z"));

        patch("修复宏状态机bug 第二部分", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(label),
                ByteCode.ALoad(1),
                ByteCode.Ldc("ifdef"));
    }
}

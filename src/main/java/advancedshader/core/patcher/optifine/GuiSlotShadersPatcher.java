package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.gui.GuiSlotShaders")
public class GuiSlotShadersPatcher extends Patcher {

    @MethodPatch("checkCompatible(Lnet/optifine/shaders/IShaderPack;I)Z")
    public void checkCompatible(MethodNode method) {
        patch("光影包兼容性检测修改 游戏版本号", method,
                ByteCode.Ldc("version.1.12.2"),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "getGameVersionKey", "(Ljava/lang/String;)Ljava/lang/String;")));

        patch("光影包兼容性检测修改 OF版本号", method,
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "getOptiFineVersion", "(Ljava/lang/String;)Ljava/lang/String;")),
                ByteCode.AStore(7));
    }
}

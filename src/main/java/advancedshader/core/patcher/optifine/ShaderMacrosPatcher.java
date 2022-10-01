package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.config.ShaderMacros")
public class ShaderMacrosPatcher extends Patcher {

    @MethodPatch("getFixedMacroLines()Ljava/lang/String;")
    public void getFixedMacroLines(MethodNode method) {
        patch("增加额外预定义宏", method,
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic(HOOK, "addMacroLines", "(Ljava/lang/StringBuilder;)V")),
                ByteCode.ALoad(0),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));
    }

    @MethodPatch("getExtensions()[Lnet/optifine/shaders/config/ShaderMacro;")
    public void getExtensions(MethodNode method) {
        patch("增加RenderStage宏", method,
                ByteCode.ALoad(1),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "addRenderStageMacros", "([Lnet/optifine/shaders/config/ShaderMacro;)[Lnet/optifine/shaders/config/ShaderMacro;")),
                ByteCode.PutStatic("net/optifine/shaders/config/ShaderMacros", "extensionMacros", "[Lnet/optifine/shaders/config/ShaderMacro;"));
    }
}

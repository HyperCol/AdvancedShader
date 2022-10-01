package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.Program")
public class ProgramPatcher extends Patcher {

    @MethodPatch("<init>(ILjava/lang/String;Lnet/optifine/shaders/ProgramStage;Z)V")
    @MethodPatch("<init>(ILjava/lang/String;Lnet/optifine/shaders/ProgramStage;Lnet/optifine/shaders/Program;)V")
    public void clinit(MethodNode method) {
        patch("帧缓冲翻转配置扩容至16", method,
                ByteCode.ALoad(0),
                remove(ByteCode.BIPush(8)),
                inject(ByteCode.BIPush(16)),
                ByteCode.NewArray("java/lang/Boolean"),
                ByteCode.PutField("net/optifine/shaders/Program", "buffersFlip", "[Ljava/lang/Boolean;"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(0),
                remove(ByteCode.BIPush(8)),
                inject(ByteCode.BIPush(16)),
                ByteCode.NewArray(ByteCode.T_BOOLEAN),
                ByteCode.PutField("net/optifine/shaders/Program", "toggleColorTextures", "[Z"));
    }
}

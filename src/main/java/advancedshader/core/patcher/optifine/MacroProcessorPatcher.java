package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.config.MacroProcessor")
public class MacroProcessorPatcher extends Patcher {

    @MethodPatch("getMacroHeader(Ljava/lang/String;)Ljava/lang/String;")
    public void getMacroHeader(MethodNode method) {
        patch("增加光影选项宏", method,
                ByteCode.ALoad(1),
                ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderMacros", "getFixedMacroLines", "()Ljava/lang/String;"),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                ByteCode.Pop(),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.ALoad(6)),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeStatic(PROPERTYFIX, "getShaderOptionHeader", "(Ljava/lang/String;Ljava/lang/StringBuilder;)V")));
    }

    @MethodPatch("process(Ljava/io/InputStream;Ljava/lang/String;)Ljava/io/InputStream;")
    public void process(MethodNode method) {
        patch("光影选项宏复位", method,
                inject(ByteCode.AConstNull()),
                inject(ByteCode.PutStatic(PROPERTYFIX, "shaderOptions", "Ljava/util/List;")),
                method.instructions.getFirst());

        patch("高版本方块ID选项修改版本宏", method,
                ByteCode.ALoad(2),
                ByteCode.InvokeStatic("net/optifine/shaders/config/MacroProcessor", "getMacroHeader", "(Ljava/lang/String;)Ljava/lang/String;"),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "replaceBlockVersionMacro", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")));
    }
}

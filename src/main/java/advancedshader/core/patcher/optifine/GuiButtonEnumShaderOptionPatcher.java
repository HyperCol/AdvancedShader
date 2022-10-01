package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.gui.GuiButtonEnumShaderOption")
public class GuiButtonEnumShaderOptionPatcher extends Patcher {

    @MethodPatch("getButtonText(Lnet/optifine/shaders/config/EnumShaderOption;)Ljava/lang/String;")
    public void getButtonText(MethodNode method) {
        LabelNode label = ByteCode.Label();

        patch("增加高版本特性按钮文本", method,
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "getButtonText", "(Lnet/optifine/shaders/config/EnumShaderOption;)Ljava/lang/String;")),
                inject(ByteCode.Dup()),
                inject(ByteCode.IfNull(label)),
                inject(ByteCode.AReturn()),
                inject(label),
                method.instructions.getFirst());
    }
}

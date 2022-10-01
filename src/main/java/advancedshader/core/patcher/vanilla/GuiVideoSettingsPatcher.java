package advancedshader.core.patcher.vanilla;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("bls")
public class GuiVideoSettingsPatcher extends Patcher {

    @MethodPatch("actionPerformed(Lbja;I)V")
    public void actionPerformed(MethodNode method) {
        patch("屏蔽光影选项对各向异性过滤的判断", method,
                remove(ByteCode.InvokeStatic("Config", "isAnisotropicFiltering", "()Z")),
                inject(ByteCode.IConst(0)),
                ByteCode.IfZero(null));
    }
}

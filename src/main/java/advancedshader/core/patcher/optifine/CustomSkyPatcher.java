package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.CustomSky")
public class CustomSkyPatcher extends Patcher {

    @MethodPatch("renderSky(Lamu;Lcdr;F)V")
    public void renderSky(MethodNode method) {
        patch("增加CUSTOM_SKY渲染阶段配置", method,
                ByteCode.Return(),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.GetStatic(RENDERSTAGE, "CUSTOM_SKY", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));
    }
}

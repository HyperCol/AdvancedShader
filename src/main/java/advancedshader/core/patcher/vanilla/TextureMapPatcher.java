package advancedshader.core.patcher.vanilla;

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("cdp")
public class TextureMapPatcher extends Patcher {

    @MethodPatch("b(Lcep;)V")
    public void loadTextureAtlas(MethodNode method) {
        patch("加载适用于各向异性过滤的法线与高光精灵图", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(21),
                ByteCode.InvokeVirtual("java/lang/Exception", "printStackTrace", "()V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.ALoad(17)),
                inject(ByteCode.InvokeStatic(ANISOTROPICFILTER, "loadShaderSprite", "(Lcdq;)V")),
                ByteCode.Goto(null));

        patch("删除适用于各向异性过滤的法线与高光精灵图", method,
                ByteCode.ALoad(3),
                ByteCode.InvokeVirtual("cdq", "deleteSpriteTexture", "()V"),
                inject(ByteCode.ALoad(3)),
                inject(ByteCode.InvokeStatic(ANISOTROPICFILTER, "unloadShaderSprite", "(Lcdq;)V")));
    }

    @MethodPatch("d()V")
    public void updateAnimations(MethodNode method) {
        LabelNode normalEnd = ByteCode.Label();
        LabelNode specularEnd = ByteCode.Label();

        patch("更新适用于各向异性过滤的法线与高光精灵图动画", method,
                ByteCode.ALoad(6),
                ByteCode.InvokeVirtual("cdq", "isAnimationActive", "()Z"),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Inc(3, 1),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.ALoad(5)),
                inject(ByteCode.GetField("cdq", "spriteNormal", "Lcdq;")),
                inject(ByteCode.ALoad(5)),
                inject(ByteCode.InvokeStatic(ANISOTROPICFILTER, "updateShaderSpriteAnimation", "(Lcdq;Lcdq;)Z")),
                inject(ByteCode.IfZero(normalEnd)),
                inject(ByteCode.Inc(3, 1)),
                inject(normalEnd),
                inject(ByteCode.ALoad(5)),
                inject(ByteCode.GetField("cdq", "spriteSpecular", "Lcdq;")),
                inject(ByteCode.ALoad(5)),
                inject(ByteCode.InvokeStatic(ANISOTROPICFILTER, "updateShaderSpriteAnimation", "(Lcdq;Lcdq;)Z")),
                inject(ByteCode.IfZero(specularEnd)),
                inject(ByteCode.Inc(3, 1)),
                inject(specularEnd),
                ByteCode.Goto(null));
    }
}

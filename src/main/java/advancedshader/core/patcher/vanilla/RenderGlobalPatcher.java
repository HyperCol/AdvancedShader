package advancedshader.core.patcher.vanilla;

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("buy")
public class RenderGlobalPatcher extends Patcher {

    @MethodPatch("a(Lvg;Lbxy;F)V")
    public void renderEntities(MethodNode method) {
        LabelNode entitiesStart = ByteCode.Label();
        LabelNode entitiesEnd = (LabelNode) patch("添加shadowEntities配置 第一部分", method,
                collect(ByteCode.Label()),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                ByteCode.ILoad(21),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "endEntities", "()V"))[0];

        patch("添加shadowEntities配置 第二部分", method,
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "beginEntities", "()V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.ILoad(21)),
                inject(ByteCode.IfZero(entitiesStart)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "isShadowPass", "Z")),
                inject(ByteCode.IfZero(entitiesStart)),
                inject(ByteCode.GetStatic(HOOK, "shaderPackShadowEntities", "Lnet/optifine/shaders/config/PropertyDefaultTrueFalse;")),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/config/PropertyDefaultTrueFalse", "isFalse", "()Z")),
                inject(ByteCode.IfNotZero(entitiesEnd)),
                inject(entitiesStart));

        LabelNode blockEntitiesStart = (LabelNode) patch("添加shadowBlockEntities配置 第一部分", method,
                ByteCode.Ldc("blockentities"),
                ByteCode.InvokeVirtual("rl", "c", "(Ljava/lang/String;)V"),
                collect(ByteCode.Label()))[0];

        LabelNode blockEntitiesEnd = (LabelNode) patch("添加shadowBlockEntities配置 第二部分", method,
                collect(ByteCode.Label()),
                ByteCode.LineNumber(),
                ByteCode.ILoad(21),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "endBlockEntities", "()V"))[0];

        patch("添加shadowBlockEntities配置 第三部分", method,
                ByteCode.Ldc("blockentities"),
                ByteCode.InvokeVirtual("rl", "c", "(Ljava/lang/String;)V"),
                inject(ByteCode.ILoad(21)),
                inject(ByteCode.IfZero(blockEntitiesStart)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "isShadowPass", "Z")),
                inject(ByteCode.IfZero(blockEntitiesStart)),
                inject(ByteCode.GetStatic(HOOK, "shaderPackShadowBlockEntities", "Lnet/optifine/shaders/config/PropertyDefaultTrueFalse;")),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/config/PropertyDefaultTrueFalse", "isFalse", "()Z")),
                inject(ByteCode.IfNotZero(blockEntitiesEnd)));

        LabelNode lightningSkip = ByteCode.Label();

        patch("高版本机制 - 使用实体着色器渲染闪电 第一部分", method,
                ByteCode.GetStatic("net/optifine/reflect/Reflector", "ForgeTileEntity_shouldRenderInPass", "Lnet/optifine/reflect/ReflectorMethod;"),
                ByteCode.InvokeVirtual("net/optifine/reflect/ReflectorMethod", "exists", "()Z"),
                ByteCode.IStore(20),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "beginLightningShader", "()Z")),
                inject(ByteCode.IfNotZero(lightningSkip)));

        patch("高版本机制 - 使用实体着色器渲染闪电 第二部分", method,
                ByteCode.ALoad(22),
                ByteCode.DLoad(5),
                ByteCode.DLoad(7),
                ByteCode.DLoad(9),
                ByteCode.InvokeVirtual("vg", "g", "(DDD)Z"),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                inject(ByteCode.ALoad(22)),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "nextLightningEntity", "(Lvg;)V")),
                ByteCode.ALoad(0),
                ByteCode.GetField("buy", "j", "Lbzf;"),
                ByteCode.ALoad(22),
                ByteCode.FLoad(3),
                ByteCode.IConst(0),
                ByteCode.InvokeVirtual("bzf", "a", "(Lvg;FZ)V"));

        patch("高版本机制 - 使用实体着色器渲染闪电 第三部分", method,
                inject(lightningSkip),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "endLightningShader", "()V")),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                ByteCode.ALoad(0),
                ByteCode.GetField("buy", "k", "Lbsb;"),
                ByteCode.GetField("bsb", "E", "Lrl;"),
                ByteCode.Ldc("entities"),
                ByteCode.InvokeVirtual("rl", "c", "(Ljava/lang/String;)V"));
    }

    @MethodPatch("a(FI)V")
    public void renderSky(MethodNode method) {
        patch("增加SUNSET渲染阶段配置", method,
                ByteCode.IfNull(null),
                ByteCode.InvokeStatic("Config", "isSunMoonEnabled", "()Z"),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.InvokeStatic("bus", "z", "()V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ILoad(3),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "disableTexture2D", "()V"),
                inject(ByteCode.GetStatic(RENDERSTAGE, "SUNSET", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));

        patch("增加SUN渲染阶段配置", method,
                ByteCode.InvokeStatic("Config", "isSunTexture", "()Z"),
                ByteCode.IfZero(null),
                inject(ByteCode.GetStatic(RENDERSTAGE, "SUN", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));

        patch("增加MOON渲染阶段配置", method,
                ByteCode.InvokeStatic("Config", "isMoonTexture", "()Z"),
                ByteCode.IfZero(null),
                inject(ByteCode.GetStatic(RENDERSTAGE, "MOON", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));

        patch("增加STARS渲染阶段配置", method,
                ByteCode.InvokeStatic("Config", "isStarsEnabled", "()Z"),
                ByteCode.IfZero(null),
                ByteCode.ALoad(0),
                ByteCode.GetField("buy", "k", "Lbsb;"),
                ByteCode.InvokeStatic("net/optifine/CustomSky", "hasSkyLayers", "(Lamu;)Z"),
                ByteCode.IfNotZero(null),
                inject(ByteCode.GetStatic(RENDERSTAGE, "STARS", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));

        patch("增加VOID渲染阶段配置", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(0),
                ByteCode.GetField("buy", "h", "Lbib;"),
                ByteCode.GetField("bib", "h", "Lbud;"),
                ByteCode.FLoad(1),
                ByteCode.InvokeVirtual("bud", "f", "(F)Lbhe;"),
                ByteCode.GetField("bhe", "c", "D"),
                ByteCode.ALoad(0),
                ByteCode.GetField("buy", "k", "Lbsb;"),
                ByteCode.InvokeVirtual("bsb", "ad", "()D"),
                ByteCode.DSub(),
                ByteCode.DStore(14),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.DLoad(14),
                ByteCode.DConst(0),
                ByteCode.DCmpG(),
                ByteCode.IfGreaterEqualZero(null),
                inject(ByteCode.GetStatic(RENDERSTAGE, "VOID", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));
    }

    @MethodPatch("a(Lvg;F)V")
    public void renderWorldBorder(MethodNode method) {
        patch("增加WORLD_BORDER渲染阶段配置", method,
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "useProgram", "(Lnet/optifine/shaders/Program;)V"),
                inject(ByteCode.GetStatic(RENDERSTAGE, "WORLD_BORDER", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));

        patch("重置渲染阶段配置", method,
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "popProgram", "()V"),
                inject(ByteCode.GetStatic(RENDERSTAGE, "NONE", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));
    }
    
    @MethodPatch("a(Lbuk;DDDDDDFFFF)V")
    public void drawBoundingBox(MethodNode method) {
        patch("描边框半透明修复1", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(0),
                ByteCode.DLoad(1),
                ByteCode.DLoad(9),
                ByteCode.DLoad(5),
                ByteCode.InvokeVirtual("buk", "b", "(DDD)Lbuk;"),
                ByteCode.FLoad(13),
                ByteCode.FLoad(14),
                ByteCode.FLoad(15),
                remove(ByteCode.FConst(0)),
                inject(ByteCode.FLoad(16)));

        patch("描边框半透明修复2", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(0),
                ByteCode.DLoad(7),
                ByteCode.DLoad(3),
                ByteCode.DLoad(11),
                ByteCode.InvokeVirtual("buk", "b", "(DDD)Lbuk;"),
                ByteCode.FLoad(13),
                ByteCode.FLoad(14),
                ByteCode.FLoad(15),
                remove(ByteCode.FConst(0)),
                inject(ByteCode.FLoad(16)));

        patch("描边框半透明修复3", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(0),
                ByteCode.DLoad(7),
                ByteCode.DLoad(9),
                ByteCode.DLoad(5),
                ByteCode.InvokeVirtual("buk", "b", "(DDD)Lbuk;"),
                ByteCode.FLoad(13),
                ByteCode.FLoad(14),
                ByteCode.FLoad(15),
                remove(ByteCode.FConst(0)),
                inject(ByteCode.FLoad(16)));

        patch("描边框防止拉丝", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.DLoad(7)),
                inject(ByteCode.DLoad(3)),
                inject(ByteCode.DLoad(5)),
                inject(ByteCode.InvokeVirtual("buk", "b", "(DDD)Lbuk;")),
                inject(ByteCode.FLoad(13)),
                inject(ByteCode.FLoad(14)),
                inject(ByteCode.FLoad(15)),
                inject(ByteCode.FConst(0)),
                inject(ByteCode.InvokeVirtual("buk", "a", "(FFFF)Lbuk;")),
                inject(ByteCode.InvokeVirtual("buk", "d", "()V")),
                ByteCode.Return());
    }
}

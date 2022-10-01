package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.EntityAliases")
public class EntityAliasesPatcher extends Patcher {

    @MethodPatch("loadEntityAliases(Ljava/io/InputStream;Ljava/lang/String;Ljava/util/List;)V")
    public void loadEntityAliases(MethodNode method) {
        patch("高版本实体ID重映射", method,
                ByteCode.ALoad(4),
                ByteCode.ALoad(8),
                inject(ByteCode.ILoad(11)),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "remapEntityID", "(Ljava/lang/String;I)Ljava/lang/String;")),
                ByteCode.InvokeVirtual("net/optifine/config/ConnectedParser", "parseEntities", "(Ljava/lang/String;)[I"));
    }

    @MethodPatch("reset()V")
    public void reset(MethodNode method) {
        patch("重置闪电实体ID", method,
                inject(ByteCode.IConst(-1)),
                inject(ByteCode.PutStatic(FORWARDFEATURES, "lightningID", "I")),
                method.instructions.getFirst());
    }
}

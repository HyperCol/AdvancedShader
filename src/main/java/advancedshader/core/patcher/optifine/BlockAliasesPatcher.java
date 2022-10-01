package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.BlockAliases")
public class BlockAliasesPatcher extends Patcher {

    @MethodPatch("loadBlockAliases(Ljava/io/InputStream;Ljava/lang/String;Ljava/util/List;)V")
    public void loadBlockAliases(MethodNode method) {
        patch("高版本方块ID重映射 - layer", method,
                ByteCode.GetStatic("net/optifine/shaders/BlockAliases", "blockLayerPropertes", "Lnet/optifine/util/PropertiesOrdered;"),
                ByteCode.ALoad(7),
                ByteCode.ALoad(8),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "remapItemID", "(Ljava/lang/String;)Ljava/lang/String;")),
                ByteCode.InvokeVirtual("net/optifine/util/PropertiesOrdered", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"));

        patch("高版本方块ID重映射 - block", method,
                ByteCode.ALoad(4),
                ByteCode.ALoad(8),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "remapBlockID", "(Ljava/lang/String;)Ljava/lang/String;")),
                ByteCode.InvokeVirtual("net/optifine/config/ConnectedParser", "parseMatchBlocks", "(Ljava/lang/String;)[Lnet/optifine/config/MatchBlock;"));
    }
}

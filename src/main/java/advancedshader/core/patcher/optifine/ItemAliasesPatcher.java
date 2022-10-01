package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.ItemAliases")
public class ItemAliasesPatcher extends Patcher {

    @MethodPatch("loadItemAliases(Ljava/io/InputStream;Ljava/lang/String;Ljava/util/List;)V")
    public void loadItemAliases(MethodNode method) {
        patch("高版本物品ID重映射", method,
                ByteCode.ALoad(4),
                ByteCode.ALoad(8),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "remapItemID", "(Ljava/lang/String;)Ljava/lang/String;")),
                ByteCode.InvokeVirtual("net/optifine/config/ConnectedParser", "parseItems", "(Ljava/lang/String;)[I"));
    }

    @MethodPatch("getItemAliasId(I)I")
    public void getItemAliasId(MethodNode method) {
        patch("高版本物品ID返回-1", method,
                ByteCode.ILoad(0),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "isNegative", "(I)I")),
                ByteCode.IReturn());

        patch("高版本物品ID返回-1", method,
                ByteCode.ILoad(0),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "isNegative", "(I)I")),
                ByteCode.IReturn());

        patch("高版本物品ID返回-1", method,
                ByteCode.ILoad(0),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "isNegative", "(I)I")),
                ByteCode.IReturn());
    }
}

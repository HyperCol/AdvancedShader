package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.config.ConnectedParser")
public class ConnectedParserPatcher extends Patcher {

    @MethodPatch("parseMatchBlock(Ljava/lang/String;)[Lnet/optifine/config/MatchBlock;")
    public void parseMatchBlock(MethodNode method) {
        patch("修复block.properties无法正确匹配方块问题", method,
                remove(ByteCode.New("net/optifine/config/MatchBlock")),
                inject(ByteCode.New(MATCHBLOCKSTATE)),
                ByteCode.Dup(),
                ByteCode.ILoad(11),
                ByteCode.ALoad(12),
                inject(ByteCode.ALoad(6)),
                remove(ByteCode.InvokeSpecial("net/optifine/config/MatchBlock", "<init>", "(I[I)V")),
                inject(ByteCode.InvokeSpecial(MATCHBLOCKSTATE, "<init>", "(I[I[Ljava/lang/String;)V")));
    }

    @MethodPatch("parseBlockMetadatas(Laow;[Ljava/lang/String;)[I")
    public void parseBlockMetadatas(MethodNode method) {
        patch("修复block.properties无法正确匹配方块问题", method,
                ByteCode.ALoad(7),
                ByteCode.InvokeInterface("java/util/List", "size", "()I"),
                remove(ByteCode.BIPush(16)),
                inject(ByteCode.IConst(-1)),
                ByteCode.IfIntNotEqual(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.AConstNull());
    }
}

package advancedshader.core.patcher.forge;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.minecraftforge.fml.client.FMLClientHandler")
public class FMLClientHandlerPatcher extends Patcher {
    @MethodPatch("getAdditionalBrandingInformation()Ljava/util/List;")
    public void getAdditionalBrandingInformation(MethodNode method) {
        patch("主界面信息标识", method, new AbstractInsnNode[] {
                remove(ByteCode.IConst(1)),
                inject(ByteCode.IConst(2)),
                ByteCode.NewArray("java/lang/String"),
                ByteCode.Dup(),
                ByteCode.IConst(0),
                ByteCode.Ldc("Optifine %s"),
                ByteCode.IConst(1),
                ByteCode.NewArray("java/lang/Object"),
                ByteCode.Dup(),
                ByteCode.IConst(0),
                ByteCode.ALoad(0),
                ByteCode.GetField("net/minecraftforge/fml/client/FMLClientHandler", "optifineContainer", "Lnet/minecraftforge/fml/common/DummyModContainer;"),
                ByteCode.InvokeVirtual("net/minecraftforge/fml/common/DummyModContainer", "getVersion", "()Ljava/lang/String;"),
                ByteCode.AAStore(),
                ByteCode.InvokeStatic("java/lang/String", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"),
                ByteCode.AAStore(),
                inject(ByteCode.Dup()),
                inject(ByteCode.IConst(1)),
                inject(ByteCode.Ldc("已启用光影前向兼容")),
                inject(ByteCode.AAStore()),
                ByteCode.InvokeStatic("java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;") });
    }
}

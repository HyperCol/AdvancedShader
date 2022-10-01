package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.config.EnumShaderOption")
public class EnumShaderOptionPatcher extends Patcher {

    @MethodPatch("<clinit>()V")
    public void clinit(MethodNode method) {
        patch("增加高版本特性选项枚举 第一部分", method,
                remove(ByteCode.BIPush(18)),
                inject(ByteCode.BIPush(21)),
                ByteCode.NewArray("net/optifine/shaders/config/EnumShaderOption"));

        patch("增加高版本特性选项枚举 第二部分", method,
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(18)),
                inject(ByteCode.New("net/optifine/shaders/config/EnumShaderOption")),
                inject(ByteCode.Dup()),
                inject(ByteCode.Ldc("RENDERER")),
                inject(ByteCode.SIPush(18)),
                inject(ByteCode.Ldc("渲染机制")),
                inject(ByteCode.Ldc("renderer")),
                inject(ByteCode.Ldc("1.12.2")),
                inject(ByteCode.InvokeSpecial("net/optifine/shaders/config/EnumShaderOption", "<init>", "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(19)),
                inject(ByteCode.New("net/optifine/shaders/config/EnumShaderOption")),
                inject(ByteCode.Dup()),
                inject(ByteCode.Ldc("BLOCK_ID")),
                inject(ByteCode.SIPush(19)),
                inject(ByteCode.Ldc("方块ID")),
                inject(ByteCode.Ldc("blockID")),
                inject(ByteCode.Ldc("1.12.2")),
                inject(ByteCode.InvokeSpecial("net/optifine/shaders/config/EnumShaderOption", "<init>", "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(20)),
                inject(ByteCode.New("net/optifine/shaders/config/EnumShaderOption")),
                inject(ByteCode.Dup()),
                inject(ByteCode.Ldc("PLAYERMOOD")),
                inject(ByteCode.SIPush(201)),
                inject(ByteCode.Ldc("氛围值机制")),
                inject(ByteCode.Ldc("playerMood")),
                inject(ByteCode.Ldc("1.12.2")),
                inject(ByteCode.InvokeSpecial("net/optifine/shaders/config/EnumShaderOption", "<init>", "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V")),
                inject(ByteCode.AAStore()),
                ByteCode.PutStatic("net/optifine/shaders/config/EnumShaderOption", "$VALUES", "[Lnet/optifine/shaders/config/EnumShaderOption;"));
    }
}

package advancedshader.core.patcher.optifine;

import org.lwjgl.opengl.GL11;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.SVertexBuilder")
public class SVertexBuilderPatcher extends Patcher {

    @MethodPatch("drawArrays(IIILbuk;)V")
    public void drawArrays(MethodNode method) {
        LabelNode singleTexture = ByteCode.Label();
        LabelNode drawEnd = ByteCode.Label();

        patch("增加调用drawMultiTexture判断", method,
                inject(ByteCode.ALoad(3)),
                inject(ByteCode.InvokeVirtual("buk", "isMultiTexture", "()Z")),
                inject(ByteCode.IfZero(singleTexture)),
                inject(ByteCode.ALoad(3)),
                inject(ByteCode.InvokeVirtual("buk", "drawMultiTexture", "()V")),
                inject(ByteCode.Goto(drawEnd)),
                inject(singleTexture),
                ByteCode.ILoad(0),
                ByteCode.ILoad(1),
                ByteCode.ILoad(2),
                ByteCode.InvokeStatic("bus", "f", "(III)V"),
                inject(drawEnd));

        // 第一部分也许是不必要的，但万一呢……？
        patch("增加gbuffers_line着色器 第一部分", method,
                inject(ByteCode.ILoad(0)),
                inject(ByteCode.ALoad(3)),
                inject(ByteCode.InvokeStatic(LINESHADER, "preDrawArray", "(ILbuk;)I")),
                inject(ByteCode.IStore(0)),
                ByteCode.ILoad(0),
                ByteCode.ILoad(1),
                ByteCode.ILoad(2),
                ByteCode.InvokeStatic("bus", "f", "(III)V"),
                inject(ByteCode.InvokeStatic(LINESHADER, "postDrawArray", "()V")));

        patch("增加gbuffers_line着色器 第二部分", method,
                inject(ByteCode.ILoad(0)),
                inject(ByteCode.ALoad(3)),
                inject(ByteCode.InvokeStatic(LINESHADER, "preDrawArray", "(ILbuk;)I")),
                inject(ByteCode.IStore(0)),
                ByteCode.ILoad(0),
                ByteCode.ILoad(1),
                ByteCode.ILoad(2),
                ByteCode.InvokeStatic("bus", "f", "(III)V"),
                inject(ByteCode.InvokeStatic(LINESHADER, "postDrawArray", "()V")),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                ByteCode.Return());

        patch("增加at_midBlock顶点属性 第一部分", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "entityAttrib", "I"),
                ByteCode.IConst(3),
                ByteCode.SIPush(GL11.GL_SHORT),
                ByteCode.IConst(0),
                ByteCode.ILoad(5),
                ByteCode.ALoad(6),
                ByteCode.InvokeStatic("org/lwjgl/opengl/GL20", "glVertexAttribPointer", "(IIIZILjava/nio/ByteBuffer;)V"),
                inject(ByteCode.GetStatic(HOOK, "midBlockAttrib", "I")),
                inject(ByteCode.IConst(3)),
                inject(ByteCode.SIPush(GL11.GL_BYTE)),
                inject(ByteCode.IConst(0)),
                inject(ByteCode.ILoad(5)),
                inject(ByteCode.ALoad(6)),
                inject(ByteCode.BIPush(52)),
                inject(ByteCode.InvokeVirtual("java/nio/ByteBuffer", "position", "(I)Ljava/nio/Buffer;")),
                inject(ByteCode.CheckCast("java/nio/ByteBuffer")),
                inject(ByteCode.InvokeStatic("org/lwjgl/opengl/GL20", "glVertexAttribPointer", "(IIIZILjava/nio/ByteBuffer;)V")));

        patch("增加at_midBlock顶点属性 第二部分", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "entityAttrib", "I"),
                ByteCode.InvokeStatic("org/lwjgl/opengl/GL20", "glEnableVertexAttribArray", "(I)V"),
                inject(ByteCode.GetStatic(HOOK, "midBlockAttrib", "I")),
                inject(ByteCode.InvokeStatic("org/lwjgl/opengl/GL20", "glEnableVertexAttribArray", "(I)V")));

        patch("增加at_midBlock顶点属性 第三部分", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "entityAttrib", "I"),
                ByteCode.InvokeStatic("org/lwjgl/opengl/GL20", "glDisableVertexAttribArray", "(I)V"),
                inject(ByteCode.GetStatic(HOOK, "midBlockAttrib", "I")),
                inject(ByteCode.InvokeStatic("org/lwjgl/opengl/GL20", "glDisableVertexAttribArray", "(I)V")));
    }

    @MethodPatch("pushEntity(Lawt;Let;Lamy;Lbuk;)V")
    public void pushEntity(MethodNode method) {
        patch("修复block.properties无法正确匹配方块问题", method,
                remove(ByteCode.ILoad(4)),
                remove(ByteCode.ILoad(5)),
                remove(ByteCode.InvokeStatic("net/optifine/shaders/BlockAliases", "getBlockAliasId", "(II)I")),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.InvokeStatic(BLOCKALIASFIX, "getBlockAliasId", "(Lawt;Let;Lamy;)I")),
                ByteCode.IStore(7),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.ILoad(7)),
                remove(ByteCode.IfLessThanZero(null)));
    }
}

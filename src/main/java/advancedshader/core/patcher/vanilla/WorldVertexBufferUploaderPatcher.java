package advancedshader.core.patcher.vanilla;

import org.lwjgl.opengl.GL11;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("bul")
public class WorldVertexBufferUploaderPatcher extends Patcher {

    @MethodPatch("a(Lbuk;)V")
    public void draw(MethodNode method) {
        LabelNode succ = ByteCode.Label();

        patch("增加线段类型渲染判断", method,
                ByteCode.ALoad(1),
                ByteCode.InvokeVirtual("buk", "i", "()I"),
                ByteCode.BIPush(GL11.GL_QUADS),
                inject(ByteCode.IfIntEqual(succ)),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeVirtual("buk", "i", "()I")),
                inject(ByteCode.InvokeStatic(LINESHADER, "shouldConvertQuads", "(I)Z")),
                inject(ByteCode.IConst(1)),
                ByteCode.IfIntNotEqual(null),
                inject(succ));

        JumpInsnNode jmp = ByteCode.IfNotZero(null);

        jmp.label = ((JumpInsnNode) patch("drawMultiTexture增加光影判断", method,
                inject(ByteCode.InvokeStatic("Config", "isShaders", "()Z")),
                inject(jmp),
                ByteCode.ALoad(8),
                ByteCode.InvokeVirtual("buk", "isMultiTexture", "()Z"),
                collect(ByteCode.IfZero(null)))[0]).label;
    }
}

package advancedshader.core.patcher.vanilla;

import org.lwjgl.opengl.GL11;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("buk")
public class BufferBuilderPatcher extends Patcher {

    @MethodPatch("quadsToTriangles()V")
    public void quadsToTriangles(MethodNode method) {
        JumpInsnNode jmp = ByteCode.IfIntEqual(null);

        jmp.label = (LabelNode) patch("增加线段类型渲染判断", method,
                ByteCode.ALoad(0),
                ByteCode.GetField("buk", "j", "I"),
                ByteCode.BIPush(GL11.GL_QUADS),
                ByteCode.IfIntEqual(null),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetField("buk", "j", "I")),
                inject(ByteCode.IConst(GL11.GL_LINES)),
                inject(jmp),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Return(),
                collect(ByteCode.Label()))[0];
    }

    @MethodPatch("i()I")
    public void getDrawMode(MethodNode method) {
        JumpInsnNode jmp = ByteCode.IfIntNotEqual(null);

        jmp.label = (LabelNode) patch("增加线段类型渲染判断", method,
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetField("buk", "j", "I")),
                inject(ByteCode.BIPush(GL11.GL_QUADS)),
                inject(jmp),
                ByteCode.ALoad(0),
                ByteCode.GetField("buk", "modeTriangles", "Z"),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.IConst(4),
                ByteCode.IReturn(),
                collect(ByteCode.Label()))[0];
    }

    @MethodPatch("d()V")
    public void endVertex(MethodNode method) {
        patch("线段增加额外顶点", method,
                ByteCode.InvokeStatic("net/optifine/shaders/SVertexBuilder", "endAddVertex", "(Lbuk;)V"),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic(LINESHADER, "endAddVertex", "(Lbuk;)V")));
    }

    @MethodPatch("drawForIcon(Lcdq;I)I")
    public void drawForIcon(MethodNode method) {
        patch("为MultiTexture绑定光影材质以及设定spriteBounds", method,
                remove(ByteCode.SIPush(3553)),
                ByteCode.ALoad(1),
                ByteCode.GetField("cdq", "glSpriteTextureId", "I"),
                remove(ByteCode.InvokeStatic("org/lwjgl/opengl/GL11", "glBindTexture", "(II)V")),
                inject(ByteCode.InvokeStatic("bus", "i", "(I)V")),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeStatic(ANISOTROPICFILTER, "bindShaderMultiTexture", "(Lcdq;)V")));
    }

    @MethodPatch("draw(II)V")
    public void draw(MethodNode method) {
        LabelNode ifend1 = ByteCode.Label();
        LabelNode quad1 = ByteCode.Label();
        LabelNode ifend2 = ByteCode.Label();
        LabelNode quad2 = ByteCode.Label();
        LabelNode ifend3 = ByteCode.Label();
        LabelNode quad3 = ByteCode.Label();

        patch("增加drawMultiTexture三角形渲染判断", method,
                ByteCode.ILoad(1),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetField("buk", "modeTriangles", "Z")),
                inject(ByteCode.IfZero(quad1)),
                inject(ByteCode.BIPush(6)),
                inject(ByteCode.Goto(ifend1)),
                inject(quad1),
                ByteCode.IConst(4),
                inject(ifend1),
                ByteCode.IMul(),
                ByteCode.IStore(4),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ILoad(3),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetField("buk", "modeTriangles", "Z")),
                inject(ByteCode.IfZero(quad2)),
                inject(ByteCode.BIPush(6)),
                inject(ByteCode.Goto(ifend2)),
                inject(quad2),
                ByteCode.IConst(4),
                inject(ifend2),
                ByteCode.IMul(),
                ByteCode.IStore(5),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetField("buk", "modeTriangles", "Z")),
                inject(ByteCode.IfZero(quad3)),
                inject(ByteCode.IConst(GL11.GL_TRIANGLES)),
                inject(ByteCode.Goto(ifend3)),
                inject(quad3),
                ByteCode.ALoad(0),
                ByteCode.GetField("buk", "j", "I"),
                inject(ifend3),
                ByteCode.ILoad(4),
                ByteCode.ILoad(5),
                ByteCode.InvokeStatic("org/lwjgl/opengl/GL11", "glDrawArrays", "(III)V"));
    }

    @MethodPatch("a(DDD)V")
    public void putPosition(MethodNode method) {
        patch("为方块设定at_midBlock顶点属性", method,
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic(VERTEXATTRIBUTE, "setMidBlock", "(Lbuk;)V")),
                method.instructions.getFirst());
    }
}

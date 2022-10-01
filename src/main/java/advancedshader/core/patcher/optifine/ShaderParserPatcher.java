package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.config.ShaderParser")
public class ShaderParserPatcher extends Patcher {

    @MethodPatch("<clinit>()V")
    public void clinit(MethodNode method) {
        patch("修改DRAWBUFFERS正则表达式所接受的数值范围", method,
                remove(ByteCode.Ldc("[0-7N]*")),
                inject(ByteCode.Ldc("[0-9N]*")));

        patch("允许RENDERTARGETS值包含逗号", method,
                remove(ByteCode.Ldc("\\s*(/\\*|//)?\\s*([A-Z]+):\\s*(\\w+)\\s*(\\*/.*|\\s*)")),
                inject(ByteCode.Ldc("\\s*(/\\*|//)?\\s*([A-Z]+):\\s*([\\w.,]+)\\s*(\\*/.*|\\s*)")));

        patch("增加Prepare和ShadowComp着色器识别", method,
                remove(ByteCode.Ldc(".*deferred[0-9]*\\.fsh")),
                inject(ByteCode.Ldc(".*(?:deferred|prepare|shadowcomp)[0-9]*\\.fsh")));

        patch("识别in顶点属性", method,
                remove(ByteCode.Ldc("\\s*attribute\\s+\\w+\\s+(\\w+).*")),
                inject(ByteCode.Ldc("\\s*(?:in|attribute)\\s+\\w+\\s+(\\w+).*")));

        patch("识别带有layout的uniform", method,
                remove(ByteCode.Ldc("\\s*uniform\\s+\\w+\\s+(\\w+).*")),
                inject(ByteCode.Ldc("[\\w\\s(,=)]*uniform\\s+\\w+\\s+(\\w+).*")));
    }

    @MethodPatch("getColorIndex(Ljava/lang/String;)I")
    public void getColorIndex(MethodNode method) {
        patch("修改最大colortex(7 -> 15)", method,
                remove(ByteCode.BIPush(7)),
                inject(ByteCode.BIPush(15)));
    }

    @MethodPatch("getIndex(Ljava/lang/String;Ljava/lang/String;II)I")
    public void getIndex(MethodNode method) {
        patch("适配更大下标 第一部分", method,
                remove(ByteCode.ALoad(0)),
                remove(ByteCode.InvokeVirtual("java/lang/String", "length", "()I")),
                remove(ByteCode.ALoad(1)),
                remove(ByteCode.InvokeVirtual("java/lang/String", "length", "()I")),
                remove(ByteCode.IConst(1)),
                remove(ByteCode.IAdd()),
                remove(ByteCode.IfIntEqual(null)),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.IConst(-1)),
                remove(ByteCode.IReturn()));

        patch("适配更大下标 第二部分", method,
                ByteCode.ALoad(0),
                ByteCode.ALoad(1),
                ByteCode.InvokeVirtual("java/lang/String", "length", "()I"),
                remove(ByteCode.InvokeVirtual("java/lang/String", "charAt", "(I)C")),
                remove(ByteCode.BIPush(48)),
                remove(ByteCode.ISub()),
                inject(ByteCode.InvokeVirtual("java/lang/String", "substring", "(I)Ljava/lang/String;")),
                inject(ByteCode.IConst(-1)),
                inject(ByteCode.InvokeStatic("Config", "parseInt", "(Ljava/lang/String;I)I")));
    }
}

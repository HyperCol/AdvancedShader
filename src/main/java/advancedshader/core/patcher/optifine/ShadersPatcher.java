package advancedshader.core.patcher.optifine;

import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.Shaders")
public class ShadersPatcher extends Patcher {

    @MethodPatch("<clinit>()V")
    public void clinit(MethodNode method) {
        patch("添加更多Uniforms", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderUniforms", "Lnet/optifine/shaders/uniform/ShaderUniforms;"),
                ByteCode.Ldc("instanceId"),
                ByteCode.InvokeVirtual("net/optifine/shaders/uniform/ShaderUniforms", "make1i", "(Ljava/lang/String;)Lnet/optifine/shaders/uniform/ShaderUniform1i;"),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "uniform_instanceId", "Lnet/optifine/shaders/uniform/ShaderUniform1i;"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderUniforms", "Lnet/optifine/shaders/uniform/ShaderUniforms;")),
                inject(ByteCode.InvokeStatic(HOOK, "addUniforms", "(Lnet/optifine/shaders/uniform/ShaderUniforms;)V")));

        patch("添加更多Programs", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "programs", "Lnet/optifine/shaders/Programs;"),
                ByteCode.Ldc("final"),
                ByteCode.InvokeVirtual("net/optifine/shaders/Programs", "makeComposite", "(Ljava/lang/String;)Lnet/optifine/shaders/Program;"),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "ProgramFinal", "Lnet/optifine/shaders/Program;"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "programs", "Lnet/optifine/shaders/Programs;")),
                inject(ByteCode.InvokeStatic(HOOK, "addPrograms", "(Lnet/optifine/shaders/Programs;)V")));

        patch("增加colortex8-15纹理配置", method,
                remove(ByteCode.BIPush(8)),
                inject(ByteCode.BIPush(16)),
                ByteCode.NewArray(ByteCode.T_INT),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "gbuffersFormat", "[I"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.BIPush(8)),
                inject(ByteCode.BIPush(16)),
                ByteCode.NewArray(ByteCode.T_BOOLEAN),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "gbuffersClear", "[Z"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.BIPush(8)),
                inject(ByteCode.BIPush(16)),
                ByteCode.NewArray("org/lwjgl/util/vector/Vector4f"),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "gbuffersClearColor", "[Lorg/lwjgl/util/vector/Vector4f;"));

        patch("为colortex8-15分配纹理单元", method,
                remove(ByteCode.BIPush(8)),
                inject(ByteCode.BIPush(16)),
                ByteCode.NewArray(ByteCode.T_INT),
                ByteCode.Dup(),
                ByteCode.IConst(0),
                ByteCode.IConst(0),
                ByteCode.IAStore(),
                ByteCode.Dup(),
                ByteCode.IConst(1),
                ByteCode.IConst(1),
                ByteCode.IAStore(),
                ByteCode.Dup(),
                ByteCode.IConst(2),
                ByteCode.IConst(2),
                ByteCode.IAStore(),
                ByteCode.Dup(),
                ByteCode.IConst(3),
                ByteCode.IConst(3),
                ByteCode.IAStore(),
                ByteCode.Dup(),
                ByteCode.IConst(4),
                ByteCode.BIPush(7),
                ByteCode.IAStore(),
                ByteCode.Dup(),
                ByteCode.IConst(5),
                ByteCode.BIPush(8),
                ByteCode.IAStore(),
                ByteCode.Dup(),
                ByteCode.BIPush(6),
                ByteCode.BIPush(9),
                ByteCode.IAStore(),
                ByteCode.Dup(),
                ByteCode.BIPush(7),
                ByteCode.BIPush(10),
                ByteCode.IAStore(),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(8)),
                inject(ByteCode.BIPush(16)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(9)),
                inject(ByteCode.BIPush(17)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(10)),
                inject(ByteCode.BIPush(18)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(11)),
                inject(ByteCode.BIPush(19)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(12)),
                inject(ByteCode.BIPush(20)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(13)),
                inject(ByteCode.BIPush(21)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(14)),
                inject(ByteCode.BIPush(22)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(15)),
                inject(ByteCode.BIPush(23)),
                inject(ByteCode.IAStore()),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "colorTextureImageUnit", "[I"));

        patch("连续地址缓冲区扩容", method,
                remove(ByteCode.SIPush(285)),
                inject(ByteCode.SIPush(285 + 8 * 2)), // 8个dfb纹理ping-pong
                ByteCode.BIPush(8),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "ProgramCount", "I"),
                ByteCode.IMul(),
                ByteCode.IAdd(),
                ByteCode.IConst(4),
                ByteCode.IMul(),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "bigBufferSize", "I"));

        patch("增加帧缓冲纹理数量至32", method,
                remove(ByteCode.BIPush(16)),
                inject(ByteCode.BIPush(32)),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "nextIntBuffer", "(I)Ljava/nio/IntBuffer;"),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "dfbColorTextures", "Ljava/nio/IntBuffer;"));

        patch("增加Ping-Pong帧缓冲数量至16", method,
                ByteCode.New("net/optifine/shaders/FlipTextures"),
                ByteCode.Dup(),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTextures", "Ljava/nio/IntBuffer;"),
                remove(ByteCode.BIPush(8)),
                inject(ByteCode.BIPush(16)),
                ByteCode.InvokeSpecial("net/optifine/shaders/FlipTextures", "<init>", "(Ljava/nio/IntBuffer;I)V"));

        patch("100个Deferred着色器", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "programs", "Lnet/optifine/shaders/Programs;"),
                ByteCode.Ldc("deferred"),
                remove(ByteCode.BIPush(16)),
                inject(ByteCode.BIPush(100)),
                ByteCode.InvokeVirtual("net/optifine/shaders/Programs", "makeDeferreds", "(Ljava/lang/String;I)[Lnet/optifine/shaders/Program;"));

        patch("100个Composite着色器", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "programs", "Lnet/optifine/shaders/Programs;"),
                ByteCode.Ldc("composite"),
                remove(ByteCode.BIPush(16)),
                inject(ByteCode.BIPush(100)),
                ByteCode.InvokeVirtual("net/optifine/shaders/Programs", "makeComposites", "(Ljava/lang/String;I)[Lnet/optifine/shaders/Program;"));

        patch("增加 8bits 与 16bits 整数型纹理格式 第一部分", method,
                remove(ByteCode.BIPush(37)),
                inject(ByteCode.BIPush(53)),
                ByteCode.NewArray("java/lang/String"),
                ByteCode.Dup(),
                ByteCode.IConst(0),
                ByteCode.Ldc("R8"));

        patch("增加 8bits 与 16bits 整数型纹理格式 第二部分", method,
                ByteCode.Ldc("RGB9_E5"),
                ByteCode.AAStore(),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(37)),
                inject(ByteCode.Ldc("R8I")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(38)),
                inject(ByteCode.Ldc("RG8I")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(39)),
                inject(ByteCode.Ldc("RGB8I")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(40)),
                inject(ByteCode.Ldc("RGBA8I")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(41)),
                inject(ByteCode.Ldc("R8UI")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(42)),
                inject(ByteCode.Ldc("RG8UI")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(43)),
                inject(ByteCode.Ldc("RGB8UI")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(44)),
                inject(ByteCode.Ldc("RGBA8UI")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(45)),
                inject(ByteCode.Ldc("R16I")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(46)),
                inject(ByteCode.Ldc("RG16I")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(47)),
                inject(ByteCode.Ldc("RGB16I")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(48)),
                inject(ByteCode.Ldc("RGBA16I")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(49)),
                inject(ByteCode.Ldc("R16UI")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(50)),
                inject(ByteCode.Ldc("RG16UI")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(51)),
                inject(ByteCode.Ldc("RGB16UI")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(52)),
                inject(ByteCode.Ldc("RGBA16UI")),
                inject(ByteCode.AAStore()),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "formatNames", "[Ljava/lang/String;"));

        patch("增加 8bits 与 16bits 整数型纹理格式 第三部分", method,
                remove(ByteCode.BIPush(37)),
                inject(ByteCode.BIPush(53)),
                ByteCode.NewArray(ByteCode.T_INT),
                ByteCode.Dup(),
                ByteCode.IConst(0),
                ByteCode.Ldc(GL30.GL_R8));

        patch("增加 8bits 与 16bits 整数型纹理格式 第四部分", method,
                ByteCode.Ldc(GL30.GL_RGB9_E5),
                ByteCode.IAStore(),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(37)),
                inject(ByteCode.Ldc(GL30.GL_R8I)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(38)),
                inject(ByteCode.Ldc(GL30.GL_RG8I)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(39)),
                inject(ByteCode.Ldc(GL30.GL_RGB8I)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(40)),
                inject(ByteCode.Ldc(GL30.GL_RGBA8I)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(41)),
                inject(ByteCode.Ldc(GL30.GL_R8UI)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(42)),
                inject(ByteCode.Ldc(GL30.GL_RG8UI)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(43)),
                inject(ByteCode.Ldc(GL30.GL_RGB8UI)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(44)),
                inject(ByteCode.Ldc(GL30.GL_RGBA8UI)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(45)),
                inject(ByteCode.Ldc(GL30.GL_R16I)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(46)),
                inject(ByteCode.Ldc(GL30.GL_RG16I)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(47)),
                inject(ByteCode.Ldc(GL30.GL_RGB16I)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(48)),
                inject(ByteCode.Ldc(GL30.GL_RGBA16I)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(49)),
                inject(ByteCode.Ldc(GL30.GL_R16UI)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(50)),
                inject(ByteCode.Ldc(GL30.GL_RG16UI)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(51)),
                inject(ByteCode.Ldc(GL30.GL_RGB16UI)),
                inject(ByteCode.IAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.BIPush(52)),
                inject(ByteCode.Ldc(GL30.GL_RGBA16UI)),
                inject(ByteCode.IAStore()),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "formatIds", "[I"));

        patch("增加Prepare和ShadowComp纹理Stages", method,
                remove(ByteCode.IConst(3)),
                inject(ByteCode.IConst(5)),
                ByteCode.NewArray("java/lang/String"),
                ByteCode.Dup(),
                ByteCode.IConst(0),
                ByteCode.Ldc("gbuffers"),
                ByteCode.AAStore(),
                ByteCode.Dup(),
                ByteCode.IConst(1),
                ByteCode.Ldc("composite"),
                ByteCode.AAStore(),
                ByteCode.Dup(),
                ByteCode.IConst(2),
                ByteCode.Ldc("deferred"),
                ByteCode.AAStore(),
                inject(ByteCode.Dup()),
                inject(ByteCode.IConst(3)),
                inject(ByteCode.Ldc("prepare")),
                inject(ByteCode.AAStore()),
                inject(ByteCode.Dup()),
                inject(ByteCode.IConst(4)),
                inject(ByteCode.Ldc("shadowcomp")),
                inject(ByteCode.AAStore()),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "STAGE_NAMES", "[Ljava/lang/String;"));

        patch("为阴影渲染创建ping-pong缓冲区", method,
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;"),
                inject(ByteCode.New(SHADOWFILPTEXTURES)),
                inject(ByteCode.Dup()),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "sfbColorTextures", "Ljava/nio/IntBuffer;")),
                inject(ByteCode.InvokeSpecial(SHADOWFILPTEXTURES, "<init>", "(Ljava/nio/IntBuffer;)V")),
                inject(ByteCode.PutStatic(HOOK, "sfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")));

        patch("复制变量 colorTextureImageUnit", method,
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "colorTextureImageUnit", "[I"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "colorTextureImageUnit", "[I")),
                inject(ByteCode.PutStatic(HOOK, "colorTextureImageUnit", "[I")));

        patch("复制变量 dfbColorTexturesFlip", method,
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")),
                inject(ByteCode.PutStatic(HOOK, "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")));

        patch("复制变量 sfbDrawBuffers", method,
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "sfbDrawBuffers", "Ljava/nio/IntBuffer;"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "sfbDrawBuffers", "Ljava/nio/IntBuffer;")),
                inject(ByteCode.PutStatic(HOOK, "sfbDrawBuffers", "Ljava/nio/IntBuffer;")));

        patch("复制变量 gbuffersFormat", method,
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "gbuffersFormat", "[I"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "gbuffersFormat", "[I")),
                inject(ByteCode.PutStatic(HOOK, "gbuffersFormat", "[I")));

        patch("复制变量 modelView", method,
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "modelView", "Ljava/nio/FloatBuffer;"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "modelView", "Ljava/nio/FloatBuffer;")),
                inject(ByteCode.PutStatic(HOOK, "modelView", "Ljava/nio/FloatBuffer;")));

        patch("复制变量 modelViewInverse", method,
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "modelViewInverse", "Ljava/nio/FloatBuffer;"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "modelViewInverse", "Ljava/nio/FloatBuffer;")),
                inject(ByteCode.PutStatic(HOOK, "modelViewInverse", "Ljava/nio/FloatBuffer;")));

        patch("复制变量 shadowModelView", method,
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "shadowModelView", "Ljava/nio/FloatBuffer;"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shadowModelView", "Ljava/nio/FloatBuffer;")),
                inject(ByteCode.PutStatic(HOOK, "shadowModelView", "Ljava/nio/FloatBuffer;")));

        patch("复制变量 shadowModelViewInverse", method,
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "shadowModelViewInverse", "Ljava/nio/FloatBuffer;"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shadowModelViewInverse", "Ljava/nio/FloatBuffer;")),
                inject(ByteCode.PutStatic(HOOK, "shadowModelViewInverse", "Ljava/nio/FloatBuffer;")));

        patch("复制变量 tempMatrixDirectBuffer", method,
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "tempMatrixDirectBuffer", "Ljava/nio/FloatBuffer;"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "tempMatrixDirectBuffer", "Ljava/nio/FloatBuffer;")),
                inject(ByteCode.PutStatic(HOOK, "tempMatrixDirectBuffer", "Ljava/nio/FloatBuffer;")));
    }

    @MethodPatch("initDrawBuffers(Lnet/optifine/shaders/Program;)V")
    public void initDrawBuffers(MethodNode method) {
        patch("限制usedDrawBuffers大小", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedColorBuffers", "I"),
                inject(ByteCode.BIPush(8)),
                inject(ByteCode.InvokeStatic("java/lang/Math", "min", "(II)I")),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "usedDrawBuffers", "I"));
    }

    @MethodPatch("getDrawBuffer(Lnet/optifine/shaders/Program;Ljava/lang/String;I)I")
    public void getDrawBuffer(MethodNode method) {
        patch("修改DrawBuffer计算逻辑并扩大RenderTarget可用范围", method,
                ByteCode.ILoad(4),
                remove(ByteCode.BIPush(7)),
                inject(ByteCode.BIPush(15)),
                ByteCode.IfIntGreaterThan(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(0),
                ByteCode.InvokeVirtual("net/optifine/shaders/Program", "getToggleColorTextures", "()[Z"),
                ByteCode.ILoad(4),
                ByteCode.IConst(1),
                ByteCode.BAStore(),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.ILoad(4)),
                inject(ByteCode.ILoad(2)),
                ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT0));

        patch("识别ShadowComp着色器为阴影后处理着色器", method,
                ByteCode.ALoad(0),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/Program", "getName", "()Ljava/lang/String;")),
                inject(ByteCode.Ldc("shadow")),
                inject(ByteCode.InvokeVirtual("java/lang/String", "startsWith", "(Ljava/lang/String;)Z")),
                inject(ByteCode.InvokeStatic("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;")),
                inject(ByteCode.GetStatic("java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;")),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "ProgramShadow", "Lnet/optifine/shaders/Program;")));

        patch("为ShadowComp写入缓冲区翻转配置", method,
                ByteCode.ILoad(4),
                ByteCode.IfLessThanZero(null),
                ByteCode.ILoad(4),
                ByteCode.IConst(1),
                ByteCode.IfIntGreaterThan(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/Program", "getToggleColorTextures", "()[Z")),
                inject(ByteCode.ILoad(4)),
                inject(ByteCode.IConst(1)),
                inject(ByteCode.BAStore()));

        patch("修复阴影纹理数量错误", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedShadowColorBuffers", "I"),
                ByteCode.ILoad(4),
                inject(ByteCode.IConst(1)),
                inject(ByteCode.IAdd()),
                ByteCode.InvokeStatic("java/lang/Math", "max", "(II)I"),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "usedShadowColorBuffers", "I"));

        patch("修复颜色纹理数量错误", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedColorAttachs", "I"),
                ByteCode.ILoad(4),
                inject(ByteCode.IConst(1)),
                inject(ByteCode.IAdd()),
                ByteCode.InvokeStatic("java/lang/Math", "max", "(II)I"),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "usedColorAttachs", "I"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedColorBuffers", "I"),
                ByteCode.ILoad(4),
                inject(ByteCode.IConst(1)),
                inject(ByteCode.IAdd()),
                ByteCode.InvokeStatic("java/lang/Math", "max", "(II)I"),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "usedColorBuffers", "I"));
    }

    @MethodPatch("bindGbuffersTextures()V")
    public void bindGbuffersTextures(MethodNode method) {
        patch("为Gbuffer着色器绑定colortex8-15纹理", method,
                remove(ByteCode.ILoad(0)),
                remove(ByteCode.IConst(4)),
                remove(ByteCode.IfIntGreaterEqual(null)),
                ByteCode.IConst(4),
                ByteCode.ILoad(0),
                ByteCode.IAdd(),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedColorBuffers", "I"),
                ByteCode.IfIntGreaterEqual(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.Ldc(GL13.GL_TEXTURE7)),
                inject(ByteCode.Ldc(GL13.GL_TEXTURE0)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "colorTextureImageUnit", "[I")),
                ByteCode.ILoad(0),
                inject(ByteCode.IConst(4)),
                inject(ByteCode.IAdd()),
                inject(ByteCode.IALoad()),
                ByteCode.IAdd());
    }

    @MethodPatch("useProgram(Lnet/optifine/shaders/Program;)V")
    public void useProgram(MethodNode method) {
        patch("绑定颜色缓冲", method,
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic(MOREBUFFERS, "attachColorBuffer", "(Lnet/optifine/shaders/Program;)V")),
                ByteCode.ALoad(0),
                ByteCode.InvokeVirtual("net/optifine/shaders/Program", "getDrawBuffers", "()Ljava/nio/IntBuffer;"),
                ByteCode.AStore(2));

        LabelNode nvl = ByteCode.Label();
        LabelNode call = ByteCode.Label();

        patch("设定新Uniform值", method,
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "hasDeferredPrograms", "Z")),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "customTexturesGbuffers", "[Lnet/optifine/shaders/ICustomTexture;")),
                inject(ByteCode.IfNull(nvl)),
                inject(ByteCode.IConst(1)),
                inject(ByteCode.Goto(call)),
                inject(nvl),
                inject(ByteCode.IConst(0)),
                inject(call),
                inject(ByteCode.InvokeStatic(HOOK, "updateUniform", "(Lnet/optifine/shaders/Program;ZZ)V")),
                ByteCode.GetStatic("net/optifine/shaders/Shaders$1", "$SwitchMap$net$optifine$shaders$ProgramStage", "[I"));
    }

    @MethodPatch("getBufferIndexFromString(Ljava/lang/String;)I")
    public void getBufferIndexFromString(MethodNode method) {
        LabelNode failed8 = ByteCode.Label();
        LabelNode failed9 = ByteCode.Label();
        LabelNode failed10 = ByteCode.Label();
        LabelNode failed11 = ByteCode.Label();
        LabelNode failed12 = ByteCode.Label();
        LabelNode failed13 = ByteCode.Label();
        LabelNode failed14 = ByteCode.Label();
        LabelNode failed15 = ByteCode.Label();
        patch("识别colortex8-15配置", method,
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.Ldc("colortex8")),
                inject(ByteCode.InvokeVirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z")),
                inject(ByteCode.IfZero(failed8)),
                inject(ByteCode.BIPush(8)),
                inject(ByteCode.IReturn()),
                inject(failed8),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.Ldc("colortex9")),
                inject(ByteCode.InvokeVirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z")),
                inject(ByteCode.IfZero(failed9)),
                inject(ByteCode.BIPush(9)),
                inject(ByteCode.IReturn()),
                inject(failed9),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.Ldc("colortex10")),
                inject(ByteCode.InvokeVirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z")),
                inject(ByteCode.IfZero(failed10)),
                inject(ByteCode.BIPush(10)),
                inject(ByteCode.IReturn()),
                inject(failed10),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.Ldc("colortex11")),
                inject(ByteCode.InvokeVirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z")),
                inject(ByteCode.IfZero(failed11)),
                inject(ByteCode.BIPush(11)),
                inject(ByteCode.IReturn()),
                inject(failed11),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.Ldc("colortex12")),
                inject(ByteCode.InvokeVirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z")),
                inject(ByteCode.IfZero(failed12)),
                inject(ByteCode.BIPush(12)),
                inject(ByteCode.IReturn()),
                inject(failed12),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.Ldc("colortex13")),
                inject(ByteCode.InvokeVirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z")),
                inject(ByteCode.IfZero(failed13)),
                inject(ByteCode.BIPush(13)),
                inject(ByteCode.IReturn()),
                inject(failed13),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.Ldc("colortex14")),
                inject(ByteCode.InvokeVirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z")),
                inject(ByteCode.IfZero(failed14)),
                inject(ByteCode.BIPush(14)),
                inject(ByteCode.IReturn()),
                inject(failed14),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.Ldc("colortex15")),
                inject(ByteCode.InvokeVirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z")),
                inject(ByteCode.IfZero(failed15)),
                inject(ByteCode.BIPush(15)),
                inject(ByteCode.IReturn()),
                inject(failed15),
                ByteCode.IConst(-1),
                ByteCode.IReturn());
    }

    @MethodPatch("setupFrameBuffer()V")
    public void setupFrameBuffer(MethodNode method) {
        patch("增加申请的纹理ID数量16 -> 32", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTextures", "Ljava/nio/IntBuffer;"),
                ByteCode.InvokeVirtual("java/nio/IntBuffer", "clear", "()Ljava/nio/Buffer;"),
                remove(ByteCode.BIPush(16)),
                inject(ByteCode.BIPush(32)),
                ByteCode.InvokeVirtual("java/nio/Buffer", "limit", "(I)Ljava/nio/Buffer;"),
                ByteCode.CheckCast("java/nio/IntBuffer"),
                ByteCode.InvokeStatic("org/lwjgl/opengl/GL11", "glGenTextures", "(Ljava/nio/IntBuffer;)V"));

        LabelNode label1 = ByteCode.Label();
        LabelNode label2 = ByteCode.Label();

        patch("增加颜色纹理初始化绑定判断（colortex8-16与size.buffer） 第一部分", method,
                inject(ByteCode.ILoad(0)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "shouldBindForSetup", "(I)Z")),
                inject(ByteCode.IfZero(label1)),
                ByteCode.Ldc(GL30.GL_FRAMEBUFFER),
                ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT0),
                ByteCode.ILoad(0),
                ByteCode.IAdd(),
                ByteCode.SIPush(GL11.GL_TEXTURE_2D),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;"),
                ByteCode.ILoad(0),
                ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "getA", "(I)I"),
                ByteCode.IConst(0),
                ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V"),
                inject(label1));

        patch("增加颜色纹理初始化绑定判断（colortex8-16与size.buffer） 第二部分", method,
                inject(ByteCode.ILoad(1)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "shouldBindForSetup", "(I)Z")),
                inject(ByteCode.IfZero(label2)),
                ByteCode.Ldc(GL30.GL_FRAMEBUFFER),
                ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT0),
                ByteCode.ILoad(1),
                ByteCode.IAdd(),
                ByteCode.SIPush(GL11.GL_TEXTURE_2D),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;"),
                ByteCode.ILoad(1),
                ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "getA", "(I)I"),
                ByteCode.IConst(0),
                ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V"),
                inject(label2));

        patch("根据size.buffer修改纹理大小 第一部分", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "gbuffersFormat", "[I"),
                ByteCode.ILoad(0),
                ByteCode.IALoad(),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "renderWidth", "I"),
                inject(ByteCode.ILoad(0)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "getResizedWidth", "(II)I")),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "renderHeight", "I"),
                inject(ByteCode.ILoad(0)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "getResizedHeight", "(II)I")));

        patch("根据size.buffer修改纹理大小 第二部分", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "gbuffersFormat", "[I"),
                ByteCode.ILoad(0),
                ByteCode.IALoad(),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "renderWidth", "I"),
                inject(ByteCode.ILoad(0)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "getResizedWidth", "(II)I")),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "renderHeight", "I"),
                inject(ByteCode.ILoad(0)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "getResizedHeight", "(II)I")));

        patch("根据size.buffer修改纹理大小 第三部分", method,
                ByteCode.SIPush(GL11.GL_RGBA),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "renderWidth", "I"),
                inject(ByteCode.ILoad(1)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "getResizedWidth", "(II)I")),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "renderHeight", "I"),
                inject(ByteCode.ILoad(1)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "getResizedHeight", "(II)I")));

        patch("初始化size.buffer专用帧缓冲", method,
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "dfb", "I"),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "initDynamicDimensions", "()V")));
    }

    @MethodPatch("uninit()V")
    public void uninit(MethodNode method) {
        patch("删除size.buffer专用帧缓冲", method,
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "dfb", "I"),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "uninitDynamicDimensions", "()V")));

        patch("删除计算着色器", method,
                ByteCode.ALoad(1),
                ByteCode.IConst(0),
                ByteCode.InvokeVirtual("net/optifine/shaders/Program", "setCompositeMipmapSetting", "(I)V"),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeStatic(COMPUTESHADER, "deleteComputes", "(Lnet/optifine/shaders/Program;)V")));
    }

    @MethodPatch("beginRender(Lbib;FJ)V")
    public void beginRender(MethodNode method) {
        LabelNode label1 = ByteCode.Label();

        patch("增加帧缓冲纹理绑定判断（colortex8-16与size.buffer）", method,
                ByteCode.IConst(0),
                ByteCode.IStore(5),
                ByteCode.Label(),
                ByteCode.Frame(),
                ByteCode.ILoad(5),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedColorBuffers", "I"),
                ByteCode.IfIntGreaterEqual(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                inject(ByteCode.ILoad(5)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "shouldBindForSetup", "(I)Z")),
                inject(ByteCode.IfZero(label1)),
                ByteCode.Ldc(GL30.GL_FRAMEBUFFER),
                ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT0),
                ByteCode.ILoad(5),
                ByteCode.IAdd(),
                ByteCode.SIPush(GL11.GL_TEXTURE_2D),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;"),
                ByteCode.ILoad(5),
                ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "getA", "(I)I"),
                ByteCode.IConst(0),
                ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V"),
                inject(label1),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Inc(5, 1));

        patch("重置渲染阶段配置", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.GetStatic(RENDERSTAGE, "NONE", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")),
                ByteCode.Ldc("end beginRender"),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "checkGLError", "(Ljava/lang/String;)I"));
    }

    @MethodPatch("clearRenderBuffer()V")
    public void clearRenderBuffer(MethodNode method) {
        // 防止1286
        patch("确保0号颜色附件已绑定", method,
                remove(ByteCode.Ldc(GL30.GL_FRAMEBUFFER)),
                remove(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT0)),
                remove(ByteCode.SIPush(GL11.GL_TEXTURE_2D)),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "getA", "(I)I")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V")),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.Ldc(GL30.GL_FRAMEBUFFER)),
                inject(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT0)),
                inject(ByteCode.SIPush(GL11.GL_TEXTURE_2D)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")),
                inject(ByteCode.IConst(0)),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "getA", "(I)I")),
                inject(ByteCode.IConst(0)),
                inject(ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V")));
        // 同上
        patch("确保1号颜色附件已绑定", method,
                remove(ByteCode.Ldc(GL30.GL_FRAMEBUFFER)),
                remove(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT1)),
                remove(ByteCode.SIPush(GL11.GL_TEXTURE_2D)),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")),
                remove(ByteCode.IConst(1)),
                remove(ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "getA", "(I)I")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V")),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.Ldc(GL30.GL_FRAMEBUFFER)),
                inject(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT1)),
                inject(ByteCode.SIPush(GL11.GL_TEXTURE_2D)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")),
                inject(ByteCode.IConst(1)),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "getA", "(I)I")),
                inject(ByteCode.IConst(0)),
                inject(ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V")));

        // 防止1282
        patch("glClear清理colortex2-15纹理内容时将其绑定至2号颜色缓冲区", method,
                ByteCode.Ldc(GL30.GL_FRAMEBUFFER),
                remove(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT0)),
                remove(ByteCode.ILoad(0)),
                remove(ByteCode.IAdd()),
                inject(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT2)),
                ByteCode.SIPush(GL11.GL_TEXTURE_2D),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;"),
                ByteCode.ILoad(0),
                ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "getB", "(I)I"),
                ByteCode.IConst(0),
                ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT0)),
                remove(ByteCode.ILoad(0)),
                remove(ByteCode.IAdd()),
                inject(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT2)),
                ByteCode.InvokeStatic("org/lwjgl/opengl/GL20", "glDrawBuffers", "(I)V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.SIPush(GL11.GL_COLOR_BUFFER_BIT),
                ByteCode.InvokeStatic("org/lwjgl/opengl/GL11", "glClear", "(I)V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                // 挪到括号外
                remove(ByteCode.Ldc(GL30.GL_FRAMEBUFFER)),
                remove(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT0)),
                remove(ByteCode.ILoad(0)),
                remove(ByteCode.IAdd()),
                remove(ByteCode.SIPush(GL11.GL_TEXTURE_2D)),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")),
                remove(ByteCode.ILoad(0)),
                remove(ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "getA", "(I)I")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V")),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.Ldc(GL30.GL_FRAMEBUFFER)),
                inject(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT2)),
                inject(ByteCode.SIPush(GL11.GL_TEXTURE_2D)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")),
                inject(ByteCode.ILoad(0)),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "getA", "(I)I")),
                inject(ByteCode.IConst(0)),
                inject(ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V")),
                remove(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT0)),
                remove(ByteCode.ILoad(0)),
                remove(ByteCode.IAdd()),
                inject(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT2)),
                ByteCode.InvokeStatic("org/lwjgl/opengl/GL20", "glDrawBuffers", "(I)V"));

        patch("根据size.buffer切换帧缓冲 第一部分", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "gbuffersClear", "[Z"),
                ByteCode.IConst(0),
                ByteCode.BALoad(),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                inject(ByteCode.IConst(0)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "switchFramebuffer", "(I)V")));

        patch("根据size.buffer切换帧缓冲 第二部分", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "gbuffersClear", "[Z"),
                ByteCode.IConst(1),
                ByteCode.BALoad(),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                inject(ByteCode.IConst(1)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "switchFramebuffer", "(I)V")));

        patch("根据size.buffer切换帧缓冲 第三部分", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "gbuffersClear", "[Z"),
                ByteCode.ILoad(0),
                ByteCode.BALoad(),
                ByteCode.IfNotZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Goto(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.ILoad(0)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "switchFramebuffer", "(I)V")));

        patch("根据size.buffer切换帧缓冲 第四部分", method,
                inject(ByteCode.IConst(-1)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "switchFramebuffer", "(I)V")),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbDrawBuffers", "Ljava/nio/IntBuffer;"),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "setDrawBuffers", "(Ljava/nio/IntBuffer;)V"));
    }

    @MethodPatch("renderDeferred()V")
    public void renderDeferred(MethodNode method) {
        // 防止1282
        patch("移除原有帧缓冲纹理绑定", method,
                remove(ByteCode.Ldc(GL30.GL_FRAMEBUFFER)),
                remove(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT0)),
                remove(ByteCode.ILoad(1)),
                remove(ByteCode.IAdd()),
                remove(ByteCode.SIPush(GL11.GL_TEXTURE_2D)),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")),
                remove(ByteCode.ILoad(1)),
                remove(ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "getA", "(I)I")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V")));
    }

    @MethodPatch("renderComposites([Lnet/optifine/shaders/Program;Z)V")
    public void renderComposites(MethodNode method) {
        // 防止1282
        patch("移除原有帧缓冲纹理绑定", method,
                remove(ByteCode.Ldc(GL30.GL_FRAMEBUFFER)),
                remove(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT0)),
                remove(ByteCode.ILoad(2)),
                remove(ByteCode.IAdd()),
                remove(ByteCode.SIPush(GL11.GL_TEXTURE_2D)),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")),
                remove(ByteCode.ILoad(2)),
                remove(ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "getB", "(I)I")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V")),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Inc(2, 1),
                ByteCode.Goto(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                remove(ByteCode.Ldc(GL30.GL_FRAMEBUFFER)),
                remove(ByteCode.Ldc(GL30.GL_DEPTH_ATTACHMENT)),
                remove(ByteCode.SIPush(GL11.GL_TEXTURE_2D)),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbDepthTextures", "Ljava/nio/IntBuffer;")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeVirtual("java/nio/IntBuffer", "get", "(I)I")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V")));

        patch("移除翻转后帧缓冲纹理绑定", method,
                remove(ByteCode.Ldc(GL30.GL_FRAMEBUFFER)),
                remove(ByteCode.Ldc(GL30.GL_COLOR_ATTACHMENT0)),
                remove(ByteCode.ILoad(4)),
                remove(ByteCode.IAdd()),
                remove(ByteCode.SIPush(GL11.GL_TEXTURE_2D)),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")),
                remove(ByteCode.ILoad(4)),
                remove(ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "getB", "(I)I")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeStatic("org/lwjgl/opengl/EXTFramebufferObject", "glFramebufferTexture2DEXT", "(IIIII)V")));

        patch("增加Prepare和ShadowComp自定义纹理", method,
                inject(ByteCode.ALoad(0)),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "customTexturesDeferred", "[Lnet/optifine/shaders/ICustomTexture;"),
                inject(ByteCode.InvokeStatic(MORESTAGES, "getCustomTextures", "([Lnet/optifine/shaders/Program;[Lnet/optifine/shaders/ICustomTexture;)[Lnet/optifine/shaders/ICustomTexture;")),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "bindCustomTextures", "([Lnet/optifine/shaders/ICustomTexture;)V"));

        patch("为ShadowComp生成Mipmap", method,
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "genCompositeMipmap", "()V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.InvokeStatic(MORESTAGES, "genShadowCompMipmap", "()V")),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "preDrawComposite", "()V"));

        LabelNode label1 = ByteCode.Label();
        LabelNode label2 = ByteCode.Label();

        patch("根据着色阶段选择翻转缓冲区", method,
                ByteCode.ILoad(4),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetStatic(HOOK, "programShadowComp", "[Lnet/optifine/shaders/Program;")),
                inject(ByteCode.IfObjEqual(label1)),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedColorBuffers", "I"),
                inject(ByteCode.Goto(label2)),
                inject(label1),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedShadowColorBuffers", "I")),
                inject(label2),
                ByteCode.IfIntGreaterEqual(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(3),
                ByteCode.InvokeVirtual("net/optifine/shaders/Program", "getToggleColorTextures", "()[Z"),
                ByteCode.ILoad(4),
                ByteCode.BALoad(),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic(MORESTAGES, "getFlipBuffer", "([Lnet/optifine/shaders/Program;)Lnet/optifine/shaders/FlipTextures;")),
                ByteCode.ILoad(4),
                ByteCode.InvokeVirtual("net/optifine/shaders/FlipTextures", "flip", "(I)V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Ldc(GL13.GL_TEXTURE0),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "colorTextureImageUnit", "[I")),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic(MORESTAGES, "getColorTextureImageUnit", "([Lnet/optifine/shaders/Program;)[I")),
                ByteCode.ILoad(4),
                ByteCode.IALoad(),
                ByteCode.IAdd(),
                ByteCode.InvokeStatic("bus", "g", "(I)V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "dfbColorTexturesFlip", "Lnet/optifine/shaders/FlipTextures;")),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic(MORESTAGES, "getFlipBuffer", "([Lnet/optifine/shaders/Program;)Lnet/optifine/shaders/FlipTextures;")));

        patch("执行计算着色器", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                inject(ByteCode.ALoad(3)),
                inject(ByteCode.InvokeStatic(COMPUTESHADER, "dispatchComputes", "(Lnet/optifine/shaders/Program;)V")),
                ByteCode.ALoad(3),
                ByteCode.InvokeVirtual("net/optifine/shaders/Program", "getId", "()I"),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(3),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "useProgram", "(Lnet/optifine/shaders/Program;)V"));
    }

    @MethodPatch("renderFinal()V")
    public void renderFinal(MethodNode method) {
        patch("执行计算着色器", method,
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "ProgramFinal", "Lnet/optifine/shaders/Program;")),
                inject(ByteCode.InvokeStatic(COMPUTESHADER, "dispatchComputes", "(Lnet/optifine/shaders/Program;)V")),
                method.instructions.getFirst());
    }

    @MethodPatch("createFragShader(Lnet/optifine/shaders/Program;Ljava/lang/String;)I")
    public void createFragShader(MethodNode method) {
        patch("增加RenderTargets注释匹配", method,
                ByteCode.Ldc("Invalid draw buffers: "),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                ByteCode.ALoad(10),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "toString", "()Ljava/lang/String;"),
                ByteCode.InvokeStatic("net/optifine/shaders/SMCLog", "warning", "(Ljava/lang/String;)V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                inject(ByteCode.ALoad(9)),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic(MOREBUFFERS, "checkRenderTargets", "(Lnet/optifine/shaders/config/ShaderLine;Lnet/optifine/shaders/Program;)V")));

        patch("阴影颜色纹理格式匹配", method,
                ByteCode.ALoad(11),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "getTextureFormatFromString", "(Ljava/lang/String;)I"),
                ByteCode.IStore(13),
                inject(ByteCode.ALoad(10)),
                inject(ByteCode.ILoad(13)),
                inject(ByteCode.ALoad(11)),
                inject(ByteCode.InvokeStatic(MORESTAGES, "parseShadowFormat", "(Ljava/lang/String;ILjava/lang/String;)V")));

        patch("阴影颜色纹理glClear开关匹配", method,
                ByteCode.Ldc("Clear"),
                ByteCode.InvokeStatic("net/optifine/util/StrUtils", "removeSuffix", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"),
                ByteCode.AStore(10),
                inject(ByteCode.ALoad(10)),
                inject(ByteCode.InvokeStatic(MORESTAGES, "parseShadowClear", "(Ljava/lang/String;)V")));

        patch("阴影颜色纹理glClearColor匹配", method,
                ByteCode.Ldc("ClearColor"),
                ByteCode.InvokeStatic("net/optifine/util/StrUtils", "removeSuffix", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"),
                ByteCode.AStore(10),
                inject(ByteCode.ALoad(10)),
                inject(ByteCode.ALoad(9)),
                inject(ByteCode.InvokeStatic(MORESTAGES, "parseShadowClearColor", "(Ljava/lang/String;Lnet/optifine/shaders/config/ShaderLine;)V")));

        patch("允许final着色器配置glClear", method,
                ByteCode.Ldc("Clear"),
                ByteCode.IConst(0),
                ByteCode.InvokeVirtual("net/optifine/shaders/config/ShaderLine", "isConstBoolSuffix", "(Ljava/lang/String;Z)Z"),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(1),
                ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderParser", "isComposite", "(Ljava/lang/String;)Z"),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderParser", "isFinal", "(Ljava/lang/String;)Z")),
                inject(ByteCode.IOr()));

        patch("允许final着色器配置glClearColor", method,
                ByteCode.Ldc("ClearColor"),
                ByteCode.InvokeVirtual("net/optifine/shaders/config/ShaderLine", "isConstVec4Suffix", "(Ljava/lang/String;)Z"),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(1),
                ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderParser", "isComposite", "(Ljava/lang/String;)Z"),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderParser", "isFinal", "(Ljava/lang/String;)Z")),
                inject(ByteCode.IOr()));

        patch("匹配colorimage/shadowcolorimage", method,
                ByteCode.ALoad(9),
                ByteCode.InvokeVirtual("net/optifine/shaders/config/ShaderLine", "isUniform", "()Z"),
                ByteCode.IfZero(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(9),
                ByteCode.InvokeVirtual("net/optifine/shaders/config/ShaderLine", "getName", "()Ljava/lang/String;"),
                ByteCode.AStore(10),
                inject(ByteCode.ALoad(10)),
                inject(ByteCode.InvokeStatic(COMPUTESHADER, "parseUniform", "(Ljava/lang/String;)V")));
    }

    @MethodPatch("getEnumShaderOption(Lnet/optifine/shaders/config/EnumShaderOption;)Ljava/lang/String;")
    public void getEnumShaderOption(MethodNode method) {
        LabelNode label = ByteCode.Label();

        patch("增加高版本特性选项配置读写", method,
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "getEnumShaderOption", "(Lnet/optifine/shaders/config/EnumShaderOption;)Ljava/lang/String;")),
                inject(ByteCode.Dup()),
                inject(ByteCode.IfNull(label)),
                inject(ByteCode.AReturn()),
                inject(label),
                method.instructions.getFirst());
    }

    @MethodPatch("setEnumShaderOption(Lnet/optifine/shaders/config/EnumShaderOption;Ljava/lang/String;)V")
    public void setEnumShaderOption(MethodNode method) {
        LabelNode label = ByteCode.Label();

        patch("增加高版本特性选项配置读写", method,
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "setEnumShaderOption", "(Lnet/optifine/shaders/config/EnumShaderOption;Ljava/lang/String;)Z")),
                inject(ByteCode.IfZero(label)),
                inject(ByteCode.Return()),
                inject(label),
                method.instructions.getFirst());
    }

    @MethodPatch("loadShaderPack()V")
    public void loadShaderPack(MethodNode method) {
        patch("屏蔽光影加载时对各向异性过滤的判断", method,
                remove(ByteCode.InvokeStatic("Config", "isAnisotropicFiltering", "()Z")),
                inject(ByteCode.IConst(0)),
                ByteCode.IfZero(null));
    }

    @MethodPatch("loadShaderPackProperties()V")
    public void loadShaderPackProperties(MethodNode method) {
        // 显存泄漏
        patch("重载自定义纹理", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "resetCustomTextures", "()V")));

        patch("为部分属性屏蔽光影选项宏 第一部分", method,
                remove(ByteCode.ALoad(2)),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPackOptions", "[Lnet/optifine/shaders/config/ShaderOption;")),
                remove(ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderPackParser", "parseOptionSliders", "(Ljava/util/Properties;[Lnet/optifine/shaders/config/ShaderOption;)Ljava/util/Set;")),
                remove(ByteCode.PutStatic("net/optifine/shaders/Shaders", "shaderPackOptionSliders", "Ljava/util/Set;")),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.ALoad(2)),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPackOptions", "[Lnet/optifine/shaders/config/ShaderOption;")),
                remove(ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderPackParser", "parseProfiles", "(Ljava/util/Properties;[Lnet/optifine/shaders/config/ShaderOption;)[Lnet/optifine/shaders/config/ShaderProfile;")),
                remove(ByteCode.PutStatic("net/optifine/shaders/Shaders", "shaderPackProfiles", "[Lnet/optifine/shaders/config/ShaderProfile;")),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.ALoad(2)),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPackProfiles", "[Lnet/optifine/shaders/config/ShaderProfile;")),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPackOptions", "[Lnet/optifine/shaders/config/ShaderOption;")),
                remove(ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderPackParser", "parseGuiScreens", "(Ljava/util/Properties;[Lnet/optifine/shaders/config/ShaderProfile;[Lnet/optifine/shaders/config/ShaderOption;)Ljava/util/Map;")),
                remove(ByteCode.PutStatic("net/optifine/shaders/Shaders", "shaderPackGuiScreens", "Ljava/util/Map;")));

        LabelNode label = ByteCode.Label();

        patch("为部分属性屏蔽光影选项宏 第二部分", method,
                ByteCode.ALoad(2),
                ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderPackParser", "parseBuffersFlip", "(Ljava/util/Properties;)V"),
                inject(ByteCode.New("net/optifine/util/PropertiesOrdered")),
                inject(ByteCode.Dup()),
                inject(ByteCode.InvokeSpecial("net/optifine/util/PropertiesOrdered", "<init>", "()V")),
                inject(ByteCode.Dup()),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPack", "Lnet/optifine/shaders/IShaderPack;")),
                inject(ByteCode.IConst(0)),
                inject(ByteCode.PutStatic(PROPERTYFIX, "useShaderOptions", "Z")),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeInterface("net/optifine/shaders/IShaderPack", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;")),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/config/MacroProcessor", "process", "(Ljava/io/InputStream;Ljava/lang/String;)Ljava/io/InputStream;")),
                inject(ByteCode.IConst(1)),
                inject(ByteCode.PutStatic(PROPERTYFIX, "useShaderOptions", "Z")),
                inject(ByteCode.DupX1()),
                inject(ByteCode.InvokeVirtual("java/util/Properties", "load", "(Ljava/io/InputStream;)V")),
                inject(ByteCode.InvokeVirtual("java/io/InputStream", "close", "()V")),
                inject(ByteCode.AStore(2)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPackOldLighting", "Lnet/optifine/shaders/config/PropertyDefaultTrueFalse;")),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/config/PropertyDefaultTrueFalse", "loadFrom", "(Ljava/util/Properties;)Z")),
                inject(ByteCode.Pop()),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPackSeparateAo", "Lnet/optifine/shaders/config/PropertyDefaultTrueFalse;")),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/config/PropertyDefaultTrueFalse", "loadFrom", "(Ljava/util/Properties;)Z")),
                inject(ByteCode.Pop()),
                // 只在loadShaderPackProperties时执行
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPackOptionSliders", "Ljava/util/Set;")),
                inject(ByteCode.IfNotNull(label)),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPackOptions", "[Lnet/optifine/shaders/config/ShaderOption;")),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderPackParser", "parseOptionSliders", "(Ljava/util/Properties;[Lnet/optifine/shaders/config/ShaderOption;)Ljava/util/Set;")),
                inject(ByteCode.PutStatic("net/optifine/shaders/Shaders", "shaderPackOptionSliders", "Ljava/util/Set;")),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPackOptions", "[Lnet/optifine/shaders/config/ShaderOption;")),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderPackParser", "parseProfiles", "(Ljava/util/Properties;[Lnet/optifine/shaders/config/ShaderOption;)[Lnet/optifine/shaders/config/ShaderProfile;")),
                inject(ByteCode.PutStatic("net/optifine/shaders/Shaders", "shaderPackProfiles", "[Lnet/optifine/shaders/config/ShaderProfile;")),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPackProfiles", "[Lnet/optifine/shaders/config/ShaderProfile;")),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPackOptions", "[Lnet/optifine/shaders/config/ShaderOption;")),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderPackParser", "parseGuiScreens", "(Ljava/util/Properties;[Lnet/optifine/shaders/config/ShaderProfile;[Lnet/optifine/shaders/config/ShaderOption;)Ljava/util/Map;")),
                inject(ByteCode.PutStatic("net/optifine/shaders/Shaders", "shaderPackGuiScreens", "Ljava/util/Map;")),
                inject(label),
                // saveFinalShader
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPack", "Lnet/optifine/shaders/IShaderPack;")),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeInterface("net/optifine/shaders/IShaderPack", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;")),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/config/MacroProcessor", "process", "(Ljava/io/InputStream;Ljava/lang/String;)Ljava/io/InputStream;")),
                inject(ByteCode.Pop()));

        patch("增加terrain/entities/blockentities阴影渲染配置 第一部分", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPackShadowTranslucent", "Lnet/optifine/shaders/config/PropertyDefaultTrueFalse;"),
                ByteCode.InvokeVirtual("net/optifine/shaders/config/PropertyDefaultTrueFalse", "resetValue", "()V"),
                inject(ByteCode.GetStatic(HOOK, "shaderPackShadowTerrain", "Lnet/optifine/shaders/config/PropertyDefaultTrueFalse;")),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/config/PropertyDefaultTrueFalse", "resetValue", "()V")),
                inject(ByteCode.GetStatic(HOOK, "shaderPackShadowEntities", "Lnet/optifine/shaders/config/PropertyDefaultTrueFalse;")),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/config/PropertyDefaultTrueFalse", "resetValue", "()V")),
                inject(ByteCode.GetStatic(HOOK, "shaderPackShadowBlockEntities", "Lnet/optifine/shaders/config/PropertyDefaultTrueFalse;")),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/config/PropertyDefaultTrueFalse", "resetValue", "()V")));

        patch("增加terrain/entities/blockentities阴影渲染配置 第二部分", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPackShadowTranslucent", "Lnet/optifine/shaders/config/PropertyDefaultTrueFalse;"),
                ByteCode.ALoad(2),
                ByteCode.InvokeVirtual("net/optifine/shaders/config/PropertyDefaultTrueFalse", "loadFrom", "(Ljava/util/Properties;)Z"),
                ByteCode.Pop(),
                inject(ByteCode.GetStatic(HOOK, "shaderPackShadowTerrain", "Lnet/optifine/shaders/config/PropertyDefaultTrueFalse;")),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/config/PropertyDefaultTrueFalse", "loadFrom", "(Ljava/util/Properties;)Z")),
                inject(ByteCode.Pop()),
                inject(ByteCode.GetStatic(HOOK, "shaderPackShadowEntities", "Lnet/optifine/shaders/config/PropertyDefaultTrueFalse;")),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/config/PropertyDefaultTrueFalse", "loadFrom", "(Ljava/util/Properties;)Z")),
                inject(ByteCode.Pop()),
                inject(ByteCode.GetStatic(HOOK, "shaderPackShadowBlockEntities", "Lnet/optifine/shaders/config/PropertyDefaultTrueFalse;")),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.InvokeVirtual("net/optifine/shaders/config/PropertyDefaultTrueFalse", "loadFrom", "(Ljava/util/Properties;)Z")),
                inject(ByteCode.Pop()));

        patch("增加size.buffer配置 第一部分", method,
                ByteCode.AConstNull(),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "customUniforms", "Lnet/optifine/shaders/uniform/CustomUniforms;"),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "resetBufferSizes", "()V")));

        patch("增加size.buffer配置 第二部分", method,
                ByteCode.ALoad(2),
                ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderPackParser", "parseBuffersFlip", "(Ljava/util/Properties;)V"),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "parseBufferSizes", "(Ljava/util/Properties;)V")));

        patch("加载Prepare与ShadowComp自定义纹理", method,
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "customTexturesDeferred", "[Lnet/optifine/shaders/ICustomTexture;"),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.IConst(3)),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "loadCustomTextures", "(Ljava/util/Properties;I)[Lnet/optifine/shaders/ICustomTexture;")),
                inject(ByteCode.PutStatic(HOOK, "customTexturesPrepare", "[Lnet/optifine/shaders/ICustomTexture;")),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.IConst(4)),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "loadCustomTextures", "(Ljava/util/Properties;I)[Lnet/optifine/shaders/ICustomTexture;")),
                inject(ByteCode.PutStatic(HOOK, "customTexturesShadowComp", "[Lnet/optifine/shaders/ICustomTexture;")));

        patch("重置blend.<program>.<buffer>配置", method,
                ByteCode.InvokeStatic("net/optifine/shaders/EntityAliases", "reset", "()V"),
                inject(ByteCode.GetStatic(BLEND, "propBlend", "Ljava/util/Map;")),
                inject(ByteCode.InvokeInterface("java/util/Map", "clear", "()V")));
    }

    @MethodPatch("init()V")
    public void init(MethodNode method) {
        patch("修改光影选项后重载shader.properties", method,
                ByteCode.Ldc("world"),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                ByteCode.ILoad(3),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;"),
                ByteCode.Ldc("/"),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "toString", "()Ljava/lang/String;"),
                ByteCode.AStore(2),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "loadShaderPackProperties", "()V")));

        patch("配置着色器size.buffer", method,
                ByteCode.ALoad(4),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "updateToggleBuffers", "(Lnet/optifine/shaders/Program;)V"),
                inject(ByteCode.ALoad(4)),
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "updateProgramSize", "(Lnet/optifine/shaders/Program;)V")));

        patch("检查Deferred着色器是否存在计算着色器", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "ProgramsDeferred", "[Lnet/optifine/shaders/Program;"),
                ByteCode.ILoad(3),
                ByteCode.AALoad(),
                ByteCode.InvokeVirtual("net/optifine/shaders/Program", "getId", "()I"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "ProgramsDeferred", "[Lnet/optifine/shaders/Program;")),
                inject(ByteCode.ILoad(3)),
                inject(ByteCode.AALoad()),
                inject(ByteCode.InvokeStatic(COMPUTESHADER, "hasComputes", "(Lnet/optifine/shaders/Program;)Z")),
                inject(ByteCode.IOr()));

        patch("检查是否存在Prepare和ShadowComp着色器", method,
                inject(ByteCode.InvokeStatic(MORESTAGES, "checkComposites", "()V")),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedColorBuffers", "I"),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "usedColorAttachs", "I"));

        LabelNode label = ByteCode.Label();

        patch("修改阴影纹理渲染条件", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedColorBuffers", "I"),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "usedColorAttachs", "I"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedShadowColorBuffers", "I")),
                inject(ByteCode.IfZero(label)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedShadowDepthBuffers", "I")),
                inject(ByteCode.IConst(1)),
                inject(ByteCode.InvokeStatic("java/lang/Math", "max", "(II)I")),
                inject(ByteCode.PutStatic("net/optifine/shaders/Shaders", "usedShadowDepthBuffers", "I")),
                inject(label));

        patch("重置阴影纹理配置", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "gbuffersClearColor", "[Lorg/lwjgl/util/vector/Vector4f;"),
                ByteCode.AConstNull(),
                ByteCode.InvokeStatic("java/util/Arrays", "fill", "([Ljava/lang/Object;Ljava/lang/Object;)V"),
                inject(ByteCode.GetStatic(HOOK, "shadowFormat", "[I")),
                inject(ByteCode.SIPush(GL11.GL_RGBA)),
                inject(ByteCode.InvokeStatic("java/util/Arrays", "fill", "([II)V")),
                inject(ByteCode.GetStatic(HOOK, "shadowClear", "[Z")),
                inject(ByteCode.IConst(1)),
                inject(ByteCode.InvokeStatic("java/util/Arrays", "fill", "([ZZ)V")),
                inject(ByteCode.GetStatic(HOOK, "shadowClearColor", "[Lorg/lwjgl/util/vector/Vector4f;")),
                inject(ByteCode.AConstNull()),
                inject(ByteCode.InvokeStatic("java/util/Arrays", "fill", "([Ljava/lang/Object;Ljava/lang/Object;)V")));

        patch("重置colorimage绑定开关", method,
                ByteCode.IConst(1),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "usedDrawBuffers", "I"),
                inject(ByteCode.IConst(0)),
                inject(ByteCode.PutStatic(HOOK, "bindColorImages", "Z")));

        patch("编译计算着色器", method,
                inject(ByteCode.ALoad(4)),
                inject(ByteCode.Ldc("/shaders/")),
                inject(ByteCode.ALoad(6)),
                inject(ByteCode.Ldc(".csh")),
                inject(ByteCode.InvokeStatic(COMPUTESHADER, "setupComputePrograms", "(Lnet/optifine/shaders/Program;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V")),
                ByteCode.ALoad(4),
                ByteCode.ALoad(9),
                ByteCode.ALoad(10),
                ByteCode.ALoad(11),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "setupProgram", "(Lnet/optifine/shaders/Program;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"));
    }

    @MethodPatch("preDrawComposite()V")
    public void preDrawComposite(MethodNode method) {
        patch("修改buffer大小", method,
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "preDrawComposite", "()V")),
                method.instructions.getFirst());

        // 这俩不会冲突的
        patch("为ShadowComp修改视图大小", method,
                inject(ByteCode.InvokeStatic(MORESTAGES, "preDrawComposite", "()V")),
                method.instructions.getFirst());
    }

    @MethodPatch("postDrawComposite()V")
    public void postDrawComposite(MethodNode method) {
        patch("恢复buffer大小", method,
                inject(ByteCode.InvokeStatic(BUFFERSIZE, "postDrawComposite", "()V")),
                method.instructions.getFirst());

        patch("ShadowComp恢复视图大小", method,
                inject(ByteCode.InvokeStatic(MORESTAGES, "postDrawComposite", "()V")),
                method.instructions.getFirst());
    }

    @MethodPatch("getFramebufferStatusText(I)Ljava/lang/String;")
    public void getFramebufferStatusText(MethodNode method) {
        LabelNode label = ByteCode.Label();
        LookupSwitchInsnNode lookup = (LookupSwitchInsnNode) patch("增加size.buffer相关报错信息 第一部分", method,
                collect(ByteCode.LookupSwitch()))[0];

        boolean matched = false;

        for (int i = 0; i < lookup.keys.size(); i++) {
            if (lookup.keys.get(i) > EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT) {
                lookup.keys.add(i, EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT);
                lookup.labels.add(i, label);

                matched = true;

                break;
            }
        }

        if (!matched) {
            lookup.keys.add(EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT);
            lookup.labels.add(label);
        }

        patch("增加size.buffer相关报错信息 第二部分", method,
                inject(label),
                inject(ByteCode.Ldc("Incomplete dimensions")),
                inject(ByteCode.AReturn()),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                ByteCode.Ldc("Unknown"),
                ByteCode.AReturn());
    }

    @MethodPatch("setupProgram(Lnet/optifine/shaders/Program;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V")
    public void setupProgram(MethodNode method) {
        patch("增加at_midBlock顶点属性 第一部分", method,
                ByteCode.IConst(0),
                ByteCode.PutStatic("net/optifine/shaders/Shaders", "progUseTangentAttrib", "Z"),
                inject(ByteCode.IConst(0)),
                inject(ByteCode.PutStatic(HOOK, "progUseVelocityAttrib", "Z")),
                inject(ByteCode.IConst(0)),
                inject(ByteCode.PutStatic(HOOK, "progUseMidBlockAttrib", "Z")));

        patch("增加at_midBlock顶点属性 第二部分", method,
                inject(ByteCode.ILoad(4)),
                inject(ByteCode.InvokeStatic(VERTEXATTRIBUTE, "bindAttributes", "(I)V")),
                ByteCode.ILoad(4),
                ByteCode.InvokeStatic("org/lwjgl/opengl/ARBShaderObjects", "glLinkProgramARB", "(I)V"));
    }

    @MethodPatch("createVertShader(Lnet/optifine/shaders/Program;Ljava/lang/String;)I")
    public void createVertShader(MethodNode method) {
        patch("增加at_midBlock顶点属性", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.ALoad(9)),
                inject(ByteCode.InvokeStatic(VERTEXATTRIBUTE, "checkAttributes", "(Lnet/optifine/shaders/config/ShaderLine;)V")),
                ByteCode.ALoad(9),
                ByteCode.Ldc("countInstances"));

        patch("Core Profile 版本转换", method,
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(4),
                ByteCode.ALoad(1),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "shaderPack", "Lnet/optifine/shaders/IShaderPack;"),
                ByteCode.IConst(0),
                ByteCode.ALoad(6),
                ByteCode.IConst(0),
                ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderPackParser", "resolveIncludes", "(Ljava/io/BufferedReader;Ljava/lang/String;Lnet/optifine/shaders/IShaderPack;ILjava/util/List;I)Ljava/io/BufferedReader;"),
                ByteCode.AStore(4),
                inject(ByteCode.ALoad(4)),
                inject(ByteCode.InvokeStatic(COREPROFILE, "convertCoreProfile", "(Ljava/io/BufferedReader;)Ljava/io/BufferedReader;")),
                inject(ByteCode.AStore(4)));
    }

    @MethodPatch("getTextureIndex(ILjava/lang/String;)I")
    public void getTextureIndex(MethodNode method) {
        LabelNode labelGbuffer = ByteCode.Label();
        LabelNode labelComp = ByteCode.Label();

        patch("添加colortex8-15自定义纹理 第一部分", method,
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "getBufferIndexFromString", "(Ljava/lang/String;)I")),
                inject(ByteCode.IStore(2)),
                inject(ByteCode.ILoad(2)),
                inject(ByteCode.IConst(4)),
                inject(ByteCode.IfIntLessThan(labelGbuffer)),
                inject(ByteCode.ILoad(2)),
                inject(ByteCode.BIPush(15)),
                inject(ByteCode.IfIntGreaterThan(labelGbuffer)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "colorTextureImageUnit", "[I")),
                inject(ByteCode.ILoad(2)),
                inject(ByteCode.IALoad()),
                inject(ByteCode.IReturn()),
                inject(labelGbuffer),
                ByteCode.ALoad(1),
                ByteCode.Ldc("texture"),
                ByteCode.InvokeVirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z"));

        patch("添加colortex8-15自定义纹理 第二部分", method,
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "getBufferIndexFromString", "(Ljava/lang/String;)I")),
                inject(ByteCode.IStore(2)),
                inject(ByteCode.ILoad(2)),
                inject(ByteCode.IConst(0)),
                inject(ByteCode.IfIntLessThan(labelComp)),
                inject(ByteCode.ILoad(2)),
                inject(ByteCode.BIPush(15)),
                inject(ByteCode.IfIntGreaterThan(labelComp)),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "colorTextureImageUnit", "[I")),
                inject(ByteCode.ILoad(2)),
                inject(ByteCode.IALoad()),
                inject(ByteCode.IReturn()),
                inject(labelComp),
                ByteCode.ALoad(1),
                ByteCode.Ldc("colortex0"),
                ByteCode.InvokeVirtual("java/lang/String", "equals", "(Ljava/lang/Object;)Z"));

        JumpInsnNode equal2 = ByteCode.IfIntEqual(null);
        JumpInsnNode equal3 = ByteCode.IfIntEqual(null);

        equal2.label = equal3.label = ((JumpInsnNode) patch("增加Prepare和ShadowComp纹理Stages", method,
                ByteCode.ILoad(0),
                ByteCode.IConst(1),
                collect(ByteCode.IfIntEqual(null)),
                ByteCode.ILoad(0),
                ByteCode.IConst(2),
                inject(equal2),
                inject(ByteCode.ILoad(0)),
                inject(ByteCode.IConst(3)),
                inject(equal3),
                inject(ByteCode.ILoad(0)),
                inject(ByteCode.IConst(4)),
                ByteCode.IfIntNotEqual(null))[0]).label;
    }

    @MethodPatch("resetCustomTextures()V")
    public void resetCustomTextures(MethodNode method) {
        patch("删除Prepare和ShadowComp自定义纹理", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "customTexturesDeferred", "[Lnet/optifine/shaders/ICustomTexture;"),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "deleteCustomTextures", "([Lnet/optifine/shaders/ICustomTexture;)V"),
                inject(ByteCode.GetStatic(HOOK, "customTexturesPrepare", "[Lnet/optifine/shaders/ICustomTexture;")),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "deleteCustomTextures", "([Lnet/optifine/shaders/ICustomTexture;)V")),
                inject(ByteCode.GetStatic(HOOK, "customTexturesShadowComp", "[Lnet/optifine/shaders/ICustomTexture;")),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "deleteCustomTextures", "([Lnet/optifine/shaders/ICustomTexture;)V")),
                inject(ByteCode.AConstNull()),
                inject(ByteCode.PutStatic(HOOK, "customTexturesPrepare", "[Lnet/optifine/shaders/ICustomTexture;")),
                inject(ByteCode.AConstNull()),
                inject(ByteCode.PutStatic(HOOK, "customTexturesShadowComp", "[Lnet/optifine/shaders/ICustomTexture;")));
    }

    @MethodPatch("setupShadowFrameBuffer()V")
    public void setupShadowFrameBuffer(MethodNode method) {
        patch("固定分配4个阴影颜色纹理", method,
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedShadowColorBuffers", "I")),
                inject(ByteCode.IConst(4)),
                ByteCode.InvokeVirtual("java/nio/Buffer", "limit", "(I)Ljava/nio/Buffer;"));

        patch("设置阴影ping-pong缓冲区与格式 第一部分", method,
                ByteCode.SIPush(GL11.GL_TEXTURE_2D),
                ByteCode.IConst(0),
                remove(ByteCode.SIPush(GL11.GL_RGBA)),
                inject(ByteCode.GetStatic(HOOK, "shadowFormat", "[I")),
                inject(ByteCode.ILoad(0)),
                inject(ByteCode.IALoad()),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "shadowMapWidth", "I"),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "shadowMapHeight", "I"),
                ByteCode.IConst(0),
                remove(ByteCode.Ldc(GL12.GL_BGRA)),
                inject(ByteCode.GetStatic(HOOK, "shadowFormat", "[I")),
                inject(ByteCode.ILoad(0)),
                inject(ByteCode.IALoad()),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "getPixelFormat", "(I)I")),
                ByteCode.Ldc(GL12.GL_UNSIGNED_INT_8_8_8_8_REV),
                ByteCode.AConstNull(),
                ByteCode.CheckCast("java/nio/ByteBuffer"),
                ByteCode.InvokeStatic("org/lwjgl/opengl/GL11", "glTexImage2D", "(IIIIIIIILjava/nio/ByteBuffer;)V"));

        patch("设置阴影ping-pong缓冲区与格式 第二部分", method,
                inject(ByteCode.InvokeStatic(MORESTAGES, "setupShadowFlipBuffer", "()V")),
                ByteCode.IConst(0),
                ByteCode.InvokeStatic("bus", "i", "(I)V"));
    }

    @MethodPatch("updateAlphaBlend(Lnet/optifine/shaders/Program;Lnet/optifine/shaders/Program;)V")
    public void updateAlphaBlend(MethodNode method) {
        patch("应用blend.<program>.<buffer>配置 第一部分", method,
                ByteCode.InvokeStatic("bus", "unlockBlend", "()V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.Frame(),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic(BLEND, "updateOldBlendStateIndexed", "(Lnet/optifine/shaders/Program;)V")));

        patch("应用blend.<program>.<buffer>配置 第二部分", method,
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeStatic(BLEND, "updateNewBlendStateIndexed", "(Lnet/optifine/shaders/Program;)V")),
                ByteCode.Return());
    }

    @MethodPatch("getPixelFormat(I)I")
    public void getPixelFormat(MethodNode method) {
        LookupSwitchInsnNode lookup = (LookupSwitchInsnNode) patch("增加 8bits 与 16bits 整数型纹理格式", method,
                collect(ByteCode.LookupSwitch()))[0];

        lookup.keys.add(GL30.GL_R8I);
        lookup.keys.add(GL30.GL_RG8I);
        lookup.keys.add(GL30.GL_RGB8I);
        lookup.keys.add(GL30.GL_RGBA8I);
        lookup.keys.add(GL30.GL_R8UI);
        lookup.keys.add(GL30.GL_RG8UI);
        lookup.keys.add(GL30.GL_RGB8UI);
        lookup.keys.add(GL30.GL_RGBA8UI);
        lookup.keys.add(GL30.GL_R16I);
        lookup.keys.add(GL30.GL_RG16I);
        lookup.keys.add(GL30.GL_RGB16I);
        lookup.keys.add(GL30.GL_RGBA16I);
        lookup.keys.add(GL30.GL_R16UI);
        lookup.keys.add(GL30.GL_RG16UI);
        lookup.keys.add(GL30.GL_RGB16UI);
        lookup.keys.add(GL30.GL_RGBA16UI);

        lookup.keys.sort(null);

        LabelNode label = lookup.labels.get(0);

        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
        lookup.labels.add(label);
    }

    @MethodPatch("setCamera(F)V")
    public void setCamera(MethodNode method) {
        patch("相机坐标修正", method,
                inject(ByteCode.InvokeStatic(CAMERAFIX, "fixCamera", "()V")),
                ByteCode.Ldc("setCamera"),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "checkGLError", "(Ljava/lang/String;)I"));
    }

    @MethodPatch("setCameraShadow(F)V")
    public void setCameraShadow(MethodNode method) {
        patch("移除阴影阶段Gbuffer矩阵Uniform写入", method,
                remove(ByteCode.SIPush(GL11.GL_PROJECTION_MATRIX)),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "projection", "Ljava/nio/FloatBuffer;")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeVirtual("java/nio/FloatBuffer", "position", "(I)Ljava/nio/Buffer;")),
                remove(ByteCode.CheckCast("java/nio/FloatBuffer")),
                remove(ByteCode.InvokeStatic("org/lwjgl/opengl/GL11", "glGetFloat", "(ILjava/nio/FloatBuffer;)V")),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "projectionInverse", "Ljava/nio/FloatBuffer;")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeVirtual("java/nio/FloatBuffer", "position", "(I)Ljava/nio/Buffer;")),
                remove(ByteCode.CheckCast("java/nio/FloatBuffer")),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "projection", "Ljava/nio/FloatBuffer;")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeVirtual("java/nio/FloatBuffer", "position", "(I)Ljava/nio/Buffer;")),
                remove(ByteCode.CheckCast("java/nio/FloatBuffer")),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "faProjectionInverse", "[F")),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "faProjection", "[F")),
                remove(ByteCode.InvokeStatic("net/optifine/shaders/SMath", "invertMat4FBFA", "(Ljava/nio/FloatBuffer;Ljava/nio/FloatBuffer;[F[F)V")),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "projection", "Ljava/nio/FloatBuffer;")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeVirtual("java/nio/FloatBuffer", "position", "(I)Ljava/nio/Buffer;")),
                remove(ByteCode.Pop()),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "projectionInverse", "Ljava/nio/FloatBuffer;")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeVirtual("java/nio/FloatBuffer", "position", "(I)Ljava/nio/Buffer;")),
                remove(ByteCode.Pop()),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.SIPush(GL11.GL_MODELVIEW_MATRIX),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "modelView", "Ljava/nio/FloatBuffer;")),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "tempMatrixDirectBuffer", "Ljava/nio/FloatBuffer;")),
                ByteCode.IConst(0),
                ByteCode.InvokeVirtual("java/nio/FloatBuffer", "position", "(I)Ljava/nio/Buffer;"),
                ByteCode.CheckCast("java/nio/FloatBuffer"),
                ByteCode.InvokeStatic("org/lwjgl/opengl/GL11", "glGetFloat", "(ILjava/nio/FloatBuffer;)V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "modelViewInverse", "Ljava/nio/FloatBuffer;")),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "tempMatrixDirectBuffer", "Ljava/nio/FloatBuffer;")),
                inject(ByteCode.InvokeVirtual("java/nio/FloatBuffer", "duplicate", "()Ljava/nio/FloatBuffer;")),
                ByteCode.IConst(0),
                ByteCode.InvokeVirtual("java/nio/FloatBuffer", "position", "(I)Ljava/nio/Buffer;"),
                ByteCode.CheckCast("java/nio/FloatBuffer"),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "modelView", "Ljava/nio/FloatBuffer;")),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "tempMatrixDirectBuffer", "Ljava/nio/FloatBuffer;")),
                inject(ByteCode.InvokeVirtual("java/nio/FloatBuffer", "duplicate", "()Ljava/nio/FloatBuffer;")),
                ByteCode.IConst(0),
                ByteCode.InvokeVirtual("java/nio/FloatBuffer", "position", "(I)Ljava/nio/Buffer;"),
                ByteCode.CheckCast("java/nio/FloatBuffer"),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "faModelViewInverse", "[F"),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "faModelView", "[F"),
                ByteCode.InvokeStatic("net/optifine/shaders/SMath", "invertMat4FBFA", "(Ljava/nio/FloatBuffer;Ljava/nio/FloatBuffer;[F[F)V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "modelView", "Ljava/nio/FloatBuffer;")),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "tempMatrixDirectBuffer", "Ljava/nio/FloatBuffer;")),
                ByteCode.IConst(0),
                ByteCode.InvokeVirtual("java/nio/FloatBuffer", "position", "(I)Ljava/nio/Buffer;"),
                ByteCode.Pop(),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.GetStatic("net/optifine/shaders/Shaders", "modelViewInverse", "Ljava/nio/FloatBuffer;")),
                remove(ByteCode.IConst(0)),
                remove(ByteCode.InvokeVirtual("java/nio/FloatBuffer", "position", "(I)Ljava/nio/Buffer;")),
                remove(ByteCode.Pop()));

        patch("相机坐标修正", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "shadowModelViewInverse", "Ljava/nio/FloatBuffer;"),
                ByteCode.IConst(0),
                ByteCode.InvokeVirtual("java/nio/FloatBuffer", "position", "(I)Ljava/nio/Buffer;"),
                inject(ByteCode.InvokeStatic(CAMERAFIX, "fixCameraShadow", "()V")),
                ByteCode.Pop(),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "uniform_gbufferProjection", "Lnet/optifine/shaders/uniform/ShaderUniformM4;"),
                ByteCode.IConst(0),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "projection", "Ljava/nio/FloatBuffer;"),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "setProgramUniformMatrix4ARB", "(Lnet/optifine/shaders/uniform/ShaderUniformM4;ZLjava/nio/FloatBuffer;)V"));
    }

    @MethodPatch("getCameraPosition()Let;")
    public void getCameraPosition(MethodNode method) {
        patch("相机坐标修正", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "cameraPositionX", "D"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "cameraOffsetX", "I")),
                inject(ByteCode.I2D()),
                inject(ByteCode.DAdd()),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "cameraPositionY", "D"),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "cameraPositionZ", "D"),
                inject(ByteCode.GetStatic("net/optifine/shaders/Shaders", "cameraOffsetZ", "I")),
                inject(ByteCode.I2D()),
                inject(ByteCode.DAdd()));
    }

    @MethodPatch("setEntityId(Lvg;)V")
    public void setEntityId(MethodNode method) {
        patch("高版本实体ID映射以及闪电实体ID", method,
                ByteCode.ILoad(1),
                ByteCode.InvokeStatic("net/optifine/shaders/EntityAliases", "getEntityAliasId", "(I)I"),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "getEntityAliasID", "(ILvg;)I")),
                ByteCode.IStore(2),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ILoad(2),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "isAllowNegative", "(I)I")));
    }

    @MethodPatch("beginSky()V")
    public void beginSky(MethodNode method) {
        patch("增加SKY渲染阶段配置", method,
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "useProgram", "(Lnet/optifine/shaders/Program;)V"),
                inject(ByteCode.GetStatic(RENDERSTAGE, "SKY", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));
    }

    @MethodPatch("beginClouds()V")
    public void beginClouds(MethodNode method) {
        patch("增加CLOUDS渲染阶段配置", method,
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "useProgram", "(Lnet/optifine/shaders/Program;)V"),
                inject(ByteCode.GetStatic(RENDERSTAGE, "CLOUDS", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));
    }

    @MethodPatch("beginEntities()V")
    public void beginEntities(MethodNode method) {
        patch("增加ENTITIES渲染阶段配置", method,
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "useProgram", "(Lnet/optifine/shaders/Program;)V"),
                inject(ByteCode.GetStatic(RENDERSTAGE, "ENTITIES", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));
    }

    @MethodPatch("beginBlockEntities()V")
    public void beginBlockEntities(MethodNode method) {
        patch("增加BLOCK_ENTITIES渲染阶段配置", method,
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "useProgram", "(Lnet/optifine/shaders/Program;)V"),
                inject(ByteCode.GetStatic(RENDERSTAGE, "BLOCK_ENTITIES", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));
    }

    @MethodPatch("beginLitParticles()V")
    @MethodPatch("beginParticles()V")
    public void beginParticles(MethodNode method) {
        patch("增加PARTICLES渲染阶段配置", method,
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "useProgram", "(Lnet/optifine/shaders/Program;)V"),
                inject(ByteCode.GetStatic(RENDERSTAGE, "PARTICLES", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));
    }

    @MethodPatch("beginWeather()V")
    public void beginWeather(MethodNode method) {
        patch("增加RAIN_SNOW渲染阶段配置", method,
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "useProgram", "(Lnet/optifine/shaders/Program;)V"),
                inject(ByteCode.GetStatic(RENDERSTAGE, "RAIN_SNOW", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));

        JumpInsnNode skip = ByteCode.IfNotZero(null);

        skip.label = ((JumpInsnNode) patch("开启高版本渲染机制后跳过复制深度纹理", method,
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "isForwardVersion", "()Z")),
                inject(skip),
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "usedDepthBuffers", "I"),
                ByteCode.IConst(3),
                collect(ByteCode.IfIntLessThan(null)))[0]).label;
    }

    @MethodPatch("endRender()V")
    @MethodPatch("endSky()V")
    @MethodPatch("endClouds()V")
    @MethodPatch("endParticles()V")
    @MethodPatch("endWeather()V")
    public void resetRenderStage(MethodNode method) {
        patch("重置渲染阶段配置", method,
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "useProgram", "(Lnet/optifine/shaders/Program;)V"),
                inject(ByteCode.GetStatic(RENDERSTAGE, "NONE", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));
    }

    @MethodPatch("beginHand(Z)V")
    public void beginHand(MethodNode method) {
        patch("增加HAND_TRANSLUCENT渲染阶段配置", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "ProgramHandWater", "Lnet/optifine/shaders/Program;"),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "useProgram", "(Lnet/optifine/shaders/Program;)V"),
                inject(ByteCode.GetStatic(RENDERSTAGE, "HAND_TRANSLUCENT", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));

        patch("增加HAND_SOLID渲染阶段配置", method,
                ByteCode.GetStatic("net/optifine/shaders/Shaders", "ProgramHand", "Lnet/optifine/shaders/Program;"),
                ByteCode.InvokeStatic("net/optifine/shaders/Shaders", "useProgram", "(Lnet/optifine/shaders/Program;)V"),
                inject(ByteCode.GetStatic(RENDERSTAGE, "HAND_SOLID", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")));
    }

    @MethodPatch("endHand()V")
    public void endHand(MethodNode method) {
        patch("重置渲染阶段配置", method,
                inject(ByteCode.GetStatic(RENDERSTAGE, "NONE", "Ladvancedshader/Hook$RenderStage;")),
                inject(ByteCode.InvokeStatic(RENDERSTAGE, "setRenderStage", "(Ladvancedshader/Hook$RenderStage;)V")),
                method.instructions.getFirst());
    }
}

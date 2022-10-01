package advancedshader.core.patcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.ImmutableMap;

import net.minecraft.launchwrapper.IClassTransformer;

public abstract class Patcher implements IClassTransformer {

    public static final Logger LOGGER = LogManager.getLogger("AdvancedShader");

    public static final String HOOK = "advancedshader/Hook";
    public static final String MOREBUFFERS = "advancedshader/Hook$MoreBuffers";
    public static final String FORWARDFEATURES = "advancedshader/Hook$ForwardFeatures";
    public static final String COREPROFILE = "advancedshader/Hook$CoreProfile";
    public static final String LINESHADER = "advancedshader/Hook$LineShader";
    public static final String CHUNKOFFSET = "advancedshader/Hook$ChunkOffset";
    public static final String ANISOTROPICFILTER = "advancedshader/Hook$AnisotropicFilter";
    public static final String RENDERSTAGE = "advancedshader/Hook$RenderStage";
    public static final String PROPERTYFIX = "advancedshader/Hook$PropertyFix";
    public static final String BUFFERSIZE = "advancedshader/Hook$BufferSize";
    public static final String VERTEXATTRIBUTE = "advancedshader/Hook$VertexAttribute";
    public static final String MORESTAGES = "advancedshader/Hook$MoreStages";
    public static final String SHADOWFILPTEXTURES = "advancedshader/Hook$MoreStages$ShadowFlipTextures";
    public static final String BLEND = "advancedshader/Hook$Blend";
    public static final String COMPUTESHADER = "advancedshader/Hook$ComputeShader";
    public static final String BLOCKALIASFIX = "advancedshader/Hook$BlockAliasFix";
    public static final String MATCHBLOCKSTATE = "advancedshader/Hook$BlockAliasFix$MatchBlockState";
    public static final String CUSTOMUNIFORM = "advancedshader/Hook$CustomUniform";
    public static final String CAMERAFIX = "advancedshader/Hook$CameraFix";
    public static final String LANGFIX = "advancedshader/Hook$LangFix";

    private final String target;
    private final Method classPatch;
    private final Map<String, Method> methodPatch;
    private final Map<String, Boolean> methodPatched;

    public Patcher() {
        Class<? extends Patcher> clazz = this.getClass();
        Patch patch = clazz.getAnnotation(Patch.class);

        if (patch != null) {
            this.target = patch.value();
        } else {
            this.target = "";
        }

        Method classPatch = null;
        Map<String, Method> methodPatch = new HashMap<>();
        Map<String, Boolean> methodPatched = new HashMap<>();

        for (Method method : clazz.getMethods()) {
            if (classPatch == null) {
                ClassPatch cp = method.getAnnotation(ClassPatch.class);

                if (cp != null) {
                    classPatch = method;

                    continue;
                }
            }

            for (MethodPatch mp : method.getAnnotationsByType(MethodPatch.class)) {
                methodPatch.put(mp.value(), method);
                methodPatched.put(mp.value(), Boolean.FALSE);
            }
        }

        this.classPatch = classPatch;
        this.methodPatch = ImmutableMap.copyOf(methodPatch);
        this.methodPatched = methodPatched;
    }

    @Override
    public final byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (name.equals(this.target) || transformedName.equals(this.target)) {
            if (name.equals(transformedName)) {
                LOGGER.info("正在加载类 {}，执行修补程序", name);
            } else {
                LOGGER.info("正在加载类 {} ({})，执行修补程序", name, transformedName);
            }

            ClassReader cr = new ClassReader(basicClass);
            ClassNode node = new ClassNode();
            cr.accept(node, 0);

            try {
                if (this.classPatch != null) {
                    LOGGER.debug(" - 执行类结构修补");
                    this.classPatch.invoke(this, node);
                }

                for (MethodNode method : node.methods) {
                    String key = method.name + method.desc;
                    Method methodPatch = this.methodPatch.get(key);

                    if (methodPatch != null) {
                        if (name.equals(transformedName)) {
                            LOGGER.debug(" - 执行字节码修补，目标方法：{}", key);
                        } else {
                            LOGGER.debug(" - 执行字节码修补，目标方法：{} ({})", key, methodPatch.getName());
                        }
                        methodPatch.invoke(this, method);
                        this.methodPatched.put(key, Boolean.TRUE);
                    }
                }

                for (String key : this.methodPatched.keySet()) {
                    if (!this.methodPatched.get(key)) {
                        LOGGER.warn(" - 未执行目标为 {} 的方法修补程式", key);
                    }
                }

                LOGGER.debug("======================");

                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                node.accept(cw);
                basicClass = cw.toByteArray();
            } catch (Throwable e) {
                RuntimeException ex = new RuntimeException("发生严重故障，模组无法正常运作，请检查模组冲突情况。", e);

                LOGGER.catching(ex);
                throw ex;
            }

//          try {
//              File f = new File("asmdebug/" + name.replace('.', '/') + ".class");
//              f.getParentFile().mkdirs();
//              FileOutputStream fos = new FileOutputStream(f);
//              fos.write(basicClass);
//              fos.close();
//          } catch (IOException e) {
//              e.printStackTrace();
//          }
        }

        return basicClass;
    }

    public final AbstractInsnNode[] patch(String task, MethodNode method, AbstractInsnNode... matchAndPatch) {
        LOGGER.debug("    - 执行修补作业：{}", task);

        AbstractInsnNode node = method.instructions.getFirst();
        ArrayList<AbstractInsnNode> list = new ArrayList<>();

        while (node != null) {
            if (checkIfMatch(node, matchAndPatch)) {
                break;
            }

            node = node.getNext();
        }

        if (node == null) {
            // 生产环境运行到这里只能说明有冲突模组，那没啥办法，崩了呗
            throw new RuntimeException("修补失败，无法匹配目标字节码。");
        }

        for (int i = 0; i < matchAndPatch.length; i++) {
            AbstractInsnNode patch = matchAndPatch[i];

            if (patch instanceof ModifyingNode) {
                ModifyingNode modify = (ModifyingNode) patch;

                if (modify.mode == ModifyingNode.MODE_REMOVE) {
                    AbstractInsnNode remove = node;

                    node = node.getNext();
                    method.instructions.remove(remove);

                    continue;
                } else if (modify.mode == ModifyingNode.MODE_INJECT) {
                    if (node == null) {
                        method.instructions.add(modify.node);
                    } else {
                        method.instructions.insertBefore(node, modify.node);
                    }

                    continue;
                } else if (modify.mode == ModifyingNode.MODE_COLLECT) {
                    list.add(node);
                } else if (modify.mode == ModifyingNode.MODE_MATCH_ALL) {}
            }

            node = node.getNext();
        }

        return list.toArray(new AbstractInsnNode[0]);
    }

    public final AbstractInsnNode inject(AbstractInsnNode node) {
        return new ModifyingNode(node, ModifyingNode.MODE_INJECT);
    }

    public final AbstractInsnNode remove(AbstractInsnNode node) {
        return new ModifyingNode(node, ModifyingNode.MODE_REMOVE);
    }

    public final AbstractInsnNode collect(AbstractInsnNode node) {
        return new ModifyingNode(node, ModifyingNode.MODE_COLLECT);
    }

    public final AbstractInsnNode matchAll() {
        return new ModifyingNode(null, ModifyingNode.MODE_MATCH_ALL);
    }

    private final boolean checkIfMatch(AbstractInsnNode node, AbstractInsnNode[] target) {
        boolean matched = false; // target总不能全是inject吧

        for (int i = 0; i < target.length; i++) {
            AbstractInsnNode match = target[i];
            boolean matchAll = false;

            if (node == null || match == null /* EXCUSE ME? */) {
                return false;
            }

            if (match instanceof ModifyingNode) {
                ModifyingNode modify = (ModifyingNode) match;

                if (modify.mode != ModifyingNode.MODE_INJECT) {
                    match = modify.node;

                    if (modify.mode == ModifyingNode.MODE_MATCH_ALL) {
                        matchAll = true;
                    }
                } else {
                    continue;
                }
            }

            if (!matchAll && !checkNodeEqual(node, match)) {
                return false;
            }

            node = node.getNext();
            matched = true;
        }

        return matched;
    }

    private static boolean checkNodeEqual(AbstractInsnNode nodeA, AbstractInsnNode nodeB) {
        if (nodeA.getClass() == nodeB.getClass() && nodeA.getOpcode() == nodeB.getOpcode()) {
            Class<? extends AbstractInsnNode> clazz = nodeA.getClass();

            if (clazz == FieldInsnNode.class) {
                FieldInsnNode a = (FieldInsnNode) nodeA;
                FieldInsnNode b = (FieldInsnNode) nodeB;

                return Objects.equals(a.name, b.name)
                        && Objects.equals(a.owner, b.owner)
                        && Objects.equals(a.desc, b.desc);
            } else if (clazz == FrameNode.class) {
                return true;
            } else if (clazz == IincInsnNode.class) {
                IincInsnNode a = (IincInsnNode) nodeA;
                IincInsnNode b = (IincInsnNode) nodeB;

                return Objects.equals(a.var, b.var)
                        && Objects.equals(a.incr, b.incr);
            } else if (clazz == InsnNode.class) {
                return true;
            } else if (clazz == IntInsnNode.class) {
                IntInsnNode a = (IntInsnNode) nodeA;
                IntInsnNode b = (IntInsnNode) nodeB;

                return a.operand == b.operand;
            } else if (clazz == InvokeDynamicInsnNode.class) {
                InvokeDynamicInsnNode a = (InvokeDynamicInsnNode) nodeA;
                InvokeDynamicInsnNode b = (InvokeDynamicInsnNode) nodeB;

                return Objects.equals(a.name, b.name)
                        && Objects.equals(a.desc, b.desc)
                        && Objects.equals(a.bsm, b.bsm)
                        && Objects.equals(a.bsmArgs, b.bsmArgs);
            } else if (clazz == JumpInsnNode.class) {
                return true;
            } else if (clazz == LabelNode.class) {
                return true;
            } else if (clazz == LdcInsnNode.class) {
                LdcInsnNode a = (LdcInsnNode) nodeA;
                LdcInsnNode b = (LdcInsnNode) nodeB;

                return Objects.equals(a.cst, b.cst);
            } else if (clazz == LineNumberNode.class) {
                return true;
            } else if (clazz == LookupSwitchInsnNode.class) {
                return true;
            } else if (clazz == MethodInsnNode.class) {
                MethodInsnNode a = (MethodInsnNode) nodeA;
                MethodInsnNode b = (MethodInsnNode) nodeB;

                return Objects.equals(a.name, b.name)
                        && Objects.equals(a.owner, b.owner)
                        && Objects.equals(a.desc, b.desc)
                        && Objects.equals(a.itf, b.itf);
            } else if (clazz == MultiANewArrayInsnNode.class) {
                MultiANewArrayInsnNode a = (MultiANewArrayInsnNode) nodeA;
                MultiANewArrayInsnNode b = (MultiANewArrayInsnNode) nodeB;

                return Objects.equals(a.dims, b.dims)
                        && Objects.equals(a.desc, b.desc);
            } else if (clazz == TableSwitchInsnNode.class) {
                return true;
            } else if (clazz == TypeInsnNode.class) {
                TypeInsnNode a = (TypeInsnNode) nodeA;
                TypeInsnNode b = (TypeInsnNode) nodeB;

                return Objects.equals(a.desc, b.desc);
            } else if (clazz == VarInsnNode.class) {
                VarInsnNode a = (VarInsnNode) nodeA;
                VarInsnNode b = (VarInsnNode) nodeB;

                return Objects.equals(a.var, b.var);
            }
        }

        return false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface Patch {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface ClassPatch {}

    @Repeatable(Methods.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface MethodPatch {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface Methods {
        MethodPatch[] value();
    }

    public static class ByteCode implements Opcodes {

        public static VarInsnNode ALoad(int var) {
            return new VarInsnNode(ALOAD, var);
        }

        public static VarInsnNode AStore(int var) {
            return new VarInsnNode(ASTORE, var);
        }

        public static VarInsnNode ILoad(int var) {
            return new VarInsnNode(ILOAD, var);
        }

        public static VarInsnNode IStore(int var) {
            return new VarInsnNode(ISTORE, var);
        }

        public static VarInsnNode LLoad(int var) {
            return new VarInsnNode(LLOAD, var);
        }

        public static VarInsnNode LStore(int var) {
            return new VarInsnNode(LSTORE, var);
        }

        public static VarInsnNode FLoad(int var) {
            return new VarInsnNode(FLOAD, var);
        }

        public static VarInsnNode FStore(int var) {
            return new VarInsnNode(FSTORE, var);
        }

        public static VarInsnNode DLoad(int var) {
            return new VarInsnNode(DLOAD, var);
        }

        public static VarInsnNode DStore(int var) {
            return new VarInsnNode(DSTORE, var);
        }

        public static FieldInsnNode GetStatic(String owner, String name, String desc) {
            return new FieldInsnNode(GETSTATIC, owner, name, desc);
        }

        public static FieldInsnNode PutStatic(String owner, String name, String desc) {
            return new FieldInsnNode(PUTSTATIC, owner, name, desc);
        }

        public static FieldInsnNode GetField(String owner, String name, String desc) {
            return new FieldInsnNode(GETFIELD, owner, name, desc);
        }

        public static FieldInsnNode PutField(String owner, String name, String desc) {
            return new FieldInsnNode(PUTFIELD, owner, name, desc);
        }

        public static MethodInsnNode InvokeStatic(String owner, String name, String desc) {
            return new MethodInsnNode(INVOKESTATIC, owner, name, desc, false);
        }

        public static MethodInsnNode InvokeVirtual(String owner, String name, String desc) {
            return new MethodInsnNode(INVOKEVIRTUAL, owner, name, desc, false);
        }

        public static MethodInsnNode InvokeSpecial(String owner, String name, String desc) {
            return new MethodInsnNode(INVOKESPECIAL, owner, name, desc, false);
        }

        public static MethodInsnNode InvokeInterface(String owner, String name, String desc) {
            return new MethodInsnNode(INVOKEINTERFACE, owner, name, desc, true);
        }

        public static LdcInsnNode Ldc(Object obj) {
            return new LdcInsnNode(obj);
        }

        public static IntInsnNode BIPush(int i) {
            if (i < -128 || i > 127 || i <= 5 && i >= -1) {
                弱智八哥报错器.让我想想怎么骂这个开发者();
            }

            return new IntInsnNode(BIPUSH, i);
        }

        public static IntInsnNode SIPush(int i) {
            if (i < -32768 || i > 32767 || i <= 5 && i >= -1) {
                弱智八哥报错器.让我想想怎么骂这个开发者();
            }

            return new IntInsnNode(SIPUSH, i);
        }

        public static IntInsnNode NewArray(int type) {
            return new IntInsnNode(NEWARRAY, type);
        }

        public static LabelNode Label() {
            return new LabelNode();
        }

        public static LineNumberNode LineNumber() {
            return new LineNumberNode(0, null);
        }

        public static InsnNode IConst(int i) {
            switch (i) {
            case 0:
                return new InsnNode(ICONST_0);
            case 1:
                return new InsnNode(ICONST_1);
            case 2:
                return new InsnNode(ICONST_2);
            case 3:
                return new InsnNode(ICONST_3);
            case 4:
                return new InsnNode(ICONST_4);
            case 5:
                return new InsnNode(ICONST_5);
            case -1:
                return new InsnNode(ICONST_M1);
            }

            弱智八哥报错器.让我想想怎么骂这个开发者();
            return null;
        }

        public static InsnNode FConst(int i) {
            switch (i) {
            case 0:
                return new InsnNode(FCONST_0);
            case 1:
                return new InsnNode(FCONST_1);
            case 2:
                return new InsnNode(FCONST_2);
            }

            弱智八哥报错器.让我想想怎么骂这个开发者();
            return null;
        }

        public static InsnNode DConst(int i) {
            switch (i) {
            case 0:
                return new InsnNode(DCONST_0);
            case 1:
                return new InsnNode(DCONST_1);
            }

            弱智八哥报错器.让我想想怎么骂这个开发者();
            return null;
        }

        public static InsnNode AConstNull() {
            return new InsnNode(ACONST_NULL);
        }

        public static InsnNode Dup() {
            return new InsnNode(DUP);
        }

        public static InsnNode DupX1() {
            return new InsnNode(DUP_X1);
        }

        public static InsnNode AALoad() {
            return new InsnNode(AALOAD);
        }

        public static InsnNode AAStore() {
            return new InsnNode(AASTORE);
        }

        public static InsnNode IALoad() {
            return new InsnNode(IALOAD);
        }

        public static InsnNode IAStore() {
            return new InsnNode(IASTORE);
        }

        public static InsnNode FALoad() {
            return new InsnNode(FALOAD);
        }

        public static InsnNode FAStore() {
            return new InsnNode(FASTORE);
        }

        public static InsnNode BALoad() {
            return new InsnNode(BALOAD);
        }

        public static InsnNode BAStore() {
            return new InsnNode(BASTORE);
        }

        public static InsnNode Return() {
            return new InsnNode(RETURN);
        }

        public static InsnNode AReturn() {
            return new InsnNode(ARETURN);
        }

        public static InsnNode IReturn() {
            return new InsnNode(IRETURN);
        }

        public static InsnNode IOr() {
            return new InsnNode(IOR);
        }

        public static InsnNode IMul() {
            return new InsnNode(IMUL);
        }

        public static InsnNode IAdd() {
            return new InsnNode(IADD);
        }

        public static InsnNode ISub() {
            return new InsnNode(ISUB);
        }

        public static InsnNode DAdd() {
            return new InsnNode(DADD);
        }

        public static InsnNode DSub() {
            return new InsnNode(DSUB);
        }

        public static InsnNode DCmpG() {
            return new InsnNode(DCMPG);
        }

        public static InsnNode FAdd() {
            return new InsnNode(FADD);
        }

        public static InsnNode FSub() {
            return new InsnNode(FSUB);
        }

        public static InsnNode FMul() {
            return new InsnNode(FMUL);
        }

        public static InsnNode FDiv() {
            return new InsnNode(FDIV);
        }

        public static InsnNode I2F() {
            return new InsnNode(I2F);
        }

        public static InsnNode I2D() {
            return new InsnNode(I2D);
        }

        public static InsnNode D2F() {
            return new InsnNode(D2F);
        }

        public static InsnNode F2D() {
            return new InsnNode(F2D);
        }

        public static InsnNode Pop() {
            return new InsnNode(POP);
        }

        public static TypeInsnNode New(String desc) {
            return new TypeInsnNode(NEW, desc);
        }

        public static TypeInsnNode NewArray(String desc) {
            return new TypeInsnNode(ANEWARRAY, desc);
        }

        public static TypeInsnNode CheckCast(String desc) {
            return new TypeInsnNode(CHECKCAST, desc);
        }

        public static TypeInsnNode InstanceOf(String desc) {
            return new TypeInsnNode(INSTANCEOF, desc);
        }

        public static JumpInsnNode Goto(LabelNode target) {
            return new JumpInsnNode(GOTO, target);
        }

        public static JumpInsnNode IfNull(LabelNode target) {
            return new JumpInsnNode(IFNULL, target);
        }

        public static JumpInsnNode IfNotNull(LabelNode target) {
            return new JumpInsnNode(IFNONNULL, target);
        }

        public static JumpInsnNode IfZero(LabelNode target) {
            return new JumpInsnNode(IFEQ, target);
        }

        public static JumpInsnNode IfNotZero(LabelNode target) {
            return new JumpInsnNode(IFNE, target);
        }

        public static JumpInsnNode IfLessThanZero(LabelNode target) {
            return new JumpInsnNode(IFLT, target);
        }

        public static JumpInsnNode IfGreaterEqualZero(LabelNode target) {
            return new JumpInsnNode(IFGE, target);
        }

        public static JumpInsnNode IfGreaterThanZero(LabelNode target) {
            return new JumpInsnNode(IFGT, target);
        }

        public static JumpInsnNode IfObjEqual(LabelNode target) {
            return new JumpInsnNode(IF_ACMPEQ, target);
        }

        public static JumpInsnNode IfObjNotEqual(LabelNode target) {
            return new JumpInsnNode(IF_ACMPNE, target);
        }

        public static JumpInsnNode IfIntEqual(LabelNode target) {
            return new JumpInsnNode(IF_ICMPEQ, target);
        }

        public static JumpInsnNode IfIntGreaterEqual(LabelNode target) {
            return new JumpInsnNode(IF_ICMPGE, target);
        }

        public static JumpInsnNode IfIntGreaterThan(LabelNode target) {
            return new JumpInsnNode(IF_ICMPGT, target);
        }

        public static JumpInsnNode IfIntLessEqual(LabelNode target) {
            return new JumpInsnNode(IF_ICMPLE, target);
        }

        public static JumpInsnNode IfIntLessThan(LabelNode target) {
            return new JumpInsnNode(IF_ICMPLT, target);
        }

        public static JumpInsnNode IfIntNotEqual(LabelNode target) {
            return new JumpInsnNode(IF_ICMPNE, target);
        }

        public static FrameNode Frame() {
            return new FrameNode(F_SAME, 0, null, 0, null);
        }

        public static IincInsnNode Inc(int var, int inc) {
            return new IincInsnNode(var, inc);
        }

        public static LookupSwitchInsnNode LookupSwitch() {
            return new LookupSwitchInsnNode(null, null, null);
        }

        public static TableSwitchInsnNode TableSwitch() {
            return new TableSwitchInsnNode(0, 0, null);
        }
    }

    private static final class ModifyingNode extends InsnNode {

        public static final int MODE_INJECT = 0;
        public static final int MODE_REMOVE = 1;
        public static final int MODE_COLLECT = 2;
        public static final int MODE_MATCH_ALL = 3;

        public final AbstractInsnNode node;
        public final int mode;

        private ModifyingNode(AbstractInsnNode node, int mode) {
            super(Opcodes.NOP);

            this.node = node;
            this.mode = mode;

            if (this.node instanceof ModifyingNode || this.mode < MODE_INJECT && this.mode > MODE_MATCH_ALL) {
                弱智八哥报错器.让我想想怎么骂这个开发者();
            }
        }
    }

    private static final class 弱智八哥报错器 {
        private static final String[] 哈哈哈哈哈哈哈 = new String[] {
                "结论：大脑进水综合征",
                "脑子瓦特了= =",
                "脑子进水了是吧？？？",
                "我有病，你有药吗",
                "建议放弃治疗",
                "这已经不是**的问题了呀（哭）",
                "看，这是你写的八阿哥，啪，游戏没了",
                "喜闻乐见锣鼓喧天鞭炮齐鸣红旗招展",
                "Standing here I realize you were just like me trying to make HISTORY",
                "你是想笑死我然后继承我的欠款吗",
                "I am the storm that is APPROOOOOOOOOOOOOOOOOOOOOOACHING!",
                "Nanobug, son.",
                "我重伤倒地，但我还活着",
                "不愧是你"
        };

        public static void 让我想想怎么骂这个开发者() {
            throw new Error(哈哈哈哈哈哈哈[new Random().nextInt(哈哈哈哈哈哈哈.length)]);
        }
    }
}

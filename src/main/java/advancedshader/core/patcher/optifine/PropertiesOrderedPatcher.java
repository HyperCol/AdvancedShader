package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.util.PropertiesOrdered")
public class PropertiesOrderedPatcher extends Patcher {

    @MethodPatch("put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
    public void put(MethodNode method) {
        LabelNode label1 = ByteCode.Label();
        LabelNode label2 = ByteCode.Label();

        patch("配置文件修正", method,
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InstanceOf("java/lang/String")),
                inject(ByteCode.IfZero(label1)),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.CheckCast("java/lang/String")),
                inject(ByteCode.InvokeVirtual("java/lang/String", "trim", "()Ljava/lang/String;")),
                inject(ByteCode.AStore(1)),
                inject(label1),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.InstanceOf("java/lang/String")),
                inject(ByteCode.IfZero(label2)),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.CheckCast("java/lang/String")),
                inject(ByteCode.InvokeVirtual("java/lang/String", "trim", "()Ljava/lang/String;")),
                inject(ByteCode.AStore(2)),
                inject(label2),
                method.instructions.getFirst());
    }
}

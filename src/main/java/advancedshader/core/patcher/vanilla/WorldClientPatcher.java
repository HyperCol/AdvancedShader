package advancedshader.core.patcher.vanilla;

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("bsb")
public class WorldClientPatcher extends Patcher {

    @MethodPatch("j()V")
    public void updateBlocks(MethodNode method) {
        patch("增加氛围值机制", method,
                ByteCode.ALoad(0),
                ByteCode.Dup(),
                ByteCode.GetField("bsb", "O", "I"),
                ByteCode.IConst(1),
                ByteCode.ISub(),
                ByteCode.PutField("bsb", "O", "I"),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.Dup()),
                inject(ByteCode.GetField("bsb", "O", "I")),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "updateMoodTick", "(I)I")),
                inject(ByteCode.PutField("bsb", "O", "I")));
    }

    @MethodPatch("a(IILaxw;)V")
    public void playMoodSoundAndCheckLight(MethodNode method) {
        LabelNode label = ByteCode.Label();

        patch("增加氛围值机制", method,
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "skipMoodCheck", "()Z")),
                inject(ByteCode.IfNotZero(label)),
                ByteCode.ALoad(10),
                ByteCode.InvokeInterface("awt", "a", "()Lbcz;"),
                ByteCode.GetStatic("bcz", "a", "Lbcz;"),
                ByteCode.IfObjNotEqual(null),
                ByteCode.ALoad(0),
                ByteCode.ALoad(9),
                ByteCode.InvokeVirtual("bsb", "j", "(Let;)I"),
                ByteCode.ALoad(0),
                ByteCode.GetField("bsb", "r", "Ljava/util/Random;"),
                ByteCode.BIPush(8),
                ByteCode.InvokeVirtual("java/util/Random", "nextInt", "(I)I"),
                ByteCode.IfIntGreaterThan(null),
                ByteCode.ALoad(0),
                ByteCode.GetStatic("ana", "a", "Lana;"),
                ByteCode.ALoad(9),
                ByteCode.InvokeVirtual("bsb", "b", "(Lana;Let;)I"),
                ByteCode.IfGreaterThanZero(null),
                inject(label));
    }
}

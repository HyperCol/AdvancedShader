package advancedshader.core.patcher.vanilla;

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("bun")
public class ChunkRenderContainerPatcher extends Patcher {

    @MethodPatch("a(Lbxr;)V")
    public void preRenderChunk(MethodNode method) {
        LabelNode label = ByteCode.Label();

        patch("添加chunkOffset Uniform变量", method,
                inject(ByteCode.Label()),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeVirtual("bxr", "k", "()Let;")),
                inject(ByteCode.InvokeVirtual("et", "p", "()I")),
                inject(ByteCode.I2D()),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetField("bun", "c", "D")),
                inject(ByteCode.DSub()),
                inject(ByteCode.D2F()),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeVirtual("bxr", "k", "()Let;")),
                inject(ByteCode.InvokeVirtual("et", "q", "()I")),
                inject(ByteCode.I2D()),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetField("bun", "d", "D")),
                inject(ByteCode.DSub()),
                inject(ByteCode.D2F()),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeVirtual("bxr", "k", "()Let;")),
                inject(ByteCode.InvokeVirtual("et", "r", "()I")),
                inject(ByteCode.I2D()),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetField("bun", "e", "D")),
                inject(ByteCode.DSub()),
                inject(ByteCode.D2F()),
                inject(ByteCode.InvokeStatic(CHUNKOFFSET, "setChunkOffset", "(FFF)Z")),
                inject(ByteCode.IfZero(label)),
                inject(ByteCode.Return()),
                inject(label),
                method.instructions.getFirst());
    }
}

package advancedshader.core.patcher.vanilla;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("bxr")
public class RenderChunkPatcher extends Patcher {

    @MethodPatch("fixBlockLayer(Lawt;Lamm;)Lamm;")
    public void fixBlockLayer(MethodNode method) {
        patch("修改绊线方块的渲染类型为Tripwire", method,
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "replaceRenderLayer", "(Lawt;Lamm;)Lamm;")), // 相信我，铁驭（这里混不混淆无所谓的= =）
                inject(ByteCode.AStore(2)),
                method.instructions.getFirst());
    }

    @MethodPatch("g()V")
    public void multModelviewMatrix(MethodNode method) {
        patch("移除意味不明的区块缩放（可能影响光追）", method,
                remove(ByteCode.ALoad(0)),
                remove(ByteCode.GetField("bxr", "k", "Ljava/nio/FloatBuffer;")),
                remove(ByteCode.InvokeStatic("bus", "a", "(Ljava/nio/FloatBuffer;)V")));
    }
}

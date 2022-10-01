package advancedshader.core.patcher.vanilla;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("bxm")
public class ChunkRenderDispatcherPatcher extends Patcher {

    @MethodPatch("a(Lamm;Lbuk;Lbxr;Lbxo;D)Lcom/google/common/util/concurrent/ListenableFuture;")
    public void uploadChunk(MethodNode method) {
        patch("设定spriteBounds值", method,
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeStatic(ANISOTROPICFILTER, "preUploadDisplayList", "(Lamm;)V")),
                ByteCode.ALoad(0),
                ByteCode.ALoad(2),
                ByteCode.ALoad(3),
                ByteCode.CheckCast("bxq"),
                ByteCode.ALoad(1),
                ByteCode.ALoad(4),
                ByteCode.InvokeVirtual("bxq", "a", "(Lamm;Lbxo;)I"),
                ByteCode.ALoad(3),
                ByteCode.InvokeSpecial("bxm", "a", "(Lbuk;ILbxr;)V"),
                inject(ByteCode.InvokeStatic(ANISOTROPICFILTER, "postUploadDisplayList", "()V")));
    }
}

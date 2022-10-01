package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.uniform.ShaderExpressionResolver")
public class ShaderExpressionResolverPatcher extends Patcher {

    @MethodPatch("registerExpressions()V")
    public void registerExpressions(MethodNode method) {
        patch("自定义Uniform变量增加额外属性", method,
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.InvokeStatic(CUSTOMUNIFORM, "registerExpressions", "(Lnet/optifine/shaders/uniform/ShaderExpressionResolver;)V")),
                ByteCode.Return());
    }
}

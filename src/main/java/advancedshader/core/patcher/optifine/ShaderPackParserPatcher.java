package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.config.ShaderPackParser")
public class ShaderPackParserPatcher extends Patcher {

    @MethodPatch("resolveIncludes(Ljava/io/BufferedReader;Ljava/lang/String;Lnet/optifine/shaders/IShaderPack;ILjava/util/List;I)Ljava/io/BufferedReader;")
    public void resolveIncludes(MethodNode method) {
        patch("替换着色器版本宏", method,
                ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderMacros", "getFixedMacroLines", "()Ljava/lang/String;"),
                inject(ByteCode.InvokeStatic(FORWARDFEATURES, "replaceShaderVersionMacro", "(Ljava/lang/String;)Ljava/lang/String;")));

//        patch("Core Profile 版本转换", method,
//                ByteCode.Label(),
//                ByteCode.LineNumber(),
//                inject(ByteCode.ALoad(12)),
//                inject(ByteCode.InvokeStatic(COREPROFILE, "convertVersion", "(Ljava/lang/String;)Ljava/lang/String;")),
//                inject(ByteCode.AStore(12)),
//                ByteCode.New("java/lang/StringBuilder"),
//                ByteCode.Dup(),
//                ByteCode.InvokeSpecial("java/lang/StringBuilder", "<init>", "()V"),
//                ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderMacros", "getFixedMacroLines", "()Ljava/lang/String;"));

//        LabelNode label = ByteCode.Label();
//
//        patch("Core Profile 兼容性转换", method,
//                inject(ByteCode.ILoad(9)),
//                inject(ByteCode.IfLessThanZero(label)),
//                inject(ByteCode.ALoad(12)),
//                inject(ByteCode.ALoad(10)),
//                inject(ByteCode.ALoad(1)),
//                inject(ByteCode.InvokeStatic(COREPROFILE, "convertCoreProfile", "(Ljava/lang/String;Ljava/util/Set;Ljava/lang/String;)Ljava/lang/String;")),
//                inject(ByteCode.AStore(12)),
//                inject(label),
//                ByteCode.ALoad(8),
//                ByteCode.ALoad(12),
//                ByteCode.InvokeVirtual("java/io/CharArrayWriter", "write", "(Ljava/lang/String;)V"),
//                ByteCode.Label(),
//                ByteCode.LineNumber(),
//                ByteCode.ALoad(8),
//                ByteCode.Ldc("\n"),
//                ByteCode.InvokeVirtual("java/io/CharArrayWriter", "write", "(Ljava/lang/String;)V"));
    }

    @MethodPatch("parseBlendStates(Ljava/util/Properties;)V")
    public void parseBlendStates(MethodNode method) {
        patch("增加blend.<program>.<buffer>配置", method,
                ByteCode.ALoad(3),
                ByteCode.Ldc("."),
                ByteCode.InvokeStatic("Config", "tokenize", "(Ljava/lang/String;Ljava/lang/String;)[Ljava/lang/String;"),
                ByteCode.AStore(4),
                inject(ByteCode.ALoad(4)),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.ALoad(3)),
                inject(ByteCode.InvokeVirtual("java/util/Properties", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;")),
                inject(ByteCode.InvokeStatic(BLEND, "parseBlendBuffer", "([Ljava/lang/String;Ljava/lang/String;)V")));
    }

    @MethodPatch("collectShaderOptions(Lnet/optifine/shaders/IShaderPack;Ljava/lang/String;[Ljava/lang/String;Ljava/util/Map;)V")
    public void collectShaderOptions(MethodNode method) {
        patch("解析几何着色器与计算着色器中的配置", method,
                ByteCode.New("java/lang/StringBuilder"),
                ByteCode.Dup(),
                ByteCode.InvokeSpecial("java/lang/StringBuilder", "<init>", "()V"),
                ByteCode.ALoad(1),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                ByteCode.Ldc("/"),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                ByteCode.ALoad(5),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                ByteCode.Ldc(".vsh"),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "toString", "()Ljava/lang/String;"),
                ByteCode.AStore(6),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.New("java/lang/StringBuilder"),
                ByteCode.Dup(),
                ByteCode.InvokeSpecial("java/lang/StringBuilder", "<init>", "()V"),
                ByteCode.ALoad(1),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                ByteCode.Ldc("/"),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                ByteCode.ALoad(5),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                ByteCode.Ldc(".fsh"),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
                ByteCode.InvokeVirtual("java/lang/StringBuilder", "toString", "()Ljava/lang/String;"),
                ByteCode.AStore(7),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(0),
                ByteCode.ALoad(6),
                ByteCode.ALoad(3),
                ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderPackParser", "collectShaderOptions", "(Lnet/optifine/shaders/IShaderPack;Ljava/lang/String;Ljava/util/Map;)V"),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                ByteCode.ALoad(0),
                ByteCode.ALoad(7),
                ByteCode.ALoad(3),
                ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderPackParser", "collectShaderOptions", "(Lnet/optifine/shaders/IShaderPack;Ljava/lang/String;Ljava/util/Map;)V"),
                inject(ByteCode.New("java/lang/StringBuilder")),
                inject(ByteCode.Dup()),
                inject(ByteCode.InvokeSpecial("java/lang/StringBuilder", "<init>", "()V")),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")),
                inject(ByteCode.Ldc("/")),
                inject(ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")),
                inject(ByteCode.ALoad(5)),
                inject(ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")),
                inject(ByteCode.Ldc(".gsh")),
                inject(ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")),
                inject(ByteCode.InvokeVirtual("java/lang/StringBuilder", "toString", "()Ljava/lang/String;")),
                inject(ByteCode.AStore(6)),
                inject(ByteCode.New("java/lang/StringBuilder")),
                inject(ByteCode.Dup()),
                inject(ByteCode.InvokeSpecial("java/lang/StringBuilder", "<init>", "()V")),
                inject(ByteCode.ALoad(1)),
                inject(ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")),
                inject(ByteCode.Ldc("/")),
                inject(ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")),
                inject(ByteCode.ALoad(5)),
                inject(ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")),
                inject(ByteCode.Ldc(".csh")),
                inject(ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")),
                inject(ByteCode.InvokeVirtual("java/lang/StringBuilder", "toString", "()Ljava/lang/String;")),
                inject(ByteCode.AStore(7)),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.ALoad(6)),
                inject(ByteCode.ALoad(3)),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderPackParser", "collectShaderOptions", "(Lnet/optifine/shaders/IShaderPack;Ljava/lang/String;Ljava/util/Map;)V")),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.ALoad(7)),
                inject(ByteCode.ALoad(3)),
                inject(ByteCode.InvokeStatic("net/optifine/shaders/config/ShaderPackParser", "collectShaderOptions", "(Lnet/optifine/shaders/IShaderPack;Ljava/lang/String;Ljava/util/Map;)V")));
    }
}

package advancedshader.core.patcher.optifine;

import org.objectweb.asm.tree.MethodNode;

import advancedshader.core.patcher.Patcher;
import advancedshader.core.patcher.Patcher.Patch;

@Patch("net.optifine.shaders.ShaderPackZip")
public class ShaderPackZipPatcher extends Patcher {

    @MethodPatch("getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;")
    public void getResourceAsStream(MethodNode method) {
        patch("修复光影压缩包无法加载语言问题", method,
                ByteCode.IfNotNull(null),
                ByteCode.Label(),
                ByteCode.LineNumber(),
                remove(ByteCode.AConstNull()),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetField("net/optifine/shaders/ShaderPackZip", "packZipFile", "Ljava/util/zip/ZipFile;")),
                inject(ByteCode.New("java/lang/StringBuilder")),
                inject(ByteCode.Dup()),
                inject(ByteCode.ALoad(0)),
                inject(ByteCode.GetField("net/optifine/shaders/ShaderPackZip", "baseFolder", "Ljava/lang/String;")),
                inject(ByteCode.InvokeSpecial("java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V")),
                inject(ByteCode.ALoad(2)),
                inject(ByteCode.InvokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")),
                inject(ByteCode.InvokeVirtual("java/lang/StringBuilder", "toString", "()Ljava/lang/String;")),
                inject(ByteCode.InvokeStatic(LANGFIX, "fixLanguage", "(Ljava/util/zip/ZipFile;Ljava/lang/String;)Ljava/io/InputStream;")),
                ByteCode.Label(),
                ByteCode.AReturn());
    }
}

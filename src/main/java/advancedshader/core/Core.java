package advancedshader.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

public class Core implements ITweaker {

    public static final File source;

    static {
        File file = null;
        try {
            URL url = Core.class.getProtectionDomain().getCodeSource().getLocation();
            URI uri = url.toURI();

            file = new File(uri);
        } catch (URISyntaxException e) {}

        source = file;
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, final File assetsDir, String profile) {}

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        if (FMLLaunchHandler.side() == Side.SERVER) {
            return;
        }

        try {
            Pattern pattern = Pattern.compile("^(advancedshader/core/patcher/.+Patcher)\\.class$");
            ZipFile mod = new ZipFile(source);
            Enumeration<? extends ZipEntry> entries = mod.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Matcher matcher = pattern.matcher(entry.getName());

                if (matcher.find()) {
                    System.out.println("加载修补程序：" + matcher.group(1).replace('/', '.'));
                    classLoader.registerTransformer(matcher.group(1).replace('/', '.'));
                }
            }

            mod.close();
        } catch (IOException | SecurityException | IllegalArgumentException e) {}

        FMLInjectionData.containers.add("advancedshader.Hook");
    }

    @Override
    public String getLaunchTarget() {
        return null;
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}

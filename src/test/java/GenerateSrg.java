import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GenerateSrg {

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream(new File("srg.txt"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

        Map<String, Item> items = Maps.newLinkedHashMap();
        Map<String, Item> items2 = Maps.newLinkedHashMap();
        String last = null;

        while (true) {
            String line = reader.readLine();

            if (line == null)
                break;

            if (line.startsWith("\t")) {
                Item item = items.get(last);
                String[] tokens = line.trim().split(" ");
                String k;
                String v;

                if (tokens.length == 3) {
                    k = tokens[0] + ' ' + tokens[1];
                    v = tokens[2];
                } else {
                    k = tokens[0];
                    v = tokens[1];
                }

                item.items.put(k, v);
            } else {
                Item item = new Item();
                String[] tokens = line.split(" ");
                item.target = tokens[1];
                items.put(tokens[0], item);
                last = tokens[0];
            }
        }

        reader.close();

        ZipFile zf = new ZipFile("dists/AdvancedShader-1.0-1.12.2.jar");

        zf.stream().forEach(file -> {
            if (file.getName().endsWith(".class")) {
                String name = file.getName().substring(0, file.getName().length() - 6);

                if (items.get(name) == null) {
                    Item item = new Item();
                    item.target = name;
                    items.put(name, item);
                }

                Item item = items.get(name);
                Item item2 = new Item();

                item2.target = item.target;

                items2.put(name, item2);

                try {
                    ClassReader cr = new ClassReader(zf.getInputStream(file));
                    ClassNode cn = new ClassNode();
                    cr.accept(cn, 0);

                    for (FieldNode f : cn.fields) {
                        if (f.name.contains("$"))
                            continue;
                        String n = f.name;

                        item2.items.put(n, item.items.getOrDefault(n, n));
                    }

                    for (MethodNode m : cn.methods) {
                        if (m.name.startsWith("<"))
                            continue;
                        if (m.name.contains("$"))
                            continue;
                        String n = m.name + ' ' + m.desc;

                        item2.items.put(n, item.items.getOrDefault(n, m.name));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });

        zf.close();

        FileOutputStream fos = new FileOutputStream(new File("srg.txt"));
        PrintStream writer = new PrintStream(fos, true, "UTF-8");

        List<String> keys = Lists.newArrayList();

        keys.addAll(items2.keySet());
        keys.sort(null);

        keys.forEach(k -> {
            writer.println(k + ' ' + items2.get(k).target);
            items2.get(k).items.forEach((kk, vv) -> {
                writer.println('\t' + kk + ' ' + vv);
            });
        });

        writer.close();
    }

    private static class Item {
        private Map<String, String> items = Maps.newLinkedHashMap();
        private String target;
    }
}

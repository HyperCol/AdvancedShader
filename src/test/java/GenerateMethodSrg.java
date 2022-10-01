import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


public class GenerateMethodSrg {

    public static void main(String[] args) throws Exception {
        
        FileInputStream fis = new FileInputStream(new File("mvsrg.txt"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        
        Map<String, Map<String, String>> items = Maps.newLinkedHashMap();
        Map<String, Map<String, String>> items2 = Maps.newLinkedHashMap();
        String last = null;
        
        while (true) {
            String line = reader.readLine();
            
            if (line == null)
                break;
            
            if (line.startsWith("\t")) {
                Map<String, String> map = items.get(last);
                String[] tokens = line.trim().split(" ");
                String k = tokens[0];
                String v = tokens[1];
                
                map.put(k, v);
            } else {
                items.put(line, Maps.newLinkedHashMap());
                last = line;
            }
        }
        
        reader.close();
        
        ZipFile zf = new ZipFile("dists/AdvancedShader-1.0-1.12.2.jar");
        
        zf.stream().forEach(file -> {
            if (file.getName().endsWith(".class")) {
                
                try {
                    ClassReader cr = new ClassReader(zf.getInputStream(file));
                    ClassNode cn = new ClassNode();
                    cr.accept(cn, 0);
                    
                    for (MethodNode m : cn.methods) {
                        if (m.name.contains("$")) continue;
                        if (m.localVariables == null) continue;
                        
                        Set<String> set = Sets.newLinkedHashSet();
                        
                        for (LocalVariableNode v : m.localVariables) {
                            if (!v.name.equals("this"))
                            set.add(v.name);
                        }
                        
                        if (set.size() == 0) continue;
                        
                        String name = cn.name + "." + m.name + m.desc;
                        
                        if (items.get(name) == null) {
                            items.put(name, Maps.newLinkedHashMap());
                        }
                        
                        items2.put(name, Maps.newLinkedHashMap());
                        
                        Map<String, String> map = items2.get(name); 
                        
                        set.stream().forEach(s -> map.put(s, items.get(name).getOrDefault(s, s)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });
        
        zf.close();

        FileOutputStream fos = new FileOutputStream(new File("mvsrg.txt"));
        PrintStream writer = new PrintStream(fos, true, "UTF-8");
        
        List<String> keys = Lists.newArrayList();
        
        keys.addAll(items2.keySet());
        keys.sort(null);

        keys.forEach(k -> {
            writer.println(k);
            items2.get(k).forEach((kk, vv) -> {
                writer.println('\t' + kk + ' ' + vv);
            });
        });
        
        writer.close();
    }
}

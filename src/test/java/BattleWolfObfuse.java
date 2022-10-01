import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class BattleWolfObfuse {

    public static void main(String[] args) throws IOException, InterruptedException {
        FileInputStream fis = new FileInputStream(new File("srg.txt"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        
        Map<String, Item> srg = Maps.newLinkedHashMap();
        Map<String, String> srgType = Maps.newLinkedHashMap();
        String last = null;
        
        srg.put("^(advancedshader/core/patcher/.+Patcher)\\.class$", new Item("^(光影前向兼容核心/修补程序集/.+修补程序)\\.class$"));
        srg.put("advancedshader.Hook", new Item("光影前向兼容"));
        
        while (true) {
            String line = reader.readLine();
            
            if (line == null)
                break;
            
            if (line.startsWith("\t")) {
                Item item = srg.get(last);
                String[] tokens = line.trim().split(" ");
                String k;
                String v;
                
                if (tokens.length == 3) {
                    k = tokens[0];
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
                srg.put(tokens[0], item);
                last = tokens[0];
                
                srgType.put("L" + tokens[0] + ";", "L" + tokens[1] + ";");
            }
        }
        
        System.out.println("Loaded srg.txt");
        
        fis.close();
        reader.close();
        
        fis = new FileInputStream(new File("mvsrg.txt"));
        reader = new BufferedReader(new InputStreamReader(fis));
        
        Map<String, Map<String, String>> mvsrg = Maps.newLinkedHashMap();
        
        while (true) {
            String line = reader.readLine();
            
            if (line == null)
                break;
            
            if (line.startsWith("\t")) {
                Map<String, String> map = mvsrg.get(last);
                String[] tokens = line.trim().split(" ");
                String k = tokens[0];
                String v = tokens[1];
                
                map.put(k, v);
            } else {
                mvsrg.put(line, Maps.newLinkedHashMap());
                last = line;
            }
        }
        
        System.out.println("Loaded mvsrg.txt");
        
        fis.close();
        reader.close();
        
        JarFile jar = new JarFile("dists/AdvancedShader-1.0-1.12.2.jar");
        JarOutputStream jos = new JarOutputStream(new FileOutputStream("temp.jar"));

        System.out.println("Remapping local variables & patch caller.");

        Enumeration<JarEntry> e = jar.entries();

        while (e.hasMoreElements()) {
            JarEntry entry = e.nextElement();
            
            if (entry.getName().endsWith(".class")) {
                
                ClassReader cr = new ClassReader(jar.getInputStream(entry));
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
                
                cn.sourceDebug = cn.sourceFile = null;
                
                Iterator<FieldNode> it = cn.fields.iterator();
                
                while (it.hasNext()) {
                    FieldNode f = it.next();
                    
                    if (f.value != null && (f.access & Opcodes.ACC_FINAL) != 0) {
                        System.out.println("Removed unused field: " + cn.name.replaceAll("/",".") + "."+ f.name);
                        it.remove();
                    }
                }
                                
                for (MethodNode method : cn.methods) {
                    String n = cn.name + "." + method.name + method.desc;
                    
                    Map<String, String> lvmap = mvsrg.get(n);
                    
                    if (lvmap != null && method.localVariables != null) {
                        for (LocalVariableNode lvn : method.localVariables) {
                            lvn.name = lvmap.getOrDefault(lvn.name, lvn.name);
                        }
                    }
                    
                    AbstractInsnNode node = method.instructions.getFirst();
                    
                    while (node != null) {
                        if (node instanceof LdcInsnNode) {
                            LdcInsnNode ldc = (LdcInsnNode) node;
                            
                            if (srg.containsKey(ldc.cst)) {
                                Item item = srg.get(ldc.cst);
                                
                                ldc.cst = item.target;
                                
                                if (ldc.getNext() instanceof LdcInsnNode) {
                                    ldc = (LdcInsnNode) node.getNext();
                                    
                                    if (item.items.containsKey(ldc.cst)) {
                                        ldc.cst = item.items.get(ldc.cst);
                                        
                                        node = ldc;
                                    }
                                }
                            } else {
                                if (ldc.cst instanceof String) {
                                    String str = (String) ldc.cst;
                                    
                                    for (String k : srgType.keySet()) {
                                        str = str.replace(k, srgType.get(k));
                                    }
                                    
                                    ldc.cst = str;
                                }
                            }
                        } else if (node instanceof LineNumberNode) {
                            AbstractInsnNode nnn = node;
                            node = node.getNext();
                            method.instructions.remove(nnn);
                            continue;
                        }
                        
                        node = node.getNext();
                    }
                }
                
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);
                
                jos.putNextEntry(new JarEntry(entry.getName()));
                jos.write(cw.toByteArray());
            } else if (entry.getName().equals("META-INF/MANIFEST.MF")) {
                BufferedReader br = new BufferedReader(new InputStreamReader(jar.getInputStream(entry)));
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                while (true) {
                    String line = br.readLine();
                    if (line == null) break;
                line = line.replaceAll("advancedshader.core.Core", "光影前向兼容核心.主程序") + "\n";
                bos.write(line.getBytes(StandardCharsets.UTF_8));
                }
                
                entry = new JarEntry(entry.getName());

                jos.putNextEntry(entry);
                jos.write(bos.toByteArray());
            } else {
                InputStream is = jar.getInputStream(entry);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                byte[] b = new byte[4096];

                while (true) {
                    int i = is.read(b);
                    if (i <= 0)
                        break;
                    bos.write(b, 0, i);
                }
                
                entry = new JarEntry(entry.getName());

                jos.putNextEntry(entry);
                jos.write(bos.toByteArray());
            }
        }
        
        jar.close();
        jos.close();

        Process p = new ProcessBuilder().command(ImmutableList.of("java", "-Dfile.encoding=UTF-8", "-jar", "SpecialSource-1.8.3-shaded.jar", "--in-jar", "temp.jar", "--out-jar", "tempsrg.jar", "--srg-in", "srg.txt", "--live")).redirectOutput(Redirect.INHERIT).start();

        while (p.isAlive())
            Thread.sleep(0L);

        jar = new JarFile("tempsrg.jar");
        jos = new JarOutputStream(new FileOutputStream("[光影前向兼容]AdvancedShader-1.0-FINAL-1.12.2.jar"));

        System.out.println("Restructuring...");
        
        e = jar.entries();

        while (e.hasMoreElements()) {
            JarEntry entry = e.nextElement();

            if (entry.isDirectory()) {
                continue;
            }

            InputStream is = jar.getInputStream(entry);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] b = new byte[4096];

            while (true) {
                int i = is.read(b);
                if (i <= 0)
                    break;
                bos.write(b, 0, i);
            }
            
            entry = new JarEntry(entry.getName());

            jos.putNextEntry(entry);
            jos.write(bos.toByteArray());
        }

        jar.close();
        jos.close();
        System.out.println("Done.");

    }
    
    private static class Item {
        private Map<String, String> items = Maps.newLinkedHashMap();
        private String target;
        
        public Item() {}
        public Item(String target) {this.target=target;}
    }
}

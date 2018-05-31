package club.bytecode.the.jda;

import club.bytecode.the.jda.api.ExceptionUI;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Loading and saving jars
 *
 * @author Konloch
 * @author WaterWolf
 */

public class JarUtils {
    public static FileContainer load(final File jarFile) throws IOException {
        FileContainer container = new FileContainer(jarFile);
        HashMap<String, byte[]> files = new HashMap<>();

        ZipInputStream jis = new ZipInputStream(new FileInputStream(jarFile));
        ZipEntry entry;
        while ((entry = jis.getNextEntry()) != null) {
            try {
                final String name = entry.getName();
                final byte[] bytes = getBytes(jis);
                if (!files.containsKey(name)) {
                    if (!name.endsWith(".class")) {
                        if (!entry.isDirectory())
                            files.put(name, bytes);
                    } else {
                        files.put(name, bytes);
                    }
                }
            } catch (Exception e) {
                new ExceptionUI(e);
            } finally {
                jis.closeEntry();
            }
        }
        jis.close();
        container.files = files;
        return container;
    }


    public static ArrayList<ClassNode> loadClasses(final File jarFile) throws IOException {
        ArrayList<ClassNode> classes = new ArrayList<>();
        ZipInputStream jis = new ZipInputStream(new FileInputStream(jarFile));
        ZipEntry entry;
        while ((entry = jis.getNextEntry()) != null) {
            try {
                final String name = entry.getName();
                if (name.endsWith(".class")) {
                    byte[] bytes = getBytes(jis);
                    String cafebabe = String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);
                    if (cafebabe.toLowerCase().equals("cafebabe")) {
                        try {
                            final ClassNode cn = getNode(bytes);
                            classes.add(cn);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println(jarFile + ">" + name + ": Header does not start with CAFEBABE, ignoring.");
                    }
                }

            } catch (Exception e) {
                new ExceptionUI(e);
            } finally {
                jis.closeEntry();
            }
        }
        jis.close();
        return classes;
    }

    /**
     * Loads resources only, just for .APK
     *
     * @param zipFile the input zip file
     * @throws IOException
     */
    public static HashMap<String, byte[]> loadResources(final File zipFile) throws IOException {
        if (!zipFile.exists())
            return null; //just ignore

        HashMap<String, byte[]> files = new HashMap<>();

        ZipInputStream jis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry;
        while ((entry = jis.getNextEntry()) != null) {
            try {
                final String name = entry.getName();
                if (!name.endsWith(".class")) {
                    if (!entry.isDirectory())
                        files.put(name, getBytes(jis));

                    jis.closeEntry();
                    continue;
                }
            } catch (Exception e) {
                new ExceptionUI(e);
            } finally {
                jis.closeEntry();
            }
        }
        jis.close();

        return files;

    }

    /**
     * Reads an InputStream and returns the read byte[]
     *
     * @param is the InputStream
     * @return the read byte[]
     * @throws IOException
     */
    public static byte[] getBytes(final InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int a = 0;
        while ((a = is.read(buffer)) != -1) {
            baos.write(buffer, 0, a);
        }
        baos.close();
        buffer = null;
        return baos.toByteArray();
    }

    /**
     * Creates a new ClassNode instances from the provided byte[]
     *
     * @param bytez the class file's byte[]
     * @return the ClassNode instance
     */
    public static ClassNode getNode(final byte[] bytez) {
        ClassReader cr = new ClassReader(bytez);
        ClassNode cn = new ClassNode();
        try {
            cr.accept(cn, ClassReader.EXPAND_FRAMES);
        } catch (Exception e) {
            try {
                cr.accept(cn, ClassReader.SKIP_FRAMES);
            } catch (Exception e2) {
                e2.printStackTrace(); //just skip it
            }
        }
        cr = null;
        return cn;
    }

    /**
     * Saves as jar with manifest
     *
     * @param nodeList the loaded ClassNodes
     * @param path     the exact path of the output jar file
     * @param manifest the manifest contents
     */
    public static void saveAsJar(ArrayList<ClassNode> nodeList, String path, String manifest) {
        try {
            JarOutputStream out = new JarOutputStream(new FileOutputStream(path));
            for (ClassNode cn : nodeList) {
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);

                out.putNextEntry(new ZipEntry(cn.name + ".class"));
                out.write(cw.toByteArray());
                out.closeEntry();
            }

            out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            out.write((manifest.trim() + "\r\n\r\n").getBytes());
            out.closeEntry();

            for (FileContainer container : JDA.getOpenFiles())
                for (Entry<String, byte[]> entry : container.files.entrySet()) {
                    String filename = entry.getKey();
                    if (!filename.startsWith("META-INF")) {
                        out.putNextEntry(new ZipEntry(filename));
                        out.write(entry.getValue());
                        out.closeEntry();
                    }
                }

            out.close();
        } catch (IOException e) {
            new ExceptionUI(e);
        }
    }

    /**
     * Saves a jar without the manifest
     *
     * @param nodeList The loaded ClassNodes
     * @param path     the exact jar output path
     */
    public static void saveAsJarClassesOnly(ArrayList<ClassNode> nodeList, String path) {
        try {
            JarOutputStream out = new JarOutputStream(new FileOutputStream(path));
            ArrayList<String> noDupe = new ArrayList<>();
            for (ClassNode cn : nodeList) {
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);

                String name = cn.name + ".class";

                if (!noDupe.contains(name)) {
                    noDupe.add(name);
                    out.putNextEntry(new ZipEntry(name));
                    out.write(cw.toByteArray());
                    out.closeEntry();
                }
            }

            noDupe.clear();
            out.close();
        } catch (IOException e) {
            new ExceptionUI(e);
        }
    }

    public static void saveAsJarClassesOnly(Map<String, byte[]> nodeList, String path) {
        try {
            JarOutputStream out = new JarOutputStream(new FileOutputStream(path));
            ArrayList<String> noDupe = new ArrayList<>();
            for (Entry<String, byte[]> cn : nodeList.entrySet()) {
                String name = cn.getKey();
                if (!noDupe.contains(name)) {
                    noDupe.add(name);
                    out.putNextEntry(new ZipEntry(name));
                    out.write(cn.getValue());
                    out.closeEntry();
                }
            }

            noDupe.clear();
            out.close();
        } catch (IOException e) {
            new ExceptionUI(e);
        }
    }

    public static void saveAsJar(Map<String, byte[]> nodeList, String path) {
        try {
            JarOutputStream out = new JarOutputStream(new FileOutputStream(path));
            ArrayList<String> noDupe = new ArrayList<>();
            for (Entry<String, byte[]> entry : nodeList.entrySet()) {
                String name = entry.getKey();
                if (!noDupe.contains(name)) {
                    noDupe.add(name);
                    out.putNextEntry(new ZipEntry(name));
                    out.write(entry.getValue());
                    out.closeEntry();
                }
            }

            for (FileContainer container : JDA.getOpenFiles())
                for (Entry<String, byte[]> entry : container.files.entrySet()) {
                    String filename = entry.getKey();
                    if (!filename.startsWith("META-INF")) {
                        if (!noDupe.contains(filename)) {
                            noDupe.add(filename);
                            out.putNextEntry(new ZipEntry(filename));
                            out.write(entry.getValue());
                            out.closeEntry();
                        }
                    }
                }

            noDupe.clear();
            out.close();
        } catch (IOException e) {
            new ExceptionUI(e);
        }
    }
}

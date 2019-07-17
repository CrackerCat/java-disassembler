package club.bytecode.the.jda;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a file container
 *
 * @author Konloch
 */

public class FileContainer {
    public FileContainer(File f) {
        this.file = f;
        this.name = f.getAbsolutePath();
    }

    public final File file;
    public final String name;

    public HashMap<String, byte[]> files = new HashMap<>(); // this is assigned outside the class?!

    public ClassNode loadClassFile(String filename) {
        byte[] bytes = files.get(filename);
        if (bytes == null)
            return null;
        ClassReader reader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        return classNode;
    }

    public String findClassfile(String className) {
        String candidate = className + ".class";
        if (name.endsWith(".class")) { // this is a single .class file. we need to strip the package path out.
            candidate = JDA.getClassName(candidate);
        }
        if (files.containsKey(candidate))
            return candidate;
        return "";
    }

    public Map<String, byte[]> getFiles() {
        return files;
    }

    @Override
    public String toString() {
        return name;
    }
}

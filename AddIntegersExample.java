// javac -cp /home/jan/.m2/repository/org/ow2/asm/asm/9.3/asm-9.3.jar AddIntegersExample.java
// java -cp .:/home/jan/.m2/repository/org/ow2/asm/asm/9.3/asm-9.3.jar AddIntegersExample

import org.objectweb.asm.*;

import java.lang.reflect.Method;

public class AddIntegersExample {
  public static void main(String[] args) throws Exception {
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    MethodVisitor mv;

    cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Adder", null, "java/lang/Object", null);

    mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "add", "(II)I", null, null);
    mv.visitCode();
    mv.visitVarInsn(Opcodes.ILOAD, 0);
    mv.visitVarInsn(Opcodes.ILOAD, 1);
    mv.visitInsn(Opcodes.IADD);
    mv.visitInsn(Opcodes.IRETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    cw.visitEnd();

    byte[] bytecode = cw.toByteArray();
    Class<?> adderClass = new CustomClassLoader().defineClass("Adder", bytecode);

    Method addMethod = adderClass.getMethod("add", int.class, int.class);
    int result = (int) addMethod.invoke(null, 5, 3);
    System.out.println("Result: " + result);
  }

  static class CustomClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] b) {
      return defineClass(name, b, 0, b.length);
    }
  }
}

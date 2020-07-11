package io.github.newhoo.mysql.transformer;

//import javassist.ClassPool;
//import javassist.CtClass;
//import javassist.CtMethod;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

//import java.io.ByteArrayInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;

/**
 * MySQLExplainTransformer
 *
 * @author huzunrong
 * @since 1.0
 */
public class MySQLExplainTransformer implements ClassFileTransformer {

    private static final String MYSQL_PREPARED_STATEMENT = "com/mysql/jdbc/PreparedStatement";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!MYSQL_PREPARED_STATEMENT.equals(className)) {
            return classfileBuffer;
        }

        byte[] asmVisit = asmVisit(classfileBuffer);
//        byte[] assistVisit = assistVisit(classfileBuffer);

        return asmVisit;
    }

//    private byte[] assistVisit(byte[] classfileBuffer) {
//        CtClass cl = null;
//        try {
//            ClassPool pool = ClassPool.getDefault();
//            cl = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
//
//            pool.importPackage("io.github.newhoo.mysql.explain.MySQLExplain");
//
//            CtMethod m = cl.getDeclaredMethod("executeInternal");
////            m.insertBefore("{ try { MySQLExplain.explainSql(getConnection(), asSql()); } catch (Exception e) {System.out.println(\"insertBefore Error: \"+e.toString()); } }");
//            m.insertBefore("{ MySQLExplain.explainSql(getConnection(), asSql()); }");
//
//            byte[] bytes = cl.toBytecode();
//            try {
//                System.out.println(2222222);
//                ///Users/zunrong/.m2/repository/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47-sources.jar!/com/mysql/jdbc/PreparedStatement.java
//                new FileOutputStream(
//                        "/Users/zunrong/OtherJavaProjects/plugin/mysql-explain/mysql-explain-agent/target/PreparedStatement1.class")
//                        .write(bytes);
//                System.out.println(33333333);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return bytes;
//        } catch (Exception e) {
//            System.err.println("Mysql explain agent error: " + e.toString());
//            e.printStackTrace();
//        } finally {
//            if (cl != null) {
//                cl.detach();// ClassPool默认不会回收，需要手动清理
//            }
//        }
//        return classfileBuffer;
//    }

    private byte[] asmVisit(byte[] classfileBuffer) {
        byte[] jsrRemoveByteCodes = classfileBuffer;

//        {
//            ClassReader cr = new ClassReader(classfileBuffer);
//            ClassWriter cw = new ClassWriter(cr, 0);
//            ClassVisitor cv = new ClassVisitor(ASM7, cw) {
//                @Override
//                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
//                    MethodVisitor mv = this.cv.visitMethod(access, name, descriptor, signature, exceptions);
//                    return new JSRInlinerAdapter(mv, access, name, descriptor, signature, exceptions);
//                }
//            };
//            cr.accept(cv, 0);
//            jsrRemoveByteCodes = cw.toByteArray();
//        }
        ClassReader cr = new ClassReader(jsrRemoveByteCodes);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!"executeInternal".equals(name)) {
                    return mv;
                }
                System.out.println("[mysql] visit method: " + name + "  ===  " + descriptor + "  ===  " + signature + "  ===  " + exceptions);

                return new AdviceAdapter(Opcodes.ASM5, mv, access, name, descriptor) {
                    @Override
                    protected void onMethodEnter() {
                        super.onMethodEnter();
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/mysql/jdbc/PreparedStatement", "getConnection", "()Ljava/sql/Connection;", false);
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/mysql/jdbc/PreparedStatement", "asSql", "()Ljava/lang/String;", false);
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "io/github/newhoo/mysql/explain/MySQLExplain", "explainSql", "(Ljava/lang/Object;Ljava/lang/String;)V", false);
                    }

//                    @Override
//                    protected void onMethodExit(int opcode) {
//                        mv.visitMaxs(11, 17);
//                        super.onMethodExit(opcode);
//                    }
                };
            }
        };

        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}
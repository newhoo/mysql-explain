package io.github.newhoo.mysql.transformer;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import static jdk.internal.org.objectweb.asm.ClassReader.EXPAND_FRAMES;

/**
 * MySQLExplainTransformer
 *
 * @author huzunrong
 * @since 1.0
 */
public class MySQL8ExplainTransformer implements ClassFileTransformer {

    private static final String MYSQL8_PREPARED_STATEMENT = "com/mysql/cj/jdbc/ClientPreparedStatement";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!MYSQL8_PREPARED_STATEMENT.equals(className)) {
            return classfileBuffer;
        }

        return asmVisitMysql8(classfileBuffer);
    }

    private byte[] asmVisitMysql8(byte[] classfileBuffer) {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!"executeInternal".equals(name)) {
                    return mv;
                }
                System.out.println("[mysql8] visit method: " + name + "  ===  " + descriptor + "  ===  " + signature + "  ===  " + exceptions);

                return new AdviceAdapter(Opcodes.ASM5, mv, access, name, descriptor) {
                    @Override
                    protected void onMethodEnter() {
                        super.onMethodEnter();

                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/mysql/cj/jdbc/ClientPreparedStatement", "getConnection", "()Ljava/sql/Connection;", false);
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/mysql/cj/jdbc/ClientPreparedStatement", "asSql", "()Ljava/lang/String;", false);
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "io/github/newhoo/mysql/explain/MySQLExplain", "explainSql", "(Ljava/lang/Object;Ljava/lang/String;)V", false);

                    }

                };
            }
        };

        cr.accept(cv, EXPAND_FRAMES);
        return cw.toByteArray();
    }
}
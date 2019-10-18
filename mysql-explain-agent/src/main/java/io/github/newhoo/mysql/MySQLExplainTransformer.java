package io.github.newhoo.mysql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * MySQLExplainTransformer
 *
 * @author huzunrong
 * @since 1.0
 */
public class MySQLExplainTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!"com/mysql/jdbc/PreparedStatement".equals(className)) {
            return classfileBuffer;
        }

        System.out.println("mysql explain agent starting...");

        CtClass cl = null;
        try {
            ClassPool pool = ClassPool.getDefault();
            cl = pool.makeClass(new ByteArrayInputStream(classfileBuffer));

            pool.importPackage("io.github.newhoo.mysql.explain.MySQLExplain");

            CtMethod m = cl.getDeclaredMethod("executeInternal");

            m.insertBefore("{ MySQLExplain.explainSql(this.connection, $2); }");

            return cl.toBytecode();
        } catch (Exception e) {
            System.err.println("mysql agent error: " + e.toString());
            e.printStackTrace();
        } finally {
            if (cl != null) {
                cl.detach();// ClassPool默认不会回收，需要手动清理
            }
        }
        return classfileBuffer;
    }
}
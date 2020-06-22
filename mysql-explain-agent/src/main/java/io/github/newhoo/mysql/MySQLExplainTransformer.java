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

    private static final String MYSQL_PREPARED_STATEMENT = "com/mysql/jdbc/PreparedStatement";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!MYSQL_PREPARED_STATEMENT.equals(className)) {
            return classfileBuffer;
        }
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("Mysql explain agent starting... " + contextClassLoader);

//        return asmVisit(classfileBuffer);

        CtClass cl = null;
        try {
            ClassPool pool = ClassPool.getDefault();
            cl = pool.makeClass(new ByteArrayInputStream(classfileBuffer));

            pool.importPackage("io.github.newhoo.mysql.explain.MySQLExplain");

            CtMethod m = cl.getDeclaredMethod("executeInternal");
            m.insertBefore(
                    "{ try { MySQLExplain.explainSql(getConnection(), asSql()); } catch (Exception e) {System.out.println(\"insertBefore Error: \"+e.toString()); } }");

            return cl.toBytecode();
        } catch (Exception e) {
            System.err.println("Mysql explain agent error: " + e.toString());
            e.printStackTrace();
        } finally {
            if (cl != null) {
                cl.detach();// ClassPool默认不会回收，需要手动清理
            }
        }
        return classfileBuffer;
    }
}
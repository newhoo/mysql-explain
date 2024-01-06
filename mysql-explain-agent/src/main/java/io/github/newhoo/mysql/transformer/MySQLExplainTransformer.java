package io.github.newhoo.mysql.transformer;

import io.github.newhoo.mysql.common.Log;
import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    private static final String MYSQL6_PREPARED_STATEMENT = "com/mysql/cj/jdbc/PreparedStatement";
    private static final String MYSQL8_PREPARED_STATEMENT = "com/mysql/cj/jdbc/ClientPreparedStatement";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (MYSQL_PREPARED_STATEMENT.equals(className)) {
            return assistVisit(classfileBuffer, false);
        }
        if (MYSQL6_PREPARED_STATEMENT.equals(className)) {
            return assistVisit(classfileBuffer, false);
        }
        if (MYSQL8_PREPARED_STATEMENT.equals(className)) {
            return assistVisit(classfileBuffer, true);
        }

        return classfileBuffer;
    }

    private byte[] assistVisit(byte[] classfileBuffer, boolean isMysql8) {
        try {
            ClassPool pool = ClassPool.getDefault(); // javassist.ClassPoolTail.appendSystemPath
            Log.info("classPool: %s, %s, %s", javassist.bytecode.ClassFile.MAJOR_VERSION, Thread.currentThread().getContextClassLoader(), pool.toString());
            try {
                return assistVisit(pool, classfileBuffer, isMysql8, false);
            } catch (CannotCompileException e) {
                Log.info("visit method compile error: %s", e.toString());
                ClassPath classPath = pool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
                Log.info("try appendClassPath: %s", classPath);
            }
            return assistVisit(pool, classfileBuffer, isMysql8, true);
        } catch (Exception e) {
            Log.error(e, "visit method error: %s", e.toString());
        }
        return classfileBuffer;
    }

    private byte[] assistVisit(ClassPool pool, byte[] classfileBuffer, boolean isMysql8, boolean appendClassPath) throws IOException, CannotCompileException, NotFoundException {
        CtClass cl = null;
        try {
            cl = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
            if (!appendClassPath) {
                pool.importPackage("io.github.newhoo.mysql.explain.MySQLExplain");
            }

            CtMethod m = cl.getDeclaredMethod("executeInternal");
            Log.info("visit method: %s", m.getLongName());
            if (isMysql8) {
                m.insertBefore("{ MySQLExplain.explainSql(getConnection(), toString()); }");
            } else {
                m.insertBefore("{ MySQLExplain.explainSql(getConnection(), asSql()); }");
            }
            return cl.toBytecode();
        } finally {
            if (cl != null) {
                cl.detach();// ClassPool默认不会回收，需要手动清理
            }
        }
    }
}
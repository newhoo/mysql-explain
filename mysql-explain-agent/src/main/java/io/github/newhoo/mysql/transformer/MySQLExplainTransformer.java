package io.github.newhoo.mysql.transformer;

import io.github.newhoo.mysql.common.Log;
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
        CtClass cl = null;
        try {
            ClassPool pool = ClassPool.getDefault();
            cl = pool.makeClass(new ByteArrayInputStream(classfileBuffer));

            pool.importPackage("io.github.newhoo.mysql.explain.MySQLExplain");

            CtMethod m = cl.getDeclaredMethod("executeInternal");
            if (isMysql8) {
                m.insertBefore("{ MySQLExplain.explainSql(getConnection(), toString()); }");
            } else {
                m.insertBefore("{ MySQLExplain.explainSql(getConnection(), asSql()); }");
            }
            Log.info("visit method: %s", m.getLongName());
            return cl.toBytecode();
        } catch (Exception e) {
            Log.error(e, "visit method error: %s", e.toString());
        } finally {
            if (cl != null) {
                cl.detach();// ClassPool默认不会回收，需要手动清理
            }
        }
        return classfileBuffer;
    }
}
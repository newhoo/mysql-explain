package io.github.newhoo.mysql;

import io.github.newhoo.mysql.common.Config;
import io.github.newhoo.mysql.transformer.MySQL8ExplainTransformer;
import io.github.newhoo.mysql.transformer.MySQLExplainTransformer;

import java.lang.instrument.Instrumentation;

/**
 * MySQLExplainInstrumentation
 *
 * @author huzunrong
 * @since 1.0
 */
public class MySQLExplainInstrumentation {

    /**
     * Java agent指定的premain方法，会在main方法之前被调用
     */
    public static void premain(String args, Instrumentation inst) {
        // print configuration
        Config.init();
        System.out.println("Mysql explain configurations: \n" + Config.getMySQLConfTable());

        // Instrumentation提供的addTransformer方法，在类加载时会回调ClassFileTransformer接口
        inst.addTransformer(new MySQLExplainTransformer());
        inst.addTransformer(new MySQL8ExplainTransformer());
    }
}
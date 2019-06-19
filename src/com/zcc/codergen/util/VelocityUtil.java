package com.zcc.codergen.util;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;

import java.io.StringWriter;
import java.util.Map;

/**
 * Velocity通用模板工具类
 */
public class VelocityUtil {

    private final static VelocityEngine velocityEngine;

    static {
        velocityEngine = new VelocityEngine();
        // Disable separate Velocity logging.
        velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
            NullLogChute.class.getName());
        velocityEngine.init();
    }

    public static String evaluate(String template, Map<String, Object> map) {
        // 创建一个上下文
        VelocityContext context = new VelocityContext();
        // 添加数据
        map.forEach(context::put);
        // 数据与模板合并，产生输出内容
        StringWriter writer = new StringWriter();
        velocityEngine.evaluate(context, writer, "", template);
        return writer.toString();
    }
}

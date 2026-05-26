package meteordevelopment.meteorclient.utils.stardust;

import meteordevelopment.meteorclient.MeteorClient;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/stardust/LogUtil.class */
public class LogUtil {
    public static void info(String msg) {
        MeteorClient.LOG.info("{} {}", MsgUtil.getRawPrefix(), msg);
    }

    public static void info(String msg, String module) {
        MeteorClient.LOG.info("{}{} {}", new Object[]{MsgUtil.getRawPrefix(), MsgUtil.getRawPrefix(module), msg});
    }

    public static void warn(String msg) {
        MeteorClient.LOG.warn("{} {}", MsgUtil.getRawPrefix(), msg);
    }

    public static void warn(String msg, String module) {
        MeteorClient.LOG.warn("{}{} {}", new Object[]{MsgUtil.getRawPrefix(), MsgUtil.getRawPrefix(module), msg});
    }

    public static void error(String msg) {
        MeteorClient.LOG.error("{} {}", MsgUtil.getRawPrefix(), msg);
    }

    public static void error(String msg, String module) {
        MeteorClient.LOG.error("{}{} {}", new Object[]{MsgUtil.getRawPrefix(), MsgUtil.getRawPrefix(module), msg});
    }

    public static void debug(String msg) {
        MeteorClient.LOG.debug("{} {}", MsgUtil.getRawPrefix(), msg);
    }

    public static void debug(String msg, String module) {
        MeteorClient.LOG.debug("{}{} {}", new Object[]{MsgUtil.getRawPrefix(), MsgUtil.getRawPrefix(module), msg});
    }
}

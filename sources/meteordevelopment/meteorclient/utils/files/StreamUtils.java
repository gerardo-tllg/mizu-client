package meteordevelopment.meteorclient.utils.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import meteordevelopment.meteorclient.MeteorClient;
import org.apache.commons.io.IOUtils;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/files/StreamUtils.class */
public class StreamUtils {
    private StreamUtils() {
    }

    public static void copy(File from, File to) {
        try {
            InputStream in = new FileInputStream(from);
            try {
                OutputStream out = new FileOutputStream(to);
                try {
                    in.transferTo(out);
                    out.close();
                    in.close();
                } catch (Throwable th) {
                    try {
                        out.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            } finally {
            }
        } catch (IOException e) {
            MeteorClient.LOG.error("Error copying from file '{}' to file '{}'.", new Object[]{from.getName(), to.getName(), e});
        }
    }

    public static void copy(InputStream in, File to) {
        try {
            try {
                OutputStream out = new FileOutputStream(to);
                try {
                    in.transferTo(out);
                    out.close();
                    IOUtils.closeQuietly(in);
                } catch (Throwable th) {
                    try {
                        out.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            } catch (IOException e) {
                MeteorClient.LOG.error("Error writing to file '{}'.", to.getName());
                IOUtils.closeQuietly(in);
            }
        } catch (Throwable th3) {
            IOUtils.closeQuietly(in);
            throw th3;
        }
    }
}

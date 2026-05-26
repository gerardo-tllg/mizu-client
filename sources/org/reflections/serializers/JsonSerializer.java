package org.reflections.serializers;

import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import org.reflections.Reflections;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/serializers/JsonSerializer.class */
public class JsonSerializer implements Serializer {
    @Override // org.reflections.serializers.Serializer
    public Reflections read(InputStream inputStream) {
        return (Reflections) new GsonBuilder().setPrettyPrinting().create().fromJson(new InputStreamReader(inputStream), Reflections.class);
    }

    @Override // org.reflections.serializers.Serializer
    public File save(Reflections reflections, String filename) {
        try {
            File file = Serializer.prepareFile(filename);
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(reflections);
            Files.write(file.toPath(), json.getBytes(Charset.defaultCharset()), new OpenOption[0]);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

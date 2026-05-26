package org.reflections.serializers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.Store;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:org/reflections/serializers/XmlSerializer.class */
public class XmlSerializer implements Serializer {
    @Override // org.reflections.serializers.Serializer
    public Reflections read(InputStream inputStream) {
        try {
            Document document = new SAXReader().read(inputStream);
            Map<String, Map<String, Set<String>>> storeMap = (Map) document.getRootElement().elements().stream().collect(Collectors.toMap((v0) -> {
                return v0.getName();
            }, index -> {
                return (Map) index.elements().stream().collect(Collectors.toMap(entry -> {
                    return entry.element("key").getText();
                }, entry2 -> {
                    return (Set) entry2.element("values").elements().stream().map((v0) -> {
                        return v0.getText();
                    }).collect(Collectors.toSet());
                }));
            }));
            return new Reflections(new Store(storeMap));
        } catch (Exception e) {
            throw new ReflectionsException("could not read.", e);
        }
    }

    @Override // org.reflections.serializers.Serializer
    public File save(Reflections reflections, String filename) {
        File file = Serializer.prepareFile(filename);
        try {
            FileOutputStream out = new FileOutputStream(file);
            Throwable th = null;
            try {
                try {
                    new XMLWriter(out, OutputFormat.createPrettyPrint()).write(createDocument(reflections.getStore()));
                    if (out != null) {
                        if (0 != 0) {
                            try {
                                out.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        } else {
                            out.close();
                        }
                    }
                    return file;
                } finally {
                }
            } finally {
            }
        } catch (Exception e) {
            throw new ReflectionsException("could not save to file " + filename, e);
        }
    }

    private Document createDocument(Store store) {
        Document document = DocumentFactory.getInstance().createDocument();
        Element root = document.addElement("Reflections");
        store.forEach((index, map) -> {
            Element indexElement = root.addElement(index);
            map.forEach((key, values) -> {
                Element entryElement = indexElement.addElement("entry");
                entryElement.addElement("key").setText(key);
                Element valuesElement = entryElement.addElement("values");
                values.forEach(value -> {
                    valuesElement.addElement("value").setText(value);
                });
            });
        });
        return document;
    }
}

package io.github.in_toto.transporters;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

import io.github.in_toto.exceptions.TransporterException;
import io.github.in_toto.models.Metablock;
import io.github.in_toto.models.Signable;

public final class FileTransporter<S extends Signable> implements Transporter<S> {
    private String directoryPath;
    
    public FileTransporter() {}
    
    public FileTransporter(String dir) {
        this.directoryPath = dir;
    }
    
    @Override
    public void dump(Metablock<S> metablock) {

        String jsonString = metablock.jsonEncodeCanonical();

        try (FileWriter writer = new FileWriter(Paths.get(directoryPath, metablock.getFullName()).toString())) {
            writer.write(jsonString);
            writer.flush();
        } catch (IOException e) {
            throw new TransporterException("Couldn't write file: " + e.toString());
        }
    }

    @Override
    public <K extends Signable> Metablock<K> load(String uri, Type type) {
        String jsonString = null;
        try {
            jsonString = new String ( Files.readAllBytes( Paths.get(uri) ) );
        }
        catch (IOException e) {
            throw new TransporterException("Couldn't read file: " + e.toString());
        }
        Gson gson = new Gson();
        return gson.fromJson(jsonString, type);
    }
}

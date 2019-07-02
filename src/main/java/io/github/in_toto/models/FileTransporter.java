package io.github.in_toto.models;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

public final class FileTransporter implements Transporter {
	
	public FileTransporter() {	
	}
	
	@Override
	public <S extends Signable> void dump(String id, Metablock<S> metablock) {

        FileWriter writer = null;
        String jsonString = metablock.toJson();

        try {
            writer = new FileWriter(id);
            writer.write(jsonString);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't write file: " + e.toString());
        }
	}

	@Override
	public <S extends Signable> Metablock<S> load(String uri, Type type) {
		String jsonString = null;
		try {
			jsonString = new String ( Files.readAllBytes( Paths.get(uri) ) );
	    }
	    catch (IOException e) {
	    	throw new RuntimeException("Couldn't read file: " + e.toString());
	    }
		Gson gson = new Gson();
	    Metablock<S> metablock = gson.fromJson(jsonString, type);
	    return metablock;
	}
}

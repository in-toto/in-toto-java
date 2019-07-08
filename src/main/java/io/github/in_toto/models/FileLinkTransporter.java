package io.github.in_toto.models;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public final class FileLinkTransporter implements LinkTransporter {
	private String directoryPath;
	

	
	public FileLinkTransporter() {}
	
	public FileLinkTransporter(String dir) {
		this.directoryPath = dir;
	}
	
	@Override
	public void dump(Metablock<Link> metablock) {

        FileWriter writer = null;
        String jsonString = metablock.toJson();

        try {
            writer = new FileWriter(Paths.get(directoryPath, metablock.getFullName()).toString());
            writer.write(jsonString);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't write file: " + e.toString());
        }
	}

	@Override
	public Metablock<Link> load(String uri) {
		String jsonString = null;
		try {
			jsonString = new String ( Files.readAllBytes( Paths.get(uri) ) );
	    }
	    catch (IOException e) {
	    	throw new RuntimeException("Couldn't read file: " + e.toString());
	    }
		Type metablockType = new TypeToken<Metablock<Link>>() {}.getType();
		Gson gson = new Gson();
	    Metablock<Link> metablock = gson.fromJson(jsonString, metablockType);
	    return metablock;
	}
}

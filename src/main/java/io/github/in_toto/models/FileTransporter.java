package io.github.in_toto.models;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class FileTransporter implements Transporter {
	private String id;
	
	public FileTransporter() {
		
	}
	
	public FileTransporter(String path) {
		this.setId(path);
	}

	@Override
	public void dump(String jsonString) {

        FileWriter writer = null;

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
	public String load() {
		String jsonString = null;
		try {
			jsonString = new String ( Files.readAllBytes( Paths.get(id) ) );
	    }
	    catch (IOException e) {
	    	throw new RuntimeException("Couldn't read file: " + e.toString());
	    }
		return jsonString; 
	}

	public String getId() {
		return this.id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

}

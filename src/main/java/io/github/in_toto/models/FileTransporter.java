package io.github.in_toto.models;

import java.io.FileWriter;
import java.io.IOException;

public class FileTransporter implements Transporter {
	private String id;
	
	public FileTransporter() {
		
	}
	
	public FileTransporter(String path) {
		this.setId(path);
	}

	@Override
	public void dump(String jsonString) {

        FileWriter writer = null;

        try{
            writer = new FileWriter(id);
            writer.write(jsonString);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't serialize object: " + e.toString());
        }
		
	}

	@Override
	public String load() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getId() {
		return this.id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

}

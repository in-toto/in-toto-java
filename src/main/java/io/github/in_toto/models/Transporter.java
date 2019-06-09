package io.github.in_toto.models;

public interface Transporter {
	
	public void setId(String id);
	
	/**
     * Dumps the current string.
     *
     * @param string to write.
     */
	public void dump(String jsonString);
	
	/**
	 * 
	 */
	public String load();

}

package de.maiksch.myblockchain;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;

import de.maiksch.myblockchain.jsonobjects.Transaktion;

public class Block {

  private int index;
  private String vorheriger_hash;
  private List<Transaktion> transaktionen;
  private long timeStamp; // Millisekunden seit 01.01.1970
  private int proof;

  /**
   * 
   * @param index
   * @param vorheriger_hash
   * @param transaktionen
   * @param proof
   */
  public Block(int index, String vorheriger_hash, List<Transaktion> transaktionen, int proof) {
    super();
    this.index = index;
    this.vorheriger_hash = vorheriger_hash;
    this.transaktionen = transaktionen;
    this.proof = proof;
    this.timeStamp = System.currentTimeMillis();
  }
  
  public int getIndex() {
    return index;
  }

  public String getVorgaengerHash() {
    return vorheriger_hash;
  }

  public List<Transaktion> getTransaktionen() {
    return transaktionen;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public int getProof() {
    return proof;
  }

  /**
   * Generiert, nachdem die gesammte Klasse zu einem JSON-Objekt umgewandelt wurde, einen SHA-256 Hash
   * 
   * @return
   * @throws UnsupportedEncodingException 
   */
  public String createHash() throws UnsupportedEncodingException {
    Gson gson = new Gson();
    String json = gson.toJson(this);
    
    final String sha256hex = DigestUtils.sha256Hex(json);
    return sha256hex;
  }

  @Override
  public String toString() {
    return "Block [\n\t index=" + index + ",\n\t vorheriger_hash=" + vorheriger_hash + ",\n\t transaktionen=" + transaktionen
        + ",\n\t timeStamp=" + timeStamp + ",\n\t proof=" + proof + "\n]";
  }
  

}

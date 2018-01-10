package de.maiksch.myblockchain;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import de.maiksch.myblockchain.jsonobjects.Transaktion;

public class Blockchain {

  private ArrayList<Transaktion> aktuelle_transaktionen;
  private ArrayList<Block> blockchain;
  private ArrayList<URL> nodes;
  private Block aktueller_block;
  private String node_identifier = UUID.randomUUID().toString().replace("-", "");

  /**
   * Konstruktor
   */
  public Blockchain() {
    super();
    // Listen initialisieren
    this.blockchain = new ArrayList<Block>();
    this.nodes = new ArrayList<URL>();

    // Genesis Block erstellen und der Blockchain hinzufügen
    this.aktueller_block = new Block(0, "1", new ArrayList<Transaktion>(), 100);
    this.blockchain.add(aktueller_block);
  }

  /**
   * Parse die URL des Nodes und füge sie falls valide der Liste der
   * benachbarten Nodes hinzu
   * 
   * @param url
   *          URL Adresse des hinzuzufügenden Nodes
   * @return
   */
  public Response registerNode(String url) {

    URL parsed_url = null;

    try {

      parsed_url = new URL(url);

    } catch (MalformedURLException e) {

      e.printStackTrace();
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();

    }

    // Füge den Node nur hinzu, falls er noch nicht vorhanden ist
    if (!this.nodes.contains(parsed_url)) {
      this.nodes.add(parsed_url);
    }

    return Response.ok(new Gson().toJson(this.nodes)).build();
  }

  /**
   * Prüft ob die übergebene Blockchain valide ist. Eine Blockchain ist dann
   * valide, wenn der in jedem Block gespeicherte Vorgängerhash dem
   * tatsächlichen Hash des Vorgängers entspricht.
   * 
   * @param chain
   *          Eine Blockchain die überprüft werden soll
   * @return True falls valide, False falls nicht
   * @throws UnsupportedEncodingException
   */
  public boolean isChainValid(ArrayList<Block> chain) throws UnsupportedEncodingException {
    Block letzter_block = chain.get(0);

    Block aktueller_block;
    for (int index = 1; index < chain.size(); index++) {
      aktueller_block = chain.get(index);

      // Prüfen, ob der Hash des Vorgängerblocks mit dem gespeicherten Hash
      // übereinstimmt
      if (aktueller_block.getVorgaengerHash() != letzter_block.createHash()) {
        return false;
      }

      // Prüfen ob der Proof of Work korrekt ist
      if (!isValidProof(letzter_block.getProof(), aktueller_block.getProof())) {
        return false;
      }

      letzter_block = chain.get(index);
    }

    return true;
  }

  /**
   * 
   * @return
   */
  public Response consensus() {

    try {

      ArrayList<Block> neue_chain = resolveConflicts();
      if (neue_chain != null) {
        this.blockchain = neue_chain;
      }

    } catch (UnsupportedEncodingException e) {

      e.printStackTrace();
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();

    }

    return Response.ok().build();

  }

  /**
   * 
   * @return Gibt neue Blockchain zurück, falls eine längere existiert die
   *         valide ist oder null ansonsten
   * @throws UnsupportedEncodingException
   */
  private ArrayList<Block> resolveConflicts() throws UnsupportedEncodingException {
    ArrayList<Block> neue_chain = null;
    int max_size = this.blockchain.size();

    // Loop Variablen
    Client client = Client.create();
    WebResource web_resource = null;
    ClientResponse response = null;
    ArrayList<Block> nachbar_chain = null;

    for (URL url : this.nodes) {

      try {

        System.out.println("Versuche zu " + url + "/chain zu connecten");
        web_resource = client.resource(url.toURI() + "/chain");

      } catch (URISyntaxException e) {

        System.out.println(url + "/chain konnte nicht erreicht werden");
        continue;

      }

      response = web_resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

      // Wenn beim Aufruf der REST API des Nachbarns ein Fehler auftritt, mache
      // beim nächsten Client weiter
      if (response.getStatus() != 200) {
        continue;
      }

      // Hole die JSON Response und konvertiere diese in eine ArrayList
      String json = response.getEntity(String.class);
      Type listType = new TypeToken<ArrayList<Block>>() {
      }.getType();
      nachbar_chain = new Gson().fromJson(json, listType);

      // Prüfe ob die gefundene Blockchain länger ist als die eigene und
      // speichere sie falls ja
      System.out.println("Länge andere Kette: " + nachbar_chain.size());
      if (isChainValid(nachbar_chain) && nachbar_chain.size() > max_size) {
        neue_chain = nachbar_chain;
        max_size = nachbar_chain.size();

        System.out.println("Neue Chain von " + url + "/chain: ");
        System.out.println(nachbar_chain);
      }

    }

    return neue_chain;

  }

  /**
   * 
   * @return
   */
  public Response neuenBlockMinen() {
    // Zunächst den Proof of Work Algorithmus laufen lassen um neuen Proof zu
    // berechnen
    int letzter_proof = this.aktueller_block.getProof();
    int proof = proofOfWork(letzter_proof);

    // Erstelle eine neue Transaktion in Höhe von 1 als Belohnung für das Finden
    // des Proofs
    neueTransaktion(new Transaktion("0", this.node_identifier, 1));

    // Erstelle den neuen Block und füge diesen der Blockchain hinzu
    String letzter_hash = "";

    try {
      letzter_hash = this.aktueller_block.createHash();
    } catch (UnsupportedEncodingException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    Block neuer_block = new Block(this.aktueller_block.getIndex() + 1, letzter_hash, this.aktuelle_transaktionen,
        proof);
    this.blockchain.add(neuer_block);
    this.aktueller_block = neuer_block;
    this.aktuelle_transaktionen = null;

    Gson gson = new Gson();
    String json = gson.toJson(neuer_block);
    return Response.ok(json, MediaType.APPLICATION_JSON).build();
  }

  /**
   * Füge der Liste der aktuellen, noch unbestätigten Transaktionen eine weitere
   * hinzu
   * 
   * @param sender
   * @param empfaenger
   * @param betrag
   */
  public Response neueTransaktion(Transaktion transaktion) {

    if (transaktion.isValid()) {

      if (this.aktuelle_transaktionen == null) {

        this.aktuelle_transaktionen = new ArrayList<Transaktion>();

      }

      this.aktuelle_transaktionen.add(transaktion);

      Gson gson = new Gson();
      String json = gson.toJson(this.aktuelle_transaktionen);
      return Response.ok(json, MediaType.APPLICATION_JSON).build();

    } else {

      return Response.status(Status.BAD_REQUEST).build();

    }

  }

  /**
   * Liefert die komplette Blockchain in Form eines JSON-Objekts zurück
   * 
   * @return
   */
  public Response getBlockchain() {
    Gson gson = new Gson();
    String json = gson.toJson(this.blockchain);

    return Response.ok(json, MediaType.APPLICATION_JSON).build();
  }

  /**
   * Berechnet einen neuen Proof, mit dem ein neuer Block generiert werden kann.
   * 
   * Sucht dabei eine Zahl die angefügt an den vorhergegangenen Proof einen
   * SHA-256 Hash erzeugt, der 4 vorangehende Nullen besitzt.
   * 
   * @param letzter_proof
   * @return
   */
  private int proofOfWork(int vorgaenger_proof) {
    int proof = 0;
    String hash = "";

    System.out.println("Suche nach Proof gestartet");
    long start_zeit = System.currentTimeMillis();
    while (!isValidProof(vorgaenger_proof, proof)) {
      proof++;
      System.out.println(proof);
    }
    long end_zeit = System.currentTimeMillis();
    long temp = end_zeit - start_zeit;
    String differenz = String.format("%d min, %d sec, %d ms", TimeUnit.MILLISECONDS.toMinutes(temp),
        TimeUnit.MILLISECONDS.toSeconds(temp) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(temp)),
        TimeUnit.MILLISECONDS.toMillis(temp) - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(temp))
            - TimeUnit.MINUTES.toMillis(TimeUnit.MILLISECONDS.toMinutes(temp)));
    System.out.println("Proof gefunden nach " + differenz + "! Es ist " + proof);

    return proof;
  }

  private boolean isValidProof(int vorgaenger_proof, int aktueller_proof) {

    if (DigestUtils.sha256Hex(String.valueOf(vorgaenger_proof) + String.valueOf(aktueller_proof)).startsWith("0000")) {
      return true;
    }

    return false;
  }

}

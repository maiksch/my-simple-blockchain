package de.maiksch.myblockchain;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.codehaus.jackson.annotate.JsonProperty;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

import de.maiksch.myblockchain.jsonobjects.NewNode;
import de.maiksch.myblockchain.jsonobjects.Transaktion;

@Path("")
public class Node {

  private static final Blockchain BLOCKCHAIN = new Blockchain();
  
  public static void main(String[] args) {

    String port = "8080";
//    Options options = new Options();
//    options.addOption("port", false, "Port auf dem der Node laufen soll. Default 8080.");
//    
//    CommandLineParser parser = new DefaultParser();
//    CommandLine cmd = null;
//    try {
//      cmd = parser.parse( options, args);
//    } catch (ParseException e1) {
//      e1.printStackTrace();
//    }
//    
//    if(cmd.hasOption("port")) {
//      port = cmd.getOptionValue("port");
//    } 
    
    HttpServer server;
    try {
      
      server = HttpServerFactory.create( "http://localhost:" + port + "/" );
      server.start();
      
    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {   
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  
  @GET
  @Path("mine")
  @Produces(MediaType.TEXT_PLAIN)
  public Response neuenBlockMinen() {
    return BLOCKCHAIN.neuenBlockMinen();
  }
  
  @POST
  @Path("transactions/new")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response neueTransaktion(Transaktion transaktion) {
    return BLOCKCHAIN.neueTransaktion(transaktion);
  }
  
  @GET
  @Path("chain")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getBlockchain() {
    return BLOCKCHAIN.getBlockchain();
  }
  
  @POST
  @Path("nodes/register")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response registerNode(NewNode node) {
    return BLOCKCHAIN.registerNode(node.getUrl());
  }
  
  @GET
  @Path("nodes/resolve")
  @Produces(MediaType.APPLICATION_JSON)
  public Response resolve() {
    return BLOCKCHAIN.consensus();
  }

}

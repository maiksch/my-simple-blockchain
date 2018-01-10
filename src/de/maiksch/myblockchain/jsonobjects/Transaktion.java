package de.maiksch.myblockchain.jsonobjects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Transaktion {
  
  private String sender;
  private String empfaenger;
  private double betrag;

  public Transaktion() {
    super();
  }
  
  public Transaktion(String sender, String empfaenger, double betrag) {
    super();
    this.sender = sender;
    this.empfaenger = empfaenger;
    this.betrag = betrag;
  }

  public boolean isValid() {
    if( sender == null || sender.trim().isEmpty() || empfaenger == null || empfaenger.trim().isEmpty() || betrag == 0 ) {
      return false;
    }
     
    return true;
  }

  @XmlElement
  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  @XmlElement
  public String getEmpfaenger() {
    return empfaenger;
  }

  public void setEmpfaenger(String empfaenger) {
    this.empfaenger = empfaenger;
  }

  @XmlElement
  public double getBetrag() {
    return betrag;
  }

  public void setBetrag(double betrag) {
    this.betrag = betrag;
  }

  @Override
  public String toString() {
    return "Transaktion [sender=" + sender + ", empfaenger=" + empfaenger + ", betrag=" + betrag + "]";
  }
  
  
  
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Lucas
 */
public class Message {
    
    private byte[] byteArray;
    private int byteArrayLength = 0;
    
    private final byte startByte = (byte) 0x78;
    private final byte sessionIdByte;
    private byte messageCodeByte;
    private byte totalPayloadLengthByte;
    private byte[] payloadBytes;
    
    public static final int numHeaderBytes = 4;
    public static final byte replyMessageCode = (byte) 0x72;
    private final String connectionMessagePayload = "Hello";
    private final String confirmationMessagePayload = "Ok";
    private final String yesMessagePayload = "Yes";
    private final String noMessagePayload = "No";
    private final String errorMessagePayload = "Error";
    private final String keepAliveMessagePayload = "KA";
    private final String notSupportedMessagePayload = "Not Suppported";
    
    public Message(int sessionID) {
        byte[] byteArrayInt = this.intToByteArray(sessionID);
        this.sessionIdByte = (byte) byteArrayInt[byteArrayInt.length - 1];
    }
    
    public Message(byte sessionIdByte, byte messageCodeByte, byte totalPayloadLengthByte, byte[] payloadBytes) {
        this.sessionIdByte = sessionIdByte;
        this.messageCodeByte = messageCodeByte;
        this.totalPayloadLengthByte = totalPayloadLengthByte;
        this.payloadBytes = payloadBytes;
        
        this.byteArrayLength = Message.numHeaderBytes + this.payloadBytes.length;
        
        this.packMessage();
    }
    
    public byte[] getByteArray() {
        return this.byteArray;
    }
    
    public byte[] getPayloadBytes() {
        return this.payloadBytes;
    }
    
    public void makeConnectMessage() {
        this.makeMessageWithPayload(connectionMessagePayload);
        this.packMessage();
    }
    
    public void makeConfirmationMessage() {
        this.makeMessageWithPayload(confirmationMessagePayload);
        this.packMessage();
    }
    
    public void makeErrorMessage() {
        this.makeMessageWithPayload(errorMessagePayload);
        this.packMessage();
    }
    
    public void makeYesMessage() {
        this.makeMessageWithPayload(yesMessagePayload);
        this.packMessage();
    }
    
    public void makeNoMessage() {
        this.makeMessageWithPayload(noMessagePayload);
        this.packMessage();
    }
    
    public void makeTimeMessage() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String sCertDate = dateFormat.format(new Date());
        
        this.makeMessageWithPayload(sCertDate);
        this.packMessage();
    }
    
    public void makeKeepAliveMessage() {
        this.makeMessageWithPayload(keepAliveMessagePayload);
        this.packMessage();
    }
    
    public void makeNotSupportedMessage() {
        this.makeMessageWithPayload(notSupportedMessagePayload);
        this.packMessage();
    }
    
    public void makeMessageWithPayload(String payload) {
        
        this.messageCodeByte = this.replyMessageCode;
        this.payloadBytes = payload.getBytes();
        
        this.byteArrayLength = Message.numHeaderBytes + this.payloadBytes.length;
        
        byte[] byteArrayInt = this.intToByteArray(byteArrayLength);
        this.totalPayloadLengthByte = (byte) byteArrayInt[byteArrayInt.length - 1];

        this.packMessage();
    }
    
    public byte getMessageCodeByte() {
        return this.messageCodeByte;
    }
    
    private void packMessage() {
        this.byteArray = new byte[byteArrayLength];
        this.byteArray[0] = this.startByte;
        this.byteArray[1] = this.sessionIdByte;
        this.byteArray[2] = this.messageCodeByte;
        this.byteArray[3] = this.totalPayloadLengthByte;
        
        for (int i = 0; i < this.payloadBytes.length; i++) {
            byteArray[i + numHeaderBytes] = this.payloadBytes[i];
        }
    }
    
    public final byte[] intToByteArray(int value) {
        return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)value};
    }
}

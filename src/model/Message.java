/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author Lucas
 */
public class Message {
    
    private byte[] byteArray;
    private int byteArrayLength = 0;
    
    private final byte startByte = (byte) 0x78;
    private byte sessionIdByte;
    private byte messageCodeByte;
    private byte totalPayloadLengthByte;
    private byte[] payloadBytes;
    
    public static final int numHeaderBytes = 4;
    private final byte replyMessageCode = (byte) 0x72;
    private final String connectionMessagePayload = "Hello";
    
    public Message(int sessionID) {
        byte[] byteArrayInt = this.intToByteArray(sessionID);
        this.sessionIdByte = (byte) byteArrayInt[byteArrayInt.length - 1];
    }
    
    public Message(byte sessionIdByte, byte messageCodeByte, byte totalPayloadLengthByte, byte[] payloadBytes) {
        this.sessionIdByte = sessionIdByte;
        this.messageCodeByte = messageCodeByte;
        this.totalPayloadLengthByte = totalPayloadLengthByte;
        this.payloadBytes = payloadBytes;
        
        this.byteArrayLength = this.numHeaderBytes + this.payloadBytes.length;
        
        this.packMessage();
    }
    
    public byte[] getByteArray() {
        return this.byteArray;
    }
    
    public void makeConnectMessage() {
        
        this.messageCodeByte = this.replyMessageCode;
        this.payloadBytes = connectionMessagePayload.getBytes();
        
        this.byteArrayLength = this.numHeaderBytes + this.payloadBytes.length;
        
        byte[] byteArrayInt = this.intToByteArray(byteArrayLength);
        this.totalPayloadLengthByte = (byte) byteArrayInt[byteArrayInt.length - 1];

        this.packMessage();
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

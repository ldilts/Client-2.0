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
    private byte senderID;
    private byte[] payloadBytes;
    
    private boolean senderIDIsSet = false;
    
    public static final int numHeaderBytes = 4;
    public static final byte clientReplyMessageCode = (byte) 0x72; // r
    public static final byte keepAliveMessageCode = (byte) 0x4B; // K
    public static final byte serverReplyMessageCode = (byte) 0x52; // R
    public static final byte serverCrossCommandMessageCode = (byte) 0x48; // H
    public static final byte sendTextMessageCode = (byte) 0xF9;
    
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
        // Only used when a message is received from the stream
        this.sessionIdByte = sessionIdByte;
        this.messageCodeByte = messageCodeByte;
        this.totalPayloadLengthByte = totalPayloadLengthByte;
        this.payloadBytes = payloadBytes;
        
        this.byteArrayLength = Message.numHeaderBytes + this.payloadBytes.length;
        
        this.packReceiveMessage();
    }
    
    public Message(byte sessionIdByte, byte messageCodeByte, byte totalPayloadLengthByte, byte senderID, byte[] payloadBytes) {
        // Only used when a message is received from the stream
        this.sessionIdByte = sessionIdByte;
        this.messageCodeByte = messageCodeByte;
        this.totalPayloadLengthByte = totalPayloadLengthByte;
        this.senderID = senderID;
        this.payloadBytes = payloadBytes;
        
        this.byteArrayLength = Message.numHeaderBytes + this.payloadBytes.length;
        
        this.senderIDIsSet = true;
        this.packReceiveMessage();
    }
    
    public byte[] getByteArray() {
        return this.byteArray;
    }
    
    public byte[] getPayloadBytes() {
        return this.payloadBytes;
    }
    
    public byte getMessageCodeByte() {
        return this.messageCodeByte;
    }
    
    public byte getSenderIDByte() {
        return this.senderID;
    }
    
    public void makeConnectMessage() {
        this.makeMessageWithPayloadAndCommand(connectionMessagePayload, Message.clientReplyMessageCode);
        this.packSendMessage();
    }
    
    public void makeConfirmationMessage(boolean toServer, byte senderID) {
        if (toServer) {
            this.makeMessageWithPayloadAndCommand(confirmationMessagePayload, Message.clientReplyMessageCode);
        } else {
            this.makeMessageWithPayloadAndCommand(senderID, confirmationMessagePayload, Message.clientReplyMessageCode);
        }
        
        this.packSendMessage();
    }
    
    public void makeErrorMessage(boolean toServer, byte senderID) {
        if (toServer) {
            this.makeMessageWithPayloadAndCommand(errorMessagePayload, Message.clientReplyMessageCode);
        } else {
            this.makeMessageWithPayloadAndCommand(senderID, errorMessagePayload, Message.clientReplyMessageCode);
        }

        this.packSendMessage();
    }
    
    public void makeYesMessage() {
        this.makeMessageWithPayloadAndCommand(yesMessagePayload, Message.clientReplyMessageCode);
        this.packSendMessage();
    }
    
    public void makeNoMessage() {
        this.makeMessageWithPayloadAndCommand(noMessagePayload, Message.clientReplyMessageCode);
        this.packSendMessage();
    }
    
    public void makeTimeMessage(boolean toServer, byte senderID) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String sCertDate = dateFormat.format(new Date());
        
        if (toServer) {
            this.makeMessageWithPayloadAndCommand(sCertDate, Message.clientReplyMessageCode);
        } else {
            this.makeMessageWithPayloadAndCommand(senderID, sCertDate, Message.clientReplyMessageCode);
        }
        
        this.packSendMessage();
    }
    
    public void makeKeepAliveMessage() {
        this.makeMessageWithPayloadAndCommand(keepAliveMessagePayload, Message.keepAliveMessageCode);
        this.packSendMessage();
    }
    
    public void makeNotSupportedMessage(boolean toServer, byte senderID) {
        if (toServer) {
//            this.makeMessageWithPayloadAndCommand(sCertDate, Message.clientReplyMessageCode);
            this.makeMessageWithPayloadAndCommand(notSupportedMessagePayload, Message.clientReplyMessageCode);
        } else {
//            this.makeMessageWithPayloadAndCommand(senderID, sCertDate, Message.clientReplyMessageCode);
            this.makeMessageWithPayloadAndCommand(senderID, notSupportedMessagePayload, Message.clientReplyMessageCode);
        }
        
        this.packSendMessage();
    }
    
    public void makeMessageWithPayloadAndCommand(String payload, byte messageCodeByte) {
        
        this.messageCodeByte = messageCodeByte;
        this.payloadBytes = payload.getBytes();
        
        this.byteArrayLength = Message.numHeaderBytes + this.payloadBytes.length;
        
        byte[] byteArrayInt = this.intToByteArray(byteArrayLength);
        this.totalPayloadLengthByte = (byte) byteArrayInt[byteArrayInt.length - 1];

        this.packSendMessage();
    }
    
    public void makeMessageWithPayloadAndCommand(byte destinationID, String payload, byte messageCodeByte) {
        
        this.messageCodeByte = messageCodeByte;
        this.senderID = destinationID;
        this.payloadBytes = payload.getBytes();
        
        this.byteArrayLength = Message.numHeaderBytes + this.payloadBytes.length + 2;
        
        byte[] byteArrayInt = this.intToByteArray(byteArrayLength);
        this.totalPayloadLengthByte = (byte) byteArrayInt[byteArrayInt.length - 1];

        this.senderIDIsSet = true;
        this.packSendMessage();
    }
    
    private void packSendMessage() {
        this.byteArray = new byte[byteArrayLength];
        this.byteArray[0] = this.startByte;
        this.byteArray[1] = this.sessionIdByte;
        this.byteArray[2] = this.messageCodeByte;
        this.byteArray[3] = this.totalPayloadLengthByte;
        
        if (this.senderIDIsSet) {
            this.byteArray[4] = this.senderID;
            this.byteArray[5] = this.senderID;
            
            for (int i = 0; i < this.payloadBytes.length; i++) {
                byteArray[i + numHeaderBytes + 2] = this.payloadBytes[i];
            }
        } else {
            for (int i = 0; i < this.payloadBytes.length; i++) {
                byteArray[i + numHeaderBytes] = this.payloadBytes[i];
            }
        }   
    }
    
    private void packReceiveMessage() {
        this.byteArray = new byte[byteArrayLength];
        this.byteArray[0] = this.startByte;
        this.byteArray[1] = this.sessionIdByte;
        this.byteArray[2] = this.messageCodeByte;
        this.byteArray[3] = this.totalPayloadLengthByte;
        
        if (this.senderIDIsSet) {
            this.byteArray[4] = this.senderID;
            
            for (int i = 0; i < this.payloadBytes.length; i++) {
                byteArray[i + numHeaderBytes + 1] = this.payloadBytes[i];
            }
        } else {
            for (int i = 0; i < this.payloadBytes.length; i++) {
                byteArray[i + numHeaderBytes] = this.payloadBytes[i];
            }
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

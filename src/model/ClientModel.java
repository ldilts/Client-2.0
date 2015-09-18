/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import controller.ClientController;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lucas
 */
public class ClientModel implements Runnable {
    

    private int sessionID = 0;
    
    private static int keepAliveCount = 0;
    public Thread thread = null;
    private Semaphore mutex = new Semaphore(1);
    
    public ClientModel(int sessionID) {
        this.sessionID = sessionID;
    }
    
    public int getKeepAliveCount() {
        return this.keepAliveCount;
    }
    
    public void setKeepAliveCount(int num) {
        keepAliveCount = num;
    }
    
    public boolean getIsThreadAlive() {
        if (this.thread != null) {
            return this.thread.isAlive();
        } else {
            return false;
        }
        
    }
    
    public void incrementKeepAliveCount() {
        try {
            mutex.acquire();
            try {
                if (keepAliveCount < 3) {
                    this.keepAliveCount += 1;
//                    System.out.println("Count++: " + this.keepAliveCount);
                }
            } finally {
                mutex.release();
            }
        } catch(InterruptedException ie) {
            // ...
        }
    }
    
    @Override
    public void run() {
        this.keepAliveCount = 2;
        System.out.println("Starting new countdown thread.");
        while(this.thread != null) {
            try {
                this.mutex.acquire();
                try {
                    if (this.keepAliveCount < 0) {
                        // Kill this thread
                        this.thread = null;
                        this.keepAliveCount = 0;
                    } else {
                        // Decrement KA Counter
                        this.keepAliveCount -= 1;
//                        System.out.println("Count--: " + this.keepAliveCount);
                    }
                } finally {
                    this.mutex.release();
                    this.thread.sleep(3500);
                }
            } catch(InterruptedException ie) {
                    // Ignore
            }
       }
        System.out.println("Game Over!");
    }
    
    public void makeThread() {
        System.out.println("New Thread!");
        this.keepAliveCount = 1;
        this.thread = new Thread(this);
        this.thread.start();
    }
    
    public void messageToBeSent(int command) {
        switch (command) {
            case 0:
                break;
            default:
                break;
        }
    }
    
    public Message makeResponseToMessage(Message message, boolean fromServer, boolean success) {
        // The Deafult response is an OK message
        Message response;
        
        if (fromServer) {
            response = this.makeConfirmationReplyMessage(fromServer, (byte) 0x00);
        } else {
            response = this.makeConfirmationReplyMessage(fromServer, message.getSenderIDByte());
        }
        
        
        switch (message.getMessageCodeByte()) {
            case (byte) 0x4B:
                // Keep Alive -> Return Keep Alive
                Message keepAliveMessage = new Message(this.sessionID);
                keepAliveMessage.makeKeepAliveMessage();
                
                response = keepAliveMessage;
                break;
            case (byte) 0xF1:
                // Red on -> Retrun Confirmation
                break;
            case (byte) 0xF2:
                // Red off -> Retrun Confirmation
                break;
            case (byte) 0xF3:
                // Green on -> Retrun Confirmation
                break;
            case (byte) 0xF4:
                // Green off -> Retrun Confirmation
                break;
            case (byte) 0xF5:
                // Blue on -> Retrun Confirmation
                break;
            case (byte) 0xF6:
                // Blue off -> Retrun Confirmation
                break;
            case (byte) 0xF8:
                // Return date and time
                Message timeMessage = new Message(this.sessionID);
                if (fromServer) {
                    timeMessage.makeTimeMessage(fromServer, (byte) 0x00);
                } else {
                    timeMessage.makeTimeMessage(fromServer, message.getSenderIDByte());
                }
                
                response = timeMessage;
                break;
            case (byte) 0xF9:
                // Display message from server -> Retrun Confirmation  
                if (!success) {
                    if (fromServer) {
                        response = this.makeErrorReplyMessage(fromServer, (byte) 0x00);
                    } else {
                        response = this.makeErrorReplyMessage(fromServer, message.getSenderIDByte());
                    }
                }
                break;
            default:
                // Return Command not supported
                Message notSupportedMessage = new Message(this.sessionID);
                
                if (fromServer) {
                    notSupportedMessage.makeNotSupportedMessage(fromServer, (byte) 0x00);
                } else {
                    notSupportedMessage.makeNotSupportedMessage(fromServer, message.getSenderIDByte());
                }
                
                response = notSupportedMessage;
                break;
        }
        
        if (message.getMessageCodeByte() != (byte) 0x4B) {
            Message deleteMe = message;
        }
        
        return response;
    }
    
    private Message makeConfirmationReplyMessage(boolean toServer, byte senderID) {
        Message confirmationMessage = new Message(this.sessionID);
        confirmationMessage.makeConfirmationMessage(toServer, senderID);

//        this.sendMessage(confirmationMessage);
        return confirmationMessage;
    }
    
    private Message makeErrorReplyMessage(boolean fromServer, byte senderID) {
        Message errorMessage = new Message(this.sessionID);
        
        if (fromServer) {
            errorMessage.makeErrorMessage(fromServer, (byte) 0x00);
        } else {
            errorMessage.makeErrorMessage(fromServer, senderID);
        }
        
                
//        this.sendMessage(errorMessage);
        return errorMessage;
    }
}

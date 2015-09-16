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
    
    private int keepAliveCount = 0;
    private Thread thread;
    private Semaphore mutex = new Semaphore(1);
    
    public ClientModel(int sessionID) {
        this.sessionID = sessionID;
    }
    
    public int getKeepAliveCount() {
        return this.keepAliveCount;
    }
    
    @Override
    public void run() {
        while(true) {
            try {
                this.mutex.acquire();
                try {
                    if (this.keepAliveCount < 0) {
                        // Kill this thread
                        this.thread.interrupt();
                    }
                    
                    // Decrement KA Counter
                    this.keepAliveCount -= 1;
                } finally {
                    this.mutex.release();
                    this.thread.sleep(3680);
                }
            } catch(InterruptedException ie) {
                    // Ignore
            }
       }
    }
    
    public void messageToBeSent(int command) {
        switch (command) {
            case 0:
                break;
            default:
                break;
        }
    }
    
    public Message makeResponseToMessage(Message message, boolean success) {
        // The Deafult response is an OK message
        Message response = this.makeConfirmationReplyMessage();
        
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
                timeMessage.makeTimeMessage();
                
                response = timeMessage;
                break;
            case (byte) 0xF9:
                // Display message from server -> Retrun Confirmation  
                if (!success) {
                    response = this.makeErrorReplyMessage();
                }
                break;
            default:
                // Return Command not supported
                Message notSupportedMessage = new Message(this.sessionID);
                notSupportedMessage.makeNotSupportedMessage();
                
                response = notSupportedMessage;
                break;
        }
        
        return response;
    }
    
    private Message makeConfirmationReplyMessage() {
        Message confirmationMessage = new Message(this.sessionID);
        confirmationMessage.makeConfirmationMessage();
                
//        this.sendMessage(confirmationMessage);
        return confirmationMessage;
    }
    
    private Message makeErrorReplyMessage() {
        Message errorMessage = new Message(this.sessionID);
        errorMessage.makeErrorMessage();
                
//        this.sendMessage(errorMessage);
        return errorMessage;
    }
}

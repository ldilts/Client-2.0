/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.net.Socket;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import view.ClientView;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.ClientModel;
import model.Message;
import view.ClientView;

/**
 *
 * @author Lucas
 */
public class ClientController implements Runnable {
    
    private final ClientView theView;
    private final ClientModel theModel;
    
    private Socket clientSocket;
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    
    private boolean socketConnected = false;
    private boolean outputConnected = false;
    private boolean inputConnected = false;
    
    private Thread inputThread;

    private int sessionID = 0;
    private static final String SERVER_ADDRESS = "200.19.188.1";
    private static final int TCP_SERVER_PORT = 20200;
//    private static final String SERVER_ADDRESS = "localhost";
//    private static final int TCP_SERVER_PORT = 9999;
	
    public ClientController(ClientView theView, ClientModel theModel, int sessionID) {
            this.theView = theView;
            this.theModel = theModel;
            this.sessionID = sessionID;
            
            this.theView.setClientTitle("Client " + this.sessionID);

            this.theView.addClientListener(new ClientListener());
    }
    
    public void startInputObserving() {
        this.inputThread = new Thread(this);
        this.inputThread.start();
    }
    
    public void run() {
        while(true) {
            System.out.println("Try to start again");
            this.connectSocket();
            

            while (inputConnected) {
                if (this.theModel.getKeepAliveCount() >= 0) {
                    try {  
                        int length = this.dataInput.available();
                        if (length > 0) {
                            byte startByte = dataInput.readByte();
                            if (startByte == (byte) 0x78) {
                                 byte sessionIdByte = dataInput.readByte();
                                 byte messageCodeByte = dataInput.readByte();
                                 byte totalPayloadLengthByte = dataInput.readByte();

                                 
                                if (messageCodeByte == Message.clientReplyMessageCode) {
                                    // From another Client

                                    byte senderIDByte = dataInput.readByte();

                                    byte[] payloadBytes = new byte[totalPayloadLengthByte - Message.numHeaderBytes - 1];

                                    for (int i = 0; i < totalPayloadLengthByte - Message.numHeaderBytes - 1; i++) {
                                        payloadBytes[i] = dataInput.readByte();
                                    }

                                    Message message = new Message(sessionIdByte, 
                                            messageCodeByte, 
                                            totalPayloadLengthByte,
                                            senderIDByte,
                                            payloadBytes);

                                        this.messageReceived(message, false);
                                } else {
                                    // From Server

                                    byte[] payloadBytes = new byte[totalPayloadLengthByte - Message.numHeaderBytes];

                                    for (int i = 0; i < totalPayloadLengthByte - Message.numHeaderBytes; i++) {
                                        payloadBytes[i] = dataInput.readByte();
                                    }

                                    Message message = new Message(sessionIdByte, 
                                            messageCodeByte, 
                                            totalPayloadLengthByte, 
                                            payloadBytes);

                                    this.messageReceived(message, true);
                                }

//                                 else {
//                                     // Message not supported
//                                     System.out.println("Received a un-supported message");
//
//                                     // TODO remove remaining bytes from stream
//                                 }
                             }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    // Disconnect Sockets and Data Streams
                    this.disconnectSocket();
                }          
            }
            
            this.theView.setOutput("Disconnected from Server\n");
            System.out.println("Disconnected from Server\n");
            try {
                this.inputThread.sleep(2000);//2 seconds
            }
            catch(InterruptedException ie){
                ie.printStackTrace();
            }
        }
    }
    
    private void messageReceived(Message message, boolean fromServer) {
        boolean success = true;
        switch (message.getMessageCodeByte()) {
            case (byte) 0x4B:
                // Return Keep Alive
                this.theModel.incrementKeepAliveCount();
                
                if (this.outputConnected) {
                    if (this.theModel.thread == null) {
//                        if (!this.theModel.getIsThreadAlive()) {
                            this.theModel.makeThread();
//                        }
                    }
                }
                
                break;
            case (byte) 0xF1:
                // Do Red on -> Retrun Confirmation
                this.theView.setRedOn();
                break;
            case (byte) 0xF2:
                // Do Red off -> Retrun Confirmation
                this.theView.setRedOff();
                break;
            case (byte) 0xF3:
                // Do Green on -> Retrun Confirmation
                this.theView.setGreenOn();
                break;
            case (byte) 0xF4:
                // Do Green off -> Retrun Confirmation
                this.theView.setGreenOff();
                break;
            case (byte) 0xF5:
                // Do Blue on -> Retrun Confirmation
                this.theView.setBlueOn();
                break;
            case (byte) 0xF6:
                // Do Blue off -> Retrun Confirmation
                this.theView.setBlueOff();
                break;
            case (byte) 0xF8:
                // Return date and time
                break;
            case (byte) 0xF9:
                // Do Display message from server -> Retrun Confirmation                
                try {
                    String decodedMessage = new String(message.getPayloadBytes(), "UTF-8"); 
                    this.theView.setOutput(decodedMessage);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
                    success = false;
                    break;
                }
                
                success = true;
                break;
            default:
                // Return Command not supported
                break;
        }
        
        // send response
        this.sendMessage(this.theModel.makeResponseToMessage(message, fromServer, success));
    }
    
//    private void messageToBeSent(int command) {
//        switch (command) {
//            case 0:
//                break;
//            default:
//                break;
//        }
//    }
    
//    private void sendConfirmationReply() {
//        Message confirmationMessage = new Message(this.sessionID);
//        confirmationMessage.makeConfirmationMessage();
//                
//        this.sendMessage(confirmationMessage);
//    }
//    
//    private void sendErrorReply() {
//        Message errorMessage = new Message(this.sessionID);
//        errorMessage.makeErrorMessage();
//                
//        this.sendMessage(errorMessage);
//    }
        
    private void connectSocket() {
        this.socketConnected = false;
        this.outputConnected = false;
        this.inputConnected = false;
        
        this.theView.setOutput("Connecting to Server...\n");
        System.out.println("Connecting to Server...\n");
        
        while (!socketConnected) {
            try {
                clientSocket = new Socket(SERVER_ADDRESS, TCP_SERVER_PORT);
                
                socketConnected = true;
            } catch (Exception ex) {
                /* ignore */
                try {
                    this.inputThread.sleep(2000);//2 seconds
                }
                catch(InterruptedException ie){
                    ie.printStackTrace();
                }
            }
        }
        
        while (!inputConnected) {
            try {
                dataInput = new DataInputStream(clientSocket.getInputStream());
                
                inputConnected = true;
            } catch (IOException ex) {
                /* ignore */
//                Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        while (!outputConnected) {
            try {
                dataOutput = new DataOutputStream(clientSocket.getOutputStream());
                
                Message connectMessage = new Message(this.sessionID);
                connectMessage.makeConnectMessage();
                
                this.sendMessage(connectMessage);
                
                outputConnected = true;
            } catch (IOException ex) {
                /* ignore */
//                Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.theView.setOutput("Connected to Server\n");
        System.out.println("Connected to Server\n");
    }
    
    private void disconnectSocket() {
        while(inputConnected) {
            try {
                this.dataInput.close();
                this.inputConnected = false;
                System.out.println("Closed DataInput");
            } catch (IOException ex) {
                /* ignore */
//                Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        while (outputConnected) {
            try {
                this.dataOutput.close();
                this.outputConnected = false;
                System.out.println("Closed DataOutput");
            } catch (IOException ex) {
                /* ignore */
    //            Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        while (socketConnected) {
            try {
                this.clientSocket.close();
                this.socketConnected = false;
                System.out.println("Closed Socket");
            } catch (IOException ex) {
                /* ignore */
    //            Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        this.theView.setOutput("Disconnected from Server\n");
        System.out.println("Disconnected from Server\n");
    }
    
    private void sendMessage(Message message) {
        try {
            // write the message
            dataOutput.write(message.getByteArray());
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static boolean hostAvailabilityCheck() { 
        try (Socket s = new Socket(SERVER_ADDRESS, TCP_SERVER_PORT)) {
            return true;
        } catch (IOException ex) {
            /* ignore */
        }
        return false;
    }
    
    class ClientListener implements ActionListener{

        public void actionPerformed(ActionEvent e) {
            if (hostAvailabilityCheck() && clientSocket.isConnected()) {
                
                String destinationID = theView.getSecondInput();
                
                try {
                    if (theView.isSecondInputEmpty() || (0 <= Integer.parseInt(destinationID) && Integer.parseInt(destinationID) <= 255)) {
                        // Valid ID
                        
                        byte messageCodeByte;
                        boolean forServer = true;
                        byte destinationByte = 0x25;
                        
                        if (theView.isSecondInputEmpty()) {
                            // Send to server
                            messageCodeByte = Message.serverReplyMessageCode;
                            forServer = true;
                        } else {
                            // Send to a specific tag
                            destinationByte = (byte) Integer.parseInt(destinationID);
                            messageCodeByte = Message.serverCrossCommandMessageCode;
                            forServer = false;
                        }
                        
//                        byte[] destinationIdBytes = destinationID.getBytes();
//                        byte destinationByte = destinationIdBytes[destinationIdBytes.length - 1];
                        
                        
                        switch (theView.getComboBoxIndex()) {
                            case 0:
                                // Send input text 
                                Message userInputMessage = new Message((byte) sessionID);
                                
                                if (forServer) {
                                    userInputMessage.makeMessageWithPayloadAndCommand(theView.getInput(), messageCodeByte);
                                } else {
                                    userInputMessage.makeMessageWithPayloadAndCommand(destinationByte, theView.getInput(), messageCodeByte, Message.sendTextMessageCode);
                                }
                                
                                sendMessage(userInputMessage);
                                break;
                            case 1:
                                // Send YES
                                Message yesMessage = new Message((byte) sessionID);
                                yesMessage.makeYesMessage();
                                sendMessage(yesMessage);
                                break;
                            case 2:
                                // Send NO
                                Message noMessage = new Message((byte) sessionID);
                                noMessage.makeNoMessage();
                                sendMessage(noMessage);
                                break;
                            default:
                                // Nothing
                                break;
                        }
                    } else {
                        // Invalid ID
                        System.out.println("Invalid Destination ID");
                        theView.setOutput("Invalid Destination ID");
                    }
                    
                } catch (NumberFormatException ex) {
                    System.out.println("Invalid Destination ID Format");
                    theView.setOutput("Invalid Destination ID Format");
                }
                

                
                
                
            } else {
                theView.setOutput("Server Offline :(\n");
                System.out.println("Server Offline :(\n");
                
                disconnectSocket();
            }
        }	
    }
}

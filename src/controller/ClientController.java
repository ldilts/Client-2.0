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
//    private static final String SERVER_ADDRESS = "200.19.188.1";
//    private static final int TCP_SERVER_PORT = 20200;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int TCP_SERVER_PORT = 9999;
	
    public ClientController(ClientView theView, ClientModel theModel, int sessionID) {
            this.theView = theView;
            this.theModel = theModel;
            this.sessionID = sessionID;

            this.theView.addClientListener(new ClientListener());
    }
    
    public void startInputObserving() {
        this.inputThread = new Thread(this);
        this.inputThread.start();
    }
    
    public void run() {
        while(true) {
            this.connectSocket();

            while (socketConnected && inputConnected) {
                try {  
                    int length = this.dataInput.available();
                    if (length > 0) {
                        byte startByte = dataInput.readByte();
                           if (startByte == (byte) 0x78) {
                                byte sessionIdByte = dataInput.readByte();
                                byte messageCodeByte = dataInput.readByte();
                                byte totalPayloadLengthByte = dataInput.readByte();
                                byte[] payloadBytes = new byte[totalPayloadLengthByte - Message.numHeaderBytes];

                                for (int i = 0; i < totalPayloadLengthByte - Message.numHeaderBytes; i++) {
                                    payloadBytes[i] = dataInput.readByte();
                                }
                                
                                Message message = new Message(sessionIdByte, messageCodeByte, totalPayloadLengthByte, payloadBytes);
                                this.messageReceived(message);
                            }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            this.theView.setOutput("Disconnected from Server\n");
            System.out.println("Disconnected from Server\n");
        }
    }
    
    private void messageReceived(Message message) {
        switch (message.getMessageCodeByte()) {
            case (byte) 0x4B:
                // Keep Alive -> Return Keep Alive
                Message keepAliveMessage = new Message(this.sessionID);
                keepAliveMessage.makeKeepAliveMessage();
                
                this.sendMessage(keepAliveMessage);
                break;
            case (byte) 0xF1:
                // Red on -> Retrun Confirmation
                this.theView.setRedOn();

                this.sendConfirmationReply();
                break;
            case (byte) 0xF2:
                // Red off -> Retrun Confirmation
                this.theView.setRedOff();
                this.sendConfirmationReply();
                break;
            case (byte) 0xF3:
                // Green on -> Retrun Confirmation
                this.theView.setGreenOn();
                this.sendConfirmationReply();
                break;
            case (byte) 0xF4:
                // Green off -> Retrun Confirmation
                this.theView.setGreenOff();
                this.sendConfirmationReply();
                break;
            case (byte) 0xF5:
                // Blue on -> Retrun Confirmation
                this.theView.setBlueOn();
                this.sendConfirmationReply();
                break;
            case (byte) 0xF6:
                // Blue off -> Retrun Confirmation
                this.theView.setBlueOff();
                this.sendConfirmationReply();
                break;
            case (byte) 0xF8:
                // Return date and time
                
                break;
            case (byte) 0xF9:
                // Display message from server -> Retrun Confirmation

                this.sendConfirmationReply();
                break;
            default:
                // Return Command not supported
                
                break;
        }
    }
    
    private void sendConfirmationReply() {
        Message confirmationMessage = new Message(this.sessionID);
        confirmationMessage.makeConfirmationMessage();
                
        this.sendMessage(confirmationMessage);
    }
        
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
    
    private void sendMessage(Message message) {
        try {
            // write the message
            dataOutput.write(message.getByteArray());
        } catch (IOException ex) {
            Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    class ClientListener implements ActionListener{

        public void actionPerformed(ActionEvent e) {

            int firstNumber, secondNumber = 0;

            // Surround interactions with the view with
            // a try block in case numbers weren't
            // properly entered

            try{

//                        firstNumber = theView.getFirstNumber();
//                        secondNumber = theView.getSecondNumber();
//
//                        theModel.addTwoNumbers(firstNumber, secondNumber);
//
//                        theView.setCalcSolution(theModel.getCalculationValue());

            }

            catch(NumberFormatException ex){

                    System.out.println(ex);

//                        theView.displayErrorMessage("You Need to Enter 2 Integers");

            }
        }	
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import controller.ClientController;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author GabrielHabib
 */
public class MessageThreadPeriodic implements Runnable {
     
     private int timeToSend ;
     private byte idIHaveToSend ;
     
     public MessageThreadPeriodic(int timeToSend, byte idIHaveToSend) {
       // store parameter for later user
         this.timeToSend = timeToSend;
         this.idIHaveToSend = idIHaveToSend;
   }

    @Override
    public void run() {
                 com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean)
                  java.lang.management.ManagementFactory.getOperatingSystemMXBean();
                  long physicalMemorySize ;
                  String msg;
        while(true){
      
             // If the client isnotconnected hold it. KILL ??
            while(!controller.ClientController.inputConnected)
            while(!controller.ClientController.outputConnected)
            while(!controller.ClientController.socketConnected)
             
            //controller.ClientController.getDataOutput();
             try {
                    physicalMemorySize = os.getFreePhysicalMemorySize();
                    msg = Long.toString(physicalMemorySize);
                    // MUTEX DOWN
                    // write the message
                    controller.ClientController.getDataOutput().write((byte) 0x78); // start byte
                    controller.ClientController.getDataOutput().write((byte)ClientModel.getSessionID()); // myId
                    controller.ClientController.getDataOutput().write(Message.serverCrossCommandMessageCode); // cmd H
                    controller.ClientController.getDataOutput().write((byte)(6 + msg.length())); // PayloadLengh
                    controller.ClientController.getDataOutput().write(idIHaveToSend); // id Destino
                    controller.ClientController.getDataOutput().write((byte) 'r'); 
                   
                    
                     
                    for(int i = 0 ; i < msg.length() ; i ++ ){
                        controller.ClientController.getDataOutput().write((byte)(msg.toCharArray()[i])); // payload
                    }
                    
//                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  56)+30); // payload
//                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  48)+30); // payload
//                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  40)+30); // payload
//                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  32)+30); // payload
//                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  24)+30); // payload
//                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  16)+30); // payload
//                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  8)+30); // payload
//                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  0)+30); // payload
                   
                 // MUTEX UP
                } catch (IOException ex) {
                    Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
                }
            // Sleeps timeToSend s 
            try {
                Thread.sleep(1000*100);
            } catch (InterruptedException ex) {
                Logger.getLogger(MessageThreadEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
       
        }
        
    }
    
}

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
      System.out.println("Party!");
             // If the client isnotconnected hold it. KILL ??
//            while(!controller.ClientController.inputConnected)
//            while(!controller.ClientController.outputConnected)
            while(!controller.ClientController.socketConnected)
             
            //controller.ClientController.getDataOutput();
             try {
                    physicalMemorySize = os.getFreePhysicalMemorySize();
                    System.out.println("" + physicalMemorySize);
                    msg = Long.toString(physicalMemorySize);
                    // MUTEX DOWN
                    
                    // write the message
                    byte[] BYTE = new byte[1] ;
                    BYTE[0] = 0x78;
                    controller.ClientController.getDataOutput().write(BYTE); // start byte
                     BYTE[0] =(byte)ClientModel.getSessionID();
                    controller.ClientController.getDataOutput().write(BYTE); // start byte
                    BYTE[0] = Message.serverCrossCommandMessageCode;
                    controller.ClientController.getDataOutput().write(BYTE); // start byte
                    BYTE[0] = (byte)(6 + msg.length());
                    controller.ClientController.getDataOutput().write(BYTE); // start byte
                    BYTE[0] = idIHaveToSend;
                    controller.ClientController.getDataOutput().write(BYTE); // start byte
                    BYTE[0] = (byte) 'r';
                    controller.ClientController.getDataOutput().write(BYTE); // start byte
                   
                    
                     
                    for(int i = 0 ; i < msg.length() ; i ++ ){
                       BYTE[0] = (byte)(msg.toCharArray()[i]);
                        controller.ClientController.getDataOutput().write(BYTE); // start byte
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
                Thread.sleep(1000*timeToSend);
            } catch (InterruptedException ex) {
               
            }
       
        }
        
    }
    
}

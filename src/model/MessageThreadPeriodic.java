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
        while(true){
      
             // If the client isnotconnected hold it. KILL ??
            while(!controller.ClientController.inputConnected)
            while(!controller.ClientController.outputConnected)
            while(!controller.ClientController.socketConnected)
               // MUTEX DOWN
            //controller.ClientController.getDataOutput();
             try {
                    physicalMemorySize = os.getFreePhysicalMemorySize();
                    // write the message
                    controller.ClientController.getDataOutput().writeByte('x'); // start byte
                    controller.ClientController.getDataOutput().writeByte((byte)ClientModel.getSessionID()); // myId
                    controller.ClientController.getDataOutput().writeByte('H'); // cmd H
                    controller.ClientController.getDataOutput().writeByte((byte)(15)); // PayloadLengh
                    controller.ClientController.getDataOutput().writeByte(idIHaveToSend); // id Destino
                    controller.ClientController.getDataOutput().writeByte('r'); // id Destino
                    controller.ClientController.getDataOutput().writeByte((byte)ClientModel.getSessionID()); // myId
                    
                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  56)); // payload
                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  48)); // payload
                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  40)); // payload
                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  32)); // payload
                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  24)); // payload
                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  16)); // payload
                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  8)); // payload
                    controller.ClientController.getDataOutput().writeByte((byte)(physicalMemorySize >>>  0)); // payload
                   
                 // MUTEX UP
                } catch (IOException ex) {
                    Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
                }
            // Sleeps timeToSend s 
            try {
                Thread.sleep(1000*timeToSend);
            } catch (InterruptedException ex) {
                Logger.getLogger(MessageThreadEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
       
        }
        
    }
    
}

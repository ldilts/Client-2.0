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
public class MessageThreadEvent implements Runnable {
     
     private int maxMemoryUsageTrigger ;
     private byte idIHaveToSend ;
     private String msg = "Memory Warning : ";
    
     com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean)
     java.lang.management.ManagementFactory.getOperatingSystemMXBean();
     long totalPhysicalMemorySize;
     long freePhysicalMemorySize;
     
     
     public MessageThreadEvent(int maxMemoryUsageTrigger , byte idIHaveToSend) {
       // store parameter for later user
         this.maxMemoryUsageTrigger = maxMemoryUsageTrigger;
         this.idIHaveToSend = idIHaveToSend;
         msg += maxMemoryUsageTrigger + "% + ";
         
   }

    @Override
    public void run() {
       
          totalPhysicalMemorySize = os.getTotalPhysicalMemorySize();
       
        while(true){
            
             // If the client isnotconnected hold it.
//            while(!controller.ClientController.inputConnected)
//            while(!controller.ClientController.outputConnected)
//            while(!controller.ClientController.socketConnected)
            //controller.ClientController.getDataOutput();
            
            while(!controller.ClientController.socketConnected) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {

                }
            }
                    try {
                    // write the message
                        
                    if( ( totalPhysicalMemorySize - os.getFreePhysicalMemorySize() )> ( totalPhysicalMemorySize * maxMemoryUsageTrigger/100 )  ){
                   // MUTEX DOWN
                        System.out.println(totalPhysicalMemorySize - os.getFreePhysicalMemorySize());
                        System.out.println(totalPhysicalMemorySize * maxMemoryUsageTrigger/100);
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
                     
                      // MUTEX UP
                    //controller.ClientController.getDataOutput().writeByte((byte)()); // payload
                    }
                    
                    try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                Logger.getLogger(MessageThreadEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
                      
                
                } catch (IOException ex) {
                    Logger.getLogger(ClientModel.class.getName()).log(Level.SEVERE, null, ex);
                }
            // Sleeps .5s 
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(MessageThreadEvent.class.getName()).log(Level.SEVERE, null, ex);
            }
            
    
            
        }
        
    }
    
}

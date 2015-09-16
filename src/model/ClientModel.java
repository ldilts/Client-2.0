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
public class ClientModel implements Runnable {
    

    private int sessionID = 0;
    
    public ClientModel(int sessionID) {
        this.sessionID = sessionID;
    }
    
    @Override
    public void run() {
        
    }
    
    
    
    
}

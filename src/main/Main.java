/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import controller.ClientController;
import java.util.Random;
import model.ClientModel;
import model.MessageThreadPeriodic;
import view.ClientView;

/**
 *
 * @author Lucas
 */
public class Main {
    public static void main(String[] args) {
        
        Random rand = new Random();
        int sessionID = rand.nextInt((255 - 0) + 1) + 0;
    	
    	ClientView theView = new ClientView();
    	ClientModel theModel = new ClientModel(sessionID);
        ClientController theController = new ClientController(theView,theModel, sessionID);
        
        theView.setVisible(true);
        theController.startInputObserving();
    }
}

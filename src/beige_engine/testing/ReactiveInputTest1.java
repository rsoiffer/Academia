/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beige_engine.testing;

import beige_engine.behaviors.QuitOnEscapeBehavior;
import beige_engine.engine.Core;
import beige_engine.engine.Input;

/**
 *
 * @author TARS
 */
public class ReactiveInputTest1 {
    public static void main(String[] args) {
        Core.init();
        new QuitOnEscapeBehavior().create();
        Input.addListener((k, m, dm, key, pd, ch) -> {
            System.out.println(k + " " + m + " " + dm + " " + key + " " + pd + " " + ch);
        });
        
        Core.run();
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beige_engine.testing;

import beige_engine.util.math.VectorN;

/**
 *
 * @author TARS
 */
public class VectorNTesting1 {
     public static void main(String[] args) {
        VectorN t1 = VectorN.of(1, 2, 3);
         System.out.println(t1.set(0, 4) + " " + t1);
    }
}

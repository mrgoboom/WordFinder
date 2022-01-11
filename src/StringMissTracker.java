/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package src;

/**
 *
 * @author Scott Byrne
 */
public class StringMissTracker {
    private final String string;
    private final boolean isMiss;
    public StringMissTracker(String s, boolean miss){
        this.string=s;
        this.isMiss=miss;
    }
    public String string(){
        return this.string;
    }
    public boolean isMiss(){
        return this.isMiss;
    }
}

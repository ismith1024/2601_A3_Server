package com.comp2601.message;

/*********
 * COMP2601 A3
 * Submitted 2016-03-16
 * By Ian Smith #100910972
 */

//Represents the payload of the Message class
//For A3, this consists of a String
public class Body {

	private String data;
	public Body(String c_data){
        data = c_data;
    }

    public String getData(){return data;}
    public void setData(String s){data = s;}
}



package com.undi.javaforth;

public class ForthWord{
    public static enum type {
	INTEGER, DOUBLE, CHARACTER, CODE
    };
    private String name;
    private type type;

    public String getName(){
	return this.name;
    }
    public type getType(){
	return this.type;
    }
}

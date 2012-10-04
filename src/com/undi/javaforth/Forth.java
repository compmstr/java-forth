package com.undi.javaforth;

public class Forth{
    private final int stackSize = 2048;

    private int[] dataStack;
    private int[] returnStack;
    private float[] floatStack;

    public void init(){
	this.dataStack = new int[stackSize];
    }

    public static void main(String[] args){
	Forth forth = new Forth();
	forth.init();
    }
}

package com.undi.javaforth;

public class Forth{
    private final int stackSize = 2048;
		private static Forth curInstance = null;

    private int[] dataStack;
    private ForthRunnable[] returnStack;
    private float[] floatStack;
		private ForthWord dictionary = null;
		private ForthWord newWord = null;

    public void init(){
				this.dataStack = new int[stackSize];
    }

		private Forth(){
		}
		public static Forth getInstance(){
				if(Forth.curInstance == null){
						Forth.curInstance = new Forth();
				}
				return Forth.curInstance;
		}
    public static void main(String[] args){
				Forth forth = Forth.getInstance();
				forth.init();
    }
}

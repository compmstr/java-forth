package com.undi.javaforth;
import java.util.Stack;

public class Forth{
    private final int stackSize = 2048;
		private static Forth curInstance = null;
		private ForthDictionary dict = new ForthDictionary();

    public void init(){
				dict.init();
				System.out.println(dict.find("DOCOL"));
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

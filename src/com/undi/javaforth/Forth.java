package com.undi.javaforth;
import java.util.Stack;

public class Forth{
    private final int stackSize = 2048;
		private static Forth curInstance = null;
		private ForthDictionary dict = new ForthDictionary();
		//The next word to execute
		//  esi in jonesforth
		private int instructionPointer = 0;
		//The part of the word that we're currently executing
		//  eax in jonesforth
		private int execPointer = 0;
		private Stack<Integer> returnStack = new Stack<Integer>();
		private Stack<Integer> dataStack = new Stack<Integer>();

    public void init(){
				dict.init();
				System.out.println(dict.find("DOCOL"));
    }

		/**
			 Instruction pointer stuff
		**/
		public int getInstructionPointer(){
				return this.instructionPointer;
		}
		public void setInstructionPointer(int val){
				this.instructionPointer = val;
		}
		public void incInstructionPointer(int amount){
				this.instructionPointer += amount;
		}
		public void incInstructionPointer(){
				incInstructionPointer(4);
		}
		public int setExecPointer(int val){
				this.execPointer = val;
				return this.execPointer;
		}
		public int incExecPointer(int amount){
				this.execPointer += amount;
				return this.execPointer;
		}
		public int incExecPointer(){
				return incExecPointer(4);
		}

		/**
			 Basic stack manipulations
		**/
		public void pushReturnStack(int val){
				this.returnStack.push(val);
		}
		public int popReturnStack(){
				return this.returnStack.pop();
		}
		public int peekReturnStack(){
				return this.returnStack.peek();
		}
		public void pushDataStack(int val){
				this.dataStack.push(val);
		}
		public int popDataStack(){
				return this.dataStack.pop();
		}
		public int peekDataStack(){
				return this.dataStack.peek();
		}
		public int getFromDataStack(int idx){
				return this.dataStack.get(idx);
		}

		/**
			 Singleton stuff
		**/
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

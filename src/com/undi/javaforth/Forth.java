package com.undi.javaforth;
import java.util.Stack;
import java.util.EmptyStackException;

public class Forth{
    private final int stackSize = 2048;
		private static Forth curInstance = null;
		private ForthDictionary dict = new ForthDictionary();
		//The next word to execute
		//  esi in jonesforth
		private int instructionPointer = 0;
		private Stack<Integer> returnStack = new Stack<Integer>();
		private Stack<Integer> dataStack = new Stack<Integer>();
		public static final int STATE_INTERP = 0;
		public static final int STATE_COMPILE = 1;
		private int state;
		private ForthInputBuffer in = new ForthInputBuffer();

    public void init(){
				dict.init();
				//System.out.println(dict.find("DOCOL"));
				//System.out.println(dict.find("bye"));
    }

		public String getNextWord(){
				return in.getNextWord();
		}

		public void setState(int val){
				state = val;
		}
		public int getState(){
				return state;
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

		/**
			 Basic stack manipulations
		**/
		public void pushReturnStack(int val){
				this.returnStack.push(val);
		}
		public int popReturnStack(){
				try{
						return this.returnStack.pop();
				}catch(EmptyStackException ex){
						throw new StackUnderflowException();
				}
		}
		public int peekReturnStack(){
				return this.returnStack.peek();
		}
		public boolean isReturnStackEmpty(){
				return this.returnStack.isEmpty();
		}
		public void pushDataStack(int val){
				this.dataStack.push(val);
		}
		public int popDataStack(){
				try{
						return this.dataStack.pop();
				}catch(EmptyStackException ex){
						throw new StackUnderflowException();
				}
		}
		public int peekDataStack(){
				try{
						return this.dataStack.peek();
				}catch(EmptyStackException ex){
						throw new StackUnderflowException();
				}
		}
		public int getFromDataStack(int idx){
				return this.dataStack.get(idx);
		}
		public Stack<Integer> getDataStack(){
				return this.dataStack;
		}

		public void doINTERPRET(){
				try{
						System.out.print("> ");
						in.readLine();
						String curWord = null;
						while((curWord = in.getNextWord()) != null){
								int word = dict.find(curWord);
								if(word != -1){
										if(state == STATE_INTERP){
												dict.runWord(word, this);
										}else{
												if(dict.isWordImmediate(word)){
														dict.runWord(word, this);
												}else{
														dict.compileWord(word);
												}
										}
								}else{
										try{
												//Parse as an int and add to stack
												int num = Integer.parseInt(curWord);
												if(state == STATE_INTERP){
														pushDataStack(num);
												}else{
														dict.compileInt(num);
												}
										}catch(NumberFormatException ex){
												throw new ForthException("Word not found: " + curWord);
										}
								}
								//System.out.format("Word: %s -- Found: %d\n", curWord, dict.find(curWord));
						}
						System.out.println("ok");
				}catch(ForthException ex){
						System.out.println("----Forth Exception ---");
						ex.printStackTrace();
						System.out.println("-----------------------");
				}
		}
		
		public void run(){
				doQUIT();
		}

		public void doQUIT(){
				while(true){
						//clear return stack
						returnStack.empty();
						//Interpret next word
						doINTERPRET();
				}
		}
		
		public void doNEXT(){
				// get next addr to execute
				int toExec = getInstructionPointer();
				// increment instruction pointer
				incInstructionPointer();
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
				forth.run();
    }
}

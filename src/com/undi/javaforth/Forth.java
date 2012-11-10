package com.undi.javaforth;
import java.util.Stack;

public class Forth{
    private final int stackSize = 2048;
		private static Forth curInstance = null;

    private Stack<Integer> dataStack;
    private Stack<ForthExecutable> returnStack;
		private ForthWord dictionary = null;
		private ForthWord newWord = null;

    public void init(){
				addWord(new ForthPrimitive("*", false, new ForthExecutable(){
								public int execute(Stack datStack, Stack retStack){
										if(datStack.size() < 2){
												throw new StackUnderflowException();
										}
										return 1;
								}
						}));
    }

		public ForthWord findWord(String str){
				ForthWord cur = dictionary;
				while(cur != null){
						if(cur.getName().equals(str)){
								return cur;
						}
						cur = cur.getPrev();
				}
				return null;
		}

		public void addWord(ForthWord word){
				word.setPrev(dictionary);
				this.dictionary = word;
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

package com.undi.javaforth;
import java.util.Stack;
import java.util.Vector;

public class ForthCompWord extends ForthWord{
		private Vector<ForthExecutable> codewords = new Vector<ForthExecutable>();
		public int execute(Stack datStack, Stack retStack){
				return 1;
		}

		public void addWord(ForthExecutable word){
				codewords.add(word);
		}

		public int nextWordIndex(){
				return codewords.size() + 1;
		}
}

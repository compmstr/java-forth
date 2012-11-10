package com.undi.javaforth;
import java.util.Stack;

public class ForthPrimitive extends ForthWord{
		private ForthExecutable codeword;
		public ForthPrimitive(String name, boolean immediate, ForthExecutable code){
				this.immediate = immediate;
				this.name = name;
				this.primitive = true;
				this.codeword = code;
		}
		public int execute(Stack datStack, Stack retStack){
				return this.codeword.execute(datStack, retStack);
		}
}

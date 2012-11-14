package com.undi.javaforth;
import java.util.Stack;

public interface ForthExecutable{
		/**
			 Executes, returns an index increment
			 ex: NEXT is 1, BRANCH is to where it needs to go, and EXIT is 0 (errors are through exceptions)
		 **/
		public int execute(Stack datStack, Stack retStack);
}

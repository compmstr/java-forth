package com.undi.javaforth;

public class StackUnderflowException extends ForthException{
		public StackUnderflowException(){
				super("Stack Underflow");
		}
}

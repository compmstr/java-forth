package com.undi.javaforth;

public class ForthWord implements ForthRunnable{
		private ForthWord prev;
    private String name;
    private ForthRunnable[] codeword;

		private ForthWord(){
		}
		public ForthWord(String name, ForthRunnable[] code){
		}
    public String getName(){
				return this.name;
    }
		public ForthRunnable.result run(){
				return this.codeword[0].run();
		}
}

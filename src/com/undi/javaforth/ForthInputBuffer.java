package com.undi.javaforth;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class ForthInputBuffer{
		private BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		private int pos = 0;
		private String curLine = null;

		public void readLine(){
				if(input != null){
						try{
								curLine = input.readLine();
						}catch(IOException ex){
								ex.printStackTrace();
								System.exit(1);
						}
				}else{
						System.out.println("No console for input");
						curLine = "3 4 * . bye";
				}
				pos = 0;		
		}

		private void eatWhitespace(){
				for(; pos < curLine.length(); pos++){
						if(!Character.isWhitespace(curLine.charAt(pos))){
								return;
						}
				}
		}
		/**
			 Finds the next character of whitespace in the current line
			 If at the end of the line, will return curLine.length()
		**/
		private int findNextWhitespace(){
				int cur = 0;
				for(cur = pos; cur < curLine.length(); cur++){
						if(Character.isWhitespace(curLine.charAt(cur))){
								return cur;
						}
				}
				return cur;
		}
		/**
			 Finds next instance of character c in the line
			 Returns -1 if none found
		**/
		private int findNextInstance(char c){
				for(int cur = pos; cur < curLine.length(); cur++){
						if(c == curLine.charAt(cur)){
								return cur;
						}
				}
				return -1;
		}

		/**
			 returns the next whitespace delimited word
			 or null, if no more words
		**/
		public String getNextWord(){
				eatWhitespace();
				int end = findNextWhitespace();
				if(pos == end){
						return null;
				}
				String word = curLine.substring(pos, end);
				pos = end;
				return word;
		}
		/**
			 Returns text until it reaches char or the end of the line
		**/
		public String getUntilChar(char c){
				return "";
		}
}
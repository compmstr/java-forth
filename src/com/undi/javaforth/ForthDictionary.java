package com.undi.javaforth;
import java.nio.ByteBuffer;
import java.util.Vector;

public class ForthDictionary{
		//Size of the dictionary (128KB currently)
		private final int DICTIONARY_SIZE = 128 * 1024;
		private ByteBuffer dict = ByteBuffer.allocate(DICTIONARY_SIZE);
		private final short WORD_FLAG_IMMEDIATE = 0x80;
		private final short WORD_FLAG_HIDDEN = 0x40;
		private final short WORD_FLAG_PRIMITIVE = 0x20;
		private Vector<ForthExecutable> primitives = new Vector<ForthExecutable>();

		//Index of the start of the last CREATEd word
		private int lastWord = 0;
		//Index where the next insert will go
		private int curPos = 0;

		/**
			 aligns the dictionary to 32-bits (4 bytes)
		**/
		public void align(){
				curPos += ForthUtils.alignDWord(curPos);
		}
		public int getLastWord(){
				return this.lastWord;
		}
		public void compileInt(final int val){
				dict.putInt(curPos);
				curPos += 4;
		}
		/**
			 Returns the word with the name str
			 -1 if not found
		**/
		public int find(String str){
				return -1;
		}
		/**
			 Returns the first code word for the passed in word index
		**/
		public int getCodeWord(int word){
				return -1;
		}

		public int getPrevWord(int word){
				return dict.getInt(word);
		}
		public int getWordNameFlagLoc(int word){
				return word + 4;
		}
		/**
			 Gets (and converts to unsigned byte range) Flag/Wordlen byte
		**/
		public short getWordNameFlag(int loc){
				return (short)(dict.getShort(loc) & 0xFF);
		}
		public boolean isWordImmediate(int word){
				int nameFlag = getWordNameFlagLoc(word);
				if((nameFlag & WORD_FLAG_IMMEDIATE) == 0){
						return false;
				}else{
						return true;
				}
		}
		public boolean isWordHidden(int word){
				int nameFlag = getWordNameFlagLoc(word);
				if((nameFlag & WORD_FLAG_HIDDEN) == 0){
						return false;
				}else{
						return true;
				}
		}
		public boolean isWordPrimitive(int word){
				int nameFlag = getWordNameFlagLoc(word);
				if((nameFlag & WORD_FLAG_PRIMITIVE) == 0){
						return false;
				}else{
						return true;
				}
		}

		private void addString(String str){
				for(int i = 0; i < str.length(); i++){
						dict.put(curPos++, (byte)str.charAt(i));
				}
				align();
		}
		private void addPrimitive(String name, boolean immediate, ForthExecutable code){
				align();
				//Store prev pointer
				dict.putInt(curPos, lastWord);
				//Update lastWord
				lastWord = curPos;
				curPos += 4;
				//Store nameLen/flags
				short nameLenFlags = (short)(WORD_FLAG_PRIMITIVE | (short)(immediate ? WORD_FLAG_IMMEDIATE : 0));
				nameLenFlags |= name.length();
				ForthUtils.bufferPutUByte(dict, curPos, nameLenFlags);
				curPos++;
				//Store name
				addString(name);
				//Store ForthExecutable
				dict.putInt(primitives.size());
				primitives.add(code);
				curPos += 4;
		}
		
		//Bootstraps dictionary with primitives
		public void init(){
				addPrimitive("DOCOL", false, new ForthExecutable(){
								public void Execute(Forth Env){
										System.out.println("DOCOL");
								}
						});
		}
}
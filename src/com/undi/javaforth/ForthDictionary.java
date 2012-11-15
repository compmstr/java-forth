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
		private int lastWord = -1;
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
				int cur = getLastWord();
				int curName;
				do{
						curName = getWordNameFlagLoc(cur);
						curName += 1;
						if(ForthUtils.stringEqualsByteBuffer(dict, curName, str)){
								return cur;
						}
						cur = getPrevWord(cur);
				}while(cur != -1);
				return -1;
		}
		/**
			 Returns the position of the first code word for the passed in word index
		**/
		public int getCodeWord(int word){
				int loc = getWordNameFlagLoc(word);
				int count = ForthUtils.bufferGetUByte(dict, loc) & 0x1f;
				loc += count + 1;
				return loc + ForthUtils.alignDWord(loc);
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
				return (short)(dict.getShort(getWordNameFlagLoc(loc) & 0xFF));
		}
		public boolean isWordImmediate(int word){
				int nameFlag = getWordNameFlag(word);
				if((nameFlag & WORD_FLAG_IMMEDIATE) == 0){
						return false;
				}else{
						return true;
				}
		}
		public boolean isWordHidden(int word){
				int nameFlag = getWordNameFlag(word);
				if((nameFlag & WORD_FLAG_HIDDEN) == 0){
						return false;
				}else{
						return true;
				}
		}
		public boolean isWordPrimitive(int word){
				int nameFlag = getWordNameFlag(word);
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
				dict.putInt(curPos, primitives.size());
				primitives.add(code);
				curPos += 4;
		}

		/**
			 pass in a location in the dictionary, return the ForthExecutable primitive
		**/
		public void runPrimitive(int loc, Forth env){
				primitives.get(loc).Execute(env);
		}

		public void runWord(int word, Forth env){
				if(isWordPrimitive(word)){
						runPrimitive(dict.getInt(getCodeWord(word)), env);
				}else{
				}
		}
		
		//Bootstraps dictionary with primitives
		public void init(){
				addPrimitive("DOCOL", false, new ForthExecutable(){
								public void Execute(Forth env){
										System.out.println("DOCOL");
										//Push current instruction pointer to return stack
										env.pushReturnStack(env.getInstructionPointer());
										//Set instruction and exec pointers to cur + 4
										env.setInstructionPointer(env.incExecPointer());
								}
						});
				addPrimitive("*", false, new ForthExecutable(){
								public void Execute(Forth env){
										System.out.println("*");
										env.pushDataStack(env.popDataStack() *
																			env.popDataStack());
								}
						});
				addPrimitive("+", false, new ForthExecutable(){
								public void Execute(Forth env){
										System.out.println("+");
										env.pushDataStack(env.popDataStack() +
																			env.popDataStack());
								}
						});
				addPrimitive(".", false, new ForthExecutable(){
								public void Execute(Forth env){
										System.out.println(".");
										System.out.format("%d ", env.popDataStack());
								}
						});
				addPrimitive("bye", false, new ForthExecutable(){
								public void Execute(Forth env){
										System.out.println("Bye!");
										System.exit(0);
								}
						});
		}
}

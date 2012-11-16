package com.undi.javaforth;
import java.nio.ByteBuffer;
import java.util.Vector;
import java.util.Stack;

public class ForthDictionary{
		//Size of the dictionary (128KB currently)
		private final int DICTIONARY_SIZE = 128 * 1024;
		private ByteBuffer dict = ByteBuffer.allocate(DICTIONARY_SIZE);
		private final short WORD_FLAG_IMMEDIATE = 0x80;
		private final short WORD_FLAG_HIDDEN = 0x40;
		private final short WORD_FLAG_PRIMITIVE = 0x20;
		private Vector<ForthExecutable> primitives = new Vector<ForthExecutable>();
		private int lastRunWord = 0;

		//Index of the start of the last CREATEd word
		private int lastWord = -1;
		//Index where the next insert will go (HERE)
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
				dict.putInt(curPos, val);
				curPos += 4;
		}
		public String getWordName(int word){
				int nameLoc = getWordNameFlagLoc(word) + 1;
				int nameSize = getWordNameLen(word);
				StringBuilder str = new StringBuilder();
				for(int i = 0; i < nameSize; i++){
						str.append((char)(ForthUtils.bufferGetUByte(dict, nameLoc + i)));
				}
				return str.toString();
		}
		/**
			 Returns the word with the name str
			 -1 if not found
		**/
		public int find(String str){
				int cur = getLastWord();
				int curName, curNameLen;
				do{
						curNameLen = getWordNameLen(cur);
						if(curNameLen == str.length()){
							curName = getWordNameFlagLoc(cur);
							curName += 1;
							if(ForthUtils.stringEqualsByteBuffer(dict, curName, str)
								 && (!isWordHidden(cur))){
									return cur;
							}
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
				int count = getWordNameLen(word);
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
				return (short)ForthUtils.bufferGetUByte(dict, getWordNameFlagLoc(loc));
		}
		/**
			 Returns the length of the name of the word passed in
		**/
		public short getWordNameLen(int loc){
				return (short)(getWordNameFlag(loc) & 0x1f);
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
				doCreate(name);
				//Update the word flags
				short flags = (short)(WORD_FLAG_PRIMITIVE | (short)(immediate ? WORD_FLAG_IMMEDIATE : 0));
				setWordFlags(lastWord, flags);
				unsetWordFlags(lastWord, WORD_FLAG_HIDDEN);
				//Store ForthExecutable
				dict.putInt(curPos, primitives.size());
				primitives.add(code);
				curPos += 4;
		}

		private void setWordFlags(int word, short flags){
				short lenFlags = getWordNameFlag(word);
				lenFlags |= flags;
				ForthUtils.bufferPutUByte(dict, getWordNameFlagLoc(word), lenFlags);
		}
		private void unsetWordFlags(int word, short flags){
				short lenFlags = getWordNameFlag(word);
				lenFlags &= ~flags;
				ForthUtils.bufferPutUByte(dict, getWordNameFlagLoc(word), lenFlags);
		}
		private void toggleWordFlags(int word, short flags){
				short lenFlags = getWordNameFlag(word);
				lenFlags ^= flags;
				ForthUtils.bufferPutUByte(dict, getWordNameFlagLoc(word), lenFlags);
		}
		/**
			 toggles the hidden state of the passed in word
		**/
		public void wordHidden(int word){
				toggleWordFlags(word, WORD_FLAG_HIDDEN);
		}
		public void wordImmediate(int word){
				toggleWordFlags(word, WORD_FLAG_IMMEDIATE);
		}

		/**
			 pass in a location in the dictionary, return the ForthExecutable primitive
		**/
		public void runPrimitive(int loc, Forth env){
				primitives.get(loc).Execute(env);
				env.doNEXT();
		}

		public void runWord(int word, Forth env){
				lastRunWord = word;
				if(isWordPrimitive(word)){
						runPrimitive(dict.getInt(getCodeWord(word)), env);
				}else{
						//Run a composite word
						env.setInstructionPtr(dict.getInt(getCodeWord(word)));
				}
		}

		public void compileNamedWord(String word){
				int wordNum = find(word);
				if(wordNum != -1){
						compileWord(wordNum);
				}else{
						throw new ForthException("Word not found: " + word);
				}
		}
		public void compileWord(int word){
				compileInt(word);
		}

		//does a CREATE onto the end of the dictionary
		public void doCreate(String name){
				align();
				//Store prev pointer
				dict.putInt(curPos, lastWord);
				//Update lastWord
				lastWord = curPos;
				curPos += 4;
				//Store nameLen/flags
				short nameLenFlags = (short)(WORD_FLAG_HIDDEN);
				nameLenFlags |= name.length();
				ForthUtils.bufferPutUByte(dict, curPos, nameLenFlags);
				curPos++;
				//Store name
				addString(name);
		}
		
		//Bootstraps dictionary with primitives
		public void init(){
				addPrimitive("DOCOL", false, new ForthExecutable(){
								public void Execute(Forth env){
										//Push current instruction pointer to return stack
										env.pushReturnStack(env.getNextInstructionPtr());
										//Set instruction and exec pointers to cur + 4
										env.setNextInstructionPtr(lastRunWord + 4);
										env.setInstructionPtr(env.getNextInstructionPtr());
								}
						});
				addPrimitive("EXIT", false, new ForthExecutable(){
								public void Execute(Forth env){
										//Pop the return stack into the instruction pointer
										env.setNextInstructionPtr(env.popReturnStack());
								}
						});
				addPrimitive("LIT", false, new ForthExecutable(){
								public void Execute(Forth env){
										//Get the next thing in the list, push it onto data stack
										env.pushDataStack(dict.getInt(env.getNextInstructionPtr()));
										//Move instruction pointer past next item
										env.incNextInstructionPtr();
								}
						});
				addPrimitive("create", false, new ForthExecutable(){
								public void Execute(Forth env){
										doCreate(env.getNextWord());
								}
						});
				addPrimitive(",", false, new ForthExecutable(){
								public void Execute(Forth env){
										compileInt(env.popDataStack());
								}
						});
				/**
					 Turns off compilation mode
				**/
				addPrimitive("[", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.setState(Forth.STATE_INTERP);
								}
						});
				/**
					 Turns on compilation mode
				**/
				addPrimitive("]", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.setState(Forth.STATE_COMPILE);
								}
						});

				//Stack manipulations
				addPrimitive("drop", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.popDataStack();
								}
						});
				addPrimitive("swap", false, new ForthExecutable(){
								public void Execute(Forth env){
										int tmp1 = env.popDataStack();
										int tmp2 = env.popDataStack();
										env.pushDataStack(tmp1);
										env.pushDataStack(tmp2);
								}
						});
				addPrimitive("dup", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack(env.peekDataStack());
								}
						});
				addPrimitive("over", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack(env.getFromDataStack(1));
								}
						});
				addPrimitive("rot", false, new ForthExecutable(){
								public void Execute(Forth env){
										int tmp1 = env.popDataStack();
										int tmp2 = env.popDataStack();
										int tmp3 = env.popDataStack();
										env.pushDataStack(tmp2);
										env.pushDataStack(tmp1);
										env.pushDataStack(tmp3);
								}
						});
				addPrimitive("-rot", false, new ForthExecutable(){
								public void Execute(Forth env){
										int tmp1 = env.popDataStack();
										int tmp2 = env.popDataStack();
										int tmp3 = env.popDataStack();
										env.pushDataStack(tmp1);
										env.pushDataStack(tmp3);
										env.pushDataStack(tmp2);
								}
						});
				addPrimitive("2drop", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.popDataStack();
										env.popDataStack();
								}
						});
				addPrimitive("2swap", false, new ForthExecutable(){
								public void Execute(Forth env){
										int tmp1 = env.popDataStack();
										int tmp2 = env.popDataStack();
										int tmp3 = env.popDataStack();
										int tmp4 = env.popDataStack();
										env.pushDataStack(tmp2);
										env.pushDataStack(tmp1);
										env.pushDataStack(tmp4);
										env.pushDataStack(tmp3);
								}
						});
				addPrimitive("?dup", false, new ForthExecutable(){
								public void Execute(Forth env){
										int tos = env.peekDataStack();
										if(tos != 0){
												env.pushDataStack(tos);
										}
								}
						});
				addPrimitive("depth", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack(env.getDataStack().size());
								}
						});
				addPrimitive(".s", false, new ForthExecutable(){
								public void Execute(Forth env){
										Stack<Integer> datStack = env.getDataStack();
										System.out.format("<%d> ", datStack.size());
										for(int i : datStack){
												System.out.print(i + " ");
										}
								}
						});
				addPrimitive(".", false, new ForthExecutable(){
								public void Execute(Forth env){
										System.out.format("%d ", env.popDataStack());
								}
						});

				//Arithmetic
				addPrimitive("*", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack(env.popDataStack() *
																			env.popDataStack());
								}
						});
				addPrimitive("+", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack(env.popDataStack() +
																			env.popDataStack());
								}
						});
				addPrimitive("1+", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack(env.popDataStack() + 1);
								}
						});
				addPrimitive("-", false, new ForthExecutable(){
								public void Execute(Forth env){
										int tmp1 = env.popDataStack();
										int tmp2 = env.popDataStack();
										env.pushDataStack(tmp2 - tmp1);
								}
						});
				addPrimitive("1-", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack(env.popDataStack() - 1);
								}
						});
				addPrimitive("mod", false, new ForthExecutable(){
								public void Execute(Forth env){
										int tmp1 = env.popDataStack();
										int tmp2 = env.popDataStack();
										env.pushDataStack(tmp2 % tmp1);
								}
						});
				addPrimitive("/", false, new ForthExecutable(){
								public void Execute(Forth env){
										int tmp1 = env.popDataStack();
										int tmp2 = env.popDataStack();
										env.pushDataStack(tmp2 / tmp1);
								}
						});

				
				//Logical operators
				addPrimitive("=", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack((env.popDataStack() == env.popDataStack()) ? -1 : 0);
								}
						});
				addPrimitive("<>", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack((env.popDataStack() != env.popDataStack()) ? -1 : 0);
								}
						});
				addPrimitive(">", false, new ForthExecutable(){
								public void Execute(Forth env){
										int b = env.popDataStack();
										int a = env.popDataStack();
										env.pushDataStack((a > b) ? -1 : 0);
								}
						});
				addPrimitive("<", false, new ForthExecutable(){
								public void Execute(Forth env){
										int b = env.popDataStack();
										int a = env.popDataStack();
										env.pushDataStack((a < b) ? -1 : 0);
								}
						});
				addPrimitive(">=", false, new ForthExecutable(){
								public void Execute(Forth env){
										int b = env.popDataStack();
										int a = env.popDataStack();
										env.pushDataStack((a >= b) ? -1 : 0);
								}
						});
				addPrimitive("<=", false, new ForthExecutable(){
								public void Execute(Forth env){
										int b = env.popDataStack();
										int a = env.popDataStack();
										env.pushDataStack((a <= b) ? -1 : 0);
								}
						});
				addPrimitive("0=", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack((env.popDataStack() == 0) ? -1 : 0);
								}
						});
				addPrimitive("0<>", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack((env.popDataStack() != 0) ? -1 : 0);
								}
						});
				
				//Flag manipulators
				addPrimitive("hidden", false, new ForthExecutable(){
								public void Execute(Forth env){
										wordHidden(env.popDataStack());
								}
						});
				addPrimitive("immediate", false, new ForthExecutable(){
								public void Execute(Forth env){
										wordImmediate(env.popDataStack());
								}
						});

				//Memory access
				addPrimitive("@", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack(dict.getInt(env.popDataStack()));
								}
						});
				addPrimitive("!", false, new ForthExecutable(){
								public void Execute(Forth env){
										dict.putInt(env.popDataStack(), env.popDataStack());
								}
						});

				//Constants
				addPrimitive("lastxt", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack(getCodeWord(lastWord));
								}
						});
				addPrimitive("here", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.pushDataStack(curPos);
								}
						});

				//Branching
				addPrimitive("BRANCH", true, new ForthExecutable(){
								public void Execute(Forth env){
										//Get the next thing in the list, and add it to the instruction pointer
										env.incNextInstructionPtr(dict.getInt(env.getNextInstructionPtr()));
								}
						});
				addPrimitive("0BRANCH", true, new ForthExecutable(){
								public void Execute(Forth env){
										if(env.popDataStack() == 0){
												//Get the next thing in the list, and add it to the instruction pointer
												env.incNextInstructionPtr(dict.getInt(env.getNextInstructionPtr()));
										}else{
												//just skip the branch amount
												env.incNextInstructionPtr();
										}
								}
						});

				//Misc
				addPrimitive("'", false, new ForthExecutable(){
								public void Execute(Forth env){
										String word = env.getNextWord();
										int xt = find(word);
										if(xt == -1){
												throw new ForthException("Word - " + word + " - not found\n");
										}else{
												env.pushDataStack(getCodeWord(xt));
										}
								}
						});
				addPrimitive("INTERPRET", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.doINTERPRET();
								}
						});
				addPrimitive("(QUIT)", false, new ForthExecutable(){
								public void Execute(Forth env){
										env.doQUIT();
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

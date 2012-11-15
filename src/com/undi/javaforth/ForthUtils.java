package com.undi.javaforth;
import java.nio.ByteBuffer;

public class ForthUtils{
		/**
			 returns the number of bytes to add to val to align it
			 to bytes byte boundries
		**/
		public static int alignBytes(int bytes, int val){
				//System.out.format("AlignBytes: %d, %d --> %d\n", bytes, val, ((bytes - (val % bytes)) % (bytes - 1)));
				return ((bytes - (val % bytes)) % bytes);
		}
		public static int alignDWord(int val){
				return alignBytes(4, val);
		}
		public static int alignWord(int val){
				return alignBytes(2, val);
		}

		public static boolean stringEqualsByteBuffer(ByteBuffer buf, int offset, String string){
				for(int i = 0; i < string.length(); i++){
						if(string.charAt(i) != buf.get(offset + i)){
								return false;
						}
				}
				return true;
		}

		public static short bufferGetUByte(ByteBuffer buf, int loc){
				return (short)((buf.getShort(loc) >> 8) & 0xFF);
		}
		public static void bufferPutUByte(ByteBuffer buf, int loc, short val){
				buf.put(loc, (byte) val);
		}
}

package com.undi.javaforth;
import java.util.Stack;

public abstract class ForthWord implements ForthExecutable{
		private ForthWord prev = null;
    protected String name;
		protected boolean immediate = false;
		protected boolean primitive = false;

		public boolean isPrimitive(){ return this.primitive; }
		public boolean isImmediate(){ return this.immediate; }
		public void setPrimitive(boolean prim){ this.primitive = prim; }
		public void setImmediate(boolean imm){ this.immediate = imm; }

		public ForthWord getPrev(){ return this.prev; }
		public void setPrev(ForthWord word){ this.prev = word; }

    public String getName(){ return this.name; }
}

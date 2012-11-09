package com.undi.javaforth;

public interface ForthRunnable{
		/**
			return values for the run function
					NEXT -> go to next word in containing word
					EXIT -> exit containing word
		**/
		public static enum result{
				NEXT, EXIT, BRANCH
						};
		/**
			 Target for a branch response
		**/
		public ForthRunnable target = null;
		public result run();
}
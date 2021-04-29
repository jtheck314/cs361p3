package re;

import fa.nfa.NFA;

public class RE implements REInterface {

	private String regex;
	
	public RE(String input) {
		this.regex = input;
	}
	
	public NFA getNFA() {
		
	}

	public NFA parse () {...}
	
	/* Recursive descent parsing internals. */
	
	private char peek() {
		return this.regex.charAt(0) ;
	}

	private void eat(char c) {
		if (peek() == c)
			this.regex = this.regex.substring(1) ;
    else
    	throw new RuntimeException("Expected: " + c + "; got: " + peek()) ;
	}
  
	private char next() {
		char c = peek() ;
		eat(c) ;
		return c ;
	}

	private boolean more() {
		return this.regex.length() > 0 ;
	}
	
	/* Regular expression term types. */
	
	private NFA regex() {
		NFA t = term();
		
		if (more() && peek() == '|') {
		      eat ('|') ;
		      NFA regex = regex() ;
		      return new Choice(term,regex) ;
	    } else {
		     return t ;
	    }
	}
	
	private NFA term() {...}
	
	private NFA factor() {...}
	
	private NFA base() {
		switch (peek()) {
	    	case '(':
	    		eat('(') ;
	    		NFA r = regex() ;  
	    		eat(')') ;
	    		return r ;

	    	case 'e':
	    		eat ('\\') ;
	    		char esc = next() ;
	    		return new Primitive(esc) ;

	    	default:
	    		return next() ;
		}
	}
	
}

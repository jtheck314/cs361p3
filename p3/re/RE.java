package re;

import java.util.Set;

import fa.State;
import fa.nfa.NFA;
import fa.nfa.NFAState;

public class RE implements REInterface {

	private String regex;
	private int stateCount;
	private String currState;
	private char currSymb;
	
	public RE(String input) {
		this.regex = input;
		this.stateCount = 0;
		this.currState = "0";
		this.currSymb = 'e';

	}
	
	public NFA getNFA() {
		
		return regex();

	}
	
	private NFA nfaSequence(NFA from, NFA to) {
		NFA n = new NFA();
		
		n = nfaUnion(n,from);
		
		n = nfaUnion(n,to);
		
		n.addStartState(from.getStartState().getName());
		
		for(State s : from.getFinalStates()) {
			n.addTransition(s.getName(), 'e', to.getStartState().getName());
		}
		
		for(State s : to.getFinalStates()) {
			n.addFinalState(s.getName());
		}
		
		return n;
	}
	
	private NFA nfaChoice(NFA nfa1, NFA nfa2) {
		NFA n = new NFA();
		incrementState();
		n.addStartState(currState);
		
		n = nfaUnion(n,nfa1);
		
		n = nfaUnion(n,nfa2);
		
		n.addTransition(n.getStartState().getName(), 'e', nfa1.getStartState().getName());
		n.addTransition(n.getStartState().getName(), 'e', nfa2.getStartState().getName());
				
		for(State s : nfa1.getFinalStates()) {
			n.addFinalState(s.getName());
		}
		
		for(State s : nfa2.getFinalStates()) {
			n.addFinalState(s.getName());
		}
		
		return n;
	}
	
	private NFA nfaUnion(NFA n, NFA m) {
		for(State s : m.getStates()) {
			n.addState(s.getName());
		}
		n.addAbc(m.getABC());
		for(State s : m.getStates()) {
			Set<Character> set = m.getABC();
			set.add('e');
			for(char c : set) {
				Set<NFAState> toState = m.getToState((NFAState) s, c);
				for(State f : toState) {
					n.addTransition(s.getName(), c, f.getName());
				}
			}
		}
		return n;
	}
	
	private void incrementState() {
		this.stateCount++;
		this.currState = String.valueOf(this.stateCount);
		
	}
	
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
		      NFA n = regex() ;
		      return nfaChoice(t,n) ;
	    } else {
		     return t ;
	    }
	}
	
	private NFA term() {
		NFA factor = factor();

	    while (more() && peek() != ')' && peek() != '|') {
	    	NFA nextFactor = factor() ;
	    	factor = nfaSequence(factor,nextFactor) ;
	    }

	    return factor ;
	}
	
	private NFA factor() {
	    NFA base = base();
	    
	    if (more() && peek() == '*') {
	    	eat('*') ;
	    	for(State s : base.getFinalStates()) {
		    	base.addTransition(s.getName(), 'e', base.getStartState().getName());
	    	}
	    	base.addFinalState(base.getStartState().getName());
	    }
	    else {
	    	String fromState = currState;
	    	incrementState();
	    	base.addState(currState);
	    	base.addTransition(fromState, currSymb, currState);
	    	base.addFinalState(currState);
	    }

	    return base ;
	}
	
	private NFA base() {
		switch (peek()) {
	    	case '(':
	    		eat('(') ;
	    		NFA r = regex() ;  
	    		eat(')') ;
	    		return r ;


	    	default:
	    		incrementState();
	    		NFA n = new NFA();
	    		this.currSymb = next();
	    		if(more() && peek() == '*') {
	    			n.addFinalState(currState);
	    			n.addTransition(currState, currSymb, currState);
	    		}
	    		n.addStartState(currState);
	    		return n;
		}
	}
	
}

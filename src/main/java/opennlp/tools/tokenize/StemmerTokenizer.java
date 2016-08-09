package opennlp.tools.tokenize;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.Span;
import opennlp.tools.util.StringUtil;
import opennlp.tools.stemmer.PorterStemmer;

/**
 * TODO: Documentation
 */ 
public class StemmerTokenizer implements Tokenizer{

    public static final StemmerTokenizer INSTANCE = new StemmerTokenizer();
    
    private PorterStemmer stemmer = new PorterStemmer();
    
    private StemmerTokenizer() {
    }

    public String[] tokenize(String s) {
	List<String> tokens = new ArrayList<String>();
	boolean inTok = false;
	
	StringBuilder curr = new StringBuilder();
	
	for (int i = 0; i < s.length(); i++) {
	    if (StringUtil.isWhitespace(s.charAt(i))) {
		if (inTok) {
		    stemmer.stem();
		    String tok = stemmer.toString();
		    stemmer.reset();
		    
		    tokens.add(tok);
		    inTok = false;
		}
		else {
		    if (!inTok) {
			inTok = true;
		    }
		}
	    }
	    else {
		stemmer.add(s.charAt(i));
	    }  
	}
	if (inTok) {
	    stemmer.stem();
	    tokens.add(stemmer.toString());
	}

	return tokens.toArray(new String[tokens.size()]);
    }
    

    /**
     * Same as WhitespaceTokenizer
     */
    public Span[] tokenizePos(String s) {
	
	int tokStart = -1;
	List<Span>tokens = new ArrayList<Span>();
	boolean inTok = false;
	
	//gather up potential tokens
	int end = s.length();
	for (int i = 0; i < end; i++) {
	    if (StringUtil.isWhitespace(s.charAt(i))) {
		if (inTok) {
		    tokens.add(new Span(tokStart, i));
		    inTok = false;
		    tokStart = -1;
		}
	    }
	    else {
		if (!inTok) {
		    tokStart = i;
		    inTok = true;
		}
	    }
	}

	if (inTok) {
	    tokens.add(new Span(tokStart, end));
	}

	return tokens.toArray(new Span[tokens.size()]);
    }
    
}

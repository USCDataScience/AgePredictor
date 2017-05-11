package opennlp.tools.tokenize;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.util.Span;
import opennlp.tools.util.StringUtil;

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
	    }
	    else {
		stemmer.add(s.charAt(i));
		inTok = true;
	    }  
	}
	if (inTok) {
	    stemmer.stem();
	    tokens.add(stemmer.toString());
	}

	return tokens.toArray(new String[tokens.size()]);
    }
    

    public Span[] tokenizePos(String s) {
	// Not needed
	return new Span[0];
    }
    
}

package opennlp.tools.tokenize;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.StringUtil;
import opennlp.tools.stemmer.PorterStemmer;

/**
 * TODO: Documentation
 */ 
public class StemmerTokenizer {

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
    
}

package opennlp.tools.formats.authorage;

import java.io.IOException;

import gov.nasa.jpl.ml.cmdline.CLI;
import opennlp.tools.authorage.AuthorAgeSample;
import opennlp.tools.authorage.AuthorAgeSampleStream;
import opennlp.tools.cmdline.ArgumentParser;
import opennlp.tools.cmdline.ArgumentParser.OptionalParameter;
import opennlp.tools.cmdline.ArgumentParser.ParameterDescription;
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.StreamFactoryRegistry;
import opennlp.tools.cmdline.params.BasicFormatParams;
import opennlp.tools.formats.AbstractSampleStreamFactory;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.ext.ExtensionLoader;

/**
 * TODO: Documentation
 */
public class AuthorAgeSampleStreamFactory
    extends AbstractSampleStreamFactory<AuthorAgeSample> {

    protected <P> AuthorAgeSampleStreamFactory(Class<P> params) {
	super(params);
    }
    
    interface AuthorAgeStreamFactoryParams extends BasicFormatParams {
	@ParameterDescription(valueName = "tokenizer", 
	     description = "Tokenizer implementation. WhitespaceTokenizer is used if not specified.")
	@OptionalParameter
	String getTokenizer();
    }
    
    private Tokenizer getTokenizer(String tokenizer) {
        if(tokenizer != null) {
            return ExtensionLoader.instantiateExtension(Tokenizer.class, tokenizer);
        }
        return WhitespaceTokenizer.INSTANCE;
    }
    
    @Override
    public ObjectStream<AuthorAgeSample> create(String[] args) {
	AuthorAgeStreamFactoryParams params = ArgumentParser.parse(args,
	    AuthorAgeStreamFactoryParams.class);
	
	CmdLineUtil.checkInputFile("Data", params.getData());
	InputStreamFactory sampleDataIn = CmdLineUtil
	    .createInputStreamFactory(params.getData());
	ObjectStream<String> lineStream = null;
	try {
	    lineStream = new PlainTextByLineStream(sampleDataIn,
						   params.getEncoding());
	} catch (IOException ex) {
	    CmdLineUtil.handleCreateObjectStreamError(ex);
	}
	
	Tokenizer tokenizer = getTokenizer(params.getTokenizer());

	return new AuthorAgeSampleStream(lineStream, tokenizer);
    }

    public static void registerFactory() {
	StreamFactoryRegistry.registerFactory(AuthorAgeSample.class,
	    CLI.DEFAULT_FORMAT,
	    new AuthorAgeSampleStreamFactory(AuthorAgeStreamFactoryParams.class));
    }   
}
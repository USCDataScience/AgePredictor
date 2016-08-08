package opennlp.tools.formats;

import java.io.IOException;

import opennlp.tools.cmdline.ArgumentParser;
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.StreamFactoryRegistry;
import opennlp.tools.cmdline.params.BasicFormatParams;

import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

import opennlp.tools.authorage.AuthorAgeSample;
import opennlp.tools.authorage.AuthorAgeSampleStream;

import gov.nasa.jpl.ml.cmdline.CLI;

/**
 * TODO: Documentation
 */
public class AuthorAgeSampleStreamFactory
    extends AbstractSampleStreamFactory<AuthorAgeSample> {

    protected <P> AuthorAgeSampleStreamFactory(Class<P> params) {
	super(params);
    }

    
    @Override
    public ObjectStream<AuthorAgeSample> create(String[] args) {
	BasicFormatParams params = ArgumentParser.parse(args,
	    BasicFormatParams.class);
	
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
	
	return new AuthorAgeSampleStream(lineStream);
    }

    public static void registerFactory() {
	StreamFactoryRegistry.registerFactory(AuthorAgeSample.class,
	    CLI.DEFAULT_FORMAT,
	    new AuthorAgeSampleStreamFactory(BasicFormatParams.class));
    }   
}
# Author Age Prediction
This is a author age categorizer that leverages the [Apache OpenNLP](https://opennlp.apache.org/) Maximum Entropy Classifier. It takes a text sample and classifies it into the following age categories: xx-18|18-24|25-34|35-49|50-64|65-xx. 

# Usage
### How to train an Age Classifier

Note: The training data should be a line-by-line, with each line starting with the age, or age category, followed by a tab and the text associated with the age. 

``` shell
Usage: bin/authorage AgeClassifyTrainer [-factory factoryName] [-featureGenerators featuregens] [-tokenizer tokenizer] -model modelFile [-params paramsFile] -lang language -data sampleData [-encoding charsetName]

Arguments description:
	-factory factoryName
        a sub-class of DoccatFactory where to get implementation and resources.
	-featureGenerators featuregens
	    comma separated feature generator classes. Bag of words default.
	-tokenizer tokenizer
        tokenizer implementation. WhitespaceTokenizer is used if not specified.
	-model modelFile
        output model file.
	-params paramsFile
	    training parameters file.
	-lang language
	    language which is being processed.
	-data sampleData
	    data to be used, usually a file name.
	-encoding charsetName
	    encoding for reading and writing text, if absent the system default is used.
```
Example Usage:
``` shell
bin/authorage AgeClassifyTrainer -model model/en-ageClassify.bin -lang en -data data/train.txt -encoding UTF-8
```
Training data format - Age and text seperated by tab in each line like `<AGE><Tab><TEXT>`    
Sample training data-
```
12	I am just 12 year old
25	I am little bigger
35	I am mature
45	I am getting old
60	I am old like wine
```
### How to evaluate an Age Classifier Model

```shell
Usage: bin/authorage AgeClassifyEvaluator -model model [-misclassified true|false] -data sampleData [-encoding charsetName]

Arguments description:
	-model model
		the model file to be evaluated.
	-misclassified true|false
		if true will print false negatives and false positives.
	-data sampleData
		data to be used, usually a file name.
	-encoding charsetName
		encoding for reading and writing text, if absent the system default is used.
```

Example Usage:
```shell
bin/authorage AgeClassifyEvaluator -model model/en-ageClassify.bin -data data/test.txt -encoding UTF-8
```

### How to run the Age Classifier

Note: Each document must be followed by an empty line to be detected as a separate case from the others.

```shell
Usage: bin/authorage AgeClassify model < documents
```

```shell
Usage: bin/authorage AgePredict ./model/classify-unigram.bin ./model/regression-global.bin  data/sample_test.txt
```

# Downloads
For AgePredict to work you need to download `en-pos-maxent.bin`, `en-sent.bin` and `en-token.bin` from [http://opennlp.sourceforge.net/models-1.5/](http://opennlp.sourceforge.net/models-1.5/) to `model/opennlp/`

# Contributors
* Joey Hong, Caltech, CA

# License 
[Apache License, version 2](http://www.apache.org/licenses/LICENSE-2.0)

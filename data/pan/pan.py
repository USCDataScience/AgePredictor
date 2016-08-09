import truth
from bs4 import BeautifulSoup
import os
import re
from HTMLParser import HTMLParser

import sys, getopt
import codecs

class MLStripper(HTMLParser):
    def __init__(self):
        self.reset()
        self.fed = []
    def handle_data(self, d):
        self.fed.append(d)
    def get_data(self):
        return ''.join(self.fed)

# Helper Function
def strip_tags(html):
    s = MLStripper()
    s.feed(html)
    return s.get_data()


def clean_text(html):
    URLREGEX = r'http[s]?://(?:[a-z]|[0-9]|[$-_@.&+]|[!*\(\),]|(?:%[0-9a-f][0-9a-f]))+'
    HASHREGEX = r'(?:\#+[\w_]+[\w\'_\-]*[\w_]+)'
    MENTIONREGEX = r'(?:@[\w_]+)'

    cleaned = re.sub(URLREGEX, '', html)
    cleaned = re.sub(HASHREGEX, '', cleaned)
    cleaned == re.sub(MENTIONREGEX, '', cleaned)
    
    return cleaned

    
def parse_xml(filename, parser='html.parser'):
    docs = []
    with open(filename) as f:
        doc = BeautifulSoup(f, parser)
        for post in doc.find_all('document'):
            text = unicode(post.get_text().strip())
            text = strip_tags(text)
            text = clean_text(text)
            text = text.replace('\n', '')

            if len(text) > 0:
                docs.append(text)
    return docs


def parse_all(inputDir, outputDir):
    author_data = truth.read_truth_data(inputDir)
    
    if not os.path.exists(outputDir):
        os.makedirs(outputDir)

    for author, info in author_data.iteritems():
        infilename = inputDir + '/' + author + '.xml'
        docs = parse_xml(infilename)
        
        outfilename = outputDir + '/' + author + '.txt'    
        with codecs.open(outfilename, 'w', 'utf-8') as out:
            for doc in docs:
                out.write('%s\t%s\n' % (info['Age Category'], doc))
        print "Completed: ", outfilename

def main(argv):
    inputDir = ''
    outputDir = ''

    try:
        opts, args = getopt.getopt(argv, "hi:o:", ["inDir=", "outDir="])
    except getopt.GetoptError:
        print 'blogs.py -i <inputdir> -o <outputdir>'
        sys.exit(2)
        
    for opt,arg in opts:
        if opt == '-h':
            print 'pan.py -i <inputdir> -o <outputdir>'
            sys.exit()
        elif opt in ('-i', '--inDir'):
            inputDir = arg
        elif opt in ('-o', '--outDir'):
            outputDir = arg
            
    print "Input: ", inputDir
    print "Output: ", outputDir
    
    parse_all(inputDir, outputDir)
    
if __name__=='__main__':
    main(sys.argv[1:])

    

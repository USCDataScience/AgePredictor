import os
import re

from lxml import etree
import codecs

import sys, getopt

def clean_xml(file):   
    invalid_xml = re.compile(r'&#x[0-1]?[0-9a-eA-E];')
    
    file_new = ('.').join(file.split('.')[:-1]) + '.cleaned.xml'
    clean_file = codecs.open(file_new, 'w')
    with open(file, 'r+') as f:
        for line in f:
            nline, count = invalid_xml.subn('', line)
            nline = nline.decode('utf-8', 'ignore').encode('utf-8')
            clean_file.write(nline)
    clean_file.close()
    
    return file_new
    

def get_posts(file):
    clean_file = clean_xml(file)
    parser = etree.XMLParser(recover=True)
    
    tree = etree.parse(clean_file, parser)
    for post in tree.iter('post'):
        post_text = post.text.strip()
        
        yield post_text
        
    """
    for event, elem in etree.iterparse(clean_file):
        if event == "end":
            if elem.tag == "post":
                text = elem.text
                if text:
                    post = text.encode('utf-8').strip()
                    yield post
        elem.clear()
    """

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
            print 'blogs.py -i <inputdir> -o <outputdir>'
            sys.exit()
        elif opt in ('-i', '--inDir'):
            inputDir = arg
        elif opt in ('-o', '--outDir'):
            outputDir = arg
            
    print "Input: ", inputDir
    print "Output: ", outputDir
    
    for file in os.listdir(inputDir):
        if file.endswith(".xml"):
            fileparts = re.match(r'(^\d+?)\.(male|female)\.(\d+?)\.(.*$)', file)
            
            id = fileparts.group(1)
            age = fileparts.group(3)
            
            if not os.path.exists(outputDir):
                os.makedirs(outputDir)
                
            filename = outputDir + '/' + id + '-' + age + '.txt' 
            with codecs.open(filename, 'w', 'utf-8') as out:
                for post in get_posts(inputDir + '/' + file):
                    out.write('%s\t%s\n' %(age, post))
            
            print "Completed: ", filename
            
if __name__=='__main__':
    main(sys.argv[1:])

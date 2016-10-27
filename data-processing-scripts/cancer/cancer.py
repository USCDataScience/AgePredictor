import os
import re
import sys,getopt

import glob

def parse_all(inputDir, outputDir):
    file_regex = re.compile(r'cancer-(.+)?-([0-9]+).txt')
    
    if not os.path.exists(outputDir):
        os.makedirs(outputDir)
        
    count = 0
    for filename in glob.glob(inputDir + '/*'):
        match = file_regex.search(filename, re.I)
        author_id = match.group(1)
        age = match.group(2)
        if (match):
            text = ''
            with open(filename, 'r') as f:
                for line in f:
                    cleaned = line.strip()
                    if cleaned:
                        text += cleaned + ' '
            with open(outputDir + '/' + author_id + '.txt', 'w') as out:
                out.write('%s\t%s\n' %(age, text))
            count += 1
            
            if (count % 1000 == 0):
                print "Count: ", count
    print "Count: ", count
    

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

    

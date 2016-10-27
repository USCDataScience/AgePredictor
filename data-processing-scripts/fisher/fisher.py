import truth
import os
import re
import sys,getopt

import glob


author_data = truth.read_user_truth('/work/04180/tg834792/wrangler/AgePredictor/data/fisher/fe_03_p1_tran/doc/fe_03_pindata.tbl')

call_data = truth.read_call_truth('/work/04180/tg834792/wrangler/AgePredictor/data/fisher/fe_03_p1_tran/doc/fe_03_p1_calldata.tbl')
    
def collect(filename, user):
    line_regex = r'^([0-9.]+?) ([0-9.]+?) ' + user + ': (.*)$'
    
    text = ''
    with open(filename) as f:
        for line in f:
            if user in line:
                match = re.match(line_regex, line, re.I|re.M)
                if (match):
                    text += match.group(3).strip() + ' '
    return text.rstrip()
    

def parse_all(inputDir, outputDir):
    file_regex = re.compile(r'fe_03_([0-9]+?).txt')
    
    if not os.path.exists(outputDir):
        os.makedirs(outputDir)
        
    count = 0
    for filename in glob.glob(inputDir + '/*/*'):
        match = file_regex.search(filename, re.I)
        call_id = match.group(1)
        
        text_A = collect(filename, 'A')
        text_B = collect(filename, 'B')
        
        A_id = call_data[call_id]['A']
        B_id = call_data[call_id]['B']
        
        with open(outputDir + '/' + call_id + '.txt', 'w') as out:
            out.write('%s\t%s\n' % (author_data[A_id]['Age'], text_A))
            out.write('%s\t%s\n' % (author_data[B_id]['Age'], text_B))
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

    

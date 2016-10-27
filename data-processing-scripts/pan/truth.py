import sys
from pprint import pprint


def read_truth_data(path):    
    with open(path+'/truth.txt','r') as truth_file:
        authors = {}
        for author in truth_file:
            author_data = author.split(":::")
            authors[author_data[0].strip()] = {"Gender": author_data[1].strip(), 
                                       "Age Category": author_data[2].strip()}
    return authors

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print "Usage: truth.py <path to directory>"
        sys.exit();
        
    truth_data = read_truth_data(sys.argv[1])
    print pprint(truth_data)

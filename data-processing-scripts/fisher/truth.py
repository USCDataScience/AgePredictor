import sys

def read_user_truth(path):
    with open(path, 'r') as user_truth:
        authors = {}
        for author in user_truth:
            author_data = author.split(",")
            authors[author_data[0].strip()] = {"Age": author_data[2].strip()}
            
    return authors

def read_call_truth(path):
    with open(path, 'r') as call_truth:
        calls = {}
        for call in call_truth:
            call_data = call.split(",")
            calls[call_data[0].strip()] = {"A": call_data[5].strip(),
                                           "B": call_data[10].strip()}
            
    return calls


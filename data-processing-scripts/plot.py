import numpy as np
import matplotlib.pyplot as plt
import re
import os


def get_blogs():
    BLOGS_DIR = "data/blogs/blogger.com"
    BLOGS_REGEX = re.compile(r'([0-9]+)-([0-9]+).txt')
    
    blog_ages={}
    for filename in os.listdir(BLOGS_DIR):
        if filename.endswith(".txt"):
            match = BLOGS_REGEX.search(filename, re.I)
            if (match):
                age = int(match.group(2))
                blog_ages[age] = blog_ages.get(age, 0) + 1
    return [blog_ages.get(i, 0) for i in range(100)]


def get_fisher():
    FISHER_TRUTH="data/fisher/fe_03_p1_tran/doc/fe_03_pindata.tbl"
    
    fisher_ages={}
    with open(FISHER_TRUTH, 'r') as truth:
        for author in truth:
            author_data = author.split(",")
            try:
                age = int(author_data[2].strip())
                fisher_ages[age] = fisher_ages.get(age, 0) + 1
            except:
                continue
    return [fisher_ages.get(i, 0) for i in range(100)]
            

def get_cancer():
    CANCER_DIR = "data/cancer/cancer-dataset-nguyen2011/text"
    CANCER_REGEX = re.compile(r'(cancer)?-(.+)?-([0-9]+).txt')
    
    cancer_ages={}
    for filename in os.listdir(CANCER_DIR):
        if filename.endswith(".txt"):
            match = CANCER_REGEX.search(filename, re.I)
            if (match):
                age = int(match.group(3))
                cancer_ages[age] = cancer_ages.get(age, 0) + 1
    return [cancer_ages.get(i, 0) for i in range(100)]

range = np.arange(100)
width = 0.35

fig = plt.figure()
fig.patch.set_facecolor('white')
ax = fig.add_subplot(111)

blogs = get_blogs()
cancer = get_cancer()
fisher = get_fisher()

blog = ax.bar(range, blogs, width, color='black')
fisher = ax.bar(range, fisher, width, color='red')
cancer = ax.bar(range, cancer, width, color='blue')

ax.set_xlim(-width, len(range) + width)
ax.set_ylim(0, 80000)
ax.set_ylabel('Frequency')
ax.set_xlabel('Age')
ax.legend((blog, fisher, cancer), ('Blogs', 'Fisher', 'Cancer'))
plt.show()

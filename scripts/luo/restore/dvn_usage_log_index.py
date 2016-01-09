from elasticsearch import Elasticsearch
import codecs, os, sys

logfiledir = ''#C:/Users/luopc/Desktop/usage' #usage log dir
errfile = ''#C:/Users/luopc/Desktop/usage.log.err' #fail to index usage log
encoding = 'utf-8'
if len(logfiledir) == 0:
    logfiledir = sys.argv[1]
if len(errfile) == 0:
    errfile = sys.argv[2]
if len(encoding) == 0:
    encoding = sys.argv[3]

es = Elasticsearch()
with codecs.open(errfile, 'w', encoding) as outfile:
    sortedfiles = sorted(os.listdir(logfiledir), reverse=False)
    if sortedfiles[0] == 'usage.log':
        sortedfiles.remove('usage.log')
        sortedfiles.append('usage.log')
    for logfile in sortedfiles:
        print('Index usage log:',logfile,'...')
        with codecs.open(logfiledir + '/' + logfile, 'r', encoding) as infile:
            for line in infile.readlines():
                idx = line.find('[usage_msg]:')
                if idx >= 0:
                    jsonstr = line[idx + 12:-1]
                    begin_index = jsonstr.index('"date"')
                    jsonstr = jsonstr[0:jsonstr.index('.',begin_index)]+jsonstr[jsonstr.index('+0800',begin_index):]
                    try:
                        es.index(index="dataverse", doc_type='statis', body=jsonstr)
                    except Exception:
                        outfile.write(line)
#!/usr/bin/python3.6
#converts the .des vaults into .template format
#more info https://crawl.develz.org/wiki/doku.php?id=dcss:help:maps:syntax:glyphs
import os,sys

ABORTONERROR=False

TRANSLATE={
    '\n':'\n',
    ' ':' ',
    '.':'.',
    '?':' ',
    't':'#', #tree
    'l':'~', #lava
}

def register(glyphs,to):
    for g in glyphs:
        TRANSLATE[g]=to

register('0123456789',' ')
register('wW','~') #water
register('xXcmnovb','#') #walls
register('+=','+') #door
register('@\{\}()[]<>^~%*|',' ') #ignored features
register('defghijk',' ') #items
register('IABCGTUVY','!') #decoration

maps=0
failed=0

def save(m,des):
    filename=des['filename'].replace('.des','')
    i=0
    target=False
    while i==0 or os.path.isfile(target):
        i+=1
        target=os.path.join(des['directory'],filename)+str(i)+'.template'
    #print(des['directory'])
    print(m,file=open(target,'w'))

def convert(m,des):
    global maps,failed
    try:
        converted=''
        for glyph in m:
            converted+=TRANSLATE[glyph]
        save(converted,des)
        maps+=1
    except KeyError:
        if ABORTONERROR:
            print(m)
            print(des['path'])
            print('Key error: '+glyph)
            sys.exit(1)
        else:
            print('cannot load: '+des['path'])
            failed+=1

def process(des):
    m=False
    for line in open(des['path']).readlines():
        if line.startswith('MAP'):
            m=''
        elif line.startswith('ENDMAP'):
            convert(m,des)
            m=False
        elif m!=False:
            m+=line

des=[]
for root, dirs, files in os.walk('.'):
    for f in files:
        if '.des' in f:
            des.append(dict(directory=root,filename=f,path=os.path.join(root,f)))
for d in des:
    process(d)
    
print(f'{maps} converted succesfully, {failed} failed')

import re,os,glob
src=[p for p in glob.glob('app/src/**/*.*',recursive=True) if p.endswith(('.kt','.xml'))]
text={p:open(p,encoding='utf-8').read() for p in src}
main=[p for p in src if p.replace(chr(92),'/').startswith('app/src/main/java') and p.endswith('.kt')]
pat=re.compile(r'^(?:(?:public|internal)\s+)?(?:(?:data|sealed|enum|abstract|open|value|inline)\s+)*(?:class|object|interface|fun|val|const val)\s+(?:<[^>]*>\s*)?(?:[\w<>?, ]+\.)?(\w+)',re.M)
mem=re.compile(r'^    (?:(?:public|internal)\s+)?(?:const val|val|fun)\s+(?:<[^>]*>\s*)?(?:[\w<>?, ]+\.)?(\w+)',re.M)
allow=('CoreActivity','CoreFragment','CoreLayout','Outcome')
for p in main:
    t=text[p]; names=set(pat.findall(t))
    if re.search(r'^object ',t,re.M): names|=set(mem.findall(t))
    for n in names:
        if n in allow or len(n)<2: continue
        # skip preview
        if re.search(r'@Preview[^\n]*\n(?:@[^\n]*\n)*\s*(?:private\s+)?fun\s+'+n+r'\b',t): continue
        if re.search(r'private\s+(?:const\s+)?(?:val|fun|class|object)\s+(?:[\w<>?, ]+\.)?'+n+r'\b',t): continue
        if not any(re.search(r'\b'+n+r'\b',text[o]) for o in src if o!=p):
            print('UNREF',n,p)

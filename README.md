# EM-with-Inside-outside
Check Parser.java 
EM algorithm as driven function to run inside and outside algorithms (line 303) 
pseudo code structure is showed below: 
1. read CFG as g from the test.txt 
2. Input string named intstr 
3. While not converged 

   3.1. Update pre_g with current g 
   3.2. Run inside and outside functions 
   3.3. for every NT in nonTerminal, compute c(v) 
   3.4. for every p in Production T, conmpute cva, keep the running sum 
   3.5. for every p in Production N, compute cvyz, keep the running sum
   3.6.check conversion by calling compareRules function, if converged 
      3.6.1. for every p in Production T, compute eva 
      3.6.2. for every p in Production N, compute tvyz 
      

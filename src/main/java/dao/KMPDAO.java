package dao;

import util.ProgressMonitor;

public class KMPDAO {
    private final String pattern;
    private int[] f;
    private int nComp;

    public KMPDAO(String pattern) {
        this.pattern = pattern;
        this.nComp = 0;
        buildFailureFunction();
    }

    public int getNComp() {
        return nComp;
    }

    public String getPattern() {
        return pattern;
    }

    public int getPatternLength() {
        return pattern.length();
    }

    private void buildFailureFunction() {
        f = new int[pattern.length()];
        f[0] = 0;


        int x = 0;
        for (int j = 1; j < pattern.length(); j++) {
            if(pattern.charAt(x) == pattern.charAt(j)) {
                f[j] = ++x;
            } else {
                if(x == 0) f[j] = 0;
                else {
                    x = f[x - 1];
                    j--;
                }
            }
        }
    }

    public long searchPattern(String hayStack) {
        this.nComp = 0;
        long foundAt = -1;
        int currentAt = 0;
        int state = 0;
        int j = 0;
        boolean found = false;

        long end = hayStack.length() - pattern.length();
        ProgressMonitor progressMonitor = new ProgressMonitor("KMP Pattern Matching");
        progressMonitor.start();
        while ((currentAt <= end + 1) && !found) {
            this.nComp++;
            if(pattern.charAt(state) == hayStack.charAt(j)) {
                j++;
                if(state++ == (pattern.length() - 1)) {
                    foundAt = currentAt;
                    found = true;
                }
            } else {
                if(state == 0) {
                    currentAt = ++j;
                }else {
                    state = f[state];
                    currentAt = j - state + 1;
                }
            }
        }
        try{
            progressMonitor.endProcess();
            progressMonitor.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return foundAt;
    }

    public void printFailure() {
        System.out.print("Failure Function (" + pattern + "):\n   j: | ");
        for (int i = 0; i < f.length; i++) {
            System.out.print(i + " | ");
        }
        System.out.print("\nf(j): | ");
        for (int x: f) {
            System.out.print(x + " | ");
        }
        System.out.println();
    }
}

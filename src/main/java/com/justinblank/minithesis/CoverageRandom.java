package com.justinblank.minithesis;

import com.justinblank.coverage.BranchCoverage;
import com.justinblank.coverage.CoverageLoader;
import com.justinblank.coverage.CoverageRecord;

import java.util.*;

class CoverageRandom extends RandomGen {

    private final Map<CoverageRecord, MinMaxChoices> coverageSeen = new HashMap<>();
    private int testCount = 0;

    private final Deque<List<Integer>> pending = new ArrayDeque<>();
    final List<Integer> choices = new ArrayList<>();

    List<Integer> candidate;
    private int indexToMutate = 0;

    CoverageRandom() {
        CoverageLoader.init();
    }

    @Override
    void postTest(List<Integer> tcChoices) {
        choices.clear();
        testCount++;
        var coverageRecord = new CoverageRecord();
        BranchCoverage.clearBranches();
        if (coverageSeen.containsKey(coverageRecord)) {
            var current = coverageSeen.get(coverageRecord);
            // update the record for this trace with smallest/largest values that follow it
            if (compare(current.min, tcChoices) == 1) {
                pending.remove(current.min);
                current.min = tcChoices;
                pending.add(new ArrayList<>(tcChoices));
            }
            else if (compare(tcChoices, current.max) == 1) {
                pending.remove(current.max);
                current.max = tcChoices;
                pending.add(new ArrayList<>(tcChoices));
            }
        }
        else {
            coverageSeen.put(coverageRecord, new MinMaxChoices(tcChoices, tcChoices));
            pending.add(new ArrayList<>(tcChoices));
        }
        // Try to gather a pool of seeds before we begin mutating
        if (testCount > 12) {
            var seed = pending.poll();
            if (seed != null && !seed.isEmpty()) {
                indexToMutate = super.nextInt(seed.size());
                candidate = seed;
                pending.add(seed);
            }
        }
    }

    /**
     * Compare two lists of integers lexicographically: [1,2,3] sorts before [1,2,4], [2,5,7] before [3,1,1].
     * If one list is a prefix of the other, then the shorter list sorts first
     * @param left a list
     * @param right a list
     * @return a comparison (-1, 0, or 1)
     */
    private int compare(List<Integer> left, List<Integer> right) {
        var lInt = left.iterator();
        var rInt = right.iterator();
        while (lInt.hasNext() && rInt.hasNext()) {
            int cmp = lInt.next().compareTo(rInt.next());
            if (cmp != 0) {
                return cmp;
            }
        }
        if (lInt.hasNext()) {
            return 1;
        }
        else if (rInt.hasNext()) {
            return -1;
        }
        else {
            return 0;
        }
    }

    @Override
    void postTestSuite() {
        coverageSeen.clear();
        pending.clear();
        testCount = 0;
    }

    @Override
    public int nextInt() {
        return nextInt(Integer.MAX_VALUE);
    }

    @Override
    public int nextInt(int bound) {
        int i = 0;
        if (candidate != null) {
            if (candidate.size() > choices.size()) {
                i = candidate.get(choices.size());
                if (choices.size() == indexToMutate) {
                    i = super.nextInt(bound);
                }
            }
            if (i > bound) {
                i = super.nextInt(bound);
            }
        }
        else {
            i = super.nextInt(bound);
        }
        choices.add(i);
        return i;
    }

    public Collection<MinMaxChoices> seen() {
        return coverageSeen.values();
    }

    public static class MinMaxChoices {
        public List<Integer> min;
        public List<Integer> max;

        public MinMaxChoices(List<Integer> min, List<Integer> max) {
            this.min = min;
            this.max = max;
        }
    }
}

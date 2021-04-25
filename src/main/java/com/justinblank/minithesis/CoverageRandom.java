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
            if (seed != null) {
                indexToMutate = super.nextInt(seed.size());
                candidate = seed;
                pending.add(seed);
            }
        }
    }

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

    static class MinMaxChoices {
        private List<Integer> min;
        private List<Integer> max;

        public MinMaxChoices(List<Integer> min, List<Integer> max) {
            this.min = min;
            this.max = max;
        }
    }
}

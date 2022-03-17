package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.wordnet;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.sewordsim.PorterStemmer;
import edu.uniba.di.lacam.kdde.lexical_db.ILexicalDatabase;
import edu.uniba.di.lacam.kdde.lexical_db.data.Concept;
import edu.uniba.di.lacam.kdde.lexical_db.item.POS;
import edu.uniba.di.lacam.kdde.ws4j.Relatedness;
import edu.uniba.di.lacam.kdde.ws4j.RelatednessCalculator;
import org.deeplearning4j.text.stopwords.StopWords;

import java.util.*;
import java.util.stream.Collectors;

public class Ezzikouri extends RelatednessCalculator {

    private static final double MIN = 0.0;
    private static final double MAX = 1.0;
    private static final List<POS[]> POS_PAIRS = List.of(
            new POS[] { POS.NOUN, POS.NOUN },
            new POS[] { POS.VERB, POS.VERB }
    );

    public Ezzikouri(ILexicalDatabase db) {
        super(db, MIN, MAX);
    }

    @Override protected Relatedness calcRelatedness(Concept first, Concept second) {
        Set<String> firstGloss = stem(clean(getGlossWords(first)));
        Set<String> secondGloss = stem(clean(getGlossWords(second)));
        Set<String> firstWords = stem(clean(db.getWords(first)));
        Set<String> secondWords = stem(clean(db.getWords(second)));

        double wordsScore = intersection(firstWords, secondWords) / union(firstWords, secondWords);
        double glossScore = intersection(firstGloss, secondGloss) / union(firstGloss, secondGloss);
        double score = (wordsScore + glossScore) / 2.0;

        //double score = (intersection(firstWords, secondWords) + intersection(firstGloss, secondGloss))
        //                            / (union(firstWords, firstGloss, secondWords, secondGloss));

        return new Relatedness(score);
    }

    @Override public List<POS[]> getPOSPairs() { return POS_PAIRS; }

    private double union(Collection<String> first, Collection<String> second) {
        var strings = new HashSet<String>();
        strings.addAll(first);
        strings.addAll(second);
        return strings.size();
    }

    private double intersection(Set<String> first, Set<String> second) {
        // Assumption: first and second do not contain duplicates themselves (Set<> prevents that)
        int count = 0;

        for (String element : first) {
            if (second.contains(element)) {
                count++;
            }
        }

        return count;
    }

    private Set<String> stem(Set<String> strings) {
        return strings.stream()
                .map(PorterStemmer::stem)
                .collect(Collectors.toSet());
    }

    private Set<String> clean(List<String> strings) {
        var stopWords = StopWords.getStopWords();

        return strings.stream()
                .filter(str -> !stopWords.contains(str))
                .collect(Collectors.toSet());
    }

    private List<String> getGlossWords(Concept concept) {
        return Arrays.stream(db.getGloss(concept, null)
                .get(0)
                .split(" "))
                .toList();
    }

}

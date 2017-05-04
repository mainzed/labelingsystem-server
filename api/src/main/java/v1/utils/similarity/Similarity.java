package v1.utils.similarity;

import info.debatty.java.stringsimilarity.*;

/**
 * A library implementing different string similarity and distance measures. A
 * dozen of algorithms (including Levenshtein edit distance and sibblings,
 * Jaro-Winkler, Longest Common Subsequence, cosine similarity etc.) are
 * currently implemented. Check the summary table below for the complete list...
 * https://github.com/tdebatty/java-string-similarity
 *
 * @author Copyright 2015 Thibault Debatty
 * https://github.com/tdebatty/java-string-similarity/blob/master/LICENSE.md
 *
 */
public class Similarity {

	// distance, normalized [no], metric [yes]
	public static double Levenshtein(String s1, String s2) {
		Levenshtein l = new Levenshtein();
		return l.distance(s1, s2);
	}

	// distance/similarity, normalized [yes], metric [no]
	public static double NormalizedLevenshtein(String s1, String s2) {
		NormalizedLevenshtein nl = new NormalizedLevenshtein();
		return nl.distance(s1, s2);
	}

	// distance, normalized [no], metric [no]
	public static double WeightedLevenshtein(String s1, String s2) {
		WeightedLevenshtein wl = new WeightedLevenshtein(
				new CharacterSubstitutionInterface() {
			public double cost(char c1, char c2) {
				// The cost for substituting 't' and 'r' is considered
				// smaller as these 2 are located next to each other
				// on a keyboard
				if (c1 == 't' && c2 == 'r') {
					return 0.5;
				}
				// For most cases, the cost of substituting 2 characters
				// is 1.0
				return 1.0;
			}
		});
		return wl.distance(s1, s2);
	}

	// distance, normalized [no], metric [no]
	public static double Damerau(String s1, String s2) {
		Damerau d = new Damerau();
		return d.distance(s1, s2);
	}

	// distance/similarity, normalized [yes], metric [no]
	public static double JaroWinkler(String s1, String s2) {
		JaroWinkler jw = new JaroWinkler();
		return jw.similarity(s1, s2);
	}

	// distance, normalized [no], metric [no]
	public static double LongestCommonSubsequence(String s1, String s2) {
		LongestCommonSubsequence lcs = new LongestCommonSubsequence();
		return lcs.distance(s1, s2);
	}

	// distance, normalized [yes], metric [yes]
	public static double MetricLCS(String s1, String s2) {
		MetricLCS lcs = new MetricLCS();
		return lcs.distance(s1, s2);
	}

	// distance, normalized [yes], metric [no]
	public static double twoGram(String s1, String s2) {
		NGram twogram = new NGram(2);
		return twogram.distance(s1, s2);
	}

	// distance, normalized [yes], metric [no]
	public static double NGram(String s1, String s2) {
		NGram ngram = new NGram(4);
		return ngram.distance(s1, s2);
	}

	// distance, normalized [no], metric [no]
	public static double QGram(String s1, String s2) {
		QGram dig = new QGram(2);
		return dig.distance(s1, s2);
	}
}

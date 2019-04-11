package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages
 * in which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {

	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the
	 * associated value is an array list of all occurrences of the keyword in
	 * documents. The array list is maintained in DESCENDING order of frequencies.
	 */
	HashMap<String, ArrayList<Occurrence>> keywordsIndex;

	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;

	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String, ArrayList<Occurrence>>(1000, 2.0f);
		noiseWords = new HashSet<String>(100, 2.0f);
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword
	 * occurrences in the document. Uses the getKeyWord method to separate keywords
	 * from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an
	 *         Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String, Occurrence> loadKeywordsFromDocument(String docFile) throws FileNotFoundException {
		// Create a scanner to read the file.
		Scanner scanner = new Scanner(new File(docFile));

		// Create a HashMap to access occurrences by a keyword.
		HashMap<String, Occurrence> keywordToOcc = new HashMap<>();

		// Read every line of the document.
		while (scanner.hasNext()) {
			// Read the next line of the document.
			String line = scanner.nextLine();

			// Split the line string by any whitespace "\s".
			String[] lineTokens = line.split("\\s");

			for (String token : lineTokens) {
				// Get keyword from token.
				String keyword = this.getKeyword(token);

				if (keyword != null) {
					// If hashMap doesn't contain the current keyword, add it.
					keywordToOcc.putIfAbsent(keyword, new Occurrence(docFile, 0));

					// Increment the occurrence frequency.
					Occurrence occ = keywordToOcc.get(keyword);
					occ.frequency++;
				}
			}

		}

		// Close the scanner.
		scanner.close();

		return keywordToOcc;
	}

	/**
	 * Merges the keywords for a single document into the master keywordsIndex hash
	 * table. For each keyword, its Occurrence in the current document must be
	 * inserted in the correct place (according to descending order of frequency) in
	 * the same keyword's Occurrence list in the master hash table. This is done by
	 * calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String, Occurrence> kws) {
		// should check for doc duplicates?

		// Get all keys in the "kws" HashTable.
		Set<String> keys = kws.keySet();

		for (String key : keys) {
			// Occurrence in the "kws" HashTable.
			Occurrence kwsOcc = kws.get(key);

			this.keywordsIndex.putIfAbsent(key, new ArrayList<>());

			// Get the occurrences in the main table.
			ArrayList<Occurrence> occs = this.keywordsIndex.get(key);

			// Add "kwsOcc" to the main table "occs".
			occs.add(kwsOcc);

			// Re-sort the array.
			insertLastOccurrence(occs);

		}
	}

	/**
	 * Given a character, determines whether or not it contains punctuation.
	 * 
	 * @param ch character to check
	 * @return whether or not the character is punctuation
	 */
	private static boolean isPunc(char ch) {
		return ch == '.' || ch == ',' || ch == '?' || ch == ':' || ch == ';' || ch == '!';
	}

	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of
	 * any trailing punctuation(s), consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!' NO
	 * OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
	 * 
	 * If a word has multiple trailing punctuation characters, they must all be
	 * stripped So "word!!" will become "word", and "word?!?!" will also become
	 * "word"
	 * 
	 * See assignment description for examples
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) {
		// Convert the word to lower case. (Treated as case insensitive)
		word = word.toLowerCase();

		// Find the last index.
		int endingIndex = word.length() - 1;

		// Ignore trailing punctuation.
		while (endingIndex >= 0 && isPunc(word.charAt(endingIndex))) {
			endingIndex--;
		}

		// Make sure every character is alphabetic.
		for (int i = 0; i <= endingIndex; ++i) {
			char ch = word.charAt(i);
			boolean isAlphabetic = Character.isAlphabetic(ch);

			// If a character is not alphabetic, return null.
			if (!isAlphabetic) {
				return null;
			}
		}

		// Given the word is alphabetic, extract it from the string.
		String keyword = word.substring(0, endingIndex + 1);

		// If resulting keyword is empty, return null.
		if (keyword.length() < 1) {
			return null;
		}

		// If the word is a noise word, return null.
		if (this.noiseWords.contains(keyword)) {
			return null;
		}

		return keyword;
	}

	private int binarySearch(Occurrence lastOcc, int lo, int hi, ArrayList<Occurrence> occs,
			ArrayList<Integer> midpoints) {
		while (lo <= hi) {
			int mid = (hi - lo) / 2 + lo;
			midpoints.add(mid);

			if (lastOcc.frequency == occs.get(mid).frequency) {
				return mid;
			}

			if (lastOcc.frequency < occs.get(mid).frequency) {
				lo = mid + 1;
			} else {
				hi = mid - 1;
			}
		}

		return lo;
	}

	/**
	 * Inserts the last occurrence in the parameter list in the correct position in
	 * the list, based on ordering occurrences on descending frequencies. The
	 * elements 0..n-2 in the list are already in the correct order. Insertion is
	 * done by first finding the correct spot using binary search, then inserting at
	 * that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary
	 *         search process, null if the size of the input list is 1. This
	 *         returned array list is only used to test your code - it is not used
	 *         elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		// Store the midpoints that we encounter.
		ArrayList<Integer> midpoints = new ArrayList<>();

		// Remove and store the last occurrence.
		Occurrence lastOcc = occs.remove(occs.size() - 1);

		// Binary search for the position to add it to.
		int pos = binarySearch(lastOcc, 0, occs.size() - 1, occs, midpoints);

		// Add it there.
		if (pos > occs.size() - 1) {
			occs.add(lastOcc);
		} else {
			occs.add(pos, lastOcc);
		}

		// Return the midpoints.
		return midpoints;
	}

	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all
	 * keywords, each of which is associated with an array list of Occurrence
	 * objects, arranged in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile       Name of file that has a list of all the document file
	 *                       names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise
	 *                       word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input
	 *                               files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}

		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String, Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}

	public void addItems(ArrayList<Occurrence> occs, ArrayList<Occurrence> results, Set<String> seenDocs) {
		if (occs == null)
			return;

		for (Occurrence occ : occs) {
			if (!seenDocs.contains(occ.document)) {
				results.add(occ);
				seenDocs.add(occ.document);
				insertLastOccurrence(results);
			}
		}
	}

	public ArrayList<String> first(ArrayList<Occurrence> occs, int top) {
		ArrayList<String> out = new ArrayList<>();

		for (int i = 0; i < top && i < occs.size(); ++i) {
			Occurrence occ = occs.get(i);
			out.add(occ.document);
		}

		return out;
	}

	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2
	 * occurs in that document. Result set is arranged in descending order of
	 * document frequencies.
	 * 
	 * Note that a matching document will only appear once in the result.
	 * 
	 * Ties in frequency values are broken in favor of the first keyword. That is,
	 * if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same
	 * frequency f1, then doc1 will take precedence over doc2 in the result.
	 * 
	 * The result set is limited to 5 entries. If there are no matches at all,
	 * result is null.
	 * 
	 * See assignment description for examples
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in
	 *         descending order of frequencies. The result size is limited to 5
	 *         documents. If there are no matches, returns null or empty array list.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		ArrayList<Occurrence> searchResults = new ArrayList<>();
		Set<String> seenDocs = new HashSet<>();

		addItems(this.keywordsIndex.get(kw1), searchResults, seenDocs);
		addItems(this.keywordsIndex.get(kw2), searchResults, seenDocs);
//		stableSort();

		return first(searchResults, 5);
	}
}

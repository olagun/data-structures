package lse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class LittleSearchEngineDriver {
	static String prefix = "~ ";

	public static void main(String[] args) {
		Scanner scn = new Scanner(System.in);
		System.out.println("🔍 creating a little search engine... ");
		LittleSearchEngine ls = new LittleSearchEngine();
		System.out.println("🤓 little search engine created!");

		while (true) {
			System.out.println();
			System.out.println("🛤 enter...");
			System.out.println("(m) ~ makeIndex");
			System.out.println("(l) ~ loadKeywordsFromDocument");
			System.out.println("(k) ~ getKeyword");
			System.out.println("(M) ~ mergeKeywords");
			System.out.println("(i) ~ insertLastOccurrence");
			System.out.println("(t) ~ top5Search");
			System.out.println();
			System.out.print("> ");

			String next = scn.nextLine();

			if (next.equals("m")) {
				makeIndex(ls, scn);
			} else if (next.equals("l")) {
				loadKeywordsFromDocument(ls, scn);
			} else if (next.equals("k")) {
				getKeyword(ls, scn);
			} else if (next.equals("M")) {
				mergeKeywords(ls, scn);
			} else if (next.equals("i")) {
				insertLastOccurrence(ls, scn);
			} else if (next.equals("t")) {
				top5Search(ls, scn);
			}

			System.out.println(prefix + "👋 bye");
		}
	}

	// verify that the last occurrence is still in order.
	public static void insertLastOccurrence(LittleSearchEngine ls, Scanner scn) {

		// randomly generate test cases:
		// one element
		// two elements
		// number less than
		// number greater than
		// number anywhere in the middle

		// validate it with the current function
		// if invalid, print and return case
	}

	public static void top5Search(LittleSearchEngine ls, Scanner scn) {
		System.out.println(prefix + "calling 'top5Search'...");

//		while (true) {
		System.out.print(prefix + "🔑 (1) enter a key > ");
		String kw1 = scn.next();

		System.out.print(prefix + "🔑 (2) enter a key > ");
		String kw2 = scn.next();
		scn.nextLine();

		ArrayList<String> a = ls.top5search(kw1, kw2);

		if (a != null) {
			for (int i = 0; i < 5; ++i) {
				String s = "'null'";
				if (i < a.size())
					s = a.get(i);
				System.out.println(prefix + (i + 1) + ") " + s);
			}
		}

//			System.out.println(prefix + "press enter to continue or (q) to quit > ");
//			String next = scn.nextLine();
//
//			if (next.equals("q")) {
//				break;
//			}
//		}

		System.out.println(prefix + "exiting 'top5Search'...");

	}

	public static void mergeKeywords(LittleSearchEngine ls, Scanner scn) {

		while (true) {
			System.out.println(prefix + "📄 enter a document to merge or (q) to quit or (v) to view the hashmap: ");
			String doc = scn.nextLine();

			if (doc.equals("q")) {
				break;
			} else if (doc.equals("v")) {
				Object[] keys = ls.keywordsIndex.keySet().toArray();

				for (Object key : keys) {
					String s = (String) key;
					// print each document and its frequency
					ArrayList<Occurrence> occs = ls.keywordsIndex.get(s);

					String e = prefix + s + ": ";
					HashMap<String, Occurrence> freqs = new HashMap<>();

					for (Occurrence o : occs) {
						if (freqs.containsKey(o.document)) {
							freqs.get(o.document).frequency += o.frequency;
						} else {
							freqs.put(o.document, o);
						}
					}

					for (String k : freqs.keySet()) {
						e += String.format("(%d) %s", freqs.get(k).frequency, k);
						e += " ";
					}

					System.out.println(e);
				}

			} else {
				System.out.println(prefix + "calling 'loadKeywordsFromDocument'...");
				try {
					HashMap<String, Occurrence> map = ls.loadKeywordsFromDocument(doc);
					System.out.println(prefix + "calling 'mergeKeywords'...");
					ls.mergeKeywords(map);

				} catch (FileNotFoundException e) {

				}
			}

		}

		System.out.println(prefix + "exiting 'mergeKeywords'...");

	}

	public static void getKeyword(LittleSearchEngine ls, Scanner scn) {
		while (true) {

			System.out.print(prefix + "enter a keyword > ");
			String keyword = scn.nextLine();
			System.out.println(prefix + "calling 'getKeyword'...");
			// this is not right
			String k = ls.getKeyword(keyword);

			if (k != null) {
				boolean notAlpha = false;
				boolean notLowercase = false;
				String lowerE = "", alphaE = "";

				for (int i = 0; i < k.length(); ++i) {
					char ch = k.charAt(i);
					boolean isAlphabetic = Character.isAlphabetic(ch);
					boolean isLowercase = Character.isLowerCase(ch);

					if (!isAlphabetic) {
						notAlpha = true;
						alphaE = ch + "";
					}

					if (isAlphabetic && !isLowercase) {
						notLowercase = true;
						lowerE = ch + "";
					}
				}

				if (notLowercase) {
					System.out.printf(prefix + "⚠️ some keys are not lowercase, for example: '%s'\n", lowerE);
				}

				if (notAlpha) {
					System.out.printf(prefix + "⚠️ some keys contain non-alphabetic characters, for example: '%s'\n",
							alphaE);
				}

				if (!(notLowercase || notAlpha)) {
					System.out.println("all good!");
				}
			} else {
				boolean isAlpha = true;
				for (int i = 0; i < keyword.length(); ++i) {
					if (!Character.isAlphabetic(keyword.charAt(i))) {
						isAlpha = false;
						break;
					}
				}

				if (isAlpha) {
					System.out.println(prefix + "all good!");
				} else {
					System.out.println(prefix
							+ "⚠️ your 'getKeyword' returned 'null' even though all of the characters are alphabetic.");
				}
			}

			System.out.println(prefix + "press (q) to quit or enter to continue > ");
			String l = scn.nextLine();

			if (l.equals("q")) {
				break;
			}
		}
		System.out.println(prefix + "exiting 'getKeyword'...");
	}

	public static void loadKeywordsFromDocument(LittleSearchEngine ls, Scanner scn) {
		System.out.println(prefix + "calling 'loadKeywordsFromDocument'...");

		try {
			Scanner sc = new Scanner(new File("docs.txt"));
			while (true) {
				String ln = sc.nextLine();
				System.out.printf(prefix + "loading keywords from '%s'\n", ln);
				HashMap<String, Occurrence> hm = ls.loadKeywordsFromDocument(ln);

				Object[] keys = hm.keySet().toArray();

				boolean notAlpha = false;
				boolean notLowercase = false;
				String lowerE = "", alphaE = "";
				for (Object key : keys) {
					String k = (String) key;
					boolean isLowercase = k.equals(k.toLowerCase());

					for (int i = 0; i < k.length(); ++i) {
						char ch = k.charAt(i);
						boolean isAlphabetic = Character.isAlphabetic(ch);

						if (!isAlphabetic) {
							notAlpha = true;
							alphaE = k;
						}
					}

					if (!isLowercase) {
						notLowercase = true;
						lowerE = k;
					}
				}

				if (notLowercase) {
					System.out.printf(prefix + "⚠️ some keys are not lowercase, for example: '%s'\n", lowerE);
				}

				if (notAlpha) {
					System.out.printf(prefix + "⚠️ some keys contain non-alphabetic characters, for example: '%s'\n",
							alphaE);
				}
				System.out.printf(prefix + "%d total keys\n", keys.length);

				System.out.println(prefix + "📄 enter (v) to view the hashmap or enter to quit: ");

				String l = scn.nextLine();

				if (l.equals("v")) {
					for (Object key : keys) {
						String s = (String) key;
						// print each document and its frequency
						Occurrence o = hm.get(s);

						String e = prefix + s + ": ";
						HashMap<String, Integer> freqs = new HashMap<>();

						freqs.put(o.document, 1);

						for (String k : freqs.keySet()) {
							e += String.format("(%d) %s", freqs.get(k), k);
							e += " ";
						}

						System.out.println(e);

					}
				} else {
					break;
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println(prefix + "couldn't find 'docs.txt'...");
			System.out.println(prefix + "try redownloading them from sakai.");
		}

		System.out.println(prefix + "exiting 'loadKeywordsFromDocument'...");

	}

	public static void makeIndex(LittleSearchEngine ls, Scanner scn) {
		System.out.println(prefix + "calling 'makeIndex'...");

		try {
			ls.makeIndex("docs.txt", "noisewords.txt");
			Object[] keys = ls.keywordsIndex.keySet().toArray();

			boolean notAlpha = false;
			boolean notLowercase = false;
			String lowerE = "", alphaE = "";
			for (Object key : keys) {
				String k = (String) key;
				boolean isLowercase = k.equals(k.toLowerCase());

				for (int i = 0; i < k.length(); ++i) {
					char ch = k.charAt(i);
					boolean isAlphabetic = Character.isAlphabetic(ch);

					if (!isAlphabetic) {
						notAlpha = true;
						alphaE = k;
					}
				}

				if (!isLowercase) {
					notLowercase = true;
					lowerE = k;
				}
			}

			if (notLowercase) {
				System.out.printf(prefix + "⚠️ some keys are not lowercase, for example: '%s'\n", lowerE);
			}

			if (notAlpha) {
				System.out.printf(prefix + "⚠️ some keys contain non-alphabetic characters, for example: '%s'\n",
						alphaE);
			}

			System.out.printf(prefix + "%d total keys\n", keys.length);
			System.out.print(prefix + "press (k) to show keys or enter to exit > ");
			String s = scn.nextLine();
			if (s.equals("k")) {
				for (Object key : keys) {
					String l = (String) key;
					System.out.println(prefix + l);
				}
			}

		} catch (

		FileNotFoundException e) {
			System.out.println(prefix + "couldn't find 'docs.txt' or 'noisewords.txt'...");
			System.out.println(prefix + "try redownloading them from sakai.");
		}

		System.out.println(prefix + "exiting 'makeIndex'...");
	}
}

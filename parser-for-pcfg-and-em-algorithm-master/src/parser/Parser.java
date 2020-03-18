package parser;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.*;
import java.io.*;

/*
 * CFG: context-free grammar
 */
class CFG {
	// productionsT: stores all production rules whose right side is a terminal
	HashMap<String, ArrayList<ProductionT>> productionsT = new HashMap<String, ArrayList<ProductionT>>();

	// productionsN : stores all production rules whose right side are two nonterminals
	HashMap<String, ArrayList<ProductionN>> productionsN = new HashMap<String, ArrayList<ProductionN>>();
	ArrayList<String> nonTerminals = new ArrayList<String>();


	// default constructor
	CFG() {
	}

	CFG(String filePath) {
		initG(filePath);
	}

	// initialize a CFG instance with a file of production rules
	private void initG(String filePath) {
		try (BufferedReader reader = new BufferedReader(new FileReader(
				filePath))) {
			String line = reader.readLine();
			while (line != null) {
				String[] strs = line.split("(\\s\\|?\\s?)");
				if (strs.length > 3) {
					// add a production rule whose right-hand-side consists of non-terminals
					addToProductionsN(strs[0], strs[1], strs[2], Float.valueOf(strs[3]));

				} else {
					// add a production rule whose right-hand-side consists of a terminal
					addToProductionsT(strs[0], strs[1], Float.valueOf(strs[2]));
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("file not found");
		} catch (IOException e) {
			System.out.println("IOexception");
		}
	}

	public void addToProductionsT(String l, String r, float p) {
		if (!productionsT.containsKey(l)) {
			productionsT.put(l, new ArrayList<ProductionT>());
		}
		productionsT.get(l).add(new ProductionT(l, r, p));
		addNonTerminal(l);

	}

	public void addToProductionsN(String l, String r1, String r2, float p) {
		if (!productionsN.containsKey(l)) {
			productionsN.put(l, new ArrayList<ProductionN>());
		}
		productionsN.get(l).add(new ProductionN(l, r1, r2, p));
		addNonTerminal(l);
	}

	public void addNonTerminal(String str) {
		if (!nonTerminals.contains(str)) {
			nonTerminals.add(str);
		}
	}

	// gives the index of a non-terminal from the arraylist of nonterminals
	public int sIndex() {
		int i = nonTerminals.indexOf("S");
		if (i == -1) {
			System.out.println("This CFG does not have a start terminal.");
			return 0;
		}
		return i;
	}

	public void printProductionsT() {
		Set<Map.Entry<String, ArrayList<ProductionT>>> pts = productionsT.entrySet();
		Iterator<Map.Entry<String, ArrayList<ProductionT>>> ptsIterator = pts.iterator();
		while (ptsIterator.hasNext()) {
			ArrayList<ProductionT> ptsArrList = ptsIterator.next().getValue();
			for (int i = 0; i < ptsArrList.size(); i++) {
				ProductionT pt = ptsArrList.get(i);
				System.out.println(pt.left + ", " + pt.right + ", " + pt.p);
			}
		}
	}

	public void printProductionsN() {
		Set<Map.Entry<String, ArrayList<ProductionN>>> pns = productionsN.entrySet();
		Iterator<Map.Entry<String, ArrayList<ProductionN>>> pnsIterator = pns.iterator();
		while (pnsIterator.hasNext()) {
			ArrayList<ProductionN> pnsArrList = pnsIterator.next().getValue();
			for (int i = 0; i < pnsArrList.size(); i++) {
				ProductionN pn = pnsArrList.get(i);
				System.out.println(pn.left + ", " + pn.right1 + ", " + pn.right2 + ", " + pn.p);
			}
		}
	}

	public void printNonterminals() {
		for (int i = 0; i < nonTerminals.size(); i++) {
			System.out.print(nonTerminals.get(i) + " ");
		}
	}

	// get the probability of emitting a char from a certain nonterminal
	public float e(String nonTerminal, char ch) {
		float p = 0;
		ArrayList<ProductionT> pts = productionsT.get(nonTerminal);
		if (pts != null) {
			for (int i = 0; i < pts.size(); i++) {
				ProductionT pt = pts.get(i);
				if (pt.right.equals(Character.toString(ch))) {
					p = pt.p;
				}
			}
		}
		return p;

	}

	// get the probability of generating v -> y z, where v, y, z are nonTerminals
	public float t(String v, String y, String z) {
		float p = 0;
		ArrayList<ProductionN> pns = productionsN.get(v);
		if (pns != null) {
			for (int i = 0; i < pns.size(); i++) {
				ProductionN pn = pns.get(i);
				if (pn.right1.equals(y) && pn.right2.equals(z)) {
					p = pn.p;

				}
			}
		}
		return p;
	}


	// run inside algorithm given a string input
	public float[][][] inside(String str) {
		int l = str.length();
		int m = nonTerminals.size();
		float[][][] alpha = new float[l][l][m];
		// initialization
		for (int i = 0; i < l; i++) {
			for (int v = 0; v < m; v++) {
				alpha[i][i][v] = e(nonTerminals.get(v), str.charAt(i));
			}
		}

		// iteration
		for (int i = l - 1; i >= 0; i--) {
			for (int j = i + 1; j < l; j++) {
				for (int v = 0; v < m; v++) {
					// calculating a single alpha value: alpha[i][j][v]
					for (int y = 0; y < m; y++) {
						for (int z = 0; z < m; z++) {
							for (int k = i; k <= j - 1; k++) {
								alpha[i][j][v] += alpha[i][k][y] * alpha[k + 1][j][z] * t(nonTerminals.get(v), nonTerminals.get(y), nonTerminals.get(z));

							}
						}
					}
				}
			}
		}

		this.alpha = alpha;
		return alpha;
	}

	// run outside algorithm given a string input
	public float[][][] outside(String str) {
		int l = str.length();
		int m = nonTerminals.size();
		float[][][] beta = new float[l][l][m];
		float[][][] alpha = inside(str);

		// initialization:
		for (int v = 0; v < m; v++) {
			if (nonTerminals.get(v).equals("S")) {
				beta[0][l - 1][v] = 1;
			} else {
				beta[0][l - 1][v] = 0;
			}
		}

		// iteration:
		for (int i = 0; i < l; i++) {
			for (int j = l - 1; j >= i; j--) {
				for (int v = 0; v < m; v++) {
					// calculate beta:

					// for each pair of (y, z)
					for (int y = 0; y < m; y++) {
						for (int z = 0; z < m; z++) {

							// k1 goes from 0 to i-1; inclusive
							// k2 goes from j+1 to l-1; inclusive
							for (int k1 = 0; k1 < i; k1++) {
								if (i > 0) {
									beta[i][j][v] += alpha[k1][i - 1][z] * beta[k1][j][y] * t(nonTerminals.get(y), nonTerminals.get(z), nonTerminals.get(v));
								}
							}
							for (int k2 = j + 1; k2 < l; k2++) {
								beta[i][j][v] += alpha[j + 1][k2][z] * beta[i][k2][y] * t(nonTerminals.get(y), nonTerminals.get(v), nonTerminals.get(z));

							}
						}
					}
				}
			}
		}

		// Termination:
		for (int i = 0; i < l; i++) {
			float p = 0;
			for (int v = 0; v < m; v++) {
				// verification of correctness: p is the same for all i
				p += beta[i][i][v] * e(nonTerminals.get(v), str.charAt(i));
			}
		}
		this.beta = beta;
		return beta;
	}

	float[][][] alpha;
	float[][][] beta;
	float p;


	// getter methods for alpha and beta
	//public float[][][] getAlpha(){
	//	return alpha;
	//}

	//public float[][][] getBeta(){
	//	return beta;
	//}


	/*
	 * compareRules function compares the production rules in CFG instances g1 and g2
	 * g1 and g2 have the same set of production rules, but different probabilities
	 * and so each rule in g1 has a corresponding rule in g2 with a different probability
	 * if the difference between probabilities of EVERY two corresponding rules in g1 and g2 is smaller than the threshold, compareRules returns true. Otherwise, it returns false
	 * assume that rules in g1 and g2 have the same order
	 */
	public static boolean compareRules(float threshold, CFG g1, CFG g2) {
		// an arraylist of nonterminals
		ArrayList<String> nts = g1.nonTerminals;

		//loop through all nonterminals
		for (int i = 0; i < nts.size(); i++) {
			String nt = nts.get(i);

			//get production rules with nonterminal nt on the left hand side in g1 and g2
			ArrayList<ProductionT> pts1 = g1.productionsT.get(nt);
			ArrayList<ProductionN> pns1 = g1.productionsN.get(nt);

			ArrayList<ProductionT> pts2 = g2.productionsT.get(nt);
			ArrayList<ProductionN> pns2 = g2.productionsN.get(nt);

			//we assumed production rules in g1 and g2 have the same order
			//since g1 and g2 have the same set of rules, pts1 and pts2, pns1 and pns2 have the same size
			//now we compare the probabilities between each pair of corresponding rules in g1 and g2
			if (pts1 != null) {
				// pts1 == null means the nonterminal does not produce any terminal
				for (int j = 0; j < pts1.size(); j++) {
					ProductionT p1 = pts1.get(j);
					ProductionT p2 = pts2.get(j);
					if (p1.p - p2.p > threshold) {
						return false;
					}
				}
			}
			if (pns1 != null) {
				// pns1 == null means the nonterminal does not produce any pair of nonterminals
				for (int j = 0; j < pns1.size(); j++) {
					ProductionN p1 = pns1.get(j);
					ProductionN p2 = pns2.get(j);
					if (p1.p - p2.p > threshold) {
						return false;
					}
				}
			}
		}

		// if the difference between each pair of production rules probabilities in g1 and g2 is smaller than the threshold, return true
		return true;
	}


	//EM Algorithm as driven function
	public static void main(String[] args) {

		//Read in CFG as g
		CFG g = new CFG("/Users/suziexi/Desktop/parser-for-pcfg-and-em-algorithm-master/src/parser/test.txt");

		//Input string named "intstr"
		String intstr =
				"S | NP VP 1.0\n" +
				"PP | P NP 1.0\n" +
				"VP | V NP 0.5\n" +
				"VP | VP PP 0.5\n" +
				"P | a 1.0\n" +
				"V | g 1.0\n" +
				"NP | NP PP 0.25\n" +
				"NP | t 0.5\n" +
				"NP | c 0.25";

		//store the whole  pre_g
		int m = g.nonTerminals.size();
		int l = intstr.length();

		boolean conversion = false;

		//while it's not converaged
		while (conversion == false) {

			CFG pre_g = g; //update the pre_g at each round of training

			g.inside(intstr); //run inside
			g.outside(intstr); //run outside
			//System.out.println(g.outside(intstr));
			//System.out.println(g.inside(intstr));
			//System.out.println(g.p);

			float cva = 0;

			float cv = 0;
			float cv_sum = 0;

			float cvyz = 0;
			float cvyz_sum = 0;

			//ProductionN
			Set<Map.Entry<String, ArrayList<ProductionN>>> pts1 = g.productionsN.entrySet();
			Iterator<Map.Entry<String, ArrayList<ProductionN>>> ptsIterator1 = pts1.iterator();
			ArrayList<ProductionN> ptsArrList1 = ptsIterator1.next().getValue();

			//ProductionT
			Set<Map.Entry<String, ArrayList<ProductionT>>> pts2 = g.productionsT.entrySet();
			Iterator<Map.Entry<String, ArrayList<ProductionT>>> ptsIterator2 = pts2.iterator();
			ArrayList<ProductionT> ptsArrList2 = ptsIterator2.next().getValue();

			//For each large round while v increment by 1
			for (int v = 0; v < m; v++) {
				//For every NT in NonTerminals ---> cv
				for (int NT = 0; NT < m; NT++) {
					for (int i = 1; i < l; i++) {
						for (int j = i; j < l; j++) {
							//pre_cv = cv;
							cv_sum += g.beta[i][j][v] * g.alpha[i][j][v];
							cv = (1 / g.p) * cv_sum; //g.p is from outside function, p += beta[i][i][v] * e(nonTerminals.get(v), str.charAt(i));
						}
					}
				}

				//For every p in ProductionN--->cvyz
				while (ptsIterator1.hasNext()) {
					//for (int p = 0; p < ptsArrList1.size(); p++ ) {
					for (int i = 1; i < l - 1; i++) {
						for (int j = i + 1; j < l; j++) {
							for (int k = i; k < j - 1; k++) {
								for (int y = 0; y < m; y++) {
									for (int z = 0; z < m; z++) {
										cvyz_sum += g.beta[i][j][v] * g.alpha[i][k][y] * g.alpha[k + 1][j][z] * g.t(g.nonTerminals.get(v), g.nonTerminals.get(y), g.nonTerminals.get(z));
										cvyz = (1 / g.p) * cvyz_sum;
									}
								}
							}
						}
					}
				}

				//For every p in ProductionT --> cva
				while (ptsIterator2.hasNext()) {
					// for (int p = 0; p < ptsArrList2.size(); p++) {
					for (int i = 0; i < l; i++) {
						for (int a = 0; a < m; a++) {
							if (ptsArrList2.get(i).equals(a)) { //Only sum to cva when X(i) = a
								cva += g.beta[i][i][v] * g.e(g.nonTerminals.get(v),(char)a); // From previous code, e(nonTerminals.get(v), str.charAt(a));
							}
						}
					}
				}
			}

			/* At the end of each round of while loop, Check conversion:
			Check conversion of g and pre_g first, then calculate the values of eva and tvyz
			Because according to the compareRules function: if the difference between probabilities of EVERY two corresponding rules in g1 and g2
			(in this case pre_g and g) is smaller than the threshold, compareRules returns true. Otherwise, it returns false.

			And the input is g and pre_g, not the current and previous eva and tvyz
			*/
			float eva = 0;
			float tvyz = 0;
			if (compareRules(0, g, pre_g) == true) { //Can change the threshold value manually
				conversion = true; //if pre_g and g converged, mark the boolean conversion as true
				for (int p = 0; p < ptsArrList2.size(); p++) { //in Production T
					eva = cva/cv;
					System.out.println(eva);
				}
				for (int p = 0; p < ptsArrList2.size(); p++) { //in Production N
					tvyz = cvyz / cv;
					System.out.println(tvyz);
				}
				//System.out.println(conversion);
			} else {
				continue;
			}
		}
	}
}





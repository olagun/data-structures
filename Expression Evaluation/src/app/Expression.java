package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = "*+-/()[]";
	public static String spaceDelims = " |\t";

	private static float binaryEvaluate(char op, float a, float b) {
		switch (op) {
		case '*':
			return a * b;
		case '/':
			return a / b;
		case '+':
			return a + b;
		case '-':
			return a - b;
		default:
			return 0;
		}
	}

	private static int getOpPrecedence(char a) {
		switch (a) {
		case '*':
		case '/':
			return 1;
		case '-':
		case '+':
			return 0;
		default:
			return -1;
		}
	}

	private static boolean match(char target, char... values) {
		for (char value : values) {
			if (value == target) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Populates the vars list with simple variables, and arrays lists with arrays
	 * in the expression. For every variable (simple or array), a SINGLE instance is
	 * created and stored, even if it appears more than once in the expression. At
	 * this time, values for all variables and all array items are set to zero -
	 * they will be loaded from a file in the loadVariableValues method.
	 * 
	 * @param expr   The expression
	 * @param vars   The variables array list - already created by the caller
	 * @param arrays The arrays array list - already created by the caller
	 */
	public static void makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		final StringTokenizer st = new StringTokenizer(expr.replaceAll(spaceDelims, ""), delims, true);

		while (st.hasMoreTokens()) {
			final String tok = st.nextToken();

			if (Character.isLetter(tok.charAt(0))) {
				if (st.hasMoreTokens() && st.nextToken().charAt(0) == '[') {
					if (arrays.indexOf(new Array(tok)) < 0) {
						arrays.add(new Array(tok));
					}
				} else {
					if (vars.indexOf(new Variable(tok)) < 0) {
						vars.add(new Variable(tok));
					}
				}
			}
		}
	}

	/**
	 * Loads values for variables and arrays in the expression
	 * 
	 * @param sc Scanner for values input
	 * @throws IOException If there is a problem with the input
	 * @param vars   The variables array list, previously populated by
	 *               makeVariableLists
	 * @param arrays The arrays array list - previously populated by
	 *               makeVariableLists
	 */
	public static void loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays)
			throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String tok = st.nextToken();
			Variable var = new Variable(tok);
			Array arr = new Array(tok);
			int vari = vars.indexOf(var);
			int arri = arrays.indexOf(arr);
			if (vari == -1 && arri == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { // scalar symbol
				vars.get(vari).value = num;
			} else { // array symbol
				arr = arrays.get(arri);
				arr.values = new int[num];
				// following are (index,val) pairs
				while (st.hasMoreTokens()) {
					tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok, " (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					arr.values[index] = val;
				}
			}
		}
	}

	// expression ::= add
	// add ::= <multiply> (("+ | "-") <multiply>)*
	// multiply ::= <primitive> (("*" | "/") <primitive>)*
	// primitive ::= <digit> |"(" expression ")" | <identifier>
	// identifier ::= <name> ("[" expression "]")?

	/**
	 * Evaluates the expression.
	 * 
	 * @param vars   The variables array list, with values for all variables in the
	 *               expression
	 * @param arrays The arrays array list, with values for all array items
	 * @return Result of evaluation
	 */
	public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		expr = expr.replaceAll(spaceDelims, "");
		final StringTokenizer st = new StringTokenizer(expr, delims, true);
		final Stack<Float> values = new Stack<>();
		final Stack<Character> ops = new Stack<>();
		int currIndex = 0;

		while (st.hasMoreTokens()) {
			final String tok = st.nextToken();
			currIndex += tok.length();

			if (Character.isDigit(tok.charAt(0))) {
				values.push((float) Integer.parseInt(tok));
			} else if (Character.isLetter(tok.charAt(0))) {
				boolean isArray = false;

				if (currIndex < expr.length() && expr.charAt(currIndex) == '[') {
					isArray = true;
				}

				if (isArray) {
					final int arrIndex = arrays.indexOf(new Array(tok));

					// Discard first square bracket.
					currIndex += st.nextToken().length();
					final int startIndex = currIndex;

					int i = 1;
					while (i > 0) {
						final String nxtToken = st.nextToken();
						final char nxtChar = nxtToken.charAt(0);
						currIndex += nxtToken.length();

						if (match(nxtChar, ']')) {
							i--;
						} else if (match(nxtChar, '[')) {
							i++;
						}
					}

					final int index = (int) evaluate(expr.substring(startIndex, currIndex - 1), vars, arrays);
					values.push((float) arrays.get(arrIndex).values[index]);
				} else {
					final int varIndex = vars.indexOf(new Variable(tok));
					values.push((float) vars.get(varIndex).value);
				}
			} else if (match(tok.charAt(0), '(')) {
				final int startIndex = currIndex;

				int i = 1;
				while (i > 0) {
					final String nxtToken = st.nextToken();
					final char nxtChar = nxtToken.charAt(0);
					currIndex += nxtToken.length();

					if (match(nxtChar, ')')) {
						i--;
					} else if (match(nxtChar, '(')) {
						i++;
					}
				}

				final float value = evaluate(expr.substring(startIndex, currIndex - 1), vars, arrays);
				values.push(value);
			} else if (match(tok.charAt(0), '*', '/', '-', '+')) {
				// Assumes left associativity for equal operators.
				while (!ops.isEmpty() && getOpPrecedence(ops.peek()) >= getOpPrecedence(tok.charAt(0))) {
					final char opTop = ops.pop();

					final float rightOperand = values.pop();
					final float leftOperand = values.pop();

					final float value = binaryEvaluate(opTop, leftOperand, rightOperand);
					values.push(value);
				}

				ops.push(tok.charAt(0));
			}
		}

		while (!ops.isEmpty()) {
			final char opTop = ops.pop();

			final float rightOperand = values.pop();
			final float leftOperand = values.pop();

			final float value = binaryEvaluate(opTop, leftOperand, rightOperand);
			values.push(value);
		}

		return values.pop();
	}
}

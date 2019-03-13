package structures;

import java.util.*;

/**
 * This class implements an HTML DOM Tree. Each node of the tree is a TagNode,
 * with fields for tag/text, first child and sibling.
 * 
 */
public class Tree {

	/**
	 * Root node
	 */
	TagNode root = null;

	/**
	 * Scanner used to read input HTML file when building the tree
	 */
	Scanner sc;

	/**
	 * Initializes this tree object with scanner for input HTML file
	 * 
	 * @param sc Scanner for input HTML file
	 */
	public Tree(Scanner sc) {
		this.sc = sc;
		root = null;
	}

	private void addChild(TagNode parent, TagNode node) {
		TagNode headNode = new TagNode(null, null, parent.firstChild);
		TagNode currNode = headNode;

		while (currNode.sibling != null)
			currNode = currNode.sibling;

		currNode.sibling = node;
		parent.firstChild = headNode.sibling;
	}

	private TagNode build(TagNode parent) {
		if (!this.sc.hasNext())
			return parent;

		String line = this.sc.nextLine();
		if (line.charAt(0) == '<') {
			if (line.charAt(1) == '/')
				return parent;

			String tagName = line.substring(1, line.indexOf('>'));
			addChild(parent, build(new TagNode(tagName, null, null)));
		} else {
			addChild(parent, new TagNode(line, null, null));
		}

		return build(parent);
	}

	/**
	 * Builds the DOM tree from input HTML file, through scanner passed in to the
	 * constructor and stored in the sc field of this object.
	 * 
	 * The root of the tree that is built is referenced by the root field of this
	 * object.
	 */
	public void build() {
		/** COMPLETE THIS METHOD **/
		this.root = build(new TagNode("root", null, null)).firstChild;
	}

	private void replaceTag(TagNode parent, String oldTag, String newTag, int maxDepth) {
		if (parent == null || maxDepth <= 0)
			return;

		if (parent.tag.equals(oldTag))
			parent.tag = newTag;

		replaceTag(parent.firstChild, oldTag, newTag, maxDepth - 1);
		replaceTag(parent.sibling, oldTag, newTag, maxDepth);
	}

	private void replaceTag(TagNode parent, String oldTag, String newTag) {
		replaceTag(parent, oldTag, newTag, Integer.MAX_VALUE);
	}

	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag) {
		/** COMPLETE THIS METHOD **/
		TagNode newRoot = new TagNode("root", root, null);
		replaceTag(newRoot, oldTag, newTag);
		this.root = newRoot.firstChild;
	}

	private TagNode findTag(TagNode node, String tag) {
		if (node.tag.equals(tag))
			return node;

		TagNode childTag = findTag(node.firstChild, tag);

		if (childTag != null)
			return childTag;

		TagNode siblingTag = findTag(node.sibling, tag);

		if (siblingTag != null)
			return siblingTag;

		return null;
	}

	/**
	 * Boldfaces every column of the given row of the table in the DOM tree. The
	 * boldface (b) tag appears directly under the td tag of every column of this
	 * row.
	 * 
	 * @param row Row to bold, first row is numbered 1 (not 0).
	 */
	public void boldRow(int row) {
		TagNode table = findTag(root, "table");

		// Exit if no table.
		if (table == null)
			return;

		TagNode tr = table.firstChild;

		// Exit if no table rows.
		if (tr == null)
			return;

		for (int i = 1; i < row; ++i)
			// Check if there is a "tr" sibling before iterating.
			if (tr.sibling != null)
				tr = tr.sibling;
			else
				return;

		TagNode tds = tr.firstChild;
		while (tds != null) {
			tds.firstChild = new TagNode("b", tds.firstChild, null);
			tds = tds.sibling;
		}
	}

	/**
	 * Remove all occurrences of a tag from the DOM tree. If the tag is p, em, or b,
	 * all occurrences of the tag are removed. If the tag is ol or ul, then All
	 * occurrences of such a tag are removed from the tree, and, in addition, all
	 * the li tags immediately under the removed tag are converted to p tags.
	 * 
	 * @param tag Tag to be removed, can be p, em, b, ol, or ul
	 */
	public void removeTag(String tag) {
		TagNode tmpRoot = new TagNode("root", root, null);
		removeTag(tmpRoot, tag);
		this.root = tmpRoot.firstChild;
	}

	public void removeTag(TagNode parent, String tag) {
		if (parent == null)
			return;

		TagNode headChild = new TagNode(null, null, parent.firstChild);
		TagNode prevChild = headChild;
		TagNode currChild = headChild.sibling;

		while (currChild != null) {
			removeTag(currChild.firstChild, tag);

			if (currChild.tag.equals(tag)) {
				if (currChild.tag.equals("ul") || currChild.tag.equals("ol"))
					replaceTag(currChild, "li", "p", 1);

				prevChild.sibling = currChild.sibling;
				currChild = insertAfter(prevChild, currChild.firstChild);
			}

			prevChild = currChild;
			currChild = currChild.sibling;
		}

		parent.firstChild = headChild.sibling;
	}

	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag  Tag to be added
	 */
	public void addTag(String word, String tag) {
		TagNode tmpRoot = new TagNode("root", root, null);
		addTag(tmpRoot, word, tag);
		this.root = tmpRoot.firstChild;
	}

	public TagNode constructTag(String text, String word, String tag) {
		TagNode headNode = new TagNode(null, null, null);
		TagNode tail = headNode;

		StringTokenizer st = new StringTokenizer(text, "\t ", true);
		String strAcc = "";

		while (st.hasMoreTokens()) {
			String tok = st.nextToken();

			if (tok.contains(tag)) {
				if (strAcc.length() > 0) {
					tail.sibling = new TagNode(strAcc, null, null);
					tail = tail.sibling;
				}

				tail.sibling = new TagNode(tag, new TagNode(tok, null, null), null);
				tail = tail.sibling;
			} else {
				strAcc += tok;
			}
		}

		if (strAcc.length() > 0) {
			tail.sibling = new TagNode(strAcc, null, null);
			tail = tail.sibling;
		}

		return headNode.sibling;
	}

	private TagNode insertAfter(TagNode before, TagNode extension) {
		TagNode next = before.sibling;
		before.sibling = extension;

		TagNode currNode = before;
		while (currNode.sibling != null)
			currNode = currNode.sibling;

		currNode.sibling = next;
		return currNode;
	}

	private TagNode addTag(TagNode parent, String word, String tag) {
		if (parent == null)
			return parent;

		if (parent.firstChild == null)
			return constructTag(parent.tag, word, tag);

		addTag(parent.firstChild, word, tag);
		return insertAfter(parent.firstChild, addTag(parent.sibling, word, tag));

//		if (parent == null ) return;
//		
//		TagNode headChild = new TagNode(null, null, parent.firstChild);
//		TagNode prevChild = headChild;
//		TagNode currChild = headChild.sibling;
//
//		 
//		while (currChild != null) {
//			if (currChild.firstChild == null) {
//				TagNode newTag = constructTag(currChild.tag, word, tag);
//				// use circular linked list and replace (faster)
//				insertAfter(prevChild, newTag);
//			}
//
//			prevChild = currChild;
//			currChild = prevChild.sibling;
//		}
//
//		parent.firstChild = headChild.sibling;
	}

	/**
	 * Gets the HTML represented by this DOM tree. The returned string includes new
	 * lines, so that when it is printed, it will be identical to the input file
	 * from which the DOM tree was built.
	 * 
	 * @return HTML string, including new lines.
	 */
	public String getHTML() {
		StringBuilder sb = new StringBuilder();
		getHTML(root, sb);
		return sb.toString();
	}

	private void getHTML(TagNode root, StringBuilder sb) {
		for (TagNode ptr = root; ptr != null; ptr = ptr.sibling) {
			if (ptr.firstChild == null) {
				sb.append(ptr.tag);
				sb.append("\n");
			} else {
				sb.append("<");
				sb.append(ptr.tag);
				sb.append(">\n");
				getHTML(ptr.firstChild, sb);
				sb.append("</");
				sb.append(ptr.tag);
				sb.append(">\n");
			}
		}
	}

	/**
	 * Prints the DOM tree.
	 *
	 */
	public void print() {
		print(root, 1);
	}

	private void print(TagNode root, int level) {
		for (TagNode ptr = root; ptr != null; ptr = ptr.sibling) {
			for (int i = 0; i < level - 1; i++) {
				System.out.print("      ");
			}
			;
			if (root != this.root) {
				System.out.print("|----");
			} else {
				System.out.print("     ");
			}
			System.out.println(ptr.tag);
			if (ptr.firstChild != null) {
				print(ptr.firstChild, level + 1);
			}
		}
	}
}

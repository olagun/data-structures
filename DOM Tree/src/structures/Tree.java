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

	private TagNode insert(TagNode head, TagNode tn) {

		if (head.firstChild == null) {
			head.firstChild = tn;
			return tn;
		} else {
			TagNode node = head.firstChild;

			while (node.sibling != null) {
				node = node.sibling;
			}

			node.sibling = tn;
			return tn;
		}

	}

	private TagNode build(TagNode parent) {
		if (!this.sc.hasNext()) {
			return parent;
		}

		String line = this.sc.nextLine();
		if (line.charAt(0) == '<') {
			if (line.charAt(1) == '/') {
				return parent;
			}

			String tagName = line.substring(1, line.indexOf('>'));
			insert(parent, build(new TagNode(tagName, null, null)));
		} else {
			insert(parent, new TagNode(line, null, null));
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

	private void replaceRecursive(TagNode root, String oldTag, String newTag) {
		TagNode firstChild = root.firstChild;

		while (firstChild != null) {
			if (firstChild.tag.equals(oldTag))
				firstChild.tag = newTag;

			replaceRecursive(firstChild, oldTag, newTag);
			firstChild = firstChild.sibling;
		}
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
		replaceRecursive(newRoot, oldTag, newTag);
		this.root = newRoot.firstChild;
	}

	private TagNode findTag(TagNode head, String tag) {
		if (head.tag.equals(tag)) {
			return head;
		}

		TagNode firstChild = head.firstChild;

		while (firstChild != null) {
			TagNode tagNode = findTag(firstChild, tag);
			if (tagNode != null) {
				return tagNode;
			}
			firstChild = firstChild.sibling;
		}

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
		/** COMPLETE THIS METHOD **/

		// should i check for null cases?
		TagNode table = findTag(root, "table");
		TagNode tr = table.firstChild;

		int i = 0;
		while (i < row) {
			tr = tr.sibling;
			i++;
		}

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
		/** COMPLETE THIS METHOD **/
		// remove node link from parent.
		TagNode newRoot = new TagNode("root", root, null);
		TagNode result = removeTag(newRoot, tag);
		this.root = result.firstChild;

	}

	public TagNode removeTag(TagNode head, String tag) {
		// must have top reference,
		// chec kif childern are equa lto tag
		// if so, remove and add to parent

		TagNode prev = null;
		TagNode firstChild = head.firstChild;
		while (firstChild != null) {
			if (firstChild.tag.equals(tag)) {
				if (tag.equals("ul") || tag.equals("ol")) {
					// if ul or ol, replcae li with p
					replaceRecursive(firstChild, "li", "p");
				}

				TagNode next = firstChild.sibling;
				prev = insert(prev, firstChild.firstChild);
				insert(prev, next);
				firstChild = prev.sibling;
			} else {
				prev = firstChild;
				firstChild = firstChild.sibling;
			}
		}

		return removeTag(head, tag);
	}

	/**
	 * Adds a tag around all occurrences of a word in the DOM tree.
	 * 
	 * @param word Word around which tag is to be added
	 * @param tag  Tag to be added
	 */
	public void addTag(String word, String tag) {
		/** COMPLETE THIS METHOD **/
		// when encounters text node (node with no children)
		// split .tag property by space delimiter/string tokenizer
		// if not word, append to last string
		// if word, append last string as node
		// append current word as tag
		// reset string and continue
		TagNode newRoot = new TagNode("root", root, null);
		addTagRecursive(newRoot, word, tag);
		this.root = newRoot.firstChild;
	}

	public void addTagRecursive(TagNode tagNode, String word, String tag) {
		TagNode prev = null;
		TagNode firstChild = tagNode.firstChild;

		while (firstChild != null) {
			// if it has no children it is a text node

			if (firstChild.firstChild == null) {
				String text = firstChild.tag;
				String[] splitText = text.split("\\s+");
				String out = "";

				for (String str : splitText) {
					if (str.contains(word)) {
						if (out.length() > 0) {
							prev = insert(prev, new TagNode(out, null, null));
						}

						TagNode wrap = new TagNode(tag, null, null);
						wrap.firstChild = new TagNode(str, null, null);
						TagNode next = firstChild.sibling;
						// wrap next with first child
						firstChild = insert(prev, wrap);
						insert(prev, next);
					} else {
						out += str;
					}
				}

				prev = firstChild;
				firstChild = firstChild.sibling;
			} else {
				prev = firstChild;
				firstChild = firstChild.sibling;
			}
		}
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

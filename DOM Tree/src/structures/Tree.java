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

			String tagName = line.substring(1, line.length() - 1);
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
		TagNode tmpRoot = new TagNode("root", root, null);
		this.root = build(tmpRoot).firstChild;
	}

	private TagNode replaceTag(TagNode parent, String oldTag, String newTag, int maxDepth) {
		if (parent == null)
			return null;

		if (maxDepth <= 0)
			return parent;

		if (parent.tag.equals(oldTag))
			parent.tag = newTag;

		parent.firstChild = replaceTag(parent.firstChild, oldTag, newTag, maxDepth - 1);
		parent.sibling = replaceTag(parent.sibling, oldTag, newTag, maxDepth);

		return parent;
	}

	private TagNode replaceTag(TagNode parent, String oldTag, String newTag) {
		return replaceTag(parent, oldTag, newTag, Integer.MAX_VALUE);
	}

	/**
	 * Replaces all occurrences of an old tag in the DOM tree with a new tag
	 * 
	 * @param oldTag Old tag
	 * @param newTag Replacement tag
	 */
	public void replaceTag(String oldTag, String newTag) {
		TagNode tmpRoot = new TagNode("root", root, null);
		this.root = replaceTag(tmpRoot, oldTag, newTag).firstChild;
	}

	private TagNode findTag(TagNode node, String tag) {
		if (node == null)
			return null;

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
		// Can do it recursively.
		row -= 1;
		TagNode table = findTag(root, "table");

		if (table == null)
			return;

		TagNode trs = table.firstChild;
		for (int i = 0; i < row; ++i) {
			if (trs == null)
				return;

			trs = trs.sibling;
		}

		TagNode tds = trs.firstChild;
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
		this.root = removeTag(tmpRoot, tag).firstChild;
	}

	public TagNode removeTag(TagNode parent, String removedTag) {
		if (parent == null)
			return null;

		if (parent.tag.equals(removedTag)) {
			if (parent.tag.equals("ul") && removedTag.equals("ul"))
				replaceTag(parent.firstChild, "li", "p", 1);

			if (parent.tag.equals("ol") && removedTag.equals("ol"))
				replaceTag(parent.firstChild, "li", "p", 1);

			TagNode childNodes = removeTag(parent.firstChild, removedTag);
			return insertAfter(childNodes, removeTag(parent.sibling, removedTag));
		}

		parent.firstChild = removeTag(parent.firstChild, removedTag);
		parent.sibling = removeTag(parent.sibling, removedTag);

		return parent;
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

	private TagNode constructTag(String text, String word, String tag) {
		StringTokenizer st = new StringTokenizer(text, "\t ", true);
		String out = "";
		TagNode head = new TagNode(null, null, null);
		TagNode ptr = head;

		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			tok = tok.toLowerCase();

			boolean beginsWith = tok.indexOf(word.toLowerCase()) == 0;
			if (beginsWith && tok.length() == word.length()
					|| beginsWith && tok.length() == word.length() + 1 && isPunc(tok.charAt(tok.length() - 1))) {

				if (out.length() > 0) {
					ptr.sibling = new TagNode(out, null, null);
					ptr = ptr.sibling;
					out = "";
				}

				TagNode tnWrap = new TagNode(tag, null, null);
				tnWrap.firstChild = new TagNode(tok, null, null);
				ptr.sibling = tnWrap;
				ptr = ptr.sibling;

			} else {
				out += tok;
			}
		}

		if (out.length() > 0) {
			ptr.sibling = new TagNode(out, null, null);
			ptr = ptr.sibling;
			out = "";
		}

		return head.sibling;
	}

	private TagNode insertAfter(TagNode before, TagNode extension) {
		if (extension == null && before == null)
			return null;

		if (extension == null)
			return before;

		if (before == null)
			return extension;

		before.sibling = insertAfter(before.sibling, extension);
		return before;
	}

	private boolean isPunc(char a) {
		return a == ',' || a == '!' || a == '.' || a == '?' || a == ';';
	}

	private boolean isValidTag(String tag) {
		String[] validTags = { "html", "body", "p", "em", "b", "table", "tr", "td", "ol", "ul", "li" };

		for (String validTag : validTags)
			if (validTag.equals(tag))
				return true;

		return false;
	}

	private TagNode addTag(TagNode parent, String word, String tag) {
		if (parent == null)
			return parent;

		if (parent.firstChild == null) {
			TagNode addedNode = constructTag(parent.tag, word, tag);
			TagNode siblingNodes = addTag(parent.sibling, word, tag);
			return insertAfter(addedNode, siblingNodes);
		}

		parent.firstChild = addTag(parent.firstChild, word, tag);
		parent.sibling = addTag(parent.sibling, word, tag);

		return parent;
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

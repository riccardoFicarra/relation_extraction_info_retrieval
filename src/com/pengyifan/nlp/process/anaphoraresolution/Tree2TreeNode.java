package com.pengyifan.nlp.process.anaphoraresolution;

import edu.stanford.nlp.trees.Tree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;
import java.util.StringJoiner;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Convert Stanford tree to Java TreeNode
 */
public class Tree2TreeNode implements BiFunction<Tree, Integer, DefaultMutableTreeNode>{

  @Override
  public DefaultMutableTreeNode apply(Tree tree, Integer sentenceIndex) {
    DefaultMutableTreeNode tn = convertHelper(tree, sentenceIndex);
    computeWordIndex(tn);
    return tn;
  }

  private DefaultMutableTreeNode convertHelper(Tree t, int sentenceIndex) {
    if (t.isPreTerminal()) {
      String tag = t.label().value();
      String word = t.firstChild().label().value();
      return newInstance(tag, word, sentenceIndex);
    }

    String tag = t.label().value();
    StringJoiner sj = new StringJoiner(" ");
    for (Tree leaf : t.getLeaves()) {
      sj.add(leaf.label().value());
    }
    DefaultMutableTreeNode tn = newInstance(tag, sj.toString(), sentenceIndex);
    for (Tree c : t.children()) {
      tn.add(convertHelper(c, sentenceIndex));
    }
    return tn;
  }

  /**
   * if t is non-leaf, the its index is its first child's index.
   */
  private void computeWordIndex(DefaultMutableTreeNode t) {
    String rootTag = Utils.getTag(t);
    checkArgument(
        rootTag.equalsIgnoreCase("S1"),
        "shouldn't assign offset to sentence not starting with S1: %s", t);

    int offset = 0; // Syntactic unit index, zero based
    Enumeration enumeration = t.postorderEnumeration();
    while (enumeration.hasMoreElements()) {
      DefaultMutableTreeNode tn = (DefaultMutableTreeNode) enumeration.nextElement();
      TagWord tw = Utils.getTagWord(tn);
      if (tn.isLeaf()) {
        tw.setWordIndex(offset++);
      } else {
        TagWord firstChildtw = Utils.getTagWord(tn.getFirstChild());
        tw.setWordIndex(firstChildtw.getWordIndex());
      }
    }
  }

  private DefaultMutableTreeNode newInstance(String tag, String text, int sentenceIndex) {
    return new DefaultMutableTreeNode(new TagWord(tag, text, sentenceIndex, -1));
  }
}

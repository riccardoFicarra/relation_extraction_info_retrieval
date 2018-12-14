package com.pengyifan.nlp.process.anaphoraresolution;

import com.google.common.collect.Lists;
import edu.stanford.nlp.trees.Tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.List;

public abstract class Utils {

  /**
   * Parse the all (TAG word sentenceIndex charOffset)s in a string. For
   * example,
   * <p>
   * <pre>
   * (S1 (S (S (NP (PRP We)) (VP (VBD demonstrated) (SBAR (IN that)
   * (S (NP (NN katX)) ...
   * </pre>
   * <p>
   * Returns
   * <p>
   * </pre> (PRP We ? 18) (VBD demonstrated ? 33) (IN that ? 57) (NN katX ? 74)
   * ... </pre>
   *
   * @param s
   * @param sentenceIndex
   * @return
   */
  static List<TagWord> parseTagWordPairs(String s, int sentenceIndex) {
    if (s.isEmpty()) {
      return Collections.emptyList();
    }

    int adjPointer = 0; // adjunct pointer
    String tag = null;
    String word;

    int pointer = 0; // to indicate position in the string
    List<TagWord> tags = Lists.newArrayList();
    while (pointer != -1) {
      pointer = s.indexOf('(', pointer);
      if (pointer == -1) {
        break;
      }

      adjPointer = s.indexOf(" ", pointer);
      if (adjPointer == -1) {
        break;
      }

      // testing if it's (TAG word)
      if (s.startsWith("(", adjPointer + 1)) {
        pointer = adjPointer;
        continue;
      }

      tag = s.substring(pointer + 1, adjPointer);
      pointer = s.indexOf(")", adjPointer);
      word = s.substring(adjPointer + 1, pointer);
      tags.add(new TagWord(tag, word, sentenceIndex, adjPointer + 1));
    }

    return tags;
  }

  public static String getTag(TreeNode t) {
    return getTagWord(t).getTag();
  }

  public static String getText(TreeNode t) {
    return getTagWord(t).getText();
  }

  public static TagWord getTagWord(TreeNode t) {
    return (TagWord) ((DefaultMutableTreeNode) t).getUserObject();
  }

  @Deprecated
  public static Tree getPreviousSibling(Tree t, Tree root) {
    List<Tree> siblings = t.siblings(root);
    int index = siblings.indexOf(t);
    if (index == -1 || index == 0) {
      return null;
    } else {
      return siblings.get(index - 1);
    }
  }

  @Deprecated
  public static Tree getNextSibling(Tree t, Tree root) {
    List<Tree> siblings = t.siblings(root);
    int index = siblings.indexOf(t);
    if (index == -1 || index == siblings.size() - 1) {
      return null;
    } else {
      return siblings.get(index + 1);
    }
  }

  public static boolean equalsIgnoreCaseTag(TreeNode t, String tag) {
    return t != null && getTag(t).equalsIgnoreCase(tag);
  }

  public static boolean startWithTag(TreeNode t, String prefix) {
    return t != null && getTag(t).startsWith(prefix);
  }
}

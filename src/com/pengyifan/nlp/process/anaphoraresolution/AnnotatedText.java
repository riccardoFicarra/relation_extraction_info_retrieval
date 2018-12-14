/**
 * JavaRAP: a freely-available JAVA anaphora resolution implementation of the
 * classic Lappin and Leass (1994) paper:
 * 
 * An Algorithm for Pronominal Anaphora Resolution. Computational Linguistics,
 * 20(4), pp. 535-561.
 * 
 * Copyright (C) 2005 Long Qiu
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.pengyifan.nlp.process.anaphoraresolution;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.google.common.collect.Lists;

import edu.stanford.nlp.trees.Tree;

/**
 * @author Qiu Long
 * @version 1.0
 * @author "Yifan Peng"
 */

public class AnnotatedText {

  public AnnotatedText(String sentence) {
    this(Lists.newArrayList(sentence));
  }

  private List<TagWord> NPList;
  private List<TagWord> SNPList;
  private List<TagWord> PRPList;

  private DefaultMutableTreeNode rootNode;

  public AnnotatedText(List<String> sentences) {
    Tree2TreeNode converter = new Tree2TreeNode();

    rootNode = new DefaultMutableTreeNode();
    for (int i = 0; i < sentences.size(); i++) {
      String sentence = sentences.get(i);
      DefaultMutableTreeNode tn = converter.apply(Tree.valueOf(sentence), i);
      rootNode.add(tn);
    }

    NPExtractor ex = new NPExtractor(rootNode);
    NPList = ex.getNPList();
    PRPList = ex.getPRPList();
    identifyPleonasticPronoun(rootNode);
    SNPList = buildSNPList(NPList);
  }

  private List<TagWord> buildSNPList(List<TagWord> npList) {
    if (npList.isEmpty()) {
      return Collections.emptyList();
    }
    TagWord sTW = npList.get(0);
    List<TagWord> snpList = Lists.newArrayList(sTW);
    for (int i = 1; i < npList.size(); i++) {
      TagWord tw = npList.get(i);
      if (!sTW.getNP().contains(tw.getNP())) {
        sTW = tw;
        snpList.add(sTW);
      }
    }
    return snpList;
  }

  public List<TagWord> getNPList() {
    return NPList;
  }

  public List<TagWord> getPRPList() {
    return PRPList;
  }

  public List<TagWord> getSNPList() {
    return SNPList;
  }

  public DefaultMutableTreeNode getTree() {
    return rootNode;
  }

  private void identifyPleonasticPronoun(DefaultMutableTreeNode root) {
    @SuppressWarnings("rawtypes")
    Enumeration enumeration = root.preorderEnumeration();

    while (enumeration.hasMoreElements()) {
      TreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
      TagWord tagWd = Utils.getTagWord(node);
      if (tagWd == null) {
        continue;
      }

      if (!tagWd.getTag().equalsIgnoreCase("PRP")
          || !tagWd.getText().equalsIgnoreCase("it")) {
        continue;
      }

      DefaultMutableTreeNode NPnode = (DefaultMutableTreeNode) node.getParent();
      checkNotNull(NPnode, "Weird: (PRP it) has no parent");

      DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) NPnode
          .getParent();
      checkNotNull(parentNode, "Weird: (PRP it) has no grandparent");

      DefaultMutableTreeNode siblingNode = (DefaultMutableTreeNode) NPnode
          .getNextSibling();
      DefaultMutableTreeNode nephewNode1 = null;
      DefaultMutableTreeNode nephewNode2 = null;
      DefaultMutableTreeNode nephewNode3 = null;
      if ((siblingNode != null) && (siblingNode.getChildCount() > 0)) {
        nephewNode1 = (DefaultMutableTreeNode) siblingNode.getChildAt(0);
        nephewNode2 = (DefaultMutableTreeNode) nephewNode1.getNextSibling();
        if (nephewNode2 != null) {
          nephewNode3 = (DefaultMutableTreeNode) nephewNode2.getNextSibling();
        }
      }
      DefaultMutableTreeNode PrevSiblingNode = (DefaultMutableTreeNode) NPnode
          .getPreviousSibling();

      // identify pleonastic pronouns
      boolean isPleonastic = false;
      // It is very necessary
      // It is recommended that
      if (Utils.equalsIgnoreCaseTag(siblingNode, "VP")
          && Utils.equalsIgnoreCaseTag(nephewNode1, "AUX")
          && Utils.equalsIgnoreCaseTag(nephewNode2, "ADJP")) {
        isPleonastic |= ModalAdj.findAny(Utils.getText(nephewNode2).split(" "));
      }

      if (Utils.equalsIgnoreCaseTag(siblingNode, "VP")
          && Utils.equalsIgnoreCaseTag(nephewNode1, "AUX")
          && Utils.equalsIgnoreCaseTag(nephewNode3, "ADJP")) {
        isPleonastic |= ModalAdj.findAny(Utils.getText(nephewNode3).split(" "));
      }

      // really appreciate it
      if (Utils.equalsIgnoreCaseTag(PrevSiblingNode, "VB")) {
        isPleonastic |= ModalAdj.findAny(Utils.getText(PrevSiblingNode)
            .split(" "));
      }

      // it may/might be
      if (Utils.equalsIgnoreCaseTag(siblingNode, "VP")
          && Utils.equalsIgnoreCaseTag(nephewNode1, "MD")
          && Utils.equalsIgnoreCaseTag(nephewNode2, "VP")
          && nephewNode2.getChildCount() > 1
          && Utils.equalsIgnoreCaseTag(nephewNode2.getChildAt(0), "AUX")
          && Utils.equalsIgnoreCaseTag(nephewNode2.getChildAt(1), "ADJP")) {
        isPleonastic |= ModalAdj.findAny(Utils.getText(nephewNode2).split(
            " "));
      }

      DefaultMutableTreeNode uncleNode = (DefaultMutableTreeNode) parentNode
          .getPreviousSibling();
      // I will/could appreciate/ believe it
      if (Utils.equalsIgnoreCaseTag(siblingNode, "VB")
          && Utils.equalsIgnoreCaseTag(uncleNode, "MD")) {
        isPleonastic |= ModalAdj.findAny(Utils.getText(siblingNode).split(" "));
      }

      // find it important
      if (Utils.equalsIgnoreCaseTag(siblingNode, "ADJP")) {
        isPleonastic |= ModalAdj.findAny(Utils.getText(siblingNode).split(" "));
      }

      // it is thanks to/it is time to
      if (Utils.equalsIgnoreCaseTag(siblingNode, "VP")
          && Utils.equalsIgnoreCaseTag(nephewNode1, "AUX")
          && Utils.equalsIgnoreCaseTag(nephewNode2, "NP")) {
        isPleonastic |= ModalAdj.findAny(Utils.getText(nephewNode2).split(" "));
      }

      // it follows that
      if (Utils.equalsIgnoreCaseTag(siblingNode, "VP")
          && Utils.equalsIgnoreCaseTag(nephewNode1, "VB")
          && Utils.equalsIgnoreCaseTag(nephewNode2, "S")) {
        isPleonastic |= ModalAdj.find(Utils.getText(nephewNode1));
      }

      tagWd.setPleonastic(isPleonastic);
      // set parent NP as pleonastic also
      Utils.getTagWord(NPnode).setPleonastic(isPleonastic);
    } // /~while
  }

}

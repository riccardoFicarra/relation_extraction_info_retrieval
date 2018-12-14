/*
 * JavaRAP: a freely-available JAVA anaphora resolution implementation of the
 * classic Lappin and Leass (1994) paper: An Algorithm for Pronominal Anaphora
 * Resolution. Computational Linguistics, 20(4), pp. 535-561. Copyright (C)
 * 2005,2006 Long Qiu This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */

package com.pengyifan.nlp.process.anaphoraresolution;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Qiu Long
 * @version 1.0
 * @author "Yifan Peng"
 */

class NP {

  public final static int PLEO = 2; // pleonastic pronoun;
  public final static int PRON = 3; // other pronoun;
  public final static int INDEF = 4; // indefinite NP;
  // type of the Unit;
  final private int type;

  /**
   * Existential clause, such as "There is [a god]"
   */
  private boolean existential = false;

  /**
   * This belongs to Subject
   */
  private boolean isSubject = false;

  private boolean directObj = false;
  private boolean isHead = false;
  private boolean isInADVP = false;
  // Indicates whether this NP is part of a "NNX (NNX)+" combination
  // The probability of such a NP being a good antecedent of an anaphora
  // plumbs.
  private boolean hasNNXsibling = false;
  // index of the sentence where UNIT is localized. 0 based
  private int sentenceIndex;
  // distance between the beginning of the sentence and the first word in UNIT;
  private int wordIndex;

  private DefaultMutableTreeNode nodeRepresent = null;

  // containing instances of TagWord
  private final TagWord tagWord;

  NP(TagWord tagWord) {
    this.sentenceIndex = tagWord.getSentenceIndex();
    this.wordIndex = tagWord.getWordIndex();
    this.tagWord = tagWord;
    if (tagWord.getTag().startsWith("PRP")) {
      type = NP.PRON;
    } else {
      type = NP.INDEF;
    }
  }

  boolean contains(NP np) {
    return np.getNodeRepresent().isNodeAncestor(this.getNodeRepresent());
    /*
     * String s = this.tagWord.toString(); String ss = np.tagWord.toString();
     * int tt = s.indexOf(ss); if
     * (this.tagWord.toString().indexOf(np.tagWord.toString().substring(0,
     * ss.length() - 1)) > -1) { if (this.tagWord.toString().length() ==
     * np.toString().length()) { return false; } return true; } else { return
     * false; }
     */
  }

  private int getFixedSalience() {
    int fSalience = 0;
    // 6 of the 7 salience factors are considered here: (sentence recency is
    // relevant and considered in getSalience(NP otherNP))
    // Subject emphasis
    // Existential emphasis
    // Accusative emphasis (object)
    // Indirect object emphasis
    // Head noun emphasis
    // Non-adverbial emphasis
    if (isSubject) {
      fSalience += 80;
    }

    if (existential) {
      fSalience += 70;
    }

    if (directObj) {
      fSalience += 50;
    } else {
      fSalience += 90;
    }

    if (isHead) {
      fSalience += 0;
    }

    if (!isInADVP) {
      fSalience += 50;
    }

    if (hasNNXsibling) {
      fSalience -= 0; // Right. Reduce the salience score.
    }

    return fSalience;
  }

  public DefaultMutableTreeNode getNodeRepresent() {
    return this.nodeRepresent;
  }

  public int getOffset() {
    return this.wordIndex;
  }

  int getSalience(NP otherNP) {
    return this.getSentenceIdx() == otherNP.getSentenceIdx() ?
        getFixedSalience() + 100
        : getFixedSalience();
  }

  public int getSentenceIdx() {
    return this.sentenceIndex;
  }

  public int getType() {
    return this.type;
  }

  /**
   * @return true if there is a "CC and" in children
   */
  public boolean hasAnd() {
    @SuppressWarnings("rawtypes")
    Enumeration enumer = getNodeRepresent().children();
    while (enumer.hasMoreElements()) {
      DefaultMutableTreeNode aChild = (DefaultMutableTreeNode) enumer
          .nextElement();
      if (((TagWord) aChild.getUserObject()).getTag().equals("CC")) {
        return true;
      }
    }
    return false;
  }

  public boolean isDirectObj() {
    return directObj;
  }

  public boolean isExistential() {
    return existential;
  }

  public boolean isHead() {
    return this.isHead;
  }

  public boolean isPRP() {
    return type == PRON;
  }

  public boolean isReflexive() {
    return isPRP() && tagWord.getText().indexOf("sel") > 0;
  }

  public boolean isSubject() {
    return isSubject;
  }

  /**
   * merge all salience factors true for palNP, salience factors for palNP
   * remain unchanged where palNP is in the same co-reference chain as this NP.
   * 
   * @param palNP
   */
  void mergeSalience(NP palNP) {
    isSubject |= palNP.isSubject;
    existential |= palNP.existential;
    directObj |= palNP.directObj;
    isHead |= palNP.isHead;
    isInADVP |= palNP.isInADVP;
  }

  public void setDirectObj(boolean b) {
    this.directObj = b;
  }

  public void setExistential(boolean b) {
    this.existential = b;
  }

  public void setHasNNXsibling(boolean b) {
    hasNNXsibling = b;
  }

  public void setHead(boolean b) {
    this.isHead = b;
    /*
     * if(!b){ System.out.println(this.toDisplay()); }else{
     * System.out.println("\t"+this.toDisplay()); }
     */
  }

  public void setIsInADVP(boolean b) {
    isInADVP = b;
  }

  public void setNodeRepresent(DefaultMutableTreeNode t) {
    // as it's stored in the tree
    nodeRepresent = t;
  }

  public void setSubject(boolean isSubject) {
    this.isSubject = isSubject;
  }

  public String toString() {
    return sentenceIndex + "," + wordIndex + "," + type + ","
        + ",EX "
        + this.existential
        + ",SUB "
        + this.isSubject
        + ",DOBJ "
        + this.directObj
        + ",isHEAD "
        + this.isHead
        + ",SAL <"
        + getFixedSalience()
        + ">";
  }

  public TagWord getTagWord() {
    return tagWord;
  }
}

/**
 * JavaRAP: a freely-available JAVA anaphora resolution implementation of the
 * classic Lappin and Leass (1994) paper:
 * 
 * An Algorithm for Pronominal Anaphora Resolution. Computational Linguistics,
 * 20(4), pp. 535-561.
 * 
 * Copyright (C) 2005,2011 Long Qiu
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

import java.lang.*;
import java.util.List;
import java.util.Objects;

import edu.stanford.nlp.dcoref.Dictionaries.Gender;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.google.common.collect.Lists;

/**
 * @author Qiu Long
 * @version 1.0
 * @history Feb 08, 2011 To getGender() given a person name, first check
 *          against the 300 most common US male names (by the 1990 census). If
 *          no match is found, then move on to a list of known female names.
 *          Previously, a more exhaustive male name list is first checked and
 *          regardless of the result the above mentioned female name list is
 *          checked. In this case, names such as John are potentially taken as
 *          female as they do appear in the female name list. This is not
 *          absolutely a mistake but in most of the cases it tend to be.
 * @author "Yifan Peng"
 */

public class TagWord {

  private int sentenceIndex; // indicates sentence
  private int wordIndex;
  private NumberEnum number = NumberEnum.UNKNOWN;
  private Gender gender = Gender.UNKNOWN;
  private Human human = Human.UNCLEAR;
  private People people = People.UNKNOWN;

  private boolean pleonastic = false; // represents a pleonastic pronoun
  private String tag;
  private String text;
  private boolean isHeadNP = false;
  private boolean hasNPAncestor = false;
  // reference to the head for this
  // NP
  private TreeNode head = null;
  private TreeNode argumentHead = null; // the head as this NP is
                                        // augument for
  private TreeNode argumentHost = null; // the other np as
  // augument for the same
  // head
  private TreeNode adjunctHost = null; // the unit as adjunct for
  private TreeNode NPDomainHost = null;
  private TreeNode determiner = null;
  private TreeNode determinee = null;
  private List<TreeNode> containHost = Lists.newArrayList();
  private NP np = null;

  private TagWord antecedent = null;

  // the dynamically updated salience value
  int tmpSalience = 0;

  public TagWord(String tag, String text, int sentenceIndex, int wordIndex) {
    this.tag = tag;
    this.text = text;
    this.sentenceIndex = sentenceIndex;
    this.wordIndex = wordIndex;
  }

  /**
   * amplify sentence index difference by multiply 100
   */
  public int distanceInText(TagWord tw) {
    return Math.abs(getSentenceIndex() - tw.getSentenceIndex()) * 100
        + Math.abs(getWordIndex() - tw.getWordIndex());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof TagWord)) {
      return false;
    }
    TagWord rhs = (TagWord) obj;
    return Objects.equals(text, rhs.text)
        && Objects.equals(tag, rhs.tag)
        && Objects.equals(sentenceIndex, rhs.sentenceIndex)
        && Objects.equals(wordIndex, rhs.wordIndex)
        && Objects.equals(number, rhs.number)
        && Objects.equals(gender, rhs.gender)
        && Objects.equals(human, rhs.human)
        && Objects.equals(people, rhs.people)
        && Objects.equals(pleonastic, rhs.pleonastic)
        && Objects.equals(isHeadNP, rhs.isHeadNP)
        && Objects.equals(hasNPAncestor, rhs.hasNPAncestor)
        && Objects.equals(head, rhs.head)
        && Objects.equals(np, rhs.np)
        && Objects.equals(antecedent, rhs.antecedent);
  }

  public TreeNode getAdjunctHost() {
    return adjunctHost;
  }

  /**
   *
   * @return the anaphoric antecedent of the TagWord, if there is one. Itself
   *         is returned otherwise.
   */
  public TagWord getAntecedent() {
    return antecedent == null ? this : antecedent.getAntecedent();
  }

  public TreeNode getArgumentHead() {
    return argumentHead;
  }

  public TreeNode getArgumentHost() {
    return argumentHost;
  }

  public List<TreeNode> getContainHost() {
    return containHost;
  }

  public TreeNode getDeterminee() {
    return determinee;
  }

  public TreeNode getDeterminer() {
    return determiner;
  }

  /**
   * @return 0 for Male, 2 for Female and 1 for unclear
   */
  public Gender getGender() {
    if (gender != Gender.UNKNOWN) {
      return gender;
    }
    String h = head == null ? text : Utils.getTagWord(head).getText();
    if (HumanList.isMale(h)) {
      gender = Gender.MALE;
    } else if (HumanList.isFemale(h)) {
      gender = Gender.FEMALE;
    }
    return gender;
  }

  public TreeNode getHead() {
    return head;
  }

  /**
   *
   * @return 0 for human, 2 for none-human and 1 for unclear
   */
  public Human getHuman() {
    if (human != Human.UNCLEAR) {
      return human;
    }

    if (gender != Gender.UNKNOWN) {
      human = Human.HUMAN;
      return human;
    }

    // check the content of this NP as the first attempt
    String h = getText();
    if (HumanList.isHuman(h)) {
      human = Human.HUMAN;
      return human;
    } else if (HumanList.isNotHuman(h)) {
      human = Human.NON_HUMAN;
      return human;
    }

    if (head == null) {
      return human;
    }

    // If above fails, check the head of this NP
    h = Utils.getTagWord(head).getText();
    if (HumanList.isHuman(h)) {
      human = Human.HUMAN;
      return human;
    } else if (HumanList.isNotHuman(h)) {
      human = Human.NON_HUMAN;
      return human;
    }

    return human;
  }

  public NP getNP() {
    return np;
  }

  public TreeNode getNPDomainHost() {
    return NPDomainHost;
  }

  public NumberEnum getNumber() {
    if (number != NumberEnum.UNKNOWN) {
      return number;
    }
    String tag = np.getTagWord().getTag();
    if (tag.endsWith("S")) { // NNS, NPS
      number = NumberEnum.PLURAL;
    } else if (HumanList.isPlural(getText())) {
      number = NumberEnum.PLURAL;
    } else {
      number = NumberEnum.SINGLE;
    }
    if (np.hasAnd()) {
      number = NumberEnum.PLURAL;
    } else if (head != null) {
      number = Utils.getTagWord(head).getNumber();
    }
    return number;
  }

  public People getPeople() {
    if (people != People.UNKNOWN) {
      return people;
    }
    if (getText().toLowerCase().matches("we|us")) {
      people = People.FIRST;
    } else if (getText().toLowerCase().matches("you")) {
      people = People.SECOND;
    } else {
      people = People.THIRD; // default
    }
    return people;
  }

  public People getPronounPeople() {
    if (people != People.UNKNOWN) {
      return people;
    }
    String h = (head == null) ? text : Utils.getTagWord(head).getText();

    if (HumanList.isThirdPerson(h)) {
      people = People.THIRD;
    } else if (HumanList.isSecondPerson(h)) {
      people = People.SECOND;
    } else if (HumanList.isFirstPerson(h)) {
      people = People.FIRST;
    } else {
      people = People.UNKNOWN;
    }
    return people;
  }

  /**
   * @param npAlien The np that salience weight of this TagWord is considered
   *          for.
   */
  public int getSalience(NP npAlien) {

    int sal = 0;
    if ((np != null) && (npAlien != null)) {
      sal = np.getSalience(npAlien);
    }
    // dampen the salience as distance increases
    sal = sal / (Math.abs(getSentenceIndex() - npAlien.getSentenceIdx()) + 1);

    // penalize cataphora (if this appears after npAlien)
    if ((getSentenceIndex() == npAlien.getSentenceIdx()
        && np.getOffset() > npAlien.getOffset())
        || getSentenceIndex() > npAlien.getSentenceIdx()) {
      sal = sal / 4; // reduce the weight substantially
    }
    return sal;
  }

  public int getSalience(TagWord tw) {
    return getSalience(tw.getNP());
  }

  public int getSentenceIndex() {
    return sentenceIndex;
  }

  public String getTag() {
    return tag;
  }

  public String getText() {
    return text;
  }

  public int getTmpSalience() {
    return tmpSalience;
  }

  public String getWord() {
    return text;
  }

  /**
   * Returns index of the word as a whole in the sentence
   * 
   * @return index of the word as a whole in the sentence
   */
  public int getWordIndex() {
    return wordIndex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        text,
        tag,
        sentenceIndex,
        wordIndex,
        number,
        gender,
        human,
        people,
        pleonastic,
        isHeadNP,
        hasNPAncestor,
        head);
  }

  public boolean hasNPAncestor() {
    return hasNPAncestor;
  }

  /**
   * @return true if thsi NP is not contained in another NP
   */
  public boolean isHeadNP() {
    if (argumentHead != null) {
      return false;
    } else if (adjunctHost != null) {
      return false;
    } else if (hasNPAncestor) {
      return false;
    }
    isHeadNP = true;
    return isHeadNP;
  }

  public boolean isPleonastic() {
    return pleonastic;
  }

  public boolean isPRP() {
    return np.isPRP();
  }

  /**
   * merge all salience factors true for tw, salience factors for tw remain
   * unchanged
   * 
   * @param tw
   */
  public void mergeSalience(TagWord tw) {
    // In theory: merge salience factors for members in a equvalent class
    // (coreferencial chain)
    // In fact: accumulate salience factors in the chain, a member in the chain
    // has all the factors processed by the leading members
    NP npGuest = tw.getNP();
    if (np != null && npGuest != null) {
      np.mergeSalience(npGuest);
    }
  }

  public void setAdjunctHost(TreeNode adjunctHost) {
    this.adjunctHost = adjunctHost;
  }

  public void setAntecedent(TagWord antecedent) {
    if (antecedent.getAntecedent() == this) {
      return;
    }
    this.antecedent = antecedent;
  }

  public void setArgumentHead(TreeNode argumentHead) {
    this.argumentHead = argumentHead;
  }

  /**
   * @param argumentHost: the NP in the same argument domain
   */
  public void setArgumentHost(TreeNode argumentHost) {
    this.argumentHost = argumentHost;
  }

  public void setContainHost(TreeNode n) {
    this.containHost.add(n);
  }

  public void setContainHost(List<TreeNode> n) {
    containHost.addAll(n);
  }

  public void setDeterminee(DefaultMutableTreeNode determinee) {
    this.determinee = determinee;
  }

  public void setDeterminer(DefaultMutableTreeNode determiner) {
    this.determiner = determiner;
  }

  public void setHasNPAncestor(boolean hasNPAncestor) {
    this.hasNPAncestor = hasNPAncestor;
  }

  public void setHead(TreeNode n) {
    if (number != NumberEnum.UNKNOWN) {
      // number should be set afterword
      System.err.println("Number shouldn't be set before setHead.");
    }
    head = n;
  }

  public void setNP(NP np) {
    this.np = np;
  }

  public void setNPDomainHost(TreeNode NPDomainHost) {
    this.NPDomainHost = NPDomainHost;
  }

  public void setNumber(NumberEnum number) {
    this.number = number;
  }

  public void setPeople(People people) {
    this.people = people;
  }

  public void setPleonastic(boolean pleonastic) {
    this.pleonastic = pleonastic;
  }

  public void setTmpSalience(int tmpSalience) {
    this.tmpSalience = tmpSalience;
  }

  public void setWordIndex(int wordIndex) {
    this.wordIndex = wordIndex;
  }

  private String getString(String tag, TreeNode treeNode) {
    if (treeNode == null) {
      return " NULL";
    } else {
      return " (" + tag + " " + Utils.getTagWord(adjunctHost).getText() + ")";
    }
  }

  public String toString() {
    String localhead = " NULL";
    if (head != null) {
      localhead = Utils.getTagWord(head).getText();
    }

    String argHStr = getString("ARG", argumentHost);
    String adjHStr = getString("ADJ ", adjunctHost);
    String NPDHStr = getString("NPDomain", NPDomainHost);
    String argHeadStr = getString("ARGHead", argumentHead);

    String containHostStr = " NULL";
    if (containHost.size() > 0) {
      containHostStr = " (containHost ";
      for (TreeNode tn : containHost) {
        containHostStr += Utils.getTagWord(tn).getText() + "/";
      }
      containHostStr += ") ";
    }

    if (tag.startsWith("NP")
        || tag.startsWith("PP")
        || tag.startsWith("VP")) {
      localhead = " (HEAD " + localhead + ")";
    } else {
      localhead = "";
    }

    String npShow;
    if (np != null) {
      npShow = np.toString();
    } else {
      npShow = "no NP";
    }

    return wordIndex
        + " in "
        + sentenceIndex
        + "         "
        + tag
        + " "
        + getText()
        + " <NUMBER> "
        + number
        + localhead
        + argHStr
        + argHeadStr
        + adjHStr
        + NPDHStr
        + containHostStr
        + "\t "
        + npShow;

  }

  public String toStringBrief() {
    return "(" + sentenceIndex + "," + wordIndex + ") " + getText();
  }

}

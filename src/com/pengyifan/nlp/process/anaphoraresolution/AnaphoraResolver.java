/**
 * JavaRAP: a freely-available JAVA anaphora resolution implementation of the
 * classic Lappin and Leass (1994) paper:
 * 
 * An Algorithm for Pronominal Anaphora Resolution. Computational Linguistics,
 * 20(4), pp. 535-561.
 * 
 * Copyright (C) 2005,2006 Long Qiu
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
import java.lang.Number;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.dcoref.Dictionaries.Gender;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.google.common.collect.Lists;

// import edu.nus.comp.nlp.gadget.*;
/**
 * @author Qiu Long
 * @version 1.0
 * @history Feb 12, 2006 Make it work on windows. Long Qiu
 * @author "Yifan Peng"
 */

public class AnaphoraResolver {

  private static final int THRESHHOLD = 30;
  // How many sentences to look back.
  private static final int SCOPE = 1;

  public AnaphoraResolver() {
    loadEnv();
  }

  private void loadEnv() {
    // resolver mode
    System.setProperty("referenceChain", "true");
    // environment
    ClassLoader classLoader = this.getClass().getClassLoader();
    System.setProperty("dataPath", classLoader.getResource("Data").getFile());
  }

  public List<CorreferencialPair> resolverV1(List<TagWord> aNPList,
      List<TagWord> aPRPList) {
    // for substitution
    List<CorreferencialPair> results = Lists.newArrayList();

    for (TagWord prpTw : aPRPList) {

      // label pleonastic pronoun's anaphoraic antecedence as NULL and procede
      if (prpTw.isPleonastic()) {
        results.add(new CorreferencialPair(null, prpTw));
        continue;
      }

      // consider only third person pronoun
      if (!HumanList.isThirdPerson(prpTw.getText())) {
        continue;
      }

      boolean foundMatcher = false;
      // rewind
      List<TagWord> npCandidates = Lists.newArrayList();
      for (TagWord npTw : aNPList) {
        // skip pleonastic NP, whose only child is pleonastic pronoun 'it'
        if (npTw.isPleonastic()) {
          continue;
        }
        // ignore NP 'scope' sentences ahead
        if ((npTw.getSentenceIndex() + SCOPE) < prpTw.getSentenceIndex()) {
          continue;
        }
        // self reference :)
        if (isSelfReference(prpTw, npTw)) {
          continue;
        }
        // only consider anaphora
        if (npTw.getSentenceIndex() > prpTw.getSentenceIndex()) {
          break;
        }

        // filtering
        NP prpNP = prpTw.getNP();
        DefaultMutableTreeNode prpNode = prpNP.getNodeRepresent();

        if (prpNP.isReflexive()) {
          if (matchLexcialAnaphor(npTw, prpTw)) {
            foundMatcher = true;

            // building NP chains whose rings are refering to the same thing.
            if (prpNode.getSiblingCount() == 1) {
              // this PRP is the only child of the NP parent
              TreeNode parentNode = prpNode.getParent();
              if (parentNode != null) {
                Utils.getTagWord(parentNode).setAntecedent(npTw);
              }
            }

            // true/undefine by default
            if (System.getProperty("referenceChain").equals("false")) {
              results.add(new CorreferencialPair(npTw, prpTw));
            } else {
              results.add(new CorreferencialPair(npTw.getAntecedent(), prpTw));
            }
            break;
          }
        } else if (!matchPronominalAnaphor(npTw, prpTw)) {
          continue;
        }

        // grading
        // ignore those with small salience weight
        if (npTw.getSalience(prpNP) < THRESHHOLD) {
          continue;
        }
        npTw.setTmpSalience(npTw.getSalience(prpNP));
        npCandidates.add(npTw);
      }

      if (!foundMatcher) {
        TagWord[] sortedCandidates = npCandidates.toArray(new TagWord[0]);
        Arrays.sort(sortedCandidates, new TagWordSalienceComp());

        TagWord obj = getBestCandidate(sortedCandidates, prpTw);
        if (obj != null) {
          NP prpNP = prpTw.getNP();
          DefaultMutableTreeNode prpNode = prpNP.getNodeRepresent();
          // building NP chains whose 'rings' are refering to the same thing.
          if (prpNode.getSiblingCount() == 1) {
            // this PRP is the only child of the NP parent
            TreeNode parentNode = prpNode.getParent();
            if (parentNode != null) {
              Utils.getTagWord(parentNode).setAntecedent(obj);
            }
          }
          // true/undefine by default
          if (System.getProperty("referenceChain").equals("false")) {
            results.add(new CorreferencialPair(obj, prpTw));
          } else {
            results.add(new CorreferencialPair(obj.getAntecedent(), prpTw));
          }
        } else {
          // no candidate is found
          results.add(new CorreferencialPair(obj, prpTw));
        }
      }

    }
    return results;
  }

  // self reference :)
  // Case 1: (PRP$ xxx) (PRP$ xxx)
  // Case 2: (NP (PRP xxx)) (PRP xxx)
  private boolean isSelfReference(TagWord prpTw, TagWord npTw) {
    boolean b1 = prpTw == npTw;
    boolean b2 = npTw.getNP().getNodeRepresent().isNodeChild(
        prpTw.getNP().getNodeRepresent());
    boolean b3 = npTw.getNP().getNodeRepresent().getChildCount() == 1;
    boolean b4 = npTw.getNP().getNodeRepresent().isNodeDescendant(prpTw.getNP().
            getNodeRepresent());
    return b1 || (b2 && b3) || b4;
  }

  private TagWord getBestCandidate(TagWord[] sortedCandidates, TagWord tw) {
    if (sortedCandidates.length == 0) {
      return null;
    } else if (sortedCandidates.length == 1) {
      return sortedCandidates[0];
    }

    TagWord tw0 = sortedCandidates[sortedCandidates.length - 1];
    TagWord tw1 = sortedCandidates[sortedCandidates.length - 2];
    if (tw0.getTmpSalience() > tw1.getTmpSalience()) {
      return tw0;
    } else if (tw0.distanceInText(tw) < tw1.distanceInText(tw)) {
      // take closer one
      return tw0;
    } else if (tw0.getNP().getNodeRepresent().isNodeAncestor(
        tw1.getNP().getNodeRepresent())) {
      // take child
      return tw0;
    } else {
      return tw1;
    }
  }

  /**
   *
   * @param npTw
   * @param lexTw
   * @return true if the two NPs are highly likely to be co-reference
   */
  private boolean matchLexcialAnaphor(TagWord npTw, TagWord lexTw) {
    // Anaphor Binding Algorithm (Lappin and Leass)
    DefaultMutableTreeNode npNode = npTw.getNP()
        .getNodeRepresent();
    // lexical anaphor is in the argument domain of N
    if (lexTw.getArgumentHost() == npNode) {
      return true;
    } else if (lexTw.getAdjunctHost() == npNode) {
      // lexcial anaphor is in the adjunct domain of N
      return true;
    } else if (lexTw.getNPDomainHost() == npNode) {
      // lexcial anaphor is in the NP domain of N
      return true;
    } else if (morphologicalFilter(npTw, lexTw) == false) {
      return false;
    } else {
      return false;
    }
  }

  /**
   * @param npTw
   * @param prpTw
   * @return true if the two NPs are possible to be co-reference
   */
  private boolean matchPronominalAnaphor(TagWord npTw, TagWord prpTw) {
    // Syntactic Filter (Lappin and Leass)
    DefaultMutableTreeNode npNode = npTw.getNP()
        .getNodeRepresent();

    if (prpTw.getArgumentHost() == npNode) {
      // 2.pronominal anaphor is in the argument domain of N
      return false;
    } else if (prpTw.getAdjunctHost() == npNode) {
      // 3.pronominal anaphor is in the adjunct domain of N
      return false;
    } else if (prpTw.getNPDomainHost() == npNode) {
      // 5. pronominal anaphor is in the NP domain of N
      return false;
    } else if (npTw.getContainHost().contains(prpTw.getArgumentHead())) {
      // 4.
      if (!npTw.isPRP()) {
        return false;
      } else {
        return true;
      }
    } else if (npTw.getContainHost().contains(prpTw.getDeterminee())) {
      // 6.
      return false;
    } else if (morphologicalFilter(npTw, prpTw) == false) {
      // 1
      return false;
    } else {
      return true;
    }
  }

  /**
   * A morphological filter for ruling out anaphoric dependence of a pronoun on
   * an NP due to non-agreement of person, number, or gender features.
   * 
   * @return false if disagree.
   */
  private boolean morphologicalFilter(TagWord npTw, TagWord prpTw) {
    if (prpTw.getGender() != npTw.getGender()
        && prpTw.getGender() != Gender.UNKNOWN
        && npTw.getGender() != Gender.UNKNOWN) {
      return false;
    } else if (npTw.getNumber() != prpTw.getNumber()
        && npTw.getNumber() != NumberEnum.UNKNOWN
        && prpTw.getNumber() != NumberEnum.UNKNOWN) {
      return false;
    } else if (npTw.getPronounPeople() != prpTw.getPronounPeople()
        && npTw.getPronounPeople() != People.UNKNOWN
        && prpTw.getPronounPeople() != People.UNKNOWN) {
      // getPronounIdx also assigns the predicate "people" as well
      return false;
    } else if (npTw.getHuman() != prpTw.getHuman()
        && npTw.getHuman() != Human.UNCLEAR
        && prpTw.getHuman() != Human.UNCLEAR) {
      return false;
    } else if (npTw.getPeople() != prpTw.getPeople()
        && npTw.getPeople() != People.UNKNOWN
        && prpTw.getPeople() != People.UNKNOWN) {
      return false;
    } else {
      return true;
    }
  }
}

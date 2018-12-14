package com.pengyifan.nlp.process.anaphoraresolution;

/**
 * @version 1.0
 * @author "Yifan Peng"
 *
 */
public class CorreferencialPair {

  private final TagWord referee;
  private final TagWord referer;

  public CorreferencialPair(TagWord tw0, TagWord tw1) {
    referee = tw0;
    referer = tw1;
    if (referee != null) {
      // update salience factors for the detected coreferential pair
      referee.mergeSalience(referer);
      referer.mergeSalience(referee);
    }
  }

  public TagWord getReferer() {
    return referer;
  }

  public TagWord getReferee() {
    return referee;
  }

  public String toString() {
    String refereeStr = null;
    if (referee == null) {
      refereeStr = "NULL";
    } else if (System.getProperty("referenceChain").equals("false")) {
      // true/undefined by default
      refereeStr = referee.toStringBrief();
    } else {
      // bind to the earliest NP
      refereeStr = referee.getAntecedent().toStringBrief();
    }
    String refererStr = referer.toStringBrief();
    return refereeStr + " <-- " + refererStr;
  }
}

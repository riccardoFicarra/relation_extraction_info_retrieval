package com.pengyifan.nlp.process.anaphoraresolution;

import java.lang.*;
import java.lang.Number;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.google.common.collect.Lists;

public class NPExtractor {

  private DefaultMutableTreeNode rootNode;
  private List<TagWord> NPList;
  private List<TagWord> PRPList;

  public NPExtractor(DefaultMutableTreeNode rootNode) {
    this.rootNode = rootNode;
    NPList = Lists.newArrayList();
    PRPList = Lists.newArrayList();
    extract();
  }

  public List<TagWord> getNPList() {
    return NPList;
  }

  public List<TagWord> getPRPList() {
    return PRPList;
  }

  private void extract() {
    Enumeration enumeration = rootNode.preorderEnumeration();

    while (enumeration.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
      TagWord tw = Utils.getTagWord(node);

      if (tw == null) {
        continue;
      }

      if (tw.getTag().startsWith("N")
          || tw.getTag().startsWith("PRP")) {
        extractNP(node);
      } else if (tw.getTag().equalsIgnoreCase("PP")) {
        setPPHead(node);
      } else if (tw.getTag().equalsIgnoreCase("VP")) {
        setVPHead(node);
      }
    }
  }

  private void extractNP(DefaultMutableTreeNode node) {
    TagWord tw = Utils.getTagWord(node);
    NP np = new NP(tw);

    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
    String parentTag = Utils.getTag(parentNode);

    // Set 'Subject' true if it's a child of a unit tagged as "S"
    if (parentTag.equals("S")) {
      np.setSubject(true);
    }

    // Set 'existential' true if it's a object of a VP, which in turn follows a NP(subject?) who
    // has a single child tagging as 'EX'
    //
    // EX, existential "there"
    //
    // there is a god: ((NP EX) (VP (is) (node)))
    if (parentTag.equalsIgnoreCase("VP") && parentNode.getIndex(node) == 1) {
      // object (second child) of a VP
      TreeNode parentSibNode = parentNode.getPreviousSibling();
      if (Utils.equalsIgnoreCaseTag(parentSibNode, "NP")) {
        // preceeding sibling is a NP
        if (Utils.equalsIgnoreCaseTag(parentSibNode.getChildAt(0), "EX")) {
          np.setExistential(true);
        }
      }
    }

    // set 'directObj' true if it's the only NP child of a VP, or second NP of a VP whileas the
    // first NP is 'indirectObj'
    //
    // Handle: "give him a book" and "kick him"
    // Not: "give a book to him"
    if (parentTag.equalsIgnoreCase("VP")) {
      if (parentNode.getIndex(node) == 1) {
        // object (second child) of a VP
        if (Utils.equalsIgnoreCaseTag(node.getNextSibling(), "NP")) {
          // eg. give HIM a book
          np.setDirectObj(false);
        } else {
          // eg. kick him
          np.setDirectObj(true);
        }
      } else {
        // not the first NP child of the VP
        np.setDirectObj(true);
      }
    }

    // Set head for a NP, set the rightmost N* as the head, even it's not a leaf
    // for "NP" or "PRP"
//    TreeNode h = findFirstChildNode(node, "N", -1, true);
//    if (h == null) {
//      h = findFirstChildNode(node, "PRP", -1, false);
//    }
//    if (h != null) {
//      tw.setHead(h);
//    }
//    // reselect head for people
//    if (node.getChildCount() > 1) {
//      boolean allNNP = true;
//      Enumeration emu = node.children();
//      while (emu.hasMoreElements()) {
//        TreeNode child = (TreeNode) emu.nextElement();
//        if (!Utils.equalsIgnoreCaseTag(child, "NNP")) {
//          allNNP = false;
//          break;
//        }
//      }
//allNNP is always false.
//      while (allNNP && emu.hasMoreElements()) {
//        TreeNode hPeople = (TreeNode) emu.nextElement();
//        TagWord hPTw = Utils.getTagWord(hPeople);
//        // label the first name, if there is one, as the head of a NP representing a people
//        if (HumanList.isFemale(hPTw.getText())
//            || HumanList.isMale(hPTw.getText())) {
//          tw.setHead(hPeople);
//          break;
//        }
//      }
//    }

    // Connect NP with its argumentHost, i.e., the NP in the same argument domain
    // We consider NP and the NP in it's sibling VP. Lets say
    //
    // case 1 NP, find the NP contained in it's sibling VP
    TreeNode argH = findFirstChildNode(parentNode, "VP", 1, false);
    if (argH != null) {
      tw.setArgumentHead(argH);
      while (Utils.equalsIgnoreCaseTag(argH, "VP")) {
        TreeNode tmp = findFirstChildNode(argH, "VP", 1, false);
        if (tmp == null) {
          break;
        }
        argH = tmp;
      }
      argH = findFirstChildNode(argH, "NP", 1, false);

      if (argH != null) {
        tw.setArgumentHost(argH);
      }
    }
    // case 2 NP under a VP, find the sibling NP of the VP
    if (parentTag.startsWith("VP")) {
      TreeNode upperNode = parentNode;
      while (Utils.equalsIgnoreCaseTag(upperNode, "VP")
          || (Utils.equalsIgnoreCaseTag(upperNode, "S")
          && upperNode.getChildCount() < 2)) {
        upperNode = upperNode.getParent();
      }
      argH = findFirstChildNode(upperNode, "NP", 1, false);
      if (argH != null) {
        tw.setArgumentHost(argH);
        tw.setArgumentHead(parentNode);
      }
    }

    /**********************************************************/
    // connect with adjunctHost : the NP whose adjunct domain is NP in
    if (parentTag.startsWith("PP")) {
      TreeNode grandParentNode = parentNode.getParent();
      if (Utils.startWithTag(grandParentNode, "VP")) {
        argH = findFirstChildNode(grandParentNode.getParent(), "NP", 1, false);
        if (argH != null) {
          tw.setAdjunctHost(argH);
        }
      }
    }

    /**********************************************************/
    // connect with containHost: the NP containing this NP
    TreeNode argadjNode = tw.getArgumentHead();
    if (argadjNode != null) {
      tw.setContainHost(argadjNode);
    } else {
      // deepest ancestorNode NP and its containHosts and the deepest VP
      // ancestorNode; Need preorder here.

      DefaultMutableTreeNode pNode = parentNode;
      boolean gotNP = false;
      boolean gotVP = false;
      while (pNode != null) {
        if (pNode.getUserObject() == null) {
          break;
        }
        if (!gotNP && Utils.startWithTag(pNode, "NP")) {
          tw.setContainHost(Utils.getTagWord(pNode).getContainHost());
        } else if (!gotVP && Utils.startWithTag(pNode, "VP")) {
          tw.setContainHost(pNode);
        }
        if (gotNP && gotVP) {
          break;
        }
        pNode = (DefaultMutableTreeNode) pNode.getParent();
      }
    }

    /**********************************************************/
    // connect with NPDomainHost: the NP whose domain this NP is in
    if (parentTag.startsWith("PP")) {
      TreeNode previousSiblingNodeOfParent = parentNode.getPreviousSibling();
      if (previousSiblingNodeOfParent != null
          && Utils.startWithTag(previousSiblingNodeOfParent, "NP")
          && previousSiblingNodeOfParent.getChildCount() > 1) {
        TreeNode firstCousin = previousSiblingNodeOfParent.getChildAt(0);
        TreeNode secondCousin = previousSiblingNodeOfParent.getChildAt(1);
        if (Utils.startWithTag(firstCousin, "NP")
            && Utils.startWithTag(secondCousin, "N")
            && !firstCousin.isLeaf()) {
          TreeNode posNode = findFirstChildNode(
              firstCousin,
              "POS",
              1,
              false);
          if (posNode != null) {
            tw.setNPDomainHost(
                (DefaultMutableTreeNode) ((DefaultMutableTreeNode) posNode)
                    .getPreviousSibling());
          }
        }
      }
    }

    /**********************************************************/
    // connect with third person Determiner (PRP$): the PRP$ sibling of
    // this NP
    @SuppressWarnings("rawtypes")
    Enumeration siblingNodes = parentNode.children();
    while (siblingNodes.hasMoreElements()) {
      DefaultMutableTreeNode siblingNode = (DefaultMutableTreeNode) siblingNodes
          .nextElement();
      if (siblingNode == node) {
        // Finish scanning the leading siblings
        break;
      } else if (((TagWord) siblingNode.getUserObject()).getTag().equals(
          "PRP$")) {
        tw.setDeterminer(siblingNode);
        ((TagWord) siblingNode.getUserObject()).setDeterminee(node);
        break;
      }
    }

    /**********************************************************/
    // If the tag starts with "NN" (NN NNS NNP NNPS), detect whether it
    // has a sibling with similar tag
    if (tw.getTag().startsWith("NN")) {
      DefaultMutableTreeNode siblingNode = node.getPreviousSibling();
      if (Utils.startWithTag(siblingNode, "NN")) {
        np.setHasNNXsibling(true);
      }
      siblingNode = node.getNextSibling();
      if (Utils.startWithTag(siblingNode, "NN")) {
        np.setHasNNXsibling(true);
      }
    }

    /**********************************************************/
    // First try to set number of the NP, based on the head of the VP
    // related
    TreeNode siblingVPNode = findFirstChildNode(parentNode, "VP", 1, false);
    if (siblingVPNode != null) {
      TreeNode vpHead = Utils.getTagWord(siblingVPNode).getHead();
      if (vpHead != null) {
        String siblingVPHeadTw = Utils.getTag(vpHead);
        if (siblingVPHeadTw.startsWith("AUX")) {
          ;
        } else if (siblingVPHeadTw.endsWith("VBP")) {
          tw.setNumber(NumberEnum.PLURAL);
        } else if (siblingVPHeadTw.endsWith("VBZ")) {
          tw.setNumber(NumberEnum.SINGLE);
        }
      }
    }

    // check the existance of potential ancestor NP (to decide whether
    // it's a head)
    boolean hasNPAncestor = hasAncestor(node, "NP");
    tw.setHasNPAncestor(hasNPAncestor);

    boolean hasADVPAncestor = hasAncestor(node, "ADVP");
    np.setIsInADVP(hasADVPAncestor);

    np.setNodeRepresent(node);
    np.setHead(tw.isHeadNP());
    tw.setNP(np);

    if (tw.getTag().startsWith("PRP")) {
      // for (NP (PRP xxx))
      // copy all the attributes from its parent node while has no
      // siblings
      if (parentTag.startsWith("NP")
          && node.getSiblingCount() == 1) {
        TagWord parentTw = Utils.getTagWord(parentNode);
        tw.setAdjunctHost(parentTw.getAdjunctHost());
        tw.setArgumentHead(parentTw.getArgumentHead());
        tw.setArgumentHost(parentTw.getArgumentHost());
        tw.setContainHost(parentTw.getContainHost());
        tw.setContainHost(parentNode);
        tw.setNPDomainHost(parentTw.getNPDomainHost());
      }

      PRPList.add(tw);
    }

    if (!tw.getTag().equalsIgnoreCase("PRP")) {
      // Ignore PRP, since all of them appear in (NP (PRP xxx)).
      // Rewrite here if the assumption is false.
      NPList.add(tw);
    }
  }

  private void setPPHead(TreeNode node) {
    // set head for 'NP' or "PRP"
    TreeNode h = findFirstChildNode(node, "N", -1, false);
    if (h == null) {
      h = findFirstChildNode(node, "PR", -1, false);
    }
    if (h != null) {
      Utils.getTagWord(node).setHead(h);
    }
  }

  private void setVPHead(TreeNode node) {
    // set head for "V" or "AU"
    TreeNode h = findFirstChildNode(node, "V", 1, false);
    if (h == null) {
      h = findFirstChildNode(node, "AU", 1, false);
    }
    if (h != null) {
      Utils.getTagWord(node).setHead(h);
    }
  }

  private boolean hasAncestor(TreeNode node, String tag) {
    while (node.getParent() != null) {
      node = node.getParent();
      TagWord tw = Utils.getTagWord(node);
      if (tw != null && tw.getTag().equals(tag)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param direction: Binary: +1 (from left) or -1 (from right)
   * @param recursive: true to do the search recursively until a leaf node
   *          matches the requirement is found
   * @return the first child from left(+1)/right(-1) that satisfies taghead
   */
  private TreeNode findFirstChildNode(TreeNode parentNode, String taghead, int direction,
      boolean recursive) {
    if (parentNode.isLeaf()) {
      return null;
    }
    int size = parentNode.getChildCount();
    for (int i = 0; i < size; i++) {
      int idx = (direction > 0 ? i : size - 1 - i);
      TreeNode checkee = parentNode.getChildAt(idx);
      TagWord tw = Utils.getTagWord(checkee);
      if (tw.getTag().startsWith(taghead)) {
        if (recursive && !checkee.isLeaf()) {
          return findFirstChildNode(checkee, taghead, direction, true);
        } else {
          return checkee;
        }
      }
    }
    return null;
  }
}

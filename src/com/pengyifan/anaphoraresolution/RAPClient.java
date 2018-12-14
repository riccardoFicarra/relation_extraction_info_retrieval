package com.pengyifan.anaphoraresolution;

import java.util.List;

import com.google.common.collect.Lists;

import com.pengyifan.nlp.process.anaphoraresolution.AnaphoraResolver;
import com.pengyifan.nlp.process.anaphoraresolution.AnnotatedText;
import com.pengyifan.nlp.process.anaphoraresolution.CorreferencialPair;

public class RAPClient {

  public static void main(String[] args) {

    String parseText = "(S1 (S (S (NP (PRP We)) (VP (VBD demonstrated) (SBAR (IN that) (S (NP (NN katX)) (VP (VBZ is) (ADVP (RB also)) (NP (DT a) (JJ sigmaB-dependent) (JJ general) (NN stress) (NN gene)) (, ,) (SBAR (IN since) (S (NP (PRP it)) (VP (VBZ is) (ADVP (RB strongly)) (VP (VBN induced) (PP (PP (IN by) (NP (NP (NN heat)) (, ,) (NP (NN salt)) (CC and) (NP (NN ethanol) (NN stress)))) (, ,) (CONJP (RB as) (RB well) (IN as)) (PP (IN by) (NP (NN energy) (NN depletion))))))))))))) (. .))(S (S (NP (PRP We)) (VP (VBD demonstrated) (SBAR (IN that) (S (NP (NN katX)) (VP (VBZ is) (ADVP (RB also)) (NP (DT a) (JJ sigmaB-dependent) (JJ general) (NN stress) (NN gene)) (, ,) (SBAR (IN since) (S (NP (PRP it)) (VP (VBZ is) (ADVP (RB strongly)) (VP (VBN induced) (PP (PP (IN by) (NP (NP (NN heat)) (, ,) (NP (NN salt)) (CC and) (NP (NN ethanol) (NN stress)))) (, ,) (CONJP (RB as) (RB well) (IN as)) (PP (IN by) (NP (NN energy) (NN depletion))))))))))))) (. .)))";

    AnnotatedText aText = new AnnotatedText(parseText);
    AnaphoraResolver u = new AnaphoraResolver();
    List<CorreferencialPair> vet = u.resolverV1(
        aText.getNPList(),
        aText.getPRPList());

    for (CorreferencialPair p : vet) {
      System.out.println(p);
    }
  }
}

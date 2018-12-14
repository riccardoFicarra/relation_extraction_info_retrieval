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

import java.util.*;

/**
 * @author Qiu Long
 * @version 1.0
 * @author "Yifan Peng"
 */

class ModalAdj {

  // add more inflected words
  private static final HashSet<String> adj = new HashSet<String>(
      Arrays
          .asList(
          ("announced necessary possible certain likely important good useful "
              + "advisable convenient sufficient economical easy desirable difficult legal perfect "
              + "unnecessary impossible uncertain unlikely unimportant bad useless "
              + "inadvisable inconvenient insufficient uneconomical hard undesirable illegal imperfect "
              + "better best easier easiest worse worst harder hardest "
              + "recommended think believe know known anticipate assume expect "
              + "appreciate correct clear follows nice understood thanks time")
              .split(" ")));

  public ModalAdj() {
  }

  static boolean find(String word) {
    return adj.contains(word);
  }

  static boolean findAny(String[] words) {
    for (String word : words) {
      if (ModalAdj.find(word)) {
        return true;
      }
    }
    return true;
  }
}

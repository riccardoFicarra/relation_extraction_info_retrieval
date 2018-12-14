package com.pengyifan.nlp.process.anaphoraresolution;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class HumanList {

  private final static Splitter SPLITTER = Splitter.on(' ');

  private final static Set<String> maleList = split("he him himself his");
  private final static Set<String> femaleList = split("she her herself");
  private final static Set<String> complementList = split("it its itself");
  private final static Set<String> titleList = split("Mr. Mrs. Miss Ms.");

  private final static Set<String> thirdPersonList = split(
      "he him himself his she her herself they them their themselves it its itself");
  private final static Set<String> secondPersonList = split(
      "you your yourself yourselves");
  private final static Set<String> firstPersonList = split(
      "i me my myself we us our ourselves");
  private final static Set<String> pluralList = split(
      "we us ourselves our they them themselves their");
  private final static Set<String> wholeList = split(
      "he him himself his she her herself"
          + " i me myself my we us ourselves our you your yourself");

  // most common first names,
  // respectively
  // final static Map maleNameTb =
  // getNameTb(System.getProperty("dataPath") + File.separator
  // +"male_first.txt",numberOfNameToCheck);
  private final static Map<String, String> maleNameTb = getNameTb(
      System.getProperty("dataPath")
          + File.separator
          + "MostCommonMaleFirstNamesInUS.mongabay.txt");
  private final static Map<String, String> femaleNameTb = getNameTb(
      System.getProperty("dataPath") + File.separator + "female_first.txt");

  private HumanList() {
  }

  private static Set<String> split(String s) {
    return Sets.newHashSet(SPLITTER.split(s));
  }

  public static boolean isMale(String wd) {
    // People's name should start with a capital letter
    return maleList.contains(wd)
        || (wd.matches("[A-Z][a-z]*") && maleNameTb.containsKey(wd));
  }

  public static boolean isFemale(String wd) {
    // People's name should start with a capital letter
    return femaleList.contains(wd)
        || (wd.matches("[A-Z][a-z]*") && femaleNameTb.containsKey(wd));
  }

  public static boolean isHuman(String wd) {
    if (wd.indexOf(" ") > 0 && contains(titleList, wd.split(" ")[0], false)) {
      // contains more than a single word and starts with a title
      return true;
    }
    return contains(wholeList, wd, true)
        || isMale(wd)
        || isFemale(wd);
  }

  public static boolean isNotHuman(String wd) {
    return contains(complementList, wd, true);
  }

  public static boolean isPlural(String wd) {
    return contains(pluralList, wd, true);
  }

  public static boolean isThirdPerson(String wd) {
    return contains(thirdPersonList, wd, true);
  }

  public static boolean isSecondPerson(String wd) {
    return contains(secondPersonList, wd, true);
  }

  public static boolean isFirstPerson(String wd) {
    return contains(firstPersonList, wd, true);
  }

  public static boolean contains(Set<String> set, String s, boolean ignoreCase) {
    for (String str : set) {
      if (ignoreCase) {
        if (str.equalsIgnoreCase(s)) {
          return true;
        }
      } else {
        if (str.equals(s)) {
          return true;
        }
      }
    }
    return false;
  }

  private static Map<String, String> getNameTb(String fileName) {
    Map<String, String> tb = Maps.newHashMap();
    try {
      for (String line : Files.readAllLines(Paths.get(fileName.substring(1)))) {
        String name = line.charAt(0) + line.substring(1);
        tb.put(name, name);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return tb;
  }
}

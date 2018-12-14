package com.pengyifan.brat.util;

import com.google.common.collect.Sets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public class BratConfigBuilder {

  public static final String BLUE1 = "bgColor:#7fa2ff";
  public static final String BLUE2 = "bgColor:#b4c8ff";
  public static final String BLUE3 = "bgColor:#0000aa";
  public static final String BLUE4 = "bgColor:#0000cc";
  public static final String BLUE5 = "bgColor:#9999ff";

  public static final String COBALT1 = "bgColor:#8fcfff";

  public static final String YELLOW1 = "bgColor:#ffff00";
  public static final String YELLOW2 = "bgColor:#e0ff00";
  public static final String YELLOW3 = "bgColor:#ffe000";

  public static final String GREEN1 = "bgColor:#007700";
  public static final String GREEN2 = "bgColor:#9fe67f";

  public static final String ORANGE1 = "bgColor:#ffccaa";


  public static BratConfigBuilder newBuilder() {
    return new BratConfigBuilder();
  }

  public static final String annotationConfFilename = "annotation.conf";
  public static final String visualConfFilename = "visual.conf";

  Path outputDir;

  Set<Entity> entities;
  Set<Relation> relations;

  private BratConfigBuilder() {
    entities = Sets.newTreeSet();
    relations = Sets.newTreeSet();
  }

  public void setOutputDir(Path outputDir) {
    this.outputDir = outputDir;
  }

  public void addEntity(String entityType) {
    addEntity(entityType, null);
  }

  public void addEntity(String entityType, String color) {
    Entity e = new Entity();
    e.type = entityType;
    e.color = color;
    entities.add(e);
  }

  public void addRelation(String relationType, String color, String fromArg, String fromType,
      String toArg, String toType) {
    Relation r = new Relation();
    r.type = relationType;
    r.color = color;
    r.fromArg = fromArg;
    r.fromType = fromType;
    r.toArg = toArg;
    r.toType = toType;
    relations.add(r);
  }

  public void addRelation(String relationType, String fromArg, String fromType, String toArg,
      String toType) {
    addRelation(relationType, null, fromArg, fromType, toArg, toType);
  }

  public void addRelation(String relationType, String fromArg, String toArg) {
    addRelation(relationType, null, fromArg, "<TOKEN>", toArg, "<TOKEN>");
  }

  public void build() throws IOException {
    // annotation
    StringJoiner annotationSb = new StringJoiner("\n");
    annotationSb.add("[entities]");
    entities.forEach(e -> annotationSb.add(e.toAnnotationRelation()));

    annotationSb.add("");
    annotationSb.add("[relations]");
    annotationSb.add("<TOKEN>=<ENTITY>");
    relations.forEach(r -> annotationSb.add(r.toAnnotationRelation()));

    annotationSb.add("");
    annotationSb.add("[events]");
    annotationSb.add("[attributes]");
    Files.write(outputDir.resolve(annotationConfFilename), annotationSb.toString().getBytes());

    // visual
    StringJoiner visualSb = new StringJoiner("\n");
    visualSb.add("[labels]");
    visualSb.add("### Entity labels");
    entities.forEach(e -> visualSb.add(e.type));

    visualSb.add("");
    visualSb.add("### Relation labels");
    relations.forEach(r -> visualSb.add(r.type));

    visualSb.add("");
    visualSb.add("[drawing]");
    visualSb.add("### Defaults");
    visualSb.add("SPAN_DEFAULT fgColor:black, bgColor:lightgreen, borderColor:darken");
    visualSb.add("ARC_DEFAULT color:black, arrowHead:triangle-5");
    visualSb.add("ATTRIBUTE_DEFAULT glyph:*");

    visualSb.add("");
    visualSb.add("### Entity drawing");
    entities.stream()
        .filter(e -> e.color!=null)
        .forEach(e -> visualSb.add(e.toVisualEntity()));

    visualSb.add("");
    visualSb.add("### Relation drawing");
    relations.stream()
        .filter(r -> r.color!=null)
        .forEach(r -> visualSb.add(r.toVisualEntity()));

    Files.write(outputDir.resolve(visualConfFilename), visualSb.toString().getBytes());
  }

  private class Entity implements Comparable<Entity> {
    String type;
    String color;

    Entity() {
    }

    String toAnnotationRelation () {
      return type;
    }

    String toVisualEntity() {
      return type + "\t" + color;
    }

    @Override
    public int hashCode() {
      return Objects.hash(type);
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof Relation)) {
        return false;
      }
      Relation rhs = (Relation) o;
      return super.equals(o)
          && Objects.equals(type, rhs.type);
    }

    @Override
    public int compareTo(Entity entity) {
      return type.compareTo(entity.type);
    }
  }

  private class Relation implements Comparable<Relation>{
    String type;
    String color;
    String fromArg;
    String fromType;
    String toArg;
    String toType;

    Relation() {
      fromType = "<TOKEN>";
      toType = "<TOKEN>";
    }

    String toAnnotationRelation () {
      return String.format("%s\t%s:%s,%s:%s", type, fromArg, fromType, toArg, toType);
    }

    @Override
    public int hashCode() {
      return Objects.hash(type);
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof Relation)) {
        return false;
      }
      Relation rhs = (Relation) o;
      return super.equals(o)
          && Objects.equals(type, rhs.type);
    }

    @Override
    public int compareTo(Relation relation) {
      return type.compareTo(relation.type);
    }

    String toVisualEntity() {
      return type + "\t" + color;
    }
  }
}

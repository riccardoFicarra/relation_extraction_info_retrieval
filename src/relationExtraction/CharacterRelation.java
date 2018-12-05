package relationExtraction;

public class CharacterRelation {
    private String character1;
    private String character2;
    private boolean changes;
    private String affinity;
    private String coarseCategory;
    private String fineCategory;
    private String detail;

    CharacterRelation(String character1, String character2, boolean changes, String affinity, String coarseCategory, String fineCategory, String detail) {
        this.character1 = character1;
        this.character2 = character2;
        this.changes = changes;
        this.affinity = affinity;
        this.coarseCategory = coarseCategory;
        this.fineCategory = fineCategory;
        this.detail = detail;
    }

    public String getCharacter1() {
        return character1;
    }

    public String getCharacter2() {
        return character2;
    }

    public boolean changes() {
        return changes;
    }

    public String getAffinity() {
        return affinity;
    }

    public String getCoarseCategory() {
        return coarseCategory;
    }

    public String getFineCategory() {
        return fineCategory;
    }

    public String getDetail() {
        return detail;
    }
}

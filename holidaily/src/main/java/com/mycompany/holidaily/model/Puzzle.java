package com.mycompany.holidaily.model;

public class Puzzle {
    private final int startWordId;
    private final int endWordId;
    private final int themeId;
    private final Integer userId;   // gonna be null for initial seed puzzles
    private final int difficultyScore;
    private final int ladderLength;

    public Puzzle(int startWordId, int endWordId, int themeId, Integer userId,
                  int difficultyScore, int ladderLength) {
        this.startWordId = startWordId;
        this.endWordId = endWordId;
        this.themeId = themeId;
        this.userId = userId;
        this.difficultyScore = difficultyScore;
        this.ladderLength = ladderLength;
    }

    public int getStartWordId() { return startWordId; }
    public int getEndWordId() { return endWordId; }
    public int getThemeId() { return themeId; }
    public Integer getUserId() { return userId; }
    public int getDifficultyScore() { return difficultyScore; }
    public int getLadderLength() { return ladderLength; }
}

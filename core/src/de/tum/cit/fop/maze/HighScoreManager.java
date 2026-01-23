package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Manages high scores using LibGDX Preferences.
 * Scores are stored locally, sorted, and only the top values are kept.
 */

public class HighScoreManager {
    /** Local storage used to save high scores persistently. */
    private static final Preferences prefs = Gdx.app.getPreferences("MyMazeScores");

    /**
     * Saves a new score and keeps only the top 5 highest scores.
     *
     * @param score new score to be saved
     */

    public static void saveScore(int score) {
        String oldScores = prefs.getString("list", "");

        if (oldScores.isEmpty()) {
            oldScores = String.valueOf(score);
        } else {
            oldScores += "," + score;
        }

        String[] arr = oldScores.split(",");
        ArrayList<Integer> list = new ArrayList<>();
        for (String s : arr) {
            list.add(Integer.parseInt(s));
        }

        Collections.sort(list, Collections.reverseOrder());

        StringBuilder sb = new StringBuilder();
        int limit = Math.min(list.size(), 5);
        for (int i = 0; i < limit; i++) {
            sb.append(list.get(i));
            if (i < limit - 1) sb.append(",");
        }

        prefs.putString("list", sb.toString());
        prefs.flush();
    }

    /**
     * Returns the saved high scores as a string array.
     *
     * @return array of top scores, or empty array if no score exists
     */

    public static String[] getTopScores() {
        String s = prefs.getString("list", "");
        if (s.isEmpty()) return new String[0];
        return s.split(",");
    }
}
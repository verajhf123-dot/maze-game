package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MapLoader {

    public List<Wall> loadWalls(String internalPath) {
        List<Wall> walls = new ArrayList<>();
        Properties props = new Properties();

        try {
            FileHandle file = Gdx.files.local(internalPath);
            props.load(file.read());

            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key).trim();

                int type = Integer.parseInt(value);

                if (type == 0) { // 0 = Wall
                    String[] xy = key.split(",");
                    int x = Integer.parseInt(xy[0]);
                    int y = Integer.parseInt(xy[1]);

                    walls.add(new Wall(x, y));
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading map file: " + internalPath);
            e.printStackTrace();
        }

        return walls;
    }
}

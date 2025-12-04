package de.tum.cit.fop.maze;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MapLoader {

    public List<Wall> loadWalls(String filePath) {
        List<Wall> walls = new ArrayList<>();

        Properties props = new Properties();

        // 这里用 FileInputStream，直接从项目工作目录读取 "maps/level-1.properties"
        try (InputStream input = new FileInputStream(filePath)) {
            props.load(input);

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
        } catch (IOException e) {
            System.out.println("Error loading map file: " + filePath);
            e.printStackTrace();
        }

        return walls;
    }
}

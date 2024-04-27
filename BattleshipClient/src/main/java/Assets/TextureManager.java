package Assets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;

public class TextureManager {
    public static HashMap<String, Image> textureCache = new HashMap<String, Image>();

    // Expects fileName to be in the directory "textures" in the directory "materials" in the "resources" directory
    public static Image load(String fileName) {
        if (textureCache.containsKey(fileName)) {
            return textureCache.get(fileName);
        }

        long startTime = System.nanoTime();
        Image image = new Image(MeshManager.class.getResourceAsStream("/materials/textures/" + fileName));
        long endTime = System.nanoTime();
        System.out.println("    Finished reading image in " + ((endTime - startTime) / 1000000.0) + "ms");

        textureCache.put(fileName, image);
        return image;
    }
}

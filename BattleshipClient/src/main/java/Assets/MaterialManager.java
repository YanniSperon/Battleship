package Assets;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

public class MaterialManager {
    public static HashMap<String, PhongMaterial> materialCache = new HashMap<String, PhongMaterial>();

    // Expects fileName to be a file in the directory "materials" in the "resources" directory
    // Expects textures to be in materials/textures
    public static PhongMaterial load(String fileName) {
        if (materialCache.containsKey(fileName)) {
            return materialCache.get(fileName);
        }

        BufferedReader reader;
        PhongMaterial material = new PhongMaterial();
        try {
            long startTime = System.nanoTime();
            System.out.println("Loading material \"" + fileName + "\":");
            reader = new BufferedReader(new InputStreamReader(MeshManager.class.getResourceAsStream("/materials/" + fileName)));
            String line = reader.readLine();
            String[] tokens;
            int currToken = 0;
            String[] tempToks;
            float[] tempF = new float[4];

            while (line != null) {
                tokens = line.split(" ", 2);
                currToken = 0;
                switch (tokens[currToken++]) {
                    case "bumpMap:": {
                        String mapName = tokens[currToken];
                        if (!mapName.equals("Default")) {
                            material.setBumpMap(TextureManager.load(mapName));
                        }
                        break;
                    }
                    case "diffuseColor:": {
                        String color = tokens[currToken];
                        if (!color.equals("Default")) {
                            currToken = 0;
                            tempToks = color.split(" ");
                            tempF[0] = Float.parseFloat(tempToks[currToken++]);
                            tempF[1] = Float.parseFloat(tempToks[currToken++]);
                            tempF[2] = Float.parseFloat(tempToks[currToken++]);
                            tempF[3] = Float.parseFloat(tempToks[currToken]);
                            material.setDiffuseColor(new Color(tempF[0], tempF[1], tempF[2], tempF[3]));
                        }
                        break;
                    }
                    case "diffuseMap:": {
                        String mapName = tokens[currToken];
                        if (!mapName.equals("Default")) {
                            material.setDiffuseMap(TextureManager.load(mapName));
                        }
                        break;
                    }
                    case "selfIlluminationMap:": {
                        String mapName = tokens[currToken];
                        if (!mapName.equals("Default")) {
                            material.setSelfIlluminationMap(TextureManager.load(mapName));
                        }
                        break;
                    }
                    case "specularColor:": {
                        String color = tokens[currToken];
                        if (!color.equals("Default")) {
                            currToken = 0;
                            tempToks = color.split(" ");
                            tempF[0] = Float.parseFloat(tempToks[currToken++]);
                            tempF[1] = Float.parseFloat(tempToks[currToken++]);
                            tempF[2] = Float.parseFloat(tempToks[currToken++]);
                            tempF[3] = Float.parseFloat(tempToks[currToken]);
                            material.setSpecularColor(new Color(tempF[0], tempF[1], tempF[2], tempF[3]));
                        }
                        break;
                    }
                    case "specularMap:": {
                        String mapName = tokens[currToken];
                        if (!mapName.equals("Default")) {
                            material.setSpecularMap(TextureManager.load(mapName));
                        }
                        break;
                    }
                    case "specularPower:": {
                        String specularFactor = tokens[currToken];
                        if (!specularFactor.equals("Default")) {
                            material.setSpecularPower(Double.parseDouble(specularFactor));
                        }
                        break;
                    }
                }


                line = reader.readLine();
            }
            long endTime = System.nanoTime();
            System.out.println("    Finished reading material in " + ((endTime - startTime) / 1000000.0) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }

        materialCache.put(fileName, material);
        return material;
    }
}

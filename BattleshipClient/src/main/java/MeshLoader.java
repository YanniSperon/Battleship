import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class MeshLoader {
    public static HashMap<String, TriangleMesh> cache = new HashMap<String, TriangleMesh>();

    // Expects fileName to be the name of a file in the meshes directory in the resources directory
    public static TriangleMesh load(String fileName) {
        if (cache.containsKey(fileName)) {
            return cache.get(fileName);
        }

        BufferedReader reader;
        TriangleMesh mesh = new TriangleMesh();
        mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
        try {
            reader = new BufferedReader(new InputStreamReader(MeshLoader.class.getResourceAsStream("/meshes/" + fileName)));
            String line = reader.readLine();
            ArrayList<Float> verticesAL = new ArrayList<Float>();
            ArrayList<Float> normalsAL = new ArrayList<Float>();
            ArrayList<Float> textureCoordsAL = new ArrayList<Float>();
            ArrayList<Integer> facesAL = new ArrayList<Integer>();
            while (line != null) {
                Scanner lineScanner = new Scanner(line);
                String lineType = lineScanner.next();
                switch (lineType) {
                    case "v": {
                        // Vertex
                        verticesAL.add(lineScanner.nextFloat());
                        verticesAL.add(lineScanner.nextFloat());
                        verticesAL.add(lineScanner.nextFloat());
                        break;
                    }
                    case "vn": {
                        // Vertex Normal
                        normalsAL.add(lineScanner.nextFloat());
                        normalsAL.add(lineScanner.nextFloat());
                        normalsAL.add(lineScanner.nextFloat());
                        break;
                    }
                    case "vt": {
                        // Texture coordinate
                        textureCoordsAL.add(lineScanner.nextFloat());
                        textureCoordsAL.add(lineScanner.nextFloat());
                        break;
                    }
                    case "o": {
                        // Object definition, ignore for now
                        break;
                    }
                    case "mtllib": {
                        // Material library definition, possibly use
                        String materialPath = lineScanner.next();
                        break;
                    }
                    case "usemtl": {
                        // Material usage specification, possibly use
                        String materialToUse = lineScanner.next();
                        break;
                    }
                    case "l": {
                        // Defines polyline, ignore for now
                        break;
                    }
                    case "s": {
                        // Defines smooth shading as on or off
                        boolean smoothShading = false;
                        if (lineScanner.hasNextInt()) {
                            smoothShading = lineScanner.nextInt() == 1;
                        } else {
                            String state = lineScanner.next();
                            if (state.equals("on") || state.equals("true")) {
                                smoothShading = true;
                            } // Otherwise keep default value
                        }
                        // Potentially do something with smoothShading value
                        break;
                    }
                    case "f": {
                        // Face parsing
                        String v1 = lineScanner.next();
                        String v2 = lineScanner.next();
                        String v3 = lineScanner.next();
                        Scanner v1Scanner = new Scanner(v1);
                        Scanner v2Scanner = new Scanner(v2);
                        Scanner v3Scanner = new Scanner(v3);
                        v1Scanner.useDelimiter("/");
                        v2Scanner.useDelimiter("/");
                        v3Scanner.useDelimiter("/");
                        // For now, expect obj format to use vertex/normal/texcoord per triangle point

                        // First point
                        int vertexInd1 = v1Scanner.nextInt() - 1; // Vertex index
                        int textureCoordInd1 = v1Scanner.nextInt() - 1; // Texture coord index
                        int normalInd1 = v1Scanner.nextInt() - 1; // Normal index

                        facesAL.add(vertexInd1);
                        facesAL.add(normalInd1);
                        facesAL.add(textureCoordInd1);

                        // Second point
                        int vertexInd2 = v2Scanner.nextInt() - 1; // Vertex index
                        int textureCoordInd2 = v2Scanner.nextInt() - 1; // Texture coord index
                        int normalInd2 = v2Scanner.nextInt() - 1; // Normal index

                        facesAL.add(vertexInd2);
                        facesAL.add(normalInd2);
                        facesAL.add(textureCoordInd2);

                        // Third point
                        int vertexInd3 = v3Scanner.nextInt() - 1; // Vertex index
                        int textureCoordInd3 = v3Scanner.nextInt() - 1; // Texture coord index
                        int normalInd3 = v3Scanner.nextInt() - 1; // Normal index

                        facesAL.add(vertexInd3);
                        facesAL.add(normalInd3);
                        facesAL.add(textureCoordInd3);
                        break;
                    }
                }


                line = reader.readLine();
            }
            float[] vertices = new float[verticesAL.size()];
            float[] normals = new float[normalsAL.size()];
            float[] textureCoordinates = new float[textureCoordsAL.size()];
            int[] faces = new int[facesAL.size()];
            int i = 0;
            for (Float f : verticesAL) {
                vertices[i++] = f;
            }
            i = 0;
            for (Float n : normalsAL) {
                normals[i++] = n;
            }
            i = 0;
            for (Float t : textureCoordsAL) {
                textureCoordinates[i++] = t;
            }
            i = 0;
            for (Integer ind : facesAL) {
                faces[i++] = ind;
            }
            mesh.getPoints().setAll(vertices);
            mesh.getNormals().setAll(normals);
            mesh.getTexCoords().setAll(textureCoordinates);
            mesh.getFaces().setAll(faces);

        } catch (Exception e) {
            e.printStackTrace();
        }
        cache.put(fileName, mesh);
        return mesh;
    }
}

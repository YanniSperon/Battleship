package Assets;

import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MeshManager {
    public static HashMap<String, Mesh3D> regularModelCache = new HashMap<String, Mesh3D>();

    public static HashMap<String, AnimatedMesh3D> animatedModelCache = new HashMap<String, AnimatedMesh3D>();

    // Expects fileName to be the name of a file in the meshes directory in the resources folder
    public static Mesh3D load(String fileName) {
        return load(fileName, true);
    }

    // Expects fileName to be the name of a file in the meshes directory in the resources directory
    private static Mesh3D load(String fileName, boolean shouldCache) {
        if (shouldCache && regularModelCache.containsKey(fileName)) {
            return regularModelCache.get(fileName);
        }

        BufferedReader reader;
        TriangleMesh mesh = new TriangleMesh();
        mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
        try {
            long startTime = System.nanoTime();
            System.out.println("Loading mesh \"" + fileName + "\":");
            reader = new BufferedReader(new InputStreamReader(MeshManager.class.getResourceAsStream("/meshes/" + fileName)));
            String line = reader.readLine();
            String[] tokens;
            int[] tempBuf = new int[3];
            int currToken = 0;

            PrimitiveFloatArrayList vertices = new PrimitiveFloatArrayList();
            PrimitiveFloatArrayList normals = new PrimitiveFloatArrayList();
            PrimitiveFloatArrayList textureCoords = new PrimitiveFloatArrayList();
            PrimitiveIntArrayList faces = new PrimitiveIntArrayList();

            while (line != null) {
                tokens = line.split("[ /]");
                currToken = 0;
                switch (tokens[currToken++]) {
                    case "v": {
                        // Vertex
                        vertices.add(Float.parseFloat(tokens[currToken++]));
                        vertices.add(Float.parseFloat(tokens[currToken++]));
                        vertices.add(Float.parseFloat(tokens[currToken]));
                        break;
                    }
                    case "vn": {
                        // Vertex Normal
                        normals.add(Float.parseFloat(tokens[currToken++]));
                        normals.add(Float.parseFloat(tokens[currToken++]));
                        normals.add(Float.parseFloat(tokens[currToken]));
                        break;
                    }
                    case "vt": {
                        // Texture coordinate
                        textureCoords.add(Float.parseFloat(tokens[currToken++]));
                        textureCoords.add(1.0f - Float.parseFloat(tokens[currToken]));
                        break;
                    }
                    case "o": {
                        // Object definition, ignore for now
                        break;
                    }
                    case "mtllib": {
                        // Material library definition, possibly use
                        String materialPath = tokens[currToken++];
                        break;
                    }
                    case "usemtl": {
                        // Material usage specification, possibly use
                        String materialToUse = tokens[currToken++];
                        break;
                    }
                    case "l": {
                        // Defines polyline, ignore for now
                        break;
                    }
                    case "s": {
                        // Defines smooth shading as on or off
                        boolean smoothShading = false;
                        break;
                    }
                    case "f": {
                        // Face parsing
                        // For now, expect obj format to use vertex/normal/texcoord per triangle point

                        // First point
                        tempBuf[0] = Integer.parseInt(tokens[currToken++]) - 1; // Vertex 1
                        tempBuf[1] = Integer.parseInt(tokens[currToken++]) - 1; // Texture coord 1
                        tempBuf[2] = Integer.parseInt(tokens[currToken++]) - 1; // Normal 1
                        faces.add(tempBuf[0]);
                        faces.add(tempBuf[2]);
                        faces.add(tempBuf[1]);

                        tempBuf[0] = Integer.parseInt(tokens[currToken++]) - 1; // Vertex 2
                        tempBuf[1] = Integer.parseInt(tokens[currToken++]) - 1; // Texture coord 2
                        tempBuf[2] = Integer.parseInt(tokens[currToken++]) - 1; // Normal 2
                        faces.add(tempBuf[0]);
                        faces.add(tempBuf[2]);
                        faces.add(tempBuf[1]);

                        tempBuf[0] = Integer.parseInt(tokens[currToken++]) - 1; // Vertex 3
                        tempBuf[1] = Integer.parseInt(tokens[currToken++]) - 1; // Texture coord 3
                        tempBuf[2] = Integer.parseInt(tokens[currToken++]) - 1; // Normal 3
                        faces.add(tempBuf[0]);
                        faces.add(tempBuf[2]);
                        faces.add(tempBuf[1]);
                        break;
                    }
                }


                line = reader.readLine();
            }
            long endTime = System.nanoTime();
            System.out.println("    Finished reading file in " + ((endTime - startTime) / 1000000.0) + "ms");

            vertices.trim();
            normals.trim();
            textureCoords.trim();
            faces.trim();

            mesh.getPoints().setAll(vertices.data);
            mesh.getNormals().setAll(normals.data);
            mesh.getTexCoords().setAll(textureCoords.data);
            mesh.getFaces().setAll(faces.data);

            System.out.println("    Number of vertices: " + mesh.getPoints().size());
            System.out.println("    Number of triangles: " + mesh.getFaces().size());

        } catch (Exception e) {
            e.printStackTrace();
        }
        Mesh3D outputMesh = new Mesh3D(mesh);
        if (shouldCache) {
            regularModelCache.put(fileName, outputMesh);
        }
        return outputMesh;
    }

    // Expects fileNames ArrayList to be at least size of 2
    // Expects the strings in fileNames to be the name of a file in the meshes directory in the resources directory
    // Meshes are expected to have the same faces, texture coordinates, and normals, only different vertex positions
    // This likely means animated meshes cannot be properly lit as the normals will be incorrect during animation
    public static AnimatedMesh3D loadAnimated(ArrayList<String> fileNames) {
        StringBuilder cacheNameSB = new StringBuilder();
        for (String fileName : fileNames) {
            cacheNameSB.append(fileName);
        }
        String cacheName = cacheNameSB.toString();

        if (animatedModelCache.containsKey(cacheName)) {
            return animatedModelCache.get(cacheName);
        }

        BufferedReader reader;
        AnimatedMesh3D mesh = new AnimatedMesh3D();
        boolean isFirst = true;
        for (String fileName : fileNames) {
            try {
                if (isFirst) {
                    mesh.setInitialMesh(load(fileName, false).mesh);
                    isFirst = false;
                } else {
                    reader = new BufferedReader(new InputStreamReader(MeshManager.class.getResourceAsStream("/meshes/" + fileName)));
                    String line = reader.readLine();
                    PrimitiveFloatArrayList vertices = new PrimitiveFloatArrayList();
                    String[] tokens;
                    int currToken = 0;
                    while (line != null) {
                        currToken = 0;
                        tokens = line.split(" ");
                        if (tokens[currToken++].equals("v")) {// Vertex
                            vertices.add(Float.parseFloat(tokens[currToken++]));
                            vertices.add(Float.parseFloat(tokens[currToken++]));
                            vertices.add(Float.parseFloat(tokens[currToken]));
                        }

                        line = reader.readLine();
                    }
                    vertices.trim();

                    mesh.addFrame(vertices);

                    System.out.println("Loaded frame \"" + fileName + "\":");
                    System.out.println("    Number of vertices: " + vertices.size);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        animatedModelCache.put(cacheName, mesh);
        return mesh;
    }
}

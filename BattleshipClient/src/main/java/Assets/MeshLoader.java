package Assets;

import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class MeshLoader {
    public static HashMap<String, Mesh3D> regularModelCache = new HashMap<String, Mesh3D>();

    public static HashMap<String, AnimatedMesh3D> animatedModelCache = new HashMap<String, AnimatedMesh3D>();

    // Expects fileName to be the name of a file in the meshes directory in the resources directory
    public static Mesh3D load(String fileName, boolean shouldCache) {
        if (shouldCache && regularModelCache.containsKey(fileName)) {
            return regularModelCache.get(fileName);
        }

        BufferedReader reader;
        TriangleMesh mesh = new TriangleMesh();
        mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
        try {
            reader = new BufferedReader(new InputStreamReader(MeshLoader.class.getResourceAsStream("/meshes/" + fileName)));
            String line = reader.readLine();
            PrimitiveFloatArrayList vertices = new PrimitiveFloatArrayList();
            PrimitiveFloatArrayList normals = new PrimitiveFloatArrayList();
            PrimitiveFloatArrayList textureCoords = new PrimitiveFloatArrayList();
            PrimitiveIntArrayList faces = new PrimitiveIntArrayList();
            while (line != null) {
                Scanner lineScanner = new Scanner(line);
                String lineType = lineScanner.next();
                switch (lineType) {
                    case "v": {
                        // Vertex
                        vertices.add(lineScanner.nextFloat());
                        vertices.add(lineScanner.nextFloat());
                        vertices.add(lineScanner.nextFloat());
                        break;
                    }
                    case "vn": {
                        // Vertex Normal
                        normals.add(lineScanner.nextFloat());
                        normals.add(lineScanner.nextFloat());
                        normals.add(lineScanner.nextFloat());
                        break;
                    }
                    case "vt": {
                        // Texture coordinate
                        textureCoords.add(lineScanner.nextFloat());
                        textureCoords.add(lineScanner.nextFloat());
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

                        faces.add(vertexInd1);
                        faces.add(normalInd1);
                        faces.add(textureCoordInd1);

                        // Second point
                        int vertexInd2 = v2Scanner.nextInt() - 1; // Vertex index
                        int textureCoordInd2 = v2Scanner.nextInt() - 1; // Texture coord index
                        int normalInd2 = v2Scanner.nextInt() - 1; // Normal index

                        faces.add(vertexInd2);
                        faces.add(normalInd2);
                        faces.add(textureCoordInd2);

                        // Third point
                        int vertexInd3 = v3Scanner.nextInt() - 1; // Vertex index
                        int textureCoordInd3 = v3Scanner.nextInt() - 1; // Texture coord index
                        int normalInd3 = v3Scanner.nextInt() - 1; // Normal index

                        faces.add(vertexInd3);
                        faces.add(normalInd3);
                        faces.add(textureCoordInd3);
                        break;
                    }
                }


                line = reader.readLine();
            }
            vertices.trim();
            normals.trim();
            textureCoords.trim();
            faces.trim();

            mesh.getPoints().setAll(vertices.data);
            mesh.getNormals().setAll(normals.data);
            mesh.getTexCoords().setAll(textureCoords.data);
            mesh.getFaces().setAll(faces.data);

            System.out.println("Loaded mesh \"" + fileName + "\":");
            System.out.println("    Number of vertices: " + mesh.getPoints().size());
            System.out.println("    Number of normals: " + mesh.getNormals().size());
            System.out.println("    Number of texture coordinates: " + mesh.getTexCoords().size());
            System.out.println("    Number of faces: " + mesh.getFaces().size());

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
                    reader = new BufferedReader(new InputStreamReader(MeshLoader.class.getResourceAsStream("/meshes/" + fileName)));
                    String line = reader.readLine();
                    PrimitiveFloatArrayList vertices = new PrimitiveFloatArrayList();
                    while (line != null) {
                        Scanner lineScanner = new Scanner(line);
                        String lineType = lineScanner.next();
                        if (lineType.equals("v")) {// Vertex
                            vertices.add(lineScanner.nextFloat());
                            vertices.add(lineScanner.nextFloat());
                            vertices.add(lineScanner.nextFloat());
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

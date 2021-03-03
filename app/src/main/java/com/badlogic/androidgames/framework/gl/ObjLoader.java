package com.badlogic.androidgames.framework.gl;

import com.badlogic.androidgames.framework.impl.GLGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class fMaterial {
    String name;
    float r, g, b, a;
    float getR() { return r; };
    float getG() { return g;};
    float getB() { return b;};
    float getA() { return a;};
    void setR(float v) {r = v;};
    void setG(float v) {g = v;};
    void setB(float v) {b = v;};
    void setA(float v) {a = v;};
    void setName(String materialName) {name = materialName;};
}

class Materials {
    static List<fMaterial> materials = new ArrayList<fMaterial>();
    static fMaterial currentMaterial = null;

    static fMaterial findMaterial(String name) {
        for (int i = 0; i < materials.size(); i++) {
            if (materials.get(i).name.equals(name)) {
                return materials.get(i);
            }
        }
        return null;
    }

    public static void load(GLGame game, String file) {
        InputStream in = null;
        try {
            in = game.getFileIO().readAsset(file);
            List<String> lines = ObjLoader.readLines(in);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line.startsWith("newmtl")) {
                    String[] tokens = line.split("[ ]+");
                    fMaterial material = new fMaterial();
                    material.setName(tokens[1]);
                    materials.add(material);
                    currentMaterial = material;
                    continue;
                }

                if (line.startsWith("Kd") && currentMaterial != null) {
                    String[] tokens = line.split("[ ]+");
                    currentMaterial.setR(Float.parseFloat(tokens[1]));
                    currentMaterial.setG(Float.parseFloat(tokens[2]));
                    currentMaterial.setB(Float.parseFloat(tokens[3]));
                    currentMaterial.setA(1);
                    continue;
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException("couldn't load '" + file + "'", ex);
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Exception ex) {

                }
        }
    }
}


public class ObjLoader {
    public static Vertices3 load(GLGame game, String file) {
        String objFile = file + ".obj";
        String mtlFile = file + ".mtl";
        Materials.load(game, mtlFile);

        InputStream in = null;
        try {
            in = game.getFileIO().readAsset(objFile);
            List<String> lines = readLines(in);

            float[] vertices = new float[lines.size() * 3];
            float[] normals = new float[lines.size() * 3];
            float[] uv = new float[lines.size() * 2];
            float[] color_r = new float[lines.size() * 3];
            float[] color_g = new float[lines.size() * 3];
            float[] color_b = new float[lines.size() * 3];
            float[] color_a = new float[lines.size() * 3];
            float current_color_r = 0;
            float current_color_g = 0;
            float current_color_b = 1;
            float current_color_a = 1;

            int numVertices = 0;
            int numNormals = 0;
            int numUV = 0;
            int numFaces = 0;
            
            int[] facesVerts = new int[lines.size() * 3];
            int[] facesNormals = new int[lines.size() * 3];
            int[] facesUV = new int[lines.size() * 3];
            float[] normal = new float[3];
            float[] p1 = new float[3];
            float[] p2 = new float[3];
            float[] p3 = new float[3];
            int vertexIndex = 0;
            int normalIndex = 0;
            int uvIndex = 0;
            int faceIndex = 0;


            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                if (line.startsWith("v ")) {
                    String[] tokens = line.split("[ ]+");
                    vertices[vertexIndex] = Float.parseFloat(tokens[1]);
                    vertices[vertexIndex + 1] = Float.parseFloat(tokens[2]);
                    vertices[vertexIndex + 2] = Float.parseFloat(tokens[3]);
                    vertexIndex += 3;
                    numVertices++;
                    continue;
                }

                if (line.startsWith("vn ")) {
                    String[] tokens = line.split("[ ]+");
                    normals[normalIndex] = Float.parseFloat(tokens[1]);
                    normals[normalIndex + 1] = Float.parseFloat(tokens[2]);
                    normals[normalIndex + 2] = Float.parseFloat(tokens[3]);
                    normalIndex += 3;
                    numNormals++;
                    continue;
                }

                if (line.startsWith("vt")) {
                    String[] tokens = line.split("[ ]+");
                    uv[uvIndex] = Float.parseFloat(tokens[1]);
                    uv[uvIndex + 1] = Float.parseFloat(tokens[2]);
                    uvIndex += 2;
                    numUV++;
                    continue;
                }

                if (line.startsWith("usemtl")) {
                    String[] tokens = line.split("[ ]+");

                    fMaterial material = Materials.findMaterial(tokens[1]);
                    if (material != null) {
                        current_color_r = material.getR();
                        current_color_g = material.getG();
                        current_color_b = material.getB();
                        current_color_a = material.getA();
                    }
                }


                if (line.startsWith("f ")) {
                    String[] tokens = line.split("[ ]+");

                    String[] parts = tokens[1].split("/");
                    facesVerts[faceIndex] = getIndex(parts[0], numVertices);
                    if (parts.length > 2)
                        facesNormals[faceIndex] = getIndex(parts[2], numNormals);
                    if (parts.length > 1 && parts[1].length() > 0)
                        facesUV[faceIndex] = getIndex(parts[1], numUV);
                    faceIndex++;

                    parts = tokens[2].split("/");
                    facesVerts[faceIndex] = getIndex(parts[0], numVertices);
                    if (parts.length > 2)
                        facesNormals[faceIndex] = getIndex(parts[2], numNormals);
                    if (parts.length > 1 && parts[1].length() > 0)
                        facesUV[faceIndex] = getIndex(parts[1], numUV);
                    faceIndex++;

                    parts = tokens[3].split("/");
                    facesVerts[faceIndex] = getIndex(parts[0], numVertices);
                    if (parts.length > 2)
                        facesNormals[faceIndex] = getIndex(parts[2], numNormals);
                    if (parts.length > 1 && parts[1].length() > 0)
                        facesUV[faceIndex] = getIndex(parts[1], numUV);
                    color_r[numFaces] = current_color_r;
                    color_g[numFaces] = current_color_g;
                    color_b[numFaces] = current_color_b;
                    color_a[numFaces] = current_color_a;
                    faceIndex++;
                    numFaces++;

                    if (tokens.length == 5) {
                        parts = tokens[3].split("/");
                        facesVerts[faceIndex] = getIndex(parts[0], numVertices);
                        if (parts.length > 2)
                            facesNormals[faceIndex] = getIndex(parts[2], numNormals);
                        if (parts.length > 1 && parts[1].length() > 0)
                            facesUV[faceIndex] = getIndex(parts[1], numUV);
                        faceIndex++;

                        parts = tokens[4].split("/");
                        facesVerts[faceIndex] = getIndex(parts[0], numVertices);
                        if (parts.length > 2)
                            facesNormals[faceIndex] = getIndex(parts[2], numNormals);
                        if (parts.length > 1 && parts[1].length() > 0)
                            facesUV[faceIndex] = getIndex(parts[1], numUV);
                        faceIndex++;

                        parts = tokens[1].split("/");
                        facesVerts[faceIndex] = getIndex(parts[0], numVertices);
                        if (parts.length > 2)
                            facesNormals[faceIndex] = getIndex(parts[2], numNormals);
                        if (parts.length > 1 && parts[1].length() > 0)
                            facesUV[faceIndex] = getIndex(parts[1], numUV);
                        color_r[numFaces] = current_color_r;
                        color_g[numFaces] = current_color_g;
                        color_b[numFaces] = current_color_b;
                        color_a[numFaces] = current_color_a;
                        faceIndex++;
                        numFaces++;
                    }
                    continue;
                }
            }
            numUV = 0;
            float[] verts = new float[(numFaces * 3)
                                      * (3 + 4 + (numNormals > 0 ? 3 : 0) + (numUV > 0 ? 2 : 0))];
            for (int i = 0, vi = 0; i < numFaces * 3; i++) {
                int vertexIdx = facesVerts[i] * 3;
                verts[vi++] = vertices[vertexIdx];
                verts[vi++] = vertices[vertexIdx + 1];
                verts[vi++] = vertices[vertexIdx + 2];

                verts[vi++] = color_r[i/3];
                verts[vi++] = color_g[i/3];
                verts[vi++] = color_b[i/3];
                verts[vi++] = color_a[i/3];

                getFacePoints(p1, p2, p3, i, facesVerts, vertices);
                getNormal(p1, p2, p3, normal);
                verts[vi++] = normal[0];
                verts[vi++] = normal[1];
                verts[vi++] = normal[2];


                if (numUV > 0 && false) {
                    int uvIdx = facesUV[i] * 2;
                    verts[vi++] = uv[uvIdx];
                    verts[vi++] = 1 - uv[uvIdx + 1];
                }

                if (numNormals > 0 && false) {
                    int normalIdx = facesNormals[i] * 3;

                    verts[vi++] = normals[normalIdx];
                    verts[vi++] = normals[normalIdx + 1];
                    verts[vi++] = normals[normalIdx + 2];
                }
            }

            Vertices3 model = new Vertices3(game.getGLGraphics(), numFaces * 3,
                    0, true, numUV > 0, numNormals > 0);
            model.setVertices(verts, 0, verts.length);
            return model;
        } catch (Exception ex) {
            throw new RuntimeException("couldn't load '" + file + "'", ex);
        } finally{
                if (in != null)
                    try {
                        in.close();
                    } catch (Exception ex) {

                    }
            }
        }

     static void getFacePoints(float[] p1, float[] p2, float[] p3, int i, int[] facesVerts, float[] vertices) {
        int p1Index = i;
        int p2Index;
        int p3Index;

        if (i % 3 == 0) { // first vertex
            p2Index = i + 1;
            p3Index = i + 2;
        } else if (i % 3 == 1) { // second vertex
            p2Index = i + 1;
            p3Index = i - 1;
        } else { // third vertex
            p2Index = i - 2;
            p3Index = i - 1;
        }

        int vertexIdx = facesVerts[p1Index] * 3;
        p1[0] = vertices[vertexIdx];
        p1[1] = vertices[vertexIdx + 1];
        p1[2] = vertices[vertexIdx + 2];

        vertexIdx = facesVerts[p2Index] * 3;
        p2[0] = vertices[vertexIdx];
        p2[1] = vertices[vertexIdx + 1];
        p2[2] = vertices[vertexIdx + 2];

        vertexIdx = facesVerts[p3Index] * 3;
        p3[0] = vertices[vertexIdx];
        p3[1] = vertices[vertexIdx + 1];
        p3[2] = vertices[vertexIdx + 2];
     }

     static void getNormal(float[] p1, float[] p2, float[] p3, float[] normal) {
        float[] a = new float[3];
        float[] b = new float[3];

        a[0] = p2[0] - p1[0];
        a[1] = p2[1] - p1[1];
        a[2] = p2[2] - p1[2];

        b[0] = p3[0] - p1[0];
        b[1] = p3[1] - p1[1];
        b[2] = p3[2] - p1[2];

        normal[0] = a[1] * b[2] - a[2] * b[1];
        normal[1] = a[2] * b[0] - a[0] * b[2];
        normal[2] = a[0] * b[1] - a[1] * b[0];

        float magnitude = (float) Math.abs(Math.sqrt((double) (normal[0] * normal[0] + normal[1] * normal[1] +
                normal[2] * normal[2])));
        normal[0] = normal[0] / magnitude;
        normal[1] = normal[1] / magnitude;
        normal[2] = normal[2] / magnitude;

     }

    static int getIndex(String index, int size) {
        int idx = Integer.parseInt(index);
        if (idx < 0)
            return size + idx;
        else
            return idx - 1;
    }

    static List<String> readLines(InputStream in) throws IOException {
        List<String> lines = new ArrayList<String>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        while ((line = reader.readLine()) != null)
            lines.add(line);
        return lines;
    }
}

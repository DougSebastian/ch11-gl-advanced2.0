package com.badlogic.androidgames.framework.gl;

import com.badlogic.androidgames.framework.impl.GLGraphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;


public class Vertices3 {
    final GLGraphics glGraphics;
    final boolean hasColor;
    final boolean hasTexCoords;
    final boolean hasNormals;
    final int vertexSize;
    final IntBuffer vertices;
    public final int maxVertices;
    final int[] tmpBuffer;
    final ShortBuffer indices;
    int mPositionHandle;
    int coordsPerVertex;
    int vertexStride;
    int mColorHandle;

    public Vertices3(GLGraphics glGraphics, int maxVertices, int maxIndices,
                     boolean hasColor, boolean hasTexCoords, boolean hasNormals) {
        this.glGraphics = glGraphics;
        this.hasColor = hasColor;
        this.hasTexCoords = hasTexCoords;
        this.hasNormals = hasNormals;
        this.vertexSize = (3 + (hasColor ? 4 : 0) + (hasTexCoords ? 2 : 0) + (hasNormals ? 3    : 0)) * 4;
        this.tmpBuffer = new int[maxVertices * vertexSize / 4];
        this.maxVertices = maxVertices;


        ByteBuffer buffer = ByteBuffer.allocateDirect(maxVertices * vertexSize);
        buffer.order(ByteOrder.nativeOrder());
        vertices = buffer.asIntBuffer();

        if (maxIndices > 0) {
            buffer = ByteBuffer.allocateDirect(maxIndices * Short.SIZE / 8);
            buffer.order(ByteOrder.nativeOrder());
            indices = buffer.asShortBuffer();
        } else {
            indices = null;
        }
    }

    public void setVertices(float[] vertices, int offset, int length) {
        this.vertices.clear();
        int len = offset + length;
        for (int i = offset, j = 0; i < len; i++, j++)
            tmpBuffer[j] = Float.floatToRawIntBits(vertices[i]);
        this.vertices.put(tmpBuffer, 0, length);
        this.vertices.flip();
    }

    public IntBuffer getVertexBuffer() {
        return this.vertices;
    }
    public int getVertexCount() {
        return getNumVertices();
    }

    public int getVertexStride() {
        return vertexSize;
    }


    public void setIndices(short[] indices, int offset, int length) {
        this.indices.clear();
        this.indices.put(indices, offset, length);
        this.indices.flip();
    }

    public void bind() {
        /*
        GL10 gl = glGraphics.getGL();

        gl.glEnable
        gl.glEnableClientState(GL20.GL_VERTEX_ARRAY);
        vertices.position(0);
        gl.glVertexPointer(3, GL10.GL_FLOAT, vertexSize, vertices);

        if (hasColor) {
            gl.glEnableClientState(GL20.GL_COLOR_ARRAY);
            vertices.position(3);
            gl.glColorPointer(4, GL20.GL_FLOAT, vertexSize, vertices);
        }

        if (hasTexCoords) {
            gl.glEnableClientState(GL20.GL_TEXTURE_COORD_ARRAY);
            vertices.position(hasColor ? 7 : 3);
            gl.glTexCoordPointer(2, GL20.GL_FLOAT, vertexSize, vertices);
        }

        if (hasNormals) {
            gl.glEnableClientState(GL20.GL_NORMAL_ARRAY);
            int offset = 3;
            if (hasColor)
                offset += 4;
            if (hasTexCoords)
                offset += 2;
            vertices.position(offset);
            gl.glNormalPointer(GL20.GL_FLOAT, vertexSize, vertices);
        }

         */
    }

    public void draw(int primitiveType, int offset, int numVertices) {
        /*
        GL10 gl = glGraphics.getGL();

        if (indices != null) {
            indices.position(offset);
            gl.glDrawElements(primitiveType, numVertices,
                    GL10.GL_UNSIGNED_SHORT, indices);
        } else {
            gl.glDrawArrays(primitiveType, offset, numVertices);
        }

         */
    }

    public void unbind() {
        /*
        GL10 gl = glGraphics.getGL();

        if (hasTexCoords)
            gl.glDisableClientState(GL20.GL_TEXTURE_COORD_ARRAY);

        if (hasColor)
            gl.glDisableClientState(GL20.GL_COLOR_ARRAY);

        if (hasNormals)
            gl.glDisableClientState(GL20.GL_NORMAL_ARRAY);

         */
    }
    
    public int getNumIndices() {
        return indices.limit();
    }

    public int getNumVertices() {
        return vertices.limit() / (vertexSize / 4);
    }
}

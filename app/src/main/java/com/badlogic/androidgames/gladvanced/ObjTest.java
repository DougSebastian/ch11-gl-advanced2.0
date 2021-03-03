package com.badlogic.androidgames.gladvanced;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Input;
import com.badlogic.androidgames.framework.Screen;
import com.badlogic.androidgames.framework.gl.DirectionalLight;
import com.badlogic.androidgames.framework.gl.Material;
import com.badlogic.androidgames.framework.gl.ObjLoader;
import com.badlogic.androidgames.framework.gl.PointLight;
import com.badlogic.androidgames.framework.gl.Vertices3;
import com.badlogic.androidgames.framework.impl.AccelerometerHandler;
import com.badlogic.androidgames.framework.impl.GLGame;
import com.badlogic.androidgames.framework.impl.GLScreen;

import java.nio.IntBuffer;
import java.util.List;

public class ObjTest extends GLGame {
	public Screen getStartScreen() {
		return new ObjScreen(this);
	}

	class ObjScreen extends GLScreen {
		float angle_x;
		float angle_y;
		float angle_z;
		float accelerator_x;
		float accelerator_y;
		float accelerator_z;
		Vertices3 cube;
		float mAmbientLight = .2f;
		float mPointLightMagnitude = 6.0f;
		float mPointLightXPosition = 4f;
		PointLight pointLight;
		DirectionalLight directionalLight;
		Material material;
		AccelerometerHandler accelerometer;
		int mPositionHandle;
		int coordsPerVertex;
		int vertexStride;
		int mColorHandle;
		int mPointLightMagnitudeHandle;
		int mAmbientLightHandle;
		float mRotateSpeed = 1;
		float mRotateX = 0.0f;
		float mRotateY = 0.0f;
		float mRotateZ = 1.0f;

		public ObjScreen(Game game) {
			super(game);

			cube = ObjLoader.load(glGame, "big_blender2");

			accelerometer = new AccelerometerHandler(getApplicationContext());
		}

		@Override
		public void resume() {
		}

		@Override
		public void update(float deltaTime) {
			accelerator_x = accelerometer.getAccelX();
			accelerator_y = accelerometer.getAccelY();
			accelerator_z = accelerometer.getAccelZ() - 9.8f;

			angle_x += deltaTime * 2 * accelerator_x;
			angle_y += deltaTime * 2 * -accelerator_y;
			angle_z += deltaTime * 120 * accelerator_z;

			List<Input.TouchEvent> touchEvents = game.getInput().getTouchEvents();
			game.getInput().getKeyEvents();

			int len = touchEvents.size();
			for (int i = 0; i < len; i++) {
				Input.TouchEvent event = touchEvents.get(i);
				if (event.type == Input.TouchEvent.TOUCH_DRAGGED) {
					int width = glGraphics.getWidth();
					int height = glGraphics.getHeight();
					int x = event.x;
					int y = event.y;

					if (x < width / 3) {
						mAmbientLight = ((float) (height - y)) / (float) height;
					} else if (x < width * 2 / 3) {
						mPointLightMagnitude = 8.0f * ((float) (height - y)) / (float) height;
					} else {
						mPointLightXPosition = 8f - 16f * (((float) (height - y)) / (float) height);
					}

				}
			}
		}


		@Override
		public void present(float deltaTime) {
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

			// Set our per-vertex lighting program.
			GLES20.glUseProgram(mPerVertexProgramHandle);

			// Set program handles for cube drawing.
			mMVPMatrixHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "u_MVPMatrix");
			mMVMatrixHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "u_MVMatrix");
			mLightPosHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "u_LightPos");
			mPositionHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Position");
			mColorHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Color");
			mNormalHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Normal");
			mAmbientLightHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "a_AmbientLight");
			mPointLightMagnitudeHandle = GLES20.glGetUniformLocation(mPerVertexProgramHandle, "a_PointLightMagnitude");

			// Calculate position of the light. Rotate and then push into the distance.
			Matrix.setIdentityM(mLightModelMatrix, 0);
			//Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 0.0f);
			//Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
			//Matrix.rotateM(mLightModelMatrix, 0, angle_x, 1, 0, 0);
			//Matrix.rotateM(mLightModelMatrix, 0, angle_y, 0, 1, 0);

			Matrix.translateM(mLightModelMatrix, 0, mPointLightXPosition, 0.0f, -3.5f);

			Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
			Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

			// Draw some cubes.
			Matrix.setIdentityM(mModelMatrix, 0);


			Matrix.rotateM(mModelMatrix, 0, angle_x, 1, 0, 0);
			Matrix.rotateM(mModelMatrix, 0, angle_y, 0, 1, 0);
			//Matrix.rotateM(mModelMatrix, 0, angle_z, 0, 0, 1);
			drawCube();


			// Draw a point to indicate the light.
			GLES20.glUseProgram(mPointProgramHandle);
			drawLight();

		}

		private void drawCube() {
			IntBuffer vertexBuffer = cube.getVertexBuffer();
			int vertexStride = cube.getVertexStride();
			int vertexCount = cube.getVertexCount();
			// Pass in the position information
			vertexBuffer.position(0);
			GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
					vertexStride, vertexBuffer);

			GLES20.glEnableVertexAttribArray(mPositionHandle);

			// Pass in the color information
			vertexBuffer.position(mPositionDataSize);
			GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
					vertexStride, vertexBuffer);

			GLES20.glEnableVertexAttribArray(mColorHandle);

			// Pass in the normal information
			vertexBuffer.position(mPositionDataSize + mColorDataSize);
			GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
					vertexStride, vertexBuffer);

			GLES20.glEnableVertexAttribArray(mNormalHandle);

			// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
			// (which currently contains model * view).
			Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

			// Pass in the modelview matrix.
			GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

			// This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
			// (which now contains model * view * projection).
			Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

			// Pass in the combined matrix.
			GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

			// Pass in the light position in eye space.
			GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

			// Pass in the point light magnitude
			GLES20.glUniform1f(mPointLightMagnitudeHandle, mPointLightMagnitude);

			// Pass in the ambient light magnitude
			GLES20.glUniform1f(mAmbientLightHandle, mAmbientLight);

			// Draw the cube.
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
		}

		private void drawLight() {
			final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
			final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");

			// Pass in the position.
			GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

			// Since we are not using a buffer object, disable vertex arrays for this attribute.
			GLES20.glDisableVertexAttribArray(pointPositionHandle);

			// Pass in the transformation matrix.
			Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
			Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
			GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

			// Draw the point.
			GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
		}

		@Override
		public void pause() {
		}

		@Override
		public void dispose() {
		}

	}
}


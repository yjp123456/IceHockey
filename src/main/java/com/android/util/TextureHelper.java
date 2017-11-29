package com.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLUtils.texImage2D;


/**
 * Created by jieping on 2017/9/16.
 */

public class TextureHelper {
    public static String TAG = "TextureHelper";

    public static int loadTexture(Context context, int resourceId) {
        final int[] textureObjectId = new int[1];
        glGenTextures(1, textureObjectId, 0);
        if (textureObjectId[0] == 0) {
            Log.d(TAG, "can't generate a new opengl texture");
            return 0;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if (bitmap == null) {
            Log.d(TAG, "resource id" + resourceId + " can't be decoded");
            glDeleteTextures(1, textureObjectId, 0);
            return 0;
        }

        glBindTexture(GL_TEXTURE_2D, textureObjectId[0]);

        //下面是设置图片放大缩小后如何选择像素点进行优化处理来让图片尽量保持清晰
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);
        return textureObjectId[0];
    }
}

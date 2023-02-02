package advancedshader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.ARBDrawBuffersBlend;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import advancedshader.core.Core;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.optifine.ConnectedProperties;
import net.optifine.config.ConnectedParser;
import net.optifine.config.MatchBlock;
import net.optifine.expr.ConstantFloat;
import net.optifine.expr.IExpressionFloat;
import net.optifine.render.GlBlendState;
import net.optifine.shaders.BlockAlias;
import net.optifine.shaders.BlockAliases;
import net.optifine.shaders.EntityAliases;
import net.optifine.shaders.FlipTextures;
import net.optifine.shaders.ICustomTexture;
import net.optifine.shaders.IShaderPack;
import net.optifine.shaders.ItemAliases;
import net.optifine.shaders.Program;
import net.optifine.shaders.ProgramStage;
import net.optifine.shaders.Programs;
import net.optifine.shaders.SMCLog;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersTex;
import net.optifine.shaders.config.EnumShaderOption;
import net.optifine.shaders.config.MacroState;
import net.optifine.shaders.config.PropertyDefaultTrueFalse;
import net.optifine.shaders.config.ShaderLine;
import net.optifine.shaders.config.ShaderMacro;
import net.optifine.shaders.config.ShaderOption;
import net.optifine.shaders.config.ShaderPackParser;
import net.optifine.shaders.config.ShaderParser;
import net.optifine.shaders.gui.GuiButtonEnumShaderOption;
import net.optifine.shaders.uniform.ShaderExpressionResolver;
import net.optifine.shaders.uniform.ShaderUniform1f;
import net.optifine.shaders.uniform.ShaderUniform1i;
import net.optifine.shaders.uniform.ShaderUniform3f;
import net.optifine.shaders.uniform.ShaderUniform4f;
import net.optifine.shaders.uniform.ShaderUniforms;
import net.optifine.util.StrUtils;
import net.optifine.util.TextureUtils;

public class Hook extends DummyModContainer {

    public static final Logger LOGGER = LogManager.getLogger("AdvancedShader");

// ====== Shaders Hook ======

    // 总有一天我要让这里座无虚席
    public static int[] colorTextureImageUnit = null;
    public static int[] shadowTextureImageUnit = new int[] { 13, 14 };

    public static int[] gbuffersFormat = null;
    public static final int[] shadowFormat = new int[2];
    public static final boolean[] shadowClear = new boolean[2];
    public static final Vector4f[] shadowClearColor = new Vector4f[2];

    public static int velocityAttrib = 13;
    public static int midBlockAttrib = 14;

    public static boolean progUseVelocityAttrib = false;
    public static boolean progUseMidBlockAttrib = false;

    private static ShaderUniform1i[] colortex8_16 = new ShaderUniform1i[8];
    private static ShaderUniform1i[] colorimgs = new ShaderUniform1i[6];
    private static ShaderUniform1i[] shadowcolorimgs = new ShaderUniform1i[2];
    private static ShaderUniform3f uniformChunkOffset = null;
    private static ShaderUniform4f uniformSpriteBounds = null;
    private static ShaderUniform1f uniformPlayerMood = null;
    private static ShaderUniform1i uniformRenderStage = null;

    public static PropertyDefaultTrueFalse shaderPackShadowTerrain = new PropertyDefaultTrueFalse("shadowTerrain", "Shadow Terrain", 0);
    public static PropertyDefaultTrueFalse shaderPackShadowEntities = new PropertyDefaultTrueFalse("shadowEntities", "Shadow Entities", 0);
    public static PropertyDefaultTrueFalse shaderPackShadowBlockEntities = new PropertyDefaultTrueFalse("shadowBlockEntities", "Shadow Block Entities", 0);

    public static Program programLine = null;
    public static Program[] programPrepare = null;
    public static Program[] programShadowComp = null;

    public static ICustomTexture[] customTexturesPrepare = null;
    public static ICustomTexture[] customTexturesShadowComp = null;

    private static boolean hasPrepare = false;
    private static boolean hasShadowComp = false;
    public static boolean bindColorImages = false;

    private static int dynamicDimensionFB = 0;
    private static int fakeColorAttachment0 = 0;

    public static FloatBuffer modelView = null;
    public static FloatBuffer modelViewInverse = null;
    public static FloatBuffer shadowModelView = null;
    public static FloatBuffer shadowModelViewInverse = null;
    public static FloatBuffer tempMatrixDirectBuffer = null;

    public static IntBuffer sfbDrawBuffers = null;

    public static FlipTextures dfbColorTexturesFlip = null;
    public static FlipTextures sfbColorTexturesFlip = null;

    private static RenderStage renderStage = RenderStage.NONE;

    private static Method setProgramUniform1i = null;
    private static Method setProgramUniform1f = null;
    private static Method setProgramUniform3f = null;
    private static Method bindGbuffersTextures = null;
    private static Method renderComposites = null;
    private static Method getPixelFormat = null;
    private static Method applyOptions = null;
    private static Method printLogInfo = null;
    private static Method printShaderLogInfo = null;
    private static Method printChatAndLogError = null;

    private static Field dfb = null;
    private static Field sfb = null;
    private static Field usedShadowColorBuffers = null;
    private static Field shadowMapWidth = null;
    private static Field shadowMapHeight = null;
    private static Field usedShadowDepthBuffers = null;
    private static Field usedDepthBuffers = null;
    private static Field usedColorBuffers = null;
    private static Field usedDrawBuffers = null;
    private static Field cameraPositionX = null;
    private static Field cameraPositionY = null;
    private static Field cameraPositionZ = null;
    private static Field eyePosY = null;

    public static void addMacroLines(StringBuilder sb) {
        int afLevel = Minecraft.getMinecraft().gameSettings.ofAfLevel;

        if (afLevel > 1) {
            sb.append("#define MC_ANISOTROPIC_FILTERING ").append(afLevel).append('\n');
        }

        sb.append("#define MC_MOD_ADVANCED_SHADER\n");
    }

    public static void addUniforms(ShaderUniforms uniforms) {
        for (int i = 8; i < 16; i++) {
            colortex8_16[i - 8] = uniforms.make1i("colortex" + i);
        }
        for (int i = 0; i < 6; i++) {
            colorimgs[i] = uniforms.make1i("colorimg" + i);
        }
        for (int i = 0; i < 2; i++) {
            shadowcolorimgs[i] = uniforms.make1i("shadowcolorimg" + i);
        }

        uniformChunkOffset = uniforms.make3f("chunkOffset");
        uniformSpriteBounds = uniforms.make4f("spriteBounds");
        uniformPlayerMood = uniforms.make1f("playerMood");
        uniformRenderStage = uniforms.make1i("renderStage");
    }

    public static void addPrograms(Programs programs) {
        programLine = programs.makeGbuffers("gbuffers_line", Shaders.ProgramBasic);
        programPrepare = programs.makeDeferreds("prepare", 100);
        programShadowComp = programs.makeDeferreds("shadowcomp", 100);
    }

    @SuppressWarnings("incomplete-switch")
    public static void updateUniform(Program program, boolean hasDeferred, boolean hasCustomTextureGbuffer) {
        switch (program.getProgramStage()) {
        case GBUFFERS:
            if (hasDeferred || hasCustomTextureGbuffer) {
                setProgramUniform1i(Shaders.uniform_colortex4, colorTextureImageUnit[4]);
                setProgramUniform1i(Shaders.uniform_colortex5, colorTextureImageUnit[5]);
                setProgramUniform1i(Shaders.uniform_colortex6, colorTextureImageUnit[6]);
                setProgramUniform1i(Shaders.uniform_colortex7, colorTextureImageUnit[7]);

                for (int i = 0; i < 8; i++) {
                    setProgramUniform1i(colortex8_16[i], colorTextureImageUnit[i + 8]);
                }
            }
            break;
        case DEFERRED:
        case COMPOSITE:
            setProgramUniform1i(Shaders.uniform_colortex4, colorTextureImageUnit[4]);
            setProgramUniform1i(Shaders.uniform_colortex5, colorTextureImageUnit[5]);
            setProgramUniform1i(Shaders.uniform_colortex6, colorTextureImageUnit[6]);
            setProgramUniform1i(Shaders.uniform_colortex7, colorTextureImageUnit[7]);

            for (int i = 0; i < 8; i++) {
                setProgramUniform1i(colortex8_16[i], colorTextureImageUnit[i + 8]);
            }
            break;
        case SHADOW:
            if (hasCustomTextureGbuffer) {
                setProgramUniform1i(Shaders.uniform_colortex4, colorTextureImageUnit[4]);
                setProgramUniform1i(Shaders.uniform_colortex5, colorTextureImageUnit[5]);
                setProgramUniform1i(Shaders.uniform_colortex6, colorTextureImageUnit[6]);
                setProgramUniform1i(Shaders.uniform_colortex7, colorTextureImageUnit[7]);

                for (int i = 0; i < 8; i++) {
                    setProgramUniform1i(colortex8_16[i], colorTextureImageUnit[i + 8]);
                }
            }
            break;
        }

        setProgramUniform3f(uniformChunkOffset, 0F, 0F, 0F); // 其实到这里已经可以结束了，但我想搞事！
        setProgramUniform1f(uniformPlayerMood, ForwardFeatures.mood);
        setProgramUniform1i(uniformRenderStage, renderStage.ordinal());

        for (int i = 0; i < 6; i++) {
            setProgramUniform1i(colorimgs[i], i);
        }
        for (int i = 0; i < 2; i++) {
            setProgramUniform1i(shadowcolorimgs[i], i + 6);
        }
    }

    public static void setProgramUniform1i(ShaderUniform1i su, int value) {
        if (setProgramUniform1i == null) {
            setProgramUniform1i = getMethod(Shaders.class, "setProgramUniform1i", ShaderUniform1i.class, int.class);
        }

        try {
            setProgramUniform1i.invoke(null, su, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
    }

    public static void setProgramUniform1f(ShaderUniform1f su, float value) {
        if (setProgramUniform1f == null) {
            setProgramUniform1f = getMethod(Shaders.class, "setProgramUniform1f", ShaderUniform1f.class, float.class);
        }

        try {
            setProgramUniform1f.invoke(null, su, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
    }

    public static void setProgramUniform3f(ShaderUniform3f su, float x, float y, float z) {
        if (setProgramUniform3f == null) {
            setProgramUniform3f = getMethod(Shaders.class, "setProgramUniform3f", ShaderUniform3f.class, float.class, float.class, float.class);
        }

        try {
            setProgramUniform3f.invoke(null, su, x, y, z);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
    }

    public static void setProgramUniform4f(ShaderUniform4f su, float x, float y, float z, float w) {
        su.setValue(x, y, z, w);
    }

    public static void bindGbuffersTextures() {
        if (bindGbuffersTextures == null) {
            bindGbuffersTextures = getMethod(Shaders.class, "bindGbuffersTextures");
        }

        try {
            bindGbuffersTextures.invoke(null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
    }

    public static void renderComposites(Program[] programs, boolean isFinal) {
        if (renderComposites == null) {
            renderComposites = getMethod(Shaders.class, "renderComposites", Program[].class, boolean.class);
        }

        try {
            renderComposites.invoke(null, programs, isFinal);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
    }

    public static int getPixelFormat(int format) {
        if (getPixelFormat == null) {
            getPixelFormat = getMethod(Shaders.class, "getPixelFormat", int.class);
        }

        try {
            return (int) getPixelFormat.invoke(null, format);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return GL12.GL_BGRA;
        }
    }

    public static String applyOptions(String line, ShaderOption[] options) {
        if (applyOptions == null) {
            applyOptions = getMethod(Shaders.class, "applyOptions", String.class, ShaderOption[].class);
        }

        try {
            return (String) applyOptions.invoke(null, line, options);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return line;
        }
    }

    public static boolean printLogInfo(int obj, String log) {
        if (printLogInfo == null) {
            printLogInfo = getMethod(Shaders.class, "printLogInfo", int.class, String.class);
        }

        try {
            return (boolean) printLogInfo.invoke(null, obj, log);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return false;
        }
    }

    public static boolean printShaderLogInfo(int shader, String name, List<String> listFiles) {
        if (printShaderLogInfo == null) {
            printShaderLogInfo = getMethod(Shaders.class, "printShaderLogInfo", int.class, String.class, List.class);
        }

        try {
            return (boolean) printShaderLogInfo.invoke(null, shader, name, listFiles);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return false;
        }
    }

    public static void printChatAndLogError(String log) {
        if (printChatAndLogError == null) {
            printChatAndLogError = getMethod(Shaders.class, "printChatAndLogError", String.class);
        }

        try {
            printChatAndLogError.invoke(null, log);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
    }

    public static int getDfb() {
        if (dfb == null) {
            dfb = getField(Shaders.class, "dfb");
        }

        try {
            return dfb.getInt(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return 0; // Never
        }
    }

    public static int getSfb() {
        if (sfb == null) {
            sfb = getField(Shaders.class, "sfb");
        }

        try {
            return sfb.getInt(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return 0; // Never
        }
    }

    public static int getUsedShadowColorBuffers() {
        if (usedShadowColorBuffers == null) {
            usedShadowColorBuffers = getField(Shaders.class, "usedShadowColorBuffers");
        }

        try {
            return usedShadowColorBuffers.getInt(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return 0; // Never
        }
    }

    public static void setUsedShadowColorBuffers(int i) {
        if (usedShadowColorBuffers == null) {
            usedShadowColorBuffers = getField(Shaders.class, "usedShadowColorBuffers");
        }

        try {
            usedShadowColorBuffers.setInt(null, i);
        } catch (IllegalArgumentException | IllegalAccessException e) {}
    }

    public static int getShadowMapWidth() {
        if (shadowMapWidth == null) {
            shadowMapWidth = getField(Shaders.class, "shadowMapWidth");
        }

        try {
            return shadowMapWidth.getInt(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return 1024;
        }
    }

    public static int getShadowMapHeight() {
        if (shadowMapHeight == null) {
            shadowMapHeight = getField(Shaders.class, "shadowMapHeight");
        }

        try {
            return shadowMapHeight.getInt(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return 1024;
        }
    }

    public static int getUsedShadowDepthBuffers() {
        if (usedShadowDepthBuffers == null) {
            usedShadowDepthBuffers = getField(Shaders.class, "usedShadowDepthBuffers");
        }

        try {
            return usedShadowDepthBuffers.getInt(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return 0; // Never
        }
    }

    public static void setUsedShadowDepthBuffers(int i) {
        if (usedShadowDepthBuffers == null) {
            usedShadowDepthBuffers = getField(Shaders.class, "usedShadowDepthBuffers");
        }

        try {
            usedShadowDepthBuffers.setInt(null, i);
        } catch (IllegalArgumentException | IllegalAccessException e) {}
    }

    public static int getUsedDepthBuffers() {
        if (usedDepthBuffers == null) {
            usedDepthBuffers = getField(Shaders.class, "usedDepthBuffers");
        }

        try {
            return usedDepthBuffers.getInt(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return 0; // Never
        }
    }

    public static void setUsedDepthBuffers(int i) {
        if (usedDepthBuffers == null) {
            usedDepthBuffers = getField(Shaders.class, "usedDepthBuffers");
        }

        try {
            usedDepthBuffers.setInt(null, i);
        } catch (IllegalArgumentException | IllegalAccessException e) {}
    }

    public static int getUsedColorBuffers() {
        if (usedColorBuffers == null) {
            usedColorBuffers = getField(Shaders.class, "usedColorBuffers");
        }

        try {
            return usedColorBuffers.getInt(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return 0; // Never
        }
    }

    public static void setUsedColorBuffers(int i) {
        if (usedColorBuffers == null) {
            usedColorBuffers = getField(Shaders.class, "usedColorBuffers");
        }

        try {
            usedColorBuffers.setInt(null, i);
        } catch (IllegalArgumentException | IllegalAccessException e) {}
    }

    public static int getUsedDrawBuffers() {
        if (usedDrawBuffers == null) {
            usedDrawBuffers = getField(Shaders.class, "usedDrawBuffers");
        }

        try {
            return usedDrawBuffers.getInt(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return 0; // Never
        }
    }

    public static double getCameraPositionX() {
        if (cameraPositionX == null) {
            cameraPositionX = getField(Shaders.class, "cameraPositionX");
        }

        try {
            return cameraPositionX.getDouble(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return 0.0;
        }
    }

    public static void setCameraPositionX(double d) {
        if (cameraPositionX == null) {
            cameraPositionX = getField(Shaders.class, "cameraPositionX");
        }

        try {
            cameraPositionX.setDouble(null, d);
        } catch (IllegalArgumentException | IllegalAccessException e) {}
    }

    public static double getCameraPositionY() {
        if (cameraPositionY == null) {
            cameraPositionY = getField(Shaders.class, "cameraPositionY");
        }

        try {
            return cameraPositionY.getDouble(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return 0.0;
        }
    }

    public static void setCameraPositionY(double d) {
        if (cameraPositionY == null) {
            cameraPositionY = getField(Shaders.class, "cameraPositionY");
        }

        try {
            cameraPositionY.setDouble(null, d);
        } catch (IllegalArgumentException | IllegalAccessException e) {}
    }

    public static double getCameraPositionZ() {
        if (cameraPositionZ == null) {
            cameraPositionZ = getField(Shaders.class, "cameraPositionZ");
        }

        try {
            return cameraPositionZ.getDouble(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return 0.0;
        }
    }

    public static void setCameraPositionZ(double d) {
        if (cameraPositionZ == null) {
            cameraPositionZ = getField(Shaders.class, "cameraPositionZ");
        }

        try {
            cameraPositionZ.setDouble(null, d);
        } catch (IllegalArgumentException | IllegalAccessException e) {}
    }

    public static void setEyePosY(float f) {
        if (eyePosY == null) {
            eyePosY = getField(Shaders.class, "eyePosY");
        }

        try {
            eyePosY.setFloat(null, f);
        } catch (IllegalArgumentException | IllegalAccessException e) {}
    }

    private static int getShadowBufferIndex(String buffer) {
        if (buffer.equals("shadowcolor") || buffer.equals("shadowcolor0")) {
            return 0;
        } else if (buffer.equals("shadowcolor1")) {
            return 1;
        }

        return -1;
    }

    private static Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        Method method = null;

        try {
            method = clazz.getDeclaredMethod(name, args);
            method.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {}

        return method;
    }

    private static Field getField(Class<?> clazz, String name) {
        Field field = null;

        try {
            field = clazz.getDeclaredField(name);
            field.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {}

        return field;
    }

// ====== 更多Buffers ======

    public static class MoreBuffers {

        public static Pattern PATTERN_RENDER_TARGETS = Pattern.compile("[0-9N,]+");

        // Shaders.useProgram
        public static void attachColorBuffer(Program program) {
            if (!Shaders.isShaderPackInitialized || !Shaders.isRenderingWorld || program.getProgramStage() == ProgramStage.NONE || program == Shaders.ProgramFinal) {
                return;
            }

            if (bindColorImages) {
                ComputeShader.bindColorImages();
            }

            if (program instanceof ComputeShader.ComputeProgram) {
                return;
            }

            if (program.getName().startsWith("shadow")) {
                boolean isShadow = program.getProgramStage() == ProgramStage.SHADOW;

                for (int i = 0; i < getUsedShadowColorBuffers(); i++) {
                    EXTFramebufferObject.glFramebufferTexture2DEXT(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, GL11.GL_TEXTURE_2D, isShadow ? sfbColorTexturesFlip.getA(i) : sfbColorTexturesFlip.getB(i), 0);
                }

                Shaders.setDrawBuffers(program.getDrawBuffers());
            } else {
                String settings = program.getDrawBufSettings();
                boolean isGbuffers = program.getProgramStage() == ProgramStage.GBUFFERS;

                boolean resized = BufferSize.checkResize(program); // size.buffer

                if (settings == null) {
                    int usedDrawBuffers = getUsedDrawBuffers();

                    for (int i = 0; i < usedDrawBuffers; i++) {
                        int texture = !resized && BufferSize.bufferSize[i] != null ? 0 : (isGbuffers ? dfbColorTexturesFlip.getA(i) : dfbColorTexturesFlip.getB(i));

                        if (i == 0 && texture == 0) {
                            texture = fakeColorAttachment0;
                        }

                        EXTFramebufferObject.glFramebufferTexture2DEXT(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, GL11.GL_TEXTURE_2D, texture, 0);
                    }
                } else {
                    for (int i = 0; i < settings.length(); i++) {
                        int buffer = settings.charAt(i) - '0';
                        int texture = buffer >= 0 && buffer <= 15 ? (!resized && BufferSize.bufferSize[buffer] != null ? 0 : (isGbuffers ? dfbColorTexturesFlip.getA(buffer) : dfbColorTexturesFlip.getB(buffer))) : 0;

                        if (i == 0 && texture == 0) {
                            texture = fakeColorAttachment0;
                        }

                        EXTFramebufferObject.glFramebufferTexture2DEXT(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, GL11.GL_TEXTURE_2D, texture, 0);
                    }
                }

                Shaders.setDrawBuffers(program.getDrawBuffers());
            }

            Shaders.checkGLError("AdvancedShader:AttachColorBuffers");
        }

        // Shaders.createFragShader
        public static void checkRenderTargets(ShaderLine line, Program program) {
            if (line.isProperty("RENDERTARGETS")) {
                String targets = line.getValue();

                if (PATTERN_RENDER_TARGETS.matcher(targets).matches()) {
                    targets = targets.trim();

                    StringTokenizer tokenizer = new StringTokenizer(targets, ",");
                    StringBuilder builder = new StringBuilder();

                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();
                        int i = -1;

                        try {
                            i = Integer.parseInt(token);
                        } catch (NumberFormatException ex) {}

                        builder.append((char) (i + '0'));
                    }

                    program.setDrawBufSettings(builder.toString());
                } else {
                    SMCLog.warning("Invalid render targets: " + targets);
                }
            }
        }

    }

// ====== 高版本特性选项 ======

    public static class ForwardFeatures {

        public static final BlockRenderLayer TRIPWIRE = BlockRenderLayer.valueOf("TRIPWIRE");

        private static final Map<String, BlockMapper> idMap;

        static {
            Map<String, BlockMapper> map = null;

            try {
                map = new Gson().fromJson(new InputStreamReader(new GZIPInputStream(Hook.class.getResourceAsStream("/idmap.gz"))), TypeToken.getParameterized(Map.class, String.class, BlockMapper.class).getType());
            } catch (Throwable e) {
                LOGGER.error("我那么大一个idmap.gz方块ID映射表呢？？？");
            }

            idMap = map;
        }

        private static boolean forwardVersion = false;
        private static boolean idMapping = false;
        private static boolean playerMood = false;

        private static Random moodRandom = new Random();
        private static float mood = 0F;

        // EntityAliases.reset
        public static int lightningID = -1;

        // GuiButtonEnumShaderOption.getButtonText
        public static String getButtonText(EnumShaderOption option) {
            String key = option.getPropertyKey();

            if (key.equals("renderer")) {
                return I18n.format("advancedshader.option.renderer", getVersionText(forwardVersion));
            } else if (key.equals("blockID")) {
                return I18n.format("advancedshader.option.blockid", getVersionText(idMapping));
            } else if (key.equals("playerMood")) {
                return I18n.format("advancedshader.option.playermood", getVersionText(playerMood));
            }

            return null;
        }

        private static String getVersionText(boolean enabled) {
            return enabled ? TextFormatting.GREEN + "1.16.5" : "1.12.2";
        }

        // GuiShaders.actionPerformed
        public static boolean actionPerformed(GuiButton button, boolean rightClick) {
            if (button instanceof GuiButtonEnumShaderOption) {
                GuiButtonEnumShaderOption btn = (GuiButtonEnumShaderOption) button;

                if (btn.enabled) {
                    String key = btn.getEnumShaderOption().getPropertyKey();

                    if (key.equals("renderer")) {
                        forwardVersion = !forwardVersion;

                        LOGGER.info("切换渲染机制：{}", forwardVersion ? "1.16.5" : "1.12.2");

                        // 谢谢你IterationT
                        if (forwardVersion) {
                            Shaders.entityAttrib = 11;
                            Shaders.midTexCoordAttrib = 12;
                            Shaders.tangentAttrib = 13;
                            velocityAttrib = 14;
                            midBlockAttrib = 15;
                        } else {
                            Shaders.entityAttrib = 10;
                            Shaders.midTexCoordAttrib = 11;
                            Shaders.tangentAttrib = 12;
                            velocityAttrib = 13;
                            midBlockAttrib = 14;
                        }

                        Shaders.uninit();
                        Minecraft.getMinecraft().renderGlobal.loadRenderers();
                    } else if (key.equals("blockID")) {
                        if (idMap != null) {
                            idMapping = !idMapping;
                        } else {
                            idMapping = false;
                        }

                        LOGGER.info("切换方块ID版本：{}", idMapping ? "1.16.5" : "1.12.2");

                        BlockAliases.reset();
                        BlockAliases.update(Shaders.getShaderPack());
                        ItemAliases.reset();
                        ItemAliases.update(Shaders.getShaderPack());
                        EntityAliases.reset();
                        EntityAliases.update(Shaders.getShaderPack());
                        Minecraft.getMinecraft().renderGlobal.loadRenderers();
                    } else if (key.equals("playerMood")) {
                        playerMood = !playerMood;

                        LOGGER.info("切换氛围值机制：{}", playerMood ? "1.16.5" : "1.12.2");
                    } else {
                        return false;
                    }

                    btn.updateButtonText();

                    return true;
                }
            }

            return false;
        }

        // Shaders.getEnumShaderOption
        public static String getEnumShaderOption(EnumShaderOption option) {
            switch (option.getPropertyKey()) {
            case "renderer":
                return forwardVersion ? "1.16.5" : "1.12.2";
            case "blockID":
                return idMapping ? "1.16.5" : "1.12.2";
            case "playerMood":
                return playerMood ? "1.16.5" : "1.12.2";
            }

            return null;
        }

        // Shaders.setEnumShaderOption
        public static boolean setEnumShaderOption(EnumShaderOption option, String value) {
            switch (option.getPropertyKey()) {
            case "renderer":
                forwardVersion = value.equals("1.16.5");

                if (forwardVersion) {
                    Shaders.entityAttrib = 11;
                    Shaders.midTexCoordAttrib = 12;
                    Shaders.tangentAttrib = 13;
                    velocityAttrib = 14;
                    midBlockAttrib = 15;
                } else {
                    Shaders.entityAttrib = 10;
                    Shaders.midTexCoordAttrib = 11;
                    Shaders.tangentAttrib = 12;
                    velocityAttrib = 13;
                    midBlockAttrib = 14;
                }

                return true;
            case "blockID":
                idMapping = value.equals("1.16.5");

                if (idMap == null) {
                    idMapping = false;
                }

                return true;
            case "playerMood":
                playerMood = value.equals("1.16.5");
                return true;
            }

            return false;
        }

        // === 渲染机制 ===

        // EntityRenderer.renderWorldPass
        public static boolean isForwardVersion() {
            return Shaders.shaderPackLoaded && forwardVersion;
        }

        // GuiSlotShaders.checkCompatible
        public static String getGameVersionKey(String old) {
            if (forwardVersion) {
                return "version.1.16.5";
            }

            return old;
        }

        // GuiSlotShaders.checkCompatible
        public static String getOptiFineVersion(String old) {
            if (forwardVersion) {
                return "G8";
            }

            return old;
        }

        // ShaderPackParser.resolveIncludes
        public static String replaceShaderVersionMacro(String macros) {
            if (forwardVersion) {
                return macros.replace("#define MC_VERSION 11202", "#define MC_VERSION 11605");
            } else {
                return macros;
            }
        }

        // RenderChunk.fixBlockLayer
        public static BlockRenderLayer replaceRenderLayer(IBlockState state, BlockRenderLayer layer) {
            if (isForwardVersion() && state.getBlock() == Blocks.TRIPWIRE) {
                return TRIPWIRE;
            }

            return layer;
        }

        // BlockRenderLayer.values
        public static BlockRenderLayer[] getBlockRenderLayers(BlockRenderLayer[] layers) {
            StackTraceElement[] elements = new Exception().getStackTrace();
            StackTraceElement element = elements[2];
            String clazz = element.getClassName();

            boolean ie = false;

            for (int i = 2; i < Math.min(10, elements.length); i++) {
                if (elements[i].getClassName().startsWith("blusunrize.immersiveengineering")) {
                    ie = true;

                    break;
                }
            }

            if (!clazz.startsWith("java.")
                    && !clazz.startsWith("sun.")
                    && !clazz.startsWith("javax.")
                    && !clazz.startsWith("net.minecraft")
                    && !clazz.startsWith("net.optifine")
                    && !ie) {
                for (int i = 0; i < layers.length; i++) {
                    if (layers[i] == TRIPWIRE) {
                        layers[i] = BlockRenderLayer.CUTOUT;
                    }
                }
            }

            return layers;
        }

        // MinecraftForgeClient.getRenderLayer
        public static BlockRenderLayer getRenderLayer(BlockRenderLayer layer) {
            if (layer == TRIPWIRE) {
                StackTraceElement element = new Exception().getStackTrace()[2];
                String clazz = element.getClassName();

                if (!clazz.startsWith("java.")
                        && !clazz.startsWith("sun.")
                        && !clazz.startsWith("javax.")
                        && !clazz.startsWith("net.minecraft")
                        && !clazz.startsWith("net.optifine")
                        && !clazz.startsWith("blusunrize.immersiveengineering")) {
                    return BlockRenderLayer.CUTOUT;
                }
            }

            return layer;
        }

        public static void beginTripwire() {
            if (Shaders.isRenderingWorld) {
                RenderStage.setRenderStage(RenderStage.TRIPWIRE);
            }
        }

        public static void endTripwire() {
            if (Shaders.isRenderingWorld) {
                RenderStage.setRenderStage(RenderStage.NONE);
            }
        }

        public static void prepareHand() {
            if (isForwardVersion() && !Shaders.isShadowPass) {
                if (getUsedDepthBuffers() >= 3) {
                    GlStateManager.setActiveTexture(GL13.GL_TEXTURE12);
                    GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, Shaders.renderWidth, Shaders.renderHeight);
                    GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
                }

                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.disableBlend();
            }
        }

        // === 方块ID ===

        // MacroProcessor.process
        public static String replaceBlockVersionMacro(String macros, String path) {
            if ((path.endsWith("block.properties") || path.endsWith("item.properties") || path.endsWith("entity.properties")) && idMapping) {
                return macros.replace("#define MC_VERSION 11202", "#define MC_VERSION 11605");
            } else {
                return macros;
            }
        }

        // BlockAliases.loadBlockAliases
        public static String remapBlockID(String line) {
            if (!idMapping) {
                return line;
            }

            String[] states = line.split(" ");
            Set<String> set = Sets.newLinkedHashSet();
            ConnectedParser parser = new ConnectedParser("remapper");

            for (String state : states) {
                String[] tokens = state.split(":");

                String blockID;
                String[] properties;

                if (tokens.length > 1 && parser.isFullBlockName(tokens)) {
                    blockID = tokens[0] + ':' + tokens[1];
                    properties = Arrays.copyOfRange(tokens, 2, tokens.length);
                } else {
                    blockID = "minecraft:" + tokens[0];
                    properties = Arrays.copyOfRange(tokens, 1, tokens.length);
                }

                BlockMapper mapper = idMap.get(blockID);

                if (mapper != null) {
                    String[] results = mapper.match(properties);

                    if (results != null) {
                        Collections.addAll(set, results);

                        continue;
                    }
                }

                set.add(state);
            }

            return String.join(" ", set.toArray(new String[0]));
        }

        // BlockAliases.loadBlockAliases
        // ItemAliases.loadItemAliases
        public static String remapItemID(String line) {
            if (!idMapping) {
                return line;
            }

            String[] states = line.split(" ");
            Set<String> set = Sets.newLinkedHashSet();
            ConnectedParser parser = new ConnectedParser("remapper");

            for (String state : states) {
                String[] tokens = state.split(":");

                String blockID;

                if (tokens.length > 1 && parser.isFullBlockName(tokens)) {
                    blockID = tokens[0] + ':' + tokens[1];
                } else {
                    blockID = "minecraft:" + tokens[0];
                }

                BlockMapper mapper = idMap.get(blockID);

                if (mapper != null) {
                    for (Target target : mapper.targets) {
                        set.add(target.name);
                    }

                    continue;
                }

                set.add(state);
            }

            return String.join(" ", set.toArray(new String[0]));
        }

        // EntityAliases.loadEntityAliases
        public static String remapEntityID(String line, int id) {
            for (String key : line.split(" ")) {
                if (key.equals("lightning_bolt") || key.equals("minecraft:lightning_bolt")) {
                    lightningID = id;
                }
            }

            return remapItemID(line);
        }

        // Shaders.setEntityId
        public static int getEntityAliasID(int id, Entity entity) {
            if (entity instanceof EntityLightningBolt) {
                return lightningID;
            }

            return id;
        }

        // Shaders.setEntityId
        public static int isAllowNegative(int id) {
            if (idMapping) {
                return 1;
            } else {
                return id;
            }
        }

        // ItemAliases.getItemAliasId
        public static int isNegative(int id) {
            return idMapping ? -1 : id;
        }

        // 返回true时顺便skip了
        // RenderGlobal.renderEntities
        public static boolean beginLightningShader() {
            if (Shaders.shaderPackLoaded && (forwardVersion || lightningID != -1)) {
                Shaders.beginEntities();

                return Shaders.isShadowPass && shaderPackShadowEntities.isFalse();
            } else {
                return false;
            }
        }

        // RenderGlobal.renderEntities
        public static void nextLightningEntity(Entity entity) {
            if (Shaders.shaderPackLoaded && (forwardVersion || lightningID != -1)) {
                Shaders.nextEntity(entity);
            }
        }

        // RenderGlobal.renderEntities
        public static void endLightningShader() {
            if (Shaders.shaderPackLoaded && (forwardVersion || lightningID != -1)) {
                Shaders.endEntities();
            }
        }

        public static class BlockMapper {

            @SerializedName("p")
            public Set<String> sameProp = Collections.emptySet();

            @SerializedName("t")
            public Target[] targets = new Target[0];

            public String[] match(String[] properties) {
                List<String> results = Lists.newArrayList();

                Map<String, String> prop = Maps.newHashMap();
                String same = "";

                for (String property : properties) {
                    String[] tokens = property.split("=");

                    if (tokens.length != 2) {
                        return null;
                    }

                    if (this.sameProp.contains(tokens[0])) {
                        same += ":" + tokens[0] + "=" + tokens[1];
                    } else {
                        prop.put(tokens[0], tokens[1]);
                    }
                }

                for (Target target : this.targets) {
                    boolean matched = true;

                    for (String key : prop.keySet()) {
                        if (!Objects.equal(prop.get(key), target.conditions.get(key))) {
                            matched = false;

                            break;
                        }
                    }

                    if (matched) {
                        results.add(target + same);
                    }
                }

                return results.toArray(new String[0]);
            }
        }

        public static class Target {

            @SerializedName("c")
            public Map<String, String> conditions = Collections.emptyMap();

            @SerializedName("n")
            public String name = "";

            @SerializedName("p")
            public Map<String, String> properties = Collections.emptyMap();

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();

                builder.append(this.name);
                builder.append(':');

                for (Entry<String, String> entry : this.properties.entrySet()) {
                    builder.append(entry.getKey()).append('=').append(entry.getValue()).append(':');
                }

                builder.setLength(builder.length() - 1);

                return builder.toString();
            }
        }

        // === 氛围值 ===

        // GuiOverlayDebug.call
        public static void addPlayerMoodDebug(List<String> list) {
            list.add("Player Mood: " + (playerMood ? (int) (mood * 100) + "%" : "Disabled"));
        }

        // WorldClient.updateBlocks
        public static int updateMoodTick(int ambienceTicks) {
            if (!playerMood) {
                mood = 0F;

                return ambienceTicks;
            }

            if (mood >= 1.0F) {
                return 0;
            }

            boolean playSound = false;

            EntityPlayer player = Minecraft.getMinecraft().player;
            World world = player.world;
            BlockPos randomPos = new BlockPos(player.posX + (double) moodRandom.nextInt(17) - 8.0, player.posY + player.getEyeHeight() + (double) moodRandom.nextInt(17) - 8.0, player.posZ + (double) moodRandom.nextInt(17) - 8.0);
            int skyLight = world.getLightFor(EnumSkyBlock.SKY, randomPos);

            if (skyLight > 0) {
                mood -= skyLight / 0.015F;
            } else {
                mood -= (world.getLightFor(EnumSkyBlock.BLOCK, randomPos) - 1) / 6000F;
            }

            if (mood >= 1.0F) {
                mood = 1.0F;
                playSound = true;
            } else {
                mood = Math.max(0F, mood);
            }

            return playSound ? 0 : 12000;
        }

        // WorldClient.playMoodSoundAndCheckLight
        public static boolean skipMoodCheck() {
            mood = 0F;

            return playerMood;
        }
    }

// ====== Core Profile 兼容 ======

    public static class CoreProfile {

        private static final Pattern PATTERN_VERSION = Pattern.compile("^\\s*#version\\s+(\\d+)\\s*.*$");
        private static final Pattern PATTERN_UNIFORM = Pattern.compile("\\s*uniform\\s+\\w+\\s+(\\w+).*");
        private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("\\s*(?:in|attribute)\\s+(\\w+)\\s+(\\w+).*");

        private static final ShaderMacro[] MACRO_CORE = new ShaderMacro[] {
                new ShaderMacro("modelViewMatrix", "gl_ModelViewMatrix"),
                new ShaderMacro("projectionMatrix", "gl_ProjectionMatrix"),
                new ShaderMacro("modelViewMatrixInverse", "gl_ModelViewMatrixInverse"),
                new ShaderMacro("projectionMatrixInverse", "gl_ProjectionMatrixInverse"),
                new ShaderMacro("normalMatrix", "gl_NormalMatrix"),
                new ShaderMacro("textureMatrix", "gl_TextureMatrix[0]"),
                new ShaderMacro("vaPosition", "(gl_Vertex.xyz)"),
                new ShaderMacro("vaColor", "gl_Color"),
                new ShaderMacro("vaNormal", "gl_Normal"),
                new ShaderMacro("vaUV0", "(gl_MultiTexCoord0.xy)"),
                new ShaderMacro("vaUV2", "(gl_MultiTexCoord1.xy)")
        };

        // 如果有人定义了整型顶点属性我大概会生气
        private static final ShaderMacro POS_VEC4 = new ShaderMacro("vaPosition", "gl_Vertex");
        private static final ShaderMacro POS_VEC2 = new ShaderMacro("vaPosition", "(gl_Vertex.xy)");
        private static final ShaderMacro POS_FLOAT = new ShaderMacro("vaPosition", "(gl_Vertex.x)");
        private static final ShaderMacro COLOR_VEC3 = new ShaderMacro("vaColor", "(gl_Color.rgb)");
        private static final ShaderMacro COLOR_VEC2 = new ShaderMacro("vaColor", "(gl_Color.rg)");
        private static final ShaderMacro COLOR_FLOAT = new ShaderMacro("vaColor", "(gl_Color.r)");
        private static final ShaderMacro NORMAL_VEC4 = new ShaderMacro("vaNormal", "(vec4(gl_Normal,0.0))");
        private static final ShaderMacro NORMAL_VEC2 = new ShaderMacro("vaNormal", "(gl_Normal.xy)");
        private static final ShaderMacro NORMAL_FLOAT = new ShaderMacro("vaNormal", "(gl_Normal.x)");
        private static final ShaderMacro UV0_VEC4 = new ShaderMacro("vaUV0", "gl_MultiTexCoord0");
        private static final ShaderMacro UV0_VEC3 = new ShaderMacro("vaUV0", "(gl_MultiTexCoord0.xyz)");
        private static final ShaderMacro UV0_FLOAT = new ShaderMacro("vaUV0", "(gl_MultiTexCoord0.x)");
        private static final ShaderMacro UV2_VEC4 = new ShaderMacro("vaUV2", "gl_MultiTexCoord1");
        private static final ShaderMacro UV2_VEC3 = new ShaderMacro("vaUV2", "(gl_MultiTexCoord1.xyz)");
        private static final ShaderMacro UV2_FLOAT = new ShaderMacro("vaUV2", "(gl_MultiTexCoord1.x)");

//        // ShaderPackParser.resolveIncludes
//        public static String convertVersion(String version) {
//            Matcher mu = PATTERN_VERSION.matcher(version);
//            if (mu.matches()) {
//                switch (mu.group(1)) {
//                case "110":
//                case "120":
//                case "130":
//                case "140":
//                    // 我挺想知道ComplementaryShaders在#version 120里用gl_VertexID是怎么想的
//                    version = "#version 150 compatibility";
//                    break;
//                default:
//                    version = "#version " + mu.group(1) + " compatibility";
//                }
//            }
//
//            return version;
//        }
//
//        // ShaderPackParser.resolveIncludes
//        public static String convertCoreProfile(String text, Set<ShaderMacro> macros, String path) {
//            if (!path.endsWith(".vsh")) {
//                return text;
//            }
//
//            // 我也挺想知道ComplementaryShaders为啥用vaPosition、modelViewMatrix不事先声明的，得，都是被OF惯的
//            // 可以放上面addMacroLines方法里，也可以在下面判断是否调用vaPosition变量什么的，懒得优化了，就放这里吧
//            if (!macros.contains(MACRO_CORE[0])) {
//                for (ShaderMacro macro : MACRO_CORE) {
//                    macros.add(macro);
//                }
//            }
//
//            String[] lines = text.split("\n");
//            StringBuilder builder = new StringBuilder();
//
//            for (String line : lines) {
//                line = line.replaceAll("gl_MultiTexCoord2", "gl_MultiTexCoord1");
//                line = line.replaceAll("gl_TextureMatrix\\[2\\]", "gl_TextureMatrix[1]");
//
//                Matcher mu = PATTERN_UNIFORM.matcher(line);
//                if (mu.matches()) {
//                    String name = mu.group(1);
//
//                    for (ShaderMacro macro : MACRO_CORE) {
//                        if (macro.getName().equals(name)) {
////                            macros.add(macro);
//                            builder.append("// ");
//
//                            break;
//                        }
//                    }
//                }
//
//                mu = PATTERN_ATTRIBUTE.matcher(line);
//                if (mu.matches()) {
//                    String type = mu.group(1);
//                    String name = mu.group(2);
//
//                    for (ShaderMacro macro : MACRO_CORE) {
//                        if (macro.getName().equals(name)) {
////                            macros.add(macro);
//                            builder.append("// ");
//
//                            ShaderMacro replaced = null;
//
//                            if (name.equals("vaPosition")) {
//                                if (type.equals("vec4")) {
//                                    replaced = POS_VEC4;
//                                } else if (type.equals("vec2")) {
//                                    replaced = POS_VEC2;
//                                } else if (type.equals("float")) {
//                                    replaced = POS_FLOAT;
//                                }
//                            } else if (name.equals("vaColor")) {
//                                if (type.equals("vec3")) {
//                                    replaced = COLOR_VEC3;
//                                } else if (type.equals("vec2")) {
//                                    replaced = COLOR_VEC2;
//                                } else if (type.equals("float")) {
//                                    replaced = COLOR_FLOAT;
//                                }
//                            } else if (name.equals("vaNormal")) {
//                                if (type.equals("vec4")) {
//                                    replaced = NORMAL_VEC4;
//                                } else if (type.equals("vec2")) {
//                                    replaced = NORMAL_VEC2;
//                                } else if (type.equals("float")) {
//                                    replaced = NORMAL_FLOAT;
//                                }
//                            } else if (name.equals("vaUV0")) {
//                                if (type.equals("vec4")) {
//                                    replaced = UV0_VEC4;
//                                } else if (type.equals("vec3")) {
//                                    replaced = UV0_VEC3;
//                                } else if (type.equals("float")) {
//                                    replaced = UV0_FLOAT;
//                                }
//                            } else if (name.equals("vaUV2")) {
//                                if (type.equals("vec4")) {
//                                    replaced = UV2_VEC4;
//                                } else if (type.equals("vec3")) {
//                                    replaced = UV2_VEC3;
//                                } else if (type.equals("float")) {
//                                    replaced = UV2_FLOAT;
//                                }
//                            }
//
//                            if (replaced != null) {
//                                macros.remove(macro);
//                                macros.add(replaced);
//                            }
//
//                            break;
//                        }
//                    }
//                }
//
//                builder.append(line).append('\n');
//            }
//
//            if (builder.length() > 0) {
//                builder.setLength(builder.length() - 1);
//            }
//
//            return builder.toString();
//        }

        // Shaders.createVertShader
        public static BufferedReader convertCoreProfile(BufferedReader reader) throws IOException {
            Set<String> replaced = Sets.newHashSet();

            StringBuilder header = new StringBuilder();
            StringBuilder content = new StringBuilder();

            while (true) {
                String line = reader.readLine();

                if (line == null) {
                    break;
                }

                line = line.replace("gl_MultiTexCoord2", "gl_MultiTexCoord1");
                line = line.replace("gl_TextureMatrix[2]", "gl_TextureMatrix[1]");

                Matcher matcher;

                if ((matcher = PATTERN_VERSION.matcher(line)).matches()) {
                    switch (matcher.group(1)) {
                    case "110":
                    case "120":
                    case "130":
                    case "140":
                        line = "#version 150 compatibility";
                        break;
                    default:
                        line = "#version " + matcher.group(1) + " compatibility";
                    }

                    header.append(line).append('\n');
                    continue;
                } else if ((matcher = PATTERN_UNIFORM.matcher(line)).matches()) {
                    String name = matcher.group(1);

                    for (ShaderMacro macro : MACRO_CORE) {
                        if (macro.getName().equals(name)) {
                            line = macro.getSourceLine();

                            replaced.add(name);

                            break;
                        }
                    }
                } else if ((matcher = PATTERN_ATTRIBUTE.matcher(line)).matches()) {
                    String type = matcher.group(1);
                    String name = matcher.group(2);

                    for (ShaderMacro macro : MACRO_CORE) {
                        if (macro.getName().equals(name)) {
                            if (name.equals("vaPosition")) {
                                if (type.equals("vec4")) {
                                    macro = POS_VEC4;
                                } else if (type.equals("vec2")) {
                                    macro = POS_VEC2;
                                } else if (type.equals("float")) {
                                    macro = POS_FLOAT;
                                }
                            } else if (name.equals("vaColor")) {
                                if (type.equals("vec3")) {
                                    macro = COLOR_VEC3;
                                } else if (type.equals("vec2")) {
                                    macro = COLOR_VEC2;
                                } else if (type.equals("float")) {
                                    macro = COLOR_FLOAT;
                                }
                            } else if (name.equals("vaNormal")) {
                                if (type.equals("vec4")) {
                                    macro = NORMAL_VEC4;
                                } else if (type.equals("vec2")) {
                                    macro = NORMAL_VEC2;
                                } else if (type.equals("float")) {
                                    macro = NORMAL_FLOAT;
                                }
                            } else if (name.equals("vaUV0")) {
                                if (type.equals("vec4")) {
                                    macro = UV0_VEC4;
                                } else if (type.equals("vec3")) {
                                    macro = UV0_VEC3;
                                } else if (type.equals("float")) {
                                    macro = UV0_FLOAT;
                                }
                            } else if (name.equals("vaUV2")) {
                                if (type.equals("vec4")) {
                                    macro = UV2_VEC4;
                                } else if (type.equals("vec3")) {
                                    macro = UV2_VEC3;
                                } else if (type.equals("float")) {
                                    macro = UV2_FLOAT;
                                }
                            }

                            line = macro.getSourceLine();

                            replaced.add(name);

                            break;
                        }
                    }
                }

                content.append(line).append('\n');
            }

            for (ShaderMacro macro : MACRO_CORE) {
                if (!replaced.contains(macro.getName())) {
                    header.append(macro.getSourceLine()).append('\n');
                }
            }

            header.append(content);

            return new BufferedReader(new StringReader(header.toString()));
        }
    }

// ====== gbuffers_line ======

    public static class LineShader {

        public static Program lastProgram = null;
        public static ByteBuffer normalBuffer = null;
        public static boolean isCulling = false;

        // SVertexBuilder.drawArrays
        public static int preDrawArray(int drawMode, BufferBuilder builder) {
            if ((drawMode == GL11.GL_LINES || drawMode == GL11.GL_LINE_STRIP)
                    && Shaders.activeProgram != Shaders.ProgramNone) {
                lastProgram = Shaders.activeProgram; // 要用作判断所以不用pushProgram

                Shaders.useProgram(programLine);

                if (programLine.getRealProgramName().equals(programLine.getName())) {
                    ByteBuffer buffer = builder.getByteBuffer().duplicate().order(ByteOrder.nativeOrder());

                    if (normalBuffer == null || normalBuffer.capacity() < buffer.capacity() * 2) {
                        normalBuffer = GLAllocation.createDirectByteBuffer(buffer.capacity() * 2);
                    }

                    int size = builder.getVertexFormat().getSize();
                    int count = 1;
                    int retMode = drawMode;

                    normalBuffer.clear();
                    buffer.rewind();
                    buffer.limit(builder.getVertexCount() * size);

                    if (drawMode == GL11.GL_LINES) {
                        if (Shaders.canRenderQuads()) {
                            count = 4;
                            retMode = GL11.GL_QUADS;
                        } else {
                            count = 6;
                            retMode = GL11.GL_TRIANGLES;
                        }

                        while (buffer.hasRemaining()) {
                            getNormalsFromBuffer(buffer, size, count);
                            buffer.position(buffer.position() + size * count);
                        }
                    } else if (drawMode == GL11.GL_LINE_STRIP) {
                        count = 4;
                        retMode = GL11.GL_TRIANGLE_STRIP;

                        while (buffer.remaining() > size * count) {
                            getNormalsFromBuffer(buffer, size, count);
                            buffer.position(buffer.position() + size * count);
                        }

                        buffer.position(buffer.position() - size * count);
                        getNormalsFromBuffer(buffer, size, 2);
                    }

                    normalBuffer.flip();
                    GlStateManager.glNormalPointer(GL11.GL_FLOAT, 12, normalBuffer);
                    GlStateManager.glEnableClientState(GL11.GL_NORMAL_ARRAY);

                    isCulling = GL11.glIsEnabled(GL11.GL_CULL_FACE);
                    GlStateManager.disableCull();

                    return retMode;
                }
            }

            return drawMode;
        }

        // SVertexBuilder.drawArrays
        public static void postDrawArray() {
            if (lastProgram != null) {
                Shaders.useProgram(lastProgram);
                GlStateManager.glDisableClientState(GL11.GL_NORMAL_ARRAY);

                if (isCulling) {
                    GlStateManager.enableCull();
                } else {
                    GlStateManager.disableCull();
                }

                lastProgram = null;
            }
        }

        private static void getNormalsFromBuffer(ByteBuffer buffer, int vertexSize, int count) {
            int position = buffer.position();
            float x1 = buffer.getFloat(position + 0);
            float y1 = buffer.getFloat(position + 4);
            float z1 = buffer.getFloat(position + 8);
            float x2 = buffer.getFloat(position + vertexSize * 2 + 0) - x1;
            float y2 = buffer.getFloat(position + vertexSize * 2 + 4) - y1;
            float z2 = buffer.getFloat(position + vertexSize * 2 + 8) - z1;
            float dist = (float) Math.sqrt(x2 * x2 + y2 * y2 + z2 * z2);

            if (dist != 0F) {
                x2 /= dist;
                y2 /= dist;
                z2 /= dist;
            }

            for (int i = 0; i < count; i++) {
                normalBuffer.putFloat(x2);
                normalBuffer.putFloat(y2);
                normalBuffer.putFloat(z2);
            }
        }

        // BufferBuilder.endVertex
        public static void endAddVertex(BufferBuilder builder) {
            if ((builder.getDrawMode() == GL11.GL_LINES || builder.getDrawMode() == GL11.GL_LINE_STRIP)
                    && Shaders.activeProgram != Shaders.ProgramNone
                    && programLine.getRealProgramName().equals(programLine.getName())) {
                int size = builder.getVertexFormat().getSize();
                int count = builder.getVertexCount();
                ByteBuffer buffer = builder.getByteBuffer();
                ByteBuffer dup = buffer.duplicate();
                dup.order(ByteOrder.nativeOrder());
                dup.position((count - 1) * size);
                dup.limit(count * size);

                int[] vertex = new int[builder.getVertexFormat().getIntegerSize()];

                dup.asIntBuffer().get(vertex);
                builder.addVertexData(vertex);

                if (builder.getDrawMode() == GL11.GL_LINE_STRIP && count > 1) {
                    builder.addVertexData(vertex);
                    builder.addVertexData(vertex);
                }
            }
        }

        // WorldVertexBufferUploader.draw
        public static boolean shouldConvertQuads(int drawMode) {
            return drawMode == GL11.GL_LINES // 线段类型
                    && Shaders.activeProgram != Shaders.ProgramNone // 正在渲染游戏
                    && programLine.getRealProgramName().equals(programLine.getName()); // 着色器存在
        }
    }

// ====== chunkOffset ======

    public static class ChunkOffset {

        // ChunkRenderContainer.preRenderChunk
        // RenderList.preRenderRegion
        // VboRenderList.preRenderRegion
        public static boolean setChunkOffset(float offsetX, float offsetY, float offsetZ) {
            if (uniformChunkOffset.isDefined()) {
                setProgramUniform3f(uniformChunkOffset, offsetX, offsetY, offsetZ);

                return true;
            }

            return false;
        }
    }

// ====== 各向异性过滤 ======

    public static class AnisotropicFilter {

        private static Field frameCounter = null;

        // ChunkRenderDispatcher.uploadChunk
        public static void preUploadDisplayList(BlockRenderLayer layer) {
            if (layer == BlockRenderLayer.TRANSLUCENT) {
                uniformSpriteBounds.setProgram(Shaders.ProgramWater.getId());
            } else if (layer == ForwardFeatures.TRIPWIRE) {
                uniformSpriteBounds.setProgram(Shaders.ProgramTexturedLit.getId());
            } else {
                uniformSpriteBounds.setProgram(Shaders.ProgramTerrain.getId());
            }
        }

        // ChunkRenderDispatcher.uploadChunk
        public static void postUploadDisplayList() {
            uniformSpriteBounds.setProgram(0);
        }

        // BufferBuilder.drawForIcon
        public static void bindShaderMultiTexture(TextureAtlasSprite sprite) {
            if (Shaders.shaderPackLoaded) {
                int normal = sprite.spriteNormal != null ? sprite.spriteNormal.glSpriteTextureId : 0;
                int specular = sprite.spriteSpecular != null ? sprite.spriteSpecular.glSpriteTextureId : 0;
                ShadersTex.bindNSTextures(normal, specular);

                setProgramUniform4f(uniformSpriteBounds, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());
            }
        }

        // TextureMap.loadTextureAtlas
        public static void loadShaderSprite(TextureAtlasSprite sprite) {
            if (Shaders.shaderPackLoaded) {
                if (sprite.spriteNormal != null) {
                    loadSpriteSingle(sprite.spriteNormal, sprite);
                }

                if (sprite.spriteSpecular != null) {
                    loadSpriteSingle(sprite.spriteSpecular, sprite);
                }
            }
        }

        // TextureMap.updateAnimations
        public static boolean updateShaderSpriteAnimation(TextureAtlasSprite sprite, TextureAtlasSprite parent) {
            if (Shaders.shaderPackLoaded) {
                if (sprite != null) {
                    TextureAtlasSprite single = sprite.spriteSingle;

                    if (single != null) {
                        if (parent == TextureUtils.iconClock || parent == TextureUtils.iconCompass) {
//                            single.frameCounter = parent.frameCounter;
                            setFrameCounter(single, getFrameCounter(parent));
                        }

                        sprite.bindSpriteTexture();
                        single.updateAnimation();

                        return single.isAnimationActive();
                    }
                }
            }

            return false;
        }

        // TextureMap.loadTextureAtlas
        public static void unloadShaderSprite(TextureAtlasSprite sprite) {
            if (sprite.spriteNormal != null) {
                sprite.spriteNormal.deleteSpriteTexture();
            }

            if (sprite.spriteSpecular != null) {
                sprite.spriteSpecular.deleteSpriteTexture();
            }
        }

        private static void loadSpriteSingle(TextureAtlasSprite sprite, TextureAtlasSprite parent) {
            sprite.sheetWidth = parent.sheetWidth;
            sprite.sheetHeight = parent.sheetHeight;
            sprite.mipmapLevels = parent.mipmapLevels;

            TextureAtlasSprite single = sprite.spriteSingle;

            if (single != null) {
                if (single.getIconWidth() <= 0) {
                    single.setIconWidth(sprite.getIconWidth());
                    single.setIconHeight(sprite.getIconHeight());
                    single.initSprite(sprite.getIconWidth(), sprite.getIconHeight(), 0, 0, false);
                    single.clearFramesTextureData();

                    List<int[][]> data = sprite.getFramesTextureData();

                    single.setFramesTextureData(data);
                    single.setAnimationMetadata(sprite.getAnimationMetadata());
                }

                single.sheetWidth = parent.sheetWidth;
                single.sheetHeight = parent.sheetHeight;
                single.mipmapLevels = parent.mipmapLevels;
                single.setAnimationIndex(sprite.getAnimationIndex());
                sprite.bindSpriteTexture();

                try {
                    TextureUtil.uploadTextureMipmap(single.getFrameTextureData(0), single.getIconWidth(), single.getIconHeight(), single.getOriginX(), single.getOriginY(), false, true);
                } catch (Exception e) {
                    LOGGER.info("Error uploading sprite single: " + single + ", parent: " + sprite);
                    e.printStackTrace();
                }
            }
        }

        private static int getFrameCounter(TextureAtlasSprite sprite) {
            if (frameCounter == null) {
                frameCounter = getField(TextureAtlasSprite.class, "field_110973_g");
            }

            try {
                return frameCounter.getInt(sprite);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                return 0;
            }
        }

        private static void setFrameCounter(TextureAtlasSprite sprite, int i) {
            if (frameCounter == null) {
                frameCounter = getField(TextureAtlasSprite.class, "field_110973_g");
            }

            try {
                frameCounter.setInt(sprite, i);
            } catch (IllegalArgumentException | IllegalAccessException e) {}
        }
    }

// ====== RenderStage ======

    // TODO
    public static enum RenderStage {

        NONE, // 无状态
        SKY, // 天空
        SUNSET, // 晚霞
        CUSTOM_SKY, // 自定义天空
        SUN, // 日
        MOON, // 月
        STARS, // 星
        VOID, // 虚空
        TERRAIN_SOLID, // 实心方块
        TERRAIN_CUTOUT_MIPPED, // 有空隙的方块（树叶等）（Mipmap）
        TERRAIN_CUTOUT, // 同上但无Mipmap
        ENTITIES, // 实体
        BLOCK_ENTITIES, // 实体方块
        DESTROY, // 方块破坏动画
        OUTLINE, // 方块描边
        DEBUG, // 区块边界调试
        HAND_SOLID, // 不透明物品
        TERRAIN_TRANSLUCENT, // 半透明方块
        TRIPWIRE, // 绊线，texturedLit
        PARTICLES, // 粒子
        CLOUDS, // 云
        RAIN_SNOW, // 雨雪
        WORLD_BORDER, // 世界边界
        HAND_TRANSLUCENT; // 半透明物品

        // ShaderMacros.getExtensions
        public static ShaderMacro[] addRenderStageMacros(ShaderMacro[] macros) {
            RenderStage[] stages = RenderStage.values();
            ShaderMacro[] result = new ShaderMacro[macros.length + stages.length];

            for (int i = 0; i < macros.length; i++)
                result[i] = macros[i];
            for (int i = 0; i < stages.length; i++)
                result[macros.length + i] = new ShaderMacro("MC_RENDER_STAGE_" + stages[i].name(), "" + stages[i].ordinal());

            return result;
        }

        // 太多了不写了
        public static void setRenderStage(RenderStage stage) {
            if (Shaders.shaderPackLoaded && Shaders.isRenderingWorld) {
                renderStage = stage;

                setProgramUniform1i(uniformRenderStage, renderStage.ordinal());
            }
        }
    }

// ====== 补全shaders.properties光影配置宏 ======

    public static class PropertyFix {

        // Shaders.loadShaderPackProperties
        public static boolean useShaderOptions = true;

        // MacroProcessor.process
        public static List<ShaderOption> shaderOptions = null;

        // MacroProcessor.getMacroHeader
        public static void getShaderOptionHeader(String line, StringBuilder builder) {
            if (useShaderOptions) {
                if (shaderOptions == null) {
                    shaderOptions = Lists.newArrayList();
                    ShaderOption[] ashaderoption = Shaders.getShaderPackOptions();

                    for (int i = 0; i < ashaderoption.length; ++i) {
                        ShaderOption shaderoption = ashaderoption[i];
                        String s = shaderoption.getSourceLine();

                        if (s != null && s.startsWith("#")) {
                            shaderOptions.add(shaderoption);
                        }
                    }
                }

                Iterator<ShaderOption> iterator = shaderOptions.iterator();

                while (iterator.hasNext()) {
                    ShaderOption shaderoption = iterator.next();

                    if (line.contains(shaderoption.getName())) {
                        builder.append(shaderoption.getSourceLine());
                        builder.append("\n");
                        iterator.remove();
                    }
                }
            }
        }
    }

// ====== size.buffer ======

    public static class BufferSize {

        private static boolean[] isRelative = new boolean[16];
        private static float[][] bufferSize = new float[16][];
        private static Set<Program> shouldResize = Sets.newHashSet();

        private static int oldRenderWidth = -1;
        private static int oldRenderHeight = -1;

        // Shaders.loadShaderPackProperties
        public static void resetBufferSizes() {
            shouldResize.clear();

            for (int i = 0; i < 16; i++) {
                bufferSize[i] = null;
            }
        }

        // Shaders.loadShaderPackProperties
        public static void parseBufferSizes(Properties props) {
            for (Object key : props.keySet()) {
                String keyStr = (String) key;

                if (keyStr.startsWith("size.buffer.")) {
                    String[] tokens = keyStr.split("\\.");

                    if (tokens.length == 3) {
                        String s1 = tokens[2];
                        int i = Shaders.getBufferIndexFromString(s1);

                        if (i >= 0 && i < 16) {
                            String value = props.getProperty(keyStr).trim();

                            if (value == null) {
                                SMCLog.severe("Invalid buffer size: " + keyStr + "=" + value);
                            } else {
                                String[] values = value.split(" ");

                                if (values.length != 2) {
                                    SMCLog.severe("Invalid buffer size: " + keyStr + "=" + value);
                                } else {
                                    int aw = -1;
                                    int ah = -1;

                                    try {
                                        aw = Integer.parseInt(values[0]);
                                        ah = Integer.parseInt(values[1]);
                                    } catch (NumberFormatException ex) {}

                                    if (aw >= 0 && ah >= 0) { // 等于0是什么鬼啦，算了直接抄过来懒得管了
                                        isRelative[i] = false;
                                        bufferSize[i] = new float[] { aw, ah };
                                        SMCLog.info("Fixed size " + s1 + ": " + aw + " " + ah);
                                    } else {
                                        float rw = -1F;
                                        float rh = -1F;

                                        try {
                                            rw = Float.parseFloat(values[0]);
                                            rh = Float.parseFloat(values[1]);
                                        } catch (NumberFormatException ex) {}

                                        if (rw >= 0F && rh >= 0F) { // 这里也不做一下是不是1.0的判断= =虽然正常来讲没啥光影作者会设1.0吧
                                            isRelative[i] = true;
                                            bufferSize[i] = new float[] { rw, rh };
                                            SMCLog.info("Relative size " + s1 + ": " + rw + " " + rh);
                                        } else {
                                            SMCLog.severe("Invalid buffer size: " + keyStr + "=" + value);
                                        }
                                    }
                                }
                            }
                        } else {
                            SMCLog.severe("Invalid buffer name: " + keyStr);
                        }
                    }
                }
            }
        }

        // Shaders.init
        public static void updateProgramSize(Program program) {
            if (program.getProgramStage() == ProgramStage.COMPOSITE && !program.getName().equals("final")
                    || program.getProgramStage() == ProgramStage.DEFERRED && !program.getName().startsWith("shadowcomp")) {
                int i = 0;
                boolean relative = false;
                float[] size = null;

                String settings = program.getDrawBufSettings();

                if (settings == null) {
                    int usedDrawBuffers = getUsedDrawBuffers();

                    settings = "";

                    for (int c = 0; c < usedDrawBuffers; c++) {
                        settings += c;
                    }
                }

                for (char c : settings.toCharArray()) {
                    int index = c - '0';
                    float[] tempSize = bufferSize[index];

                    if (tempSize != null) {
                        i++;
                        if (size == null) {
                            size = tempSize;
                            relative = isRelative[index];
                        } else {
                            if (!Arrays.equals(tempSize, size) || relative != isRelative[index]) {
                                SMCLog.severe("Program " + program.getName() + " draws to buffers with different sizes");

                                return;
                            }
                        }
                    }
                }

                if (size != null) {
                    if (i == settings.length()) {
                        shouldResize.add(program);
                    } else {
                        SMCLog.severe("Program " + program.getName() + " draws to buffers with different sizes");
                    }
                }
            }
        }

        // Shaders.setupFrameBuffer
        public static int getResizedWidth(int width, int buffer) {
            if (bufferSize[buffer] != null) {
                return isRelative[buffer] ? (int) ((float) width * bufferSize[buffer][0]) : (int) bufferSize[buffer][0];
            } else {
                return width;
            }
        }

        // Shaders.setupFrameBuffer
        public static int getResizedHeight(int height, int buffer) {
            if (bufferSize[buffer] != null) {
                return isRelative[buffer] ? (int) ((float) height * bufferSize[buffer][1]) : (int) bufferSize[buffer][1];
            } else {
                return height;
            }
        }

        // Shaders.setupFrameBuffer
        // Shaders.beginRender
        public static boolean shouldBindForSetup(int buffer) {
            if (buffer == 0 && bufferSize[buffer] != null) {
                EXTFramebufferObject.glFramebufferTexture2DEXT(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, fakeColorAttachment0, 0);
            }

            return buffer < 8 && bufferSize[buffer] == null;
        }

        // Shaders.preDrawComposite
        public static void preDrawComposite() {
            Program active = Shaders.activeProgram;

            if (shouldResize.contains(active)) {
                oldRenderWidth = Shaders.renderWidth;
                oldRenderHeight = Shaders.renderHeight;

                String settings = active.getDrawBufSettings();
                int buffer = settings == null ? 0 : settings.charAt(0) - '0';

                Shaders.renderWidth = getResizedWidth(oldRenderWidth, buffer);
                Shaders.renderHeight = getResizedHeight(oldRenderHeight, buffer);

                GL11.glViewport(0, 0, Shaders.renderWidth, Shaders.renderHeight);
            }
        }

        // Shaders.postDrawComposite
        public static void postDrawComposite() {
            if (oldRenderWidth != -1) {
                EXTFramebufferObject.glBindFramebufferEXT(GL30.GL_FRAMEBUFFER, getDfb());

                Shaders.renderWidth = oldRenderWidth;
                Shaders.renderHeight = oldRenderHeight;

                GL11.glViewport(0, 0, Shaders.renderWidth, Shaders.renderHeight);

                oldRenderWidth = oldRenderHeight = -1;
            }
        }

        // Shaders.setupFrameBuffer
        public static void initDynamicDimensions() {
            if (dynamicDimensionFB != 0) {
                uninitDynamicDimensions();
            }

            dynamicDimensionFB = EXTFramebufferObject.glGenFramebuffersEXT();
            EXTFramebufferObject.glBindFramebufferEXT(GL30.GL_FRAMEBUFFER, dynamicDimensionFB);
            fakeColorAttachment0 = GL11.glGenTextures();
            GlStateManager.bindTexture(fakeColorAttachment0);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, Shaders.renderWidth, Shaders.renderHeight, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (ByteBuffer) null);
            GlStateManager.bindTexture(0);
            GL20.glDrawBuffers(0);
            GL11.glReadBuffer(0);
        }

        // Shaders.uninit
        public static void uninitDynamicDimensions() {
            EXTFramebufferObject.glDeleteFramebuffersEXT(dynamicDimensionFB);
            GL11.glDeleteTextures(fakeColorAttachment0);

            dynamicDimensionFB = 0;
            fakeColorAttachment0 = 0;
        }

        // Shaders.clearRenderBuffer
        public static void switchFramebuffer(int buffer) {
            if (buffer == -1) {
                EXTFramebufferObject.glBindFramebufferEXT(GL30.GL_FRAMEBUFFER, getDfb());
            } else {
                if (bufferSize[buffer] != null) {
                    EXTFramebufferObject.glBindFramebufferEXT(GL30.GL_FRAMEBUFFER, dynamicDimensionFB);

                    for (int i = 0; i < 8; i++) {
                        EXTFramebufferObject.glFramebufferTexture2DEXT(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, GL11.GL_TEXTURE_2D, 0, 0);
                    }
                } else {
                    EXTFramebufferObject.glBindFramebufferEXT(GL30.GL_FRAMEBUFFER, getDfb());
                }
            }
        }

        private static boolean checkResize(Program program) {
            boolean resized = shouldResize.contains(program);

            if (resized) {
                EXTFramebufferObject.glBindFramebufferEXT(GL30.GL_FRAMEBUFFER, dynamicDimensionFB);

                for (int i = 0; i < 8; i++) {
                    EXTFramebufferObject.glFramebufferTexture2DEXT(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, GL11.GL_TEXTURE_2D, 0, 0);
                }
            }

            return resized;
        }

    }

// ====== at_midBlock 和屏蔽 at_velocity ======

    public static class VertexAttribute {

        // Shaders.createVertShader
        public static void checkAttributes(ShaderLine line) {
            if (line.isAttribute("at_velocity")) {
                progUseVelocityAttrib = true;
            } else if (line.isAttribute("at_midBlock")) {
                progUseMidBlockAttrib = true;
            }
        }

        // Shaders.setupProgram
        public static void bindAttributes(int programID) {
            if (progUseVelocityAttrib) {
                // 只是用于屏蔽，没有写入数据，不屏蔽的话鬼知道它会绑定到哪个location上，PTGI HRR 3的实体都变得跟闪电侠似的
                ARBVertexShader.glBindAttribLocationARB(programID, velocityAttrib, "at_velocity");
                Shaders.checkGLError("at_velocity");
            }

            if (progUseMidBlockAttrib) {
                ARBVertexShader.glBindAttribLocationARB(programID, midBlockAttrib, "at_midBlock");
                Shaders.checkGLError("at_midBlock");
            }
        }

        // BuilderBuffer.putPosition
        public static void setMidBlock(BufferBuilder builder) {
            int size = builder.getVertexFormat().getSize();

            if (size == 56) {
                int count = builder.getVertexCount() - 4;
                ByteBuffer buffer = builder.getByteBuffer();

                for (int i = 0; i < 4; i++) {
                    int pos = size * (count + i);

                    int x = (int) ((0.5F - buffer.getFloat(pos + 0)) * 64F) & 255;
                    int y = (int) ((0.5F - buffer.getFloat(pos + 4)) * 64F) & 255;
                    int z = (int) ((0.5F - buffer.getFloat(pos + 8)) * 64F) & 255;
                    int w = (z << 16) + (y << 8) + x;

                    buffer.putInt(pos + 52, w);
                }
            }
        }
    }

// ====== prepare & shadowComp ======

    public static class MoreStages {

        private static int oldRenderWidth = -1;
        private static int oldRenderHeight = -1;

        // Shaders.init
        public static void checkComposites() {
            hasPrepare = false;
            hasShadowComp = false;

            for (Program prepare : programPrepare) {
                if (prepare.getId() != 0 || ComputeShader.hasComputes(prepare)) {
                    hasPrepare = true;

                    break;
                }
            }

            for (Program shadowComp : programShadowComp) {
                if (shadowComp.getId() != 0 || ComputeShader.hasComputes(shadowComp)) {
                    hasShadowComp = true;

                    break;
                }
            }
        }

        // EntityRenderer.renderWorldPass
        public static void renderPrepare() {
            if (hasPrepare) {
                renderComposites(programPrepare, false);

                GlStateManager.disableBlend();
                GlStateManager.disableLighting();

                bindGbuffersTextures();
                Shaders.useProgram(Shaders.ProgramTextured);
            }
        }

        // ShadersRender.renderShadowMap
        public static void renderShadowComp() {
            if (hasShadowComp) {
                GL11.glViewport(0, 0, Shaders.renderWidth, Shaders.renderHeight);

                renderComposites(programShadowComp, false);

                GlStateManager.disableBlend();
                GlStateManager.disableLighting();
            }
        }

        // Shaders.preDrawComposite
        public static void preDrawComposite() {
            Program active = Shaders.activeProgram;

            if (active.getName().startsWith("shadowcomp")) {
                oldRenderWidth = Shaders.renderWidth;
                oldRenderHeight = Shaders.renderHeight;

                Shaders.renderWidth = getShadowMapWidth();
                Shaders.renderHeight = getShadowMapHeight();

                GL11.glViewport(0, 0, Shaders.renderWidth, Shaders.renderHeight);
            }
        }

        // Shaders.postDrawComposite
        public static void postDrawComposite() {
            if (oldRenderWidth != -1) {
                Shaders.renderWidth = oldRenderWidth;
                Shaders.renderHeight = oldRenderHeight;

                GL11.glViewport(0, 0, Shaders.renderWidth, Shaders.renderHeight);

                oldRenderWidth = oldRenderHeight = -1;
            }
        }

        // Shaders.renderComposites
        public static ICustomTexture[] getCustomTextures(Program[] programs, ICustomTexture[] deferred) {
            if (programs == programPrepare) {
                return customTexturesPrepare;
            } else if (programs == programShadowComp) {
                return customTexturesShadowComp;
            } else {
                return deferred;
            }
        }

        // Shaders.renderComposites
        public static FlipTextures getFlipBuffer(Program[] programs) {
            if (programs == programShadowComp) {
                return sfbColorTexturesFlip;
            } else {
                return dfbColorTexturesFlip;
            }
        }

        // Shaders.renderComposites
        public static int[] getColorTextureImageUnit(Program[] programs) {
            if (programs == programShadowComp) {
                return shadowTextureImageUnit;
            } else {
                return colorTextureImageUnit;
            }
        }

        // Shaders.createFragShader
        public static void parseShadowFormat(String buffer, int format, String formatStr) {
            int i = getShadowBufferIndex(buffer);

            if (i >= 0 && format != 0) {
                shadowFormat[i] = format;

                SMCLog.info("%s format: %s", buffer, formatStr);
            }
        }

        // Shaders.createFragShader
        public static void parseShadowClear(String buffer) {
            int i = getShadowBufferIndex(buffer);

            if (i >= 0) {
                shadowClear[i] = false;

                SMCLog.info("%s clear disabled", buffer);
            }
        }

        // Shaders.createFragShader
        public static void parseShadowClearColor(String buffer, ShaderLine line) {
            int i = getShadowBufferIndex(buffer);

            if (i >= 0) {
                Vector4f color = line.getValueVec4();

                if (color != null) {
                    shadowClearColor[i] = color;
                    SMCLog.info("%s clear color: %s %s %s %s", buffer, color.getX(), color.getY(), color.getZ(), color.getW());
                } else {
                    SMCLog.warning("Invalid color value: " + line.getValue());
                }
            }
        }

        // Shaders.renderComposites
        public static void genShadowCompMipmap() {
            if (Shaders.activeProgram.getName().startsWith("shadowcomp") && Shaders.hasGlGenMipmap) {
                for (int i = 0; i < getUsedShadowColorBuffers(); i++) {
                    if (Shaders.shadowColorMipmapEnabled[i]) {
                        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0 + shadowTextureImageUnit[i]);
                        GlStateManager.bindTexture(sfbColorTexturesFlip.getA(i));
                        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, Shaders.shadowColorFilterNearest[i] ? GL11.GL_NEAREST_MIPMAP_NEAREST : GL11.GL_LINEAR_MIPMAP_LINEAR);
                    }
                }

                GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
            }
        }

        public static void clearShadowMap() {
            for (int i = 0; i < getUsedShadowColorBuffers(); i++) {
                if (shadowClear[i]) {
                    GL11.glClearColor(1F, 1F, 1F, 1F);
                    Vector4f color = shadowClearColor[i];

                    if (color != null) {
                        GL11.glClearColor(color.getX(), color.getY(), color.getZ(), color.getW());
                    }
//                    高版本也只清空了A面，甚至有的光影用这个bug才能正常运行= =
//                    EXTFramebufferObject.glFramebufferTexture2DEXT(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, GL11.GL_TEXTURE_2D, sfbColorTexturesFlip.getB(i), 0);
//                    GL20.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0 + i);
//                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                    EXTFramebufferObject.glFramebufferTexture2DEXT(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, GL11.GL_TEXTURE_2D, sfbColorTexturesFlip.getA(i), 0);
                    GL20.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0 + i);
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                }
            }

            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            GL20.glDrawBuffers(sfbDrawBuffers);
        }

        public static void setupShadowFlipBuffer() {
            for (int i = 0; i < getUsedShadowColorBuffers(); i++) {
                GlStateManager.bindTexture(sfbColorTexturesFlip.getB(i));
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, Shaders.shadowColorFilterNearest[i] ? GL11.GL_NEAREST : GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, Shaders.shadowColorFilterNearest[i] ? GL11.GL_NEAREST : GL11.GL_LINEAR);
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, shadowFormat[i], getShadowMapWidth(), getShadowMapHeight(), 0, getPixelFormat(shadowFormat[i]), GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (ByteBuffer) null);
                Shaders.checkGLError("FT sca");
            }
        }

        public static class ShadowFlipTextures extends FlipTextures {

            private IntBuffer textures;

            public ShadowFlipTextures(IntBuffer textures) {
                super(textures, 2);

                this.textures = textures;
            }

            @Override
            public void flip(int index) {
                // 因为我懒得把所有的sfbColorTextures都改成sfbColorTexturesFlip
                // 人呐，要学会偷懒
                // 尽管这个模组现在还能运行已经是奇迹了
                int flipped = this.textures.get(index + 2);

                this.textures.put(index + 2, this.textures.get(index));
                this.textures.put(index, flipped);
            }
        }

    }

// ====== blend.<program>.<buffer> ======

    public static class Blend {

        // Shaders.loadShaderPackProperties
        public static final Map<Program, GlBlendState[]> propBlend = Maps.newHashMap();

        private static Method applyCurrentBlend = null;
        private static Method parseBlendState = null;

        // ShaderPackParser.parseBlendStates
        public static void parseBlendBuffer(String[] tokens, String value) {
            if (tokens.length == 3 && "blend".equals(tokens[0])) {
                String program = tokens[1];
                String buffer = tokens[2];

                Program p = Shaders.getProgram(program);

                if (p != null) {
                    int i = program.startsWith("shadow") ? getShadowBufferIndex(buffer) : Shaders.getBufferIndexFromString(buffer);

                    if (i >= 0) {
                        if (propBlend.get(p) == null) {
                            propBlend.put(p, new GlBlendState[16]);
                        }

                        propBlend.get(p)[i] = parseBlendState(value.trim());
                        SMCLog.info("Blend " + program + "." + buffer + "=" + value.trim());
                    } else {
                        SMCLog.severe("Invalid buffer name: " + program + "." + buffer);
                    }
                } else {
                    SMCLog.severe("Invalid buffer name: " + program);
                }
            }
        }

        // Shaders.updateAlphaBlend
        public static void updateOldBlendStateIndexed(Program program) {
            if (propBlend.get(program) != null) {
                applyCurrentBlend();
            }
        }

        // Shaders.updateAlphaBlend
        public static void updateNewBlendStateIndexed(Program program) {
            GlBlendState[] states = propBlend.get(program);

            if (states != null) {
                if (program.getName().startsWith("shadow")) {
                    for (int i = 0; i < getUsedShadowColorBuffers(); i++) {
                        setBlendIndexed(i, states[i]);
                    }
                } else {
                    String settings = program.getDrawBufSettings();

                    if (settings == null) {
                        int usedDrawBuffers = getUsedDrawBuffers();

                        for (int i = 0; i < usedDrawBuffers; i++) {
                            setBlendIndexed(i, states[i]);
                        }
                    } else {
                        for (int i = 0; i < states.length; i++) {
                            setBlendIndexed(settings.indexOf(i + '0'), states[i]);
                        }
                    }
                }
            }
        }

        // 你可能好奇为什么MC源码里没有这个方法，因为这是我用asm写进去的
        private static void applyCurrentBlend() {
            if (applyCurrentBlend == null) {
                applyCurrentBlend = getMethod(GlStateManager.class, "applyCurrentBlend");
            }

            try {
                applyCurrentBlend.invoke(null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
        }

        private static void setBlendIndexed(int i, GlBlendState state) {
            if (i >= 0 && i <= 7 && state != null) {
                if (state.isEnabled()) {
                    GL30.glEnablei(GL11.GL_BLEND, i);
                } else {
                    GL30.glDisablei(GL11.GL_BLEND, i);
                }

                ARBDrawBuffersBlend.glBlendFuncSeparateiARB(i, state.getSrcFactor(), state.getDstFactor(), state.getSrcFactorAlpha(), state.getDstFactorAlpha());
            }
        }

        private static GlBlendState parseBlendState(String s) {
            if (parseBlendState == null) {
                parseBlendState = getMethod(ShaderPackParser.class, "parseBlendState", String.class);
            }

            try {
                return (GlBlendState) parseBlendState.invoke(null, s);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                return null;
            }
        }
    }

    // ====== 计算着色器 ======

    public static class ComputeShader {

        private static final Map<Program, ComputeProgram[]> computes = Maps.newHashMap();

        private static Pattern PATTERN_LAYOUT = Pattern.compile("\\s*layout\\s*\\((.*)\\)\\s*(\\w+).*");
        private static Pattern PATTERN_CONST_IVEC3 = Pattern.compile("\\s*const\\s+ivec3\\s+(\\w+)\\s*=\\s*(.+)\\s*;.*");
        private static Pattern PATTERN_CONST_VEC2 = Pattern.compile("\\s*const\\s+vec2\\s+(\\w+)\\s*=\\s*(.+)\\s*;.*");

        private static Optional<Method> apertureResolveIncludes = null;

        // Shaders.init
        public static void setupComputePrograms(Program program, String prefixShaders, String programName, String shaderExt) {
            if (program.getProgramStage() != ProgramStage.GBUFFERS) {
                List<ComputeProgram> list = Lists.newArrayList();

                for (int j = 0; j < 27; ++j) {
                    String suffix = j > 0 ? "_" + (char) ('a' + j - 1) : "";
                    String compName = programName + suffix;
                    String path = prefixShaders + compName + shaderExt;

                    ComputeProgram compProgram = new ComputeProgram(compName, program.getProgramStage());

                    setupComputeProgram(compProgram, path);

                    if (compProgram.getId() > 0) {
                        list.add(compProgram);

                        SMCLog.info("Compute program loaded: " + compName);
                    }
                }

                computes.put(program, list.toArray(new ComputeProgram[list.size()]));
            }
        }

        private static void setupComputeProgram(ComputeProgram program, String cshPath) {
            Shaders.checkGLError("pre setupProgram");
            int compShader = createCompShader(program, cshPath);
            Shaders.checkGLError("create");

            if (compShader != 0) {
                int compProgram = ARBShaderObjects.glCreateProgramObjectARB();
                Shaders.checkGLError("create");

                if (compProgram != 0) {
                    ARBShaderObjects.glAttachObjectARB(compProgram, compShader);
                    Shaders.checkGLError("attach");
                }

                ARBShaderObjects.glLinkProgramARB(compProgram);

                if (GL20.glGetProgrami(compProgram, GL20.GL_LINK_STATUS) != GL11.GL_TRUE) {
                    SMCLog.severe("Error linking program: " + compProgram + " (" + program.getName() + ")");
                }

                printLogInfo(compProgram, program.getName());

                if (compShader != 0) {
                    ARBShaderObjects.glDetachObjectARB(compProgram, compShader);
                    ARBShaderObjects.glDeleteObjectARB(compShader);
                }

                program.setId(compProgram);
                program.setRef(compProgram);
                ARBShaderObjects.glUseProgramObjectARB(compProgram);
                ARBShaderObjects.glValidateProgramARB(compProgram);
                ARBShaderObjects.glUseProgramObjectARB(0);
                printLogInfo(compProgram, program.getName());

                int code = GL20.glGetProgrami(compProgram, GL20.GL_VALIDATE_STATUS);

                if (code != GL11.GL_TRUE) {
                    printChatAndLogError("[Shaders] Error: Invalid program \"" + program.getName() + "\"");
                    ARBShaderObjects.glDeleteObjectARB(compProgram);

                    compProgram = 0;

                    program.resetId();
                }
            }
        }

        @SuppressWarnings("resource")
        private static int createCompShader(ComputeProgram program, String filename) {
            InputStream input = Shaders.getShaderPack().getResourceAsStream(filename);

            if (input == null) {
                return 0;
            } else {
                int compShader = ARBShaderObjects.glCreateShaderObjectARB(GL43.GL_COMPUTE_SHADER);

                if (compShader == 0) {
                    return 0;
                } else {
                    StringBuilder compCode = new StringBuilder(0x20000);
                    BufferedReader reader = null;

                    try {
                        reader = new BufferedReader(new InputStreamReader(input));
                    } catch (Exception e) {
                        ARBShaderObjects.glDeleteObjectARB(compShader);

                        return 0;
                    }

                    ShaderOption[] activeOptions = Shaders.getChangedOptions(Shaders.getShaderPackOptions());
                    List<String> listFiles = Lists.newArrayList();

                    if (reader != null) {
                        try {
                            reader = resolveIncludes(reader, filename, Shaders.getShaderPack(), 0, listFiles, 0);

                            MacroState macroState = new MacroState();

                            while (true) {
                                String line = reader.readLine();

                                if (line == null) {
                                    break;
                                }

                                line = applyOptions(line, activeOptions);

                                compCode.append(line).append('\n');

                                if (!macroState.processLine(line)) {
                                    continue;
                                }

                                ShaderLine shaderline = ShaderParser.parseLine(line);

                                if (shaderline != null) {
                                    if (shaderline.isUniform()) {
                                        String uniform = shaderline.getName();
                                        int j;

                                        if ((j = ShaderParser.getShadowDepthIndex(uniform)) >= 0) {
                                            setUsedShadowDepthBuffers(Math.max(getUsedShadowDepthBuffers(), j + 1));
                                        } else if ((j = ShaderParser.getShadowColorIndex(uniform)) >= 0) {
                                            setUsedShadowColorBuffers(Math.max(getUsedShadowColorBuffers(), j + 1));
                                        } else if ((j = getShadowColorImageIndex(uniform)) >= 0) {
                                            setUsedShadowColorBuffers(Math.max(getUsedShadowColorBuffers(), j + 1));

                                            bindColorImages = true;
                                        } else if ((j = ShaderParser.getDepthIndex(uniform)) >= 0) {
                                            setUsedDepthBuffers(Math.max(getUsedDepthBuffers(), j + 1));
                                        } else if ((j = ShaderParser.getColorIndex(uniform)) >= 0) {
                                            setUsedColorBuffers(Math.max(getUsedColorBuffers(), j + 1));
                                        } else if ((j = getColorImageIndex(uniform)) >= 0) {
                                            setUsedColorBuffers(Math.max(getUsedColorBuffers(), j + 1));

                                            bindColorImages = true;
                                        }
                                    } else if (shaderline.isConstBoolSuffix("MipmapEnabled", true)) {
                                        String buffer = StrUtils.removeSuffix(shaderline.getName(), "MipmapEnabled");
                                        int index = Shaders.getBufferIndexFromString(buffer);

                                        if (index >= 0) {
                                            int settings = program.getCompositeMipmapSetting();
                                            settings = settings | 1 << index;
                                            program.setCompositeMipmapSetting(settings);
                                            SMCLog.info("%s mipmap enabled", buffer);
                                        }
                                    }
                                } else if (isLayout(line, "in")) {
                                    Vector3i localSize = parseLocalSize(line);

                                    if (localSize != null) {
                                        program.setLocalSize(localSize);
                                    } else {
                                        SMCLog.severe("Invalid local size: " + line);
                                    }
                                } else if (isConstIVec3(line, "workGroups")) {
                                    Vector3i workGroups = getValueIVec3(line);

                                    if (workGroups != null) {
                                        program.setWorkGroups(workGroups);
                                    } else {
                                        SMCLog.severe("Invalid workGroups: " + line);
                                    }
                                } else if (isConstVec2(line, "workGroupsRender")) {
                                    Vector2f workGroupsRender = getValueVec2(line);

                                    if (workGroupsRender != null) {
                                        program.setWorkGroupsRender(workGroupsRender);
                                    } else {
                                        SMCLog.severe("Invalid workGroupsRender: " + line);
                                    }
                                }
                            }
                            reader.close();
                        } catch (Exception e) {
                            SMCLog.severe("Couldn't read " + filename + "!");
                            e.printStackTrace();

                            ARBShaderObjects.glDeleteObjectARB(compShader);

                            return 0;
                        }
                    }

                    if (Shaders.saveFinalShaders) {
                        Shaders.saveShader(filename, compCode.toString());
                    }

                    if (program.getLocalSize() == null) {
                        SMCLog.severe("Missing local size: " + filename);
                        GL20.glDeleteShader(compShader);

                        return 0;
                    } else {
                        ARBShaderObjects.glShaderSourceARB(compShader, compCode);
                        ARBShaderObjects.glCompileShaderARB(compShader);

                        if (GL20.glGetShaderi(compShader, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE) {
                            SMCLog.severe("Error compiling compute shader: " + filename);
                        }

                        printShaderLogInfo(compShader, filename, listFiles);

                        return compShader;
                    }
                }
            }
        }

        // Shaders.uninit
        public static void deleteComputes(Program program) {
            ComputeProgram[] comps = computes.remove(program);

            if (comps != null) {
                for (ComputeProgram comp : comps) {
                    if (comp.getRef() != 0) {
                        ARBShaderObjects.glDeleteObjectARB(comp.getRef());
                        Shaders.checkGLError("del programRef");
                    }

                    comp.setRef(0);
                    comp.setId(0);
                }
            }
        }

        // Shaders.renderComposites
        // Shaders.renderFinal
        // ShadersRender.renderShadowMap
        public static void dispatchComputes(Program program) {
            ComputeProgram[] comps = computes.get(program);

            if (comps == null) {
                return;
            }

            for (int i = 0; i < comps.length; ++i) {
                ComputeProgram comp = comps[i];

                dispatchCompute(comp);

                if (comp.hasCompositeMipmaps()) {
                    Shaders.genCompositeMipmap();
                }
            }
        }

        private static void dispatchCompute(ComputeProgram cp) {
            if (getDfb() != 0) { // ?
                boolean sp = Shaders.isShadowPass;

                // 临时屏蔽ShadowPass防止useProgram开头直接给改成ProgramShadow了= =啊Gbuffers没有计算着色器所以大丈夫
                Shaders.isShadowPass = false;
                Shaders.useProgram(cp);
                Shaders.isShadowPass = sp;

                Vector3i workGroups = cp.getWorkGroups();

                if (workGroups == null) {
                    Vector2f workGroupsRender = cp.getWorkGroupsRender();

                    if (workGroupsRender == null) {
                        workGroupsRender = new Vector2f(1.0F, 1.0F);
                    }

                    double width = (int) Math.ceil(Shaders.renderWidth * workGroupsRender.x);
                    double height = (int) Math.ceil(Shaders.renderHeight * workGroupsRender.y);
                    Vector3i localSize = cp.getLocalSize();
                    int xGroups = (int) Math.ceil(width / localSize.getX());
                    int yGroups = (int) Math.ceil(height / localSize.getY());
                    workGroups = new Vector3i(xGroups, yGroups, 1);
                }

                GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL42.GL_TEXTURE_FETCH_BARRIER_BIT);
                GL43.glDispatchCompute(workGroups.getX(), workGroups.getY(), workGroups.getZ());
                GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL42.GL_TEXTURE_FETCH_BARRIER_BIT);
                Shaders.checkGLError("compute");
            }
        }

        // Shaders.createFragShader
        public static void parseUniform(String uniform) {
            int i = -1;

            if ((i = getShadowColorImageIndex(uniform)) >= 0) {
                setUsedShadowColorBuffers(Math.max(getUsedShadowColorBuffers(), i + 1));

                bindColorImages = true;
            } else if ((i = getColorImageIndex(uniform)) >= 0) {
                setUsedColorBuffers(Math.max(getUsedColorBuffers(), i + 1));

                bindColorImages = true;
            }
        }

        private static BufferedReader resolveIncludes(BufferedReader reader, String filename, IShaderPack shaderPack, int i, List<String> listFiles, int j) throws IOException {
            if (apertureResolveIncludes == null) {
                try {
                    Class<?> clazz = Class.forName("mchorse.aperture.client.AsmShaderHandler");

                    apertureResolveIncludes = Optional.of(getMethod(clazz, "getCachedShader", Object.class, String.class, IShaderPack.class, int.class, List.class, int.class));
                    LOGGER.info("已检测到可兼容的 Aperture 模组");
                } catch (Exception e) {
                    apertureResolveIncludes = Optional.empty();
                }
            }

            if (apertureResolveIncludes.isPresent()) {
                try {
                    return (BufferedReader) apertureResolveIncludes.get().invoke(null, reader, filename, shaderPack, i, listFiles, j);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
            }

            return ShaderPackParser.resolveIncludes(reader, filename, shaderPack, i, listFiles, j);
        }

        private static int getColorImageIndex(String name) {
            int index = -1;

            if (name.startsWith("colorimg")) {
                try {
                    index = Integer.parseInt(name.substring(8));
                } catch (NumberFormatException e) {}
            }

            return index;
        }

        private static int getShadowColorImageIndex(String name) {
            int index = -1;

            if (name.startsWith("shadowcolorimg")) {
                try {
                    index = Integer.parseInt(name.substring(14));
                } catch (NumberFormatException e) {}
            }

            return index;
        }

        private static boolean isLayout(String line, String type) {
            Matcher matcher = PATTERN_LAYOUT.matcher(line);

            if (matcher.matches()) {
                return type.equals(matcher.group(2));
            }

            return false;
        }

        private static Vector3i parseLocalSize(String line) {
            Matcher matcher = PATTERN_LAYOUT.matcher(line);

            if (matcher.matches()) {
                String value = matcher.group(1);

                int x = 1;
                int y = 1;
                int z = 1;
                String[] values = value.split(",");

                for (int l = 0; l < values.length; ++l) {
                    String pair = values[l];
                    String[] tokens = pair.split("=");

                    if (tokens.length == 2) {
                        String k = tokens[0].trim();
                        String v = tokens[1].trim();
                        int num = -1;

                        try {
                            num = Integer.parseInt(v);
                        } catch (NumberFormatException e) {
                            return null;
                        }

                        if (k.equals("local_size_x")) {
                            x = num;
                        }

                        if (k.equals("local_size_y")) {
                            y = num;
                        }

                        if (k.equals("local_size_z")) {
                            z = num;
                        }
                    }
                }

                return x == 1 && y == 1 && z == 1 ? null : new Vector3i(x, y, z);
            }

            return null;
        }

        private static boolean isConstIVec3(String line, String name) {
            Matcher matcher = PATTERN_CONST_IVEC3.matcher(line);

            if (matcher.matches()) {
                return name.equals(matcher.group(1));
            }

            return false;
        }

        private static Vector3i getValueIVec3(String line) {
            Matcher matcher = PATTERN_CONST_IVEC3.matcher(line);

            if (matcher.matches()) {
                String value = matcher.group(2);

                if (value != null) {
                    String str = value.trim();
                    str = StrUtils.removePrefix(str, "ivec3");
                    str = StrUtils.trim(str, " ()");
                    String[] values = str.split(", ");

                    if (values.length != 3) {
                        return null;
                    } else {
                        int[] nums = new int[3];

                        for (int i = 0; i < values.length; ++i) {
                            String num = values[i];

                            try {
                                nums[i] = Integer.parseInt(num);
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        }

                        return new Vector3i(nums[0], nums[1], nums[2]);
                    }
                }
            }

            return null;
        }

        private static boolean isConstVec2(String line, String name) {
            Matcher matcher = PATTERN_CONST_VEC2.matcher(line);

            if (matcher.matches()) {
                return name.equals(matcher.group(1));
            }

            return false;
        }

        private static Vector2f getValueVec2(String line) {
            Matcher matcher = PATTERN_CONST_VEC2.matcher(line);

            if (matcher.matches()) {
                String value = matcher.group(2);

                if (value != null) {
                    String str = value.trim();
                    str = StrUtils.removePrefix(str, "vec2");
                    str = StrUtils.trim(str, " ()");
                    String[] values = str.split(", ");

                    if (values.length != 2) {
                        return null;
                    } else {
                        float[] nums = new float[2];

                        for (int i = 0; i < values.length; ++i) {
                            String num = values[i];

                            try {
                                nums[i] = Float.parseFloat(num);
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        }

                        return new Vector2f(nums[0], nums[1]);
                    }
                }
            }

            return null;
        }

        private static void bindColorImages() {
            for (int i = 0; i < Math.min(getUsedColorBuffers(), 6); i++) {
                GL42.glBindImageTexture(i, dfbColorTexturesFlip.getA(i), 0, false, 0, GL15.GL_READ_WRITE, getImageFormat(gbuffersFormat[i]));
            }

            for (int i = 0; i < Math.min(getUsedShadowColorBuffers(), 2); i++) {
                GL42.glBindImageTexture(i + 6, sfbColorTexturesFlip.getA(i), 0, false, 0, GL15.GL_READ_WRITE, getImageFormat(shadowFormat[i]));
            }
        }

        private static int getImageFormat(int textureFormat) {
            switch (textureFormat) {
            case GL11.GL_RGB:
                return GL11.GL_RGB8;

            case GL11.GL_RGBA:
                return GL11.GL_RGBA8;

            case GL11.GL_R:
                return GL30.GL_R8;

            case GL11.GL_R3_G3_B2:
                return GL11.GL_RGB8;

            case GL11.GL_RGB5_A1:
                return GL11.GL_RGBA8;

            case GL30.GL_RG:
                return GL30.GL_RG8;

            case GL30.GL_RGB9_E5:
                return GL11.GL_RGB16;

            default:
                return textureFormat;
            }
        }

        public static boolean hasComputes(Program program) {
            return computes.get(program).length > 0;
        }

        private static class ComputeProgram extends Program {
            private Vector3i localSize;
            private Vector3i workGroups;
            private Vector2f workGroupsRender;

            public ComputeProgram(String name, ProgramStage programStage) {
                super(-1, name, programStage, false);
            }

            @Override
            public void resetConfiguration() {
                super.resetConfiguration();

                this.localSize = null;
                this.workGroups = null;
                this.workGroupsRender = null;
            }

            public Vector3i getLocalSize() {
                return this.localSize;
            }

            public void setLocalSize(Vector3i localSize) {
                this.localSize = localSize;
            }

            public Vector3i getWorkGroups() {
                return this.workGroups;
            }

            public void setWorkGroups(Vector3i workGroups) {
                this.workGroups = workGroups;
            }

            public Vector2f getWorkGroupsRender() {
                return this.workGroupsRender;
            }

            public void setWorkGroupsRender(Vector2f workGroupsRender) {
                this.workGroupsRender = workGroupsRender;
            }

            public boolean hasCompositeMipmaps() {
                return this.getCompositeMipmapSetting() != 0;
            }
        }

        private static class Vector3i {
            public int x;
            public int y;
            public int z;

            public Vector3i(int x, int y, int z) {
                this.x = x;
                this.y = y;
                this.z = z;
            }

            public int getX() {
                return x;
            }

            public int getY() {
                return y;
            }

            public int getZ() {
                return z;
            }
        }

    }

// ====== block.properties 修复 ======

    public static class BlockAliasFix {

        private static Field blockAliases;

        // SVertexBuilder.pushEntity
        public static int getBlockAliasId(IBlockState blockState, BlockPos blockPos, IBlockAccess blockAccess) {
            Block block = blockState.getBlock();
            IBlockState state = blockState.getActualState(blockAccess, blockPos);
            BlockAlias[][] aliasList = getBlockAliases();

            int id = Block.getIdFromBlock(block);
            int metadata = block.getMetaFromState(blockState);

            if (blockState instanceof BlockStateBase) {
                BlockStateBase base = (BlockStateBase) blockState;

                id = base.getBlockId();
                metadata = base.getMetadata();
            }

            if (aliasList == null || id < 0 || id >= aliasList.length || aliasList[id] == null) {
                return ForwardFeatures.idMapping ? -1 : id;
            }

            BlockAlias[] aliases = aliasList[id];

            for (BlockAlias alias : aliases) {
                MatchBlock[] matches = alias.getMatchBlocks(id);

                for (MatchBlock match : matches) {
                    MatchBlockState matchState = (MatchBlockState) match;
                    Map<String, String> properties = matchState.properties;
                    boolean matched = true;

                    for (String key : properties.keySet()) {
                        IProperty<?> prop = ConnectedProperties.getProperty(key, state.getPropertyKeys());

                        matched = false;

                        for (String value : properties.get(key).split(",")) {
                            Comparable<?> v = ConnectedParser.getPropertyValue(value, prop.getAllowedValues());

                            if (state.getValue(prop).equals(v)) {
                                matched = true;

                                break;
                            }
                        }

                        if (!matched) {
                            break;
                        }
                    }

                    if (matched) {
                        return alias.getBlockAliasId();
                    }
                }

                if (alias.matches(id, metadata)) {
                    return alias.getBlockAliasId();
                }
            }

            return ForwardFeatures.idMapping ? -1 : id;
        }

        private static BlockAlias[][] getBlockAliases() {
            if (blockAliases == null) {
                blockAliases = getField(BlockAliases.class, "blockAliases");
            }

            try {
                return (BlockAlias[][]) blockAliases.get(null);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                return null;
            }
        }

        public static class MatchBlockState extends MatchBlock {
            private final Map<String, String> properties;

            public MatchBlockState(int id, int[] metadatas, String[] properties) {
                super(id, metadatas);

                Map<String, String> map = Maps.newHashMap();

                for (String property : properties) {
                    String[] token = property.split("=");

                    if (token.length == 2) {
                        map.put(token[0], token[1]);
                    }
                }

                this.properties = ImmutableMap.copyOf(map);
            }
        }

    }

// ====== 自定义Uniform增加变量 ======

    public static class CustomUniform {

        // ShaderExpressionResolver.registerExpressions
        public static void registerExpressions(ShaderExpressionResolver resolver) {
            resolver.registerExpression("playerMood", new IExpressionFloat() {
                @Override
                public float eval() {
                    return uniformPlayerMood.getValue();
                }
            });

            resolver.registerExpression("biome_category", new IExpressionFloat() {

                @Override
                public float eval() {
                    BlockPos pos = Shaders.getCameraPosition();
                    Biome biome = Shaders.getCurrentWorld().getBiome(pos);

                    return BiomeCategory.fromBiome(biome).ordinal();
                }
            });

            resolver.registerExpression("biome_precipitation", new IExpressionFloat() {

                @Override
                public float eval() {
                    BlockPos pos = Shaders.getCameraPosition();
                    Biome biome = Shaders.getCurrentWorld().getBiome(pos);

                    return BiomeRainType.fromBiome(biome).ordinal();
                }
            });

            for (BiomeCategory category : BiomeCategory.values()) {
                resolver.registerExpression("CAT_" + category.name().toUpperCase(), new ConstantFloat(category.ordinal()));
            }

            for (BiomeRainType rain : BiomeRainType.values()) {
                resolver.registerExpression("PPT_" + rain.name().toUpperCase(), new ConstantFloat(rain.ordinal()));
            }
        }

        public static enum BiomeCategory {
            NONE(),
            TAIGA(Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.COLD_TAIGA, Biomes.COLD_TAIGA_HILLS, Biomes.MUTATED_REDWOOD_TAIGA, Biomes.MUTATED_REDWOOD_TAIGA_HILLS, Biomes.MUTATED_TAIGA, Biomes.MUTATED_TAIGA_COLD, Biomes.REDWOOD_TAIGA, Biomes.REDWOOD_TAIGA_HILLS),
            EXTREME_HILLS(Biomes.EXTREME_HILLS, Biomes.EXTREME_HILLS_EDGE, Biomes.EXTREME_HILLS_WITH_TREES, Biomes.MUTATED_EXTREME_HILLS, Biomes.MUTATED_EXTREME_HILLS_WITH_TREES),
            JUNGLE(Biomes.JUNGLE, Biomes.JUNGLE_EDGE, Biomes.JUNGLE_HILLS, Biomes.MUTATED_JUNGLE, Biomes.MUTATED_JUNGLE_EDGE),
            MESA(Biomes.MESA, Biomes.MESA_CLEAR_ROCK, Biomes.MESA_ROCK, Biomes.MUTATED_MESA, Biomes.MUTATED_MESA_CLEAR_ROCK, Biomes.MUTATED_MESA_ROCK),
            PLAINS(Biomes.PLAINS, Biomes.MUTATED_PLAINS),
            SAVANNA(Biomes.ICE_MOUNTAINS, Biomes.ICE_PLAINS, Biomes.MUTATED_ICE_FLATS, Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER),
            ICY(Biomes.ICE_MOUNTAINS, Biomes.ICE_PLAINS, Biomes.MUTATED_ICE_FLATS, Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER),
            THEEND(Biomes.SKY),
            BEACH(Biomes.BEACH, Biomes.COLD_BEACH, Biomes.STONE_BEACH),
            FOREST(Biomes.FOREST, Biomes.FOREST_HILLS, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS, Biomes.MUTATED_BIRCH_FOREST, Biomes.MUTATED_BIRCH_FOREST_HILLS, Biomes.MUTATED_FOREST, Biomes.MUTATED_ROOFED_FOREST, Biomes.ROOFED_FOREST),
            OCEAN(Biomes.OCEAN, Biomes.DEEP_OCEAN),
            DESERT(Biomes.DESERT, Biomes.DESERT_HILLS, Biomes.MUTATED_DESERT),
            RIVER(Biomes.RIVER),
            SWAMP(Biomes.SWAMPLAND, Biomes.MUTATED_SWAMPLAND),
            MUSHROOM(Biomes.MUSHROOM_ISLAND, Biomes.MUSHROOM_ISLAND_SHORE),
            NETHER(Biomes.HELL);

            public static BiomeCategory fromBiome(Biome biome) {
                for (BiomeCategory category : BiomeCategory.values()) {
                    if (category.biomes.contains(biome)) {
                        return category;
                    }
                }

                return BiomeCategory.NONE;
            }

            private final List<Biome> biomes;

            private BiomeCategory(Biome... biomes) {
                this.biomes = Lists.newArrayList(biomes);
            }

            public void addBiome(Biome biome) {
                this.biomes.add(biome);
            }
        }

        private static enum BiomeRainType {
            NONE,
            RAIN,
            SNOW;

            public static BiomeRainType fromBiome(Biome biome) {
                if (biome.isSnowyBiome()) {
                    return SNOW;
                } else {
                    return biome.canRain() ? RAIN : NONE;
                }
            }
        }
    }

// ====== 相机坐标修正 ======

    public static class CameraFix {

        // Shaders.setCamera
        public static void fixCamera() {
            Vector3f offset = new Vector3f(modelViewInverse.get(12), modelViewInverse.get(13), modelViewInverse.get(14));
            Vector3f offsetN = offset.negate(null);

            Matrix4f mv = getMatrix(modelView);
            Matrix4f mvi = getMatrix(modelViewInverse);

            mv = mv.translate(offset);
            mvi = Matrix4f.mul(new Matrix4f().translate(offsetN), mvi, null);

            setMatrix(modelView, mv);
            setMatrix(modelViewInverse, mvi);

            setCameraPositionX(getCameraPositionX() + offset.x);
            setCameraPositionY(getCameraPositionY() + offset.y);
            setCameraPositionZ(getCameraPositionZ() + offset.z);
            setEyePosY((float) getCameraPositionY());
        }

        // Shaders.setCameraShadow
        public static void fixCameraShadow() {
            Vector3f offset = new Vector3f(tempMatrixDirectBuffer.get(12), tempMatrixDirectBuffer.get(13), tempMatrixDirectBuffer.get(14));
            Vector3f offsetN = offset.negate(null);

            Matrix4f smv = getMatrix(shadowModelView);
            Matrix4f smvi = getMatrix(shadowModelViewInverse);

            smv = smv.translate(offset);
            smvi = Matrix4f.mul(new Matrix4f().translate(offsetN), smvi, null);

            setMatrix(shadowModelView, smv);
            setMatrix(shadowModelViewInverse, smvi);

            setCameraPositionX(getCameraPositionX() + offset.x);
            setCameraPositionY(getCameraPositionY() + offset.y);
            setCameraPositionZ(getCameraPositionZ() + offset.z);
            setEyePosY((float) getCameraPositionY());
        }

        private static Matrix4f getMatrix(FloatBuffer buffer) {
            return (Matrix4f) new Matrix4f().load((FloatBuffer) buffer.asReadOnlyBuffer().position(0));
        }

        private static void setMatrix(FloatBuffer buffer, Matrix4f mat) {
            mat.store((FloatBuffer) buffer.duplicate().position(0));
        }
    }

// ====== 修复光影包语言文件破Bug ======

    public static class LangFix {

        public static InputStream fixLanguage(ZipFile zip, String filePath) throws IOException {
            if (filePath.toLowerCase().endsWith(".lang")) {
                Optional<? extends ZipEntry> optional = zip.stream().filter(entry -> entry.getName().equalsIgnoreCase(filePath)).findFirst();

                if (optional.isPresent()) {
                    return zip.getInputStream(optional.get());
                }
            }

            return null;
        }
    }

// ====== 模组信息 ======

    public Hook() {
        super(new ModMetadata());

        ModMetadata meta = this.getMetadata();
        meta.modId = "advancedshader";
        meta.name = "光影前向兼容AdvancedShader";
        meta.version = "1.0-FINAL";
        meta.credits += "\n致谢：";
        meta.credits += "\n  在我最孤单时陪着我的朋友： ネムロイ、阳炘 （没有他们就没有这个模组了哦）";
        meta.credits += "\n  内测参与者： Surisen（内测的神， 几乎所有严重Bug都是这位发现的）、ExDragine（宣发支持）、GeForceLegend（Bug提交者）、villa_qi（Bug提交者）、EpsilonSatoshi、少修、奥维利亚two";
        meta.authorList = ImmutableList.of("一只猫", "I have no name", "I am but two days old", "you can call me V", "and this is POWER!");
        meta.description += TextFormatting.RED + "" + TextFormatting.BOLD + "<由于作者身体原因， 本模组不再进行维护与更新， 如有Bug， 凑合用吧~>\n" + TextFormatting.RESET;
        meta.description += "\n";
        meta.description += "“有欲望而无行动者滋生瘟疫。”\n";
        meta.description += "这就是为什么我写了这个模组\n";
        meta.description += "这是一个很简单的模组， 只是让你能在这里使用Minecraft 1.16.5+光影， 仅此而已。\n";
        meta.description += "\n";
        meta.description += "如果有奇奇怪怪的渲染问题请尝试将渲染机制在1.12.2和1.16.5之间反复横跳\n";
        meta.description += "如果草方块像摇曳鳗一样晃起来了的话请将方块ID设为1.16.5\n";
        meta.description += "如果你想知道什么时候会被游戏的惊悚声音吓一跳的话请将氛围值机制设为1.16.5并打开F3\n";
        meta.description += "Have fun~\n";
        meta.description += "\n";
        meta.description += "\n";
        meta.description += "支持特性：\n";
        meta.description += " - 计算着色器与colorimg、 shadowcolorimg\n";
        meta.description += " - RenderTargets注释和colortex8-15\n";
        meta.description += " - Prepare预处理着色器和ShadowComp阴影后处理着色器\n";
        meta.description += " - RenderStage渲染阶段Uniform变量\n";
        meta.description += " - 1.17+的Core Profile （不支持alphaTestRef）\n";
        meta.description += " - at_midBlock顶点属性\n";
        meta.description += " - gbuffers_line线段着色器\n";
        meta.description += " - 8 bits 与 16 bits 的整数型纹理格式\n";
        meta.description += " - 100个后处理着色器 （#6530， 嗯……无力吐槽）\n";
        meta.description += " - 阴影阶段渲染地形、实体、实体方块配置\n";
        meta.description += " - size.buffer.<buffer>配置和blend.<program>.<buffer>配置\n";
        meta.description += "\n";
        meta.description += "不支持特性：\n";
        meta.description += " - at_velocity顶点属性 （没法写~不过我让它固定为0了）\n";
        meta.description += " - 阴影视裁框剔除 （懒得写~）\n";
        meta.description += "\n";
        meta.description += "其他修改：\n";
        meta.description += " - 增加渲染机制、方块ID、氛围值机制选项用以模拟高版本特性\n";
        meta.description += " - 修复了无法正常加载压缩包内语言文件的破Bug （英文文件是en_US大小写混合， 其他语言是zh_cn这种全小写的， 文件名还区分大小写= =去死吧！）\n";
        meta.description += " - 支持识别in顶点属性（以前好像不识别来着……？） \n";
        meta.description += " - 支持同时开启光影与各向异性过滤\n";
        meta.description += " - shaders.properties增加对光影选项宏的支持 （看着好像是没写完， 高版本已经有了， 顺便补一下吧）\n";
        meta.description += " - 修复新版本JRE（8u292-b10之后）导致无法打开光影文件夹的问题\n";
        meta.description += " - 修复block.properties无法正常匹配方块状态问题\n";
        meta.description += " - 增加biome_category与biome_precipitation用于自定义Uniform\n";
        meta.description += " - cameraPosition更改为实际相机位置\n";
        meta.description += " - 修改物品侧面UV防止dfdx、dfdy返回0\n";
        meta.description += " - 兼容Aperture模组 （但光影本身不兼容Aperture的则没办法= =）\n";
        meta.description += " - 修复宏状态机的bug\n";
        meta.description += "\n";
        meta.description += "光影开发相关说明：\n";
        meta.description += " - 光影内可通过 #ifdef MC_MOD_ADVANCED_SHADER 判断是否已加载此模组\n";
        meta.description += " - Core Profile原理是将顶点属性和Uniform变量替换为宏， 并不是真的实现了Core Profile\n";
        meta.description += "    - 为保证兼容性，只有光影包 “使用” 了chunkOffset变量的情况才会将此偏移从gl_ModelViewMatrix中分离出来\n";
        meta.description += " - 如果光影包提供了gbuffers_line着色器， 游戏将会用三角形/四边形模式渲染线段， 同1.17+一致 （为什么OF没有传入线宽度？？）\n";
        meta.description += "    - 如果没有gbuffers_line， 则会在gbuffers_basic里用线段模式渲染\n";
        meta.description += "    - 请参考原版1.17自带rendertype_line.vsh着色器文件来编写顶点着色器\n";
        meta.description += " - 开启各向异性过滤后， 游戏会执行以下操作 （当然也是同高版本OF一致啦= =）\n";
        meta.description += "    - #define MC_ANISOTROPIC_FILTERING 2-16\n";
        meta.description += "    - 依次根据纹理分组渲染方块， UV也会做相应修改\n";
        meta.description += "    - 向spriteBounds Uniform变量写入当前方块所使用的纹理在textures/atlas/blocks.png中的坐标\n";
        meta.description += " - 渲染机制设为1.16.5之后\n";
        meta.description += "    - 着色器中的MC_VERSION将被定义为11605， block.properties等文件不受影响\n";
        meta.description += "    - 游戏将使用高版本的渲染顺序， 例如绊线方块、粒子、云会在半透明方块之后渲染\n";
        meta.description += "    - 绊线会调用gbuffers_textured_lit着色器而不是gbuffers_water， 并拥有独立的RenderStage\n";
        meta.description += " - 方块ID设为1.16.5之后\n";
        meta.description += "    - block/item/entity.properties中的MC_VERSION将被定义为11605， 并会在加载过程中对ID进行转换\n";
        meta.description += " - 氛围值机制设为1.16.5之后\n";
        meta.description += "    - 将使用高版本氛围值机制控制环境音效， 并且使光影中的playerMood可用\n";
        meta.description += " - 这个模组优先考虑的是让不懂开发光影玩家能直接使用高版本光影，所以有些机制设计可能对光影开发者不友好，还请见谅~\n";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

    @Override
    public File getSource() {
        return Core.source;
    }

    @Override
    public Class<?> getCustomResourcePackClass() {
        return FMLFileResourcePack.class;
    }
}

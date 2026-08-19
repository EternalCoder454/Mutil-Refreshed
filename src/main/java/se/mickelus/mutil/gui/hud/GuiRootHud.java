package se.mickelus.mutil.gui.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.MultiBufferSource;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;

public class GuiRootHud extends GuiElement {

    // Full block and sky light packed together, 0xF000F0. Written out because the constant
    // that held it is not where it was, and Tetra's own renderers use the literal too.
    private static final int fullBright = 15728880;

    public GuiRootHud() {
        super(0, 0, 0, 0);
    }

    public void draw(GuiGraphicsExtractor graphics, PoseStack pose, Vec3 proj, BlockHitResult rayTrace, VoxelShape shape) {
        BlockPos blockPos = rayTrace.getBlockPos();

        Vec3 hitVec = rayTrace.getLocation();

        draw(graphics, pose, blockPos.getX() - proj.x, blockPos.getY() - proj.y, blockPos.getZ() - proj.z,
                hitVec.x - blockPos.getX(), hitVec.y - blockPos.getY(), hitVec.z - blockPos.getZ(),
                rayTrace.getDirection(), shape.bounds());
    }

    /**
     * Put the pose on the block face and size this element to it.
     *
     * Both draw paths need exactly this, and the screen one had it inline. Shared rather than
     * copied, because two versions of a transform this fiddly would drift the first time one of
     * them was touched.
     *
     * {@return where on the face the cursor is, in this element's own coordinates}
     */
    protected int[] positionOnFace(PoseStack pose, double x, double y, double z, double hitX, double hitY, double hitZ,
            Direction facing, AABB boundingBox) {
        pose.pushPose();
        pose.translate(x, y, z);

        int mouseX = 0;
        int mouseY = 0;

        float size = 64;

        // magic number is the same used to offset the outline, stops textures from flickering
        Vec3 magicOffset = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(0.0020000000949949026D);
        pose.translate(magicOffset.x(), magicOffset.y(), magicOffset.z());

        switch (facing) {
            case NORTH:
                mouseX = (int) ( ( boundingBox.maxX - hitX ) * size );
                mouseY = (int) ( ( boundingBox.maxY - hitY ) * size );

                width = (int) ((boundingBox.maxX - boundingBox.minX) * size);
                height = (int) ((boundingBox.maxY - boundingBox.minY) * size);

                pose.translate(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
                pose.mulPose(Axis.YP.rotationDegrees(180));
                break;
            case SOUTH:
                mouseX = (int) ( ( hitX - boundingBox.minX ) * size );
                mouseY = (int) ( ( boundingBox.maxY - hitY ) * size );

                width = (int) ((boundingBox.maxX - boundingBox.minX) * size);
                height = (int) ((boundingBox.maxY - boundingBox.minY) * size);

                pose.translate(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
                break;
            case EAST:
                mouseX = (int) ( ( boundingBox.maxZ - hitZ ) * size );
                mouseY = (int) ( ( boundingBox.maxY - hitY ) * size );

                width = (int) ((boundingBox.maxZ - boundingBox.minZ) * size);
                height = (int) ((boundingBox.maxY - boundingBox.minY) * size);

                pose.translate(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                pose.mulPose(Axis.YP.rotationDegrees(90));
                break;
            case WEST:
                mouseX = (int) ( ( hitZ - boundingBox.minZ ) * size );
                mouseY = (int) ( ( boundingBox.maxY - hitY ) * size );

                width = (int) ((boundingBox.maxZ - boundingBox.minZ) * size);
                height = (int) ((boundingBox.maxY - boundingBox.minY) * size);

                pose.translate(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
                pose.mulPose(Axis.YP.rotationDegrees(-90));
                break;
            case UP:
                mouseX = (int) ( ( boundingBox.maxX - hitX ) * size );
                mouseY = (int) ( ( boundingBox.maxZ - hitZ ) * size );

                width = (int) ((boundingBox.maxX - boundingBox.minX) * size);
                height = (int) ((boundingBox.maxZ - boundingBox.minZ) * size);

                pose.translate(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                pose.mulPose(Axis.XP.rotationDegrees(90));
                pose.scale(-1, 1, 1);
                break;
            case DOWN:
                mouseX = (int) ( ( hitX - boundingBox.minX ) * size );
                mouseY = (int) ( ( boundingBox.maxZ - hitZ ) * size );

                width = (int) ((boundingBox.maxX - boundingBox.minX) * size);
                height = (int) ((boundingBox.maxZ - boundingBox.minZ) * size);

                pose.translate(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
                pose.mulPose(Axis.XP.rotationDegrees(90));
                break;
        }

        pose.scale(1 / size, -1 / size, 1 / size);
        pose.translate(0.0D, 0, 0.02);
        return new int[]{mouseX, mouseY};
    }

    public void draw(GuiGraphicsExtractor graphics, PoseStack pose, double x, double y, double z, double hitX, double hitY, double hitZ,
            Direction facing, AABB boundingBox) {
        activeAnimations.removeIf(keyframeAnimation -> !keyframeAnimation.isActive());
        activeAnimations.forEach(KeyframeAnimation::preDraw);

        int[] mouse = positionOnFace(pose, x, y, z, hitX, hitY, hitZ, facing, boundingBox);
        updateFocusState(0, 0, mouse[0], mouse[1]);
        drawChildren(graphics, 0, 0, width, height, mouse[0], mouse[1], 1);
        pose.popPose();
    }

    /**
     * The same overlay, drawn onto the block face in the world.
     *
     * A block outline renderer hands over a pose and a buffer source, which is what the screen
     * path lost when GuiGraphics became an extract phase object writing into a gui render state.
     * Full brightness, because an interaction hint that dims in a dark room is a hint nobody sees.
     */
    public void drawWorld(PoseStack pose, MultiBufferSource buffers, Vec3 proj, BlockHitResult rayTrace, VoxelShape shape) {
        BlockPos blockPos = rayTrace.getBlockPos();
        Vec3 hitVec = rayTrace.getLocation();
        AABB boundingBox = shape.bounds();

        activeAnimations.removeIf(keyframeAnimation -> !keyframeAnimation.isActive());
        activeAnimations.forEach(KeyframeAnimation::preDraw);

        int[] mouse = positionOnFace(pose,
                blockPos.getX() - proj.x, blockPos.getY() - proj.y, blockPos.getZ() - proj.z,
                hitVec.x - blockPos.getX(), hitVec.y - blockPos.getY(), hitVec.z - blockPos.getZ(),
                rayTrace.getDirection(), boundingBox);

        updateFocusState(0, 0, mouse[0], mouse[1]);
        drawChildrenWorld(pose, buffers, 0, 0, 1, fullBright);
        pose.popPose();
    }
}

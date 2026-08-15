package twopiradians.blockArmor.client.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import twopiradians.blockArmor.mixin.ModelPartAccessor;
import twopiradians.blockArmor.common.command.CommandDev;
import java.awt.Color;

/** The original Forge armor geometry, using a Fabric atlas-sprite vertex wrapper. */
public final class ModelBAArmor extends HumanoidModel<LivingEntity> {
    private static final Field POLYGONS = findPolygonField();
    private final ModelPart waist;
    private final ModelPart rightFoot;
    private final ModelPart leftFoot;
    private final TextureAtlasSprite sprite;
    private final float red, green, blue;
    private LivingEntity entity;

    public ModelBAArmor(EquipmentSlot slot, TextureAtlasSprite sprite, int color) {
        this(makeRoot(slot), slot, sprite, color);
    }

    private ModelBAArmor(ModelPart root, EquipmentSlot slot, TextureAtlasSprite sprite, int color) {
        super(root);
        this.waist = root.getChild("waist");
        this.rightFoot = root.getChild("right_foot");
        this.leftFoot = root.getChild("left_foot");
        this.sprite = sprite;
        this.red = color < 0 ? 1 : ((color >> 16) & 255) / 255f;
        this.green = color < 0 ? 1 : ((color >> 8) & 255) / 255f;
        this.blue = color < 0 ? 1 : (color & 255) / 255f;
        setAllVisible(false);
        switch (slot) {
            case HEAD -> head.visible = true;
            case CHEST -> { body.visible = true; rightArm.visible = true; leftArm.visible = true; }
            case LEGS -> { waist.visible = true; rightLeg.visible = true; leftLeg.visible = true; }
            case FEET -> { rightFoot.visible = true; leftFoot.visible = true; }
            default -> { }
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay,
            float ignoredRed, float ignoredGreen, float ignoredBlue, float alpha) {
        VertexConsumer wrapped = sprite.wrap(consumer);
        float r=red,g=green,b=blue;
        Float[] dev = entity == null ? null : CommandDev.devColors.get(entity.getUUID());
        if (dev != null && dev[0]==0 && dev[1]==0 && dev[2]==0) {
            Color rainbow=Color.getHSBColor(entity.tickCount/30f,1f,1f); r=rainbow.getRed()/255f;g=rainbow.getGreen()/255f;b=rainbow.getBlue()/255f;
        } else if (dev != null) { double pulse=(Math.cos(entity.tickCount/5f)+1d)/3d+.01d; r+=pulse*dev[0];g+=pulse*dev[1];b+=pulse*dev[2]; }
        pose.pushPose();
        if (young) { pose.scale(.5f,.5f,.5f); pose.translate(0,1.5,0); }
        head.render(pose, wrapped, light, overlay, r,g,b,alpha); body.render(pose, wrapped, light, overlay,r,g,b,alpha);
        rightArm.render(pose, wrapped, light, overlay,r,g,b,alpha); leftArm.render(pose, wrapped, light, overlay,r,g,b,alpha);
        waist.render(pose, wrapped, light, overlay,r,g,b,alpha); rightLeg.render(pose, wrapped, light, overlay,r,g,b,alpha);
        leftLeg.render(pose, wrapped, light, overlay,r,g,b,alpha); rightFoot.render(pose, wrapped, light, overlay,r,g,b,alpha);
        leftFoot.render(pose, wrapped, light, overlay,r,g,b,alpha);
        pose.popPose();
    }

    public void setEntity(LivingEntity entity) { this.entity = entity; }

    public void copyPose(HumanoidModel<LivingEntity> source) {
        source.copyPropertiesTo(this);
        waist.copyFrom(body);
        rightFoot.copyFrom(rightLeg);
        leftFoot.copyFrom(leftLeg);
        leftLeg.xRot -= 0.001f;
    }

    private static ModelPart makeRoot(EquipmentSlot slot) {
        ModelPart head = part(0, 0, 0), hat = part(0, 0, 0), body = part(0, 0, 0);
        ModelPart rightArm = part(-5, 2, 0), leftArm = part(5, 2, 0);
        ModelPart rightLeg = part(-1.9f, 12, 0), leftLeg = part(1.9f, 12, 0);
        ModelPart waist = part(0, 0, 0), rightFoot = part(-1.9f, 12, 0), leftFoot = part(1.9f, 12, 0);
        switch (slot) {
            case HEAD -> helmet(head);
            case CHEST -> chest(body, rightArm, leftArm);
            case LEGS -> legs(waist, rightLeg, leftLeg);
            case FEET -> feet(rightFoot, leftFoot);
            default -> { }
        }
        return new ModelPart(Lists.newArrayList(), Maps.newHashMap(Map.ofEntries(
                Map.entry("head", head), Map.entry("hat", hat), Map.entry("body", body),
                Map.entry("right_arm", rightArm), Map.entry("left_arm", leftArm),
                Map.entry("right_leg", rightLeg), Map.entry("left_leg", leftLeg),
                Map.entry("waist", waist), Map.entry("right_foot", rightFoot), Map.entry("left_foot", leftFoot))));
    }

    private static ModelPart part(float x, float y, float z) {
        ModelPart part = new ModelPart(Lists.newArrayList(), Maps.newHashMap());
        part.setPos(x, y, z);
        part.visible = false;
        return part;
    }

    private static void helmet(ModelPart p) {
        plane(p,9,3,-5,-9,-5,10,0,10,false); plane(p,9,0,-5,-9,5,10,8,0,true);
        plane(p,-1,8,-3,-1,5,6,1,0,true); plane(p,6,-10,-5,-9,-5,0,5,10,false);
        plane(p,0,0,-5,-4,0,0,1,5,false); plane(p,6,-10,5,-9,-5,0,5,10,true);
        plane(p,11,0,5,-4,0,0,1,5,true); plane(p,3,0,-5,-9,-5,10,4,0,false);
        plane(p,0,4,-5,-5,-5,1,1,0,false); plane(p,7,4,-1,-5,-5,2,2,0,false);
        plane(p,12,4,4,-5,-5,1,1,0,false);
    }

    private static void chest(ModelPart b, ModelPart r, ModelPart l) {
        plane(b,3,4,-5,1,-3,10,8,0,false); plane(b,4,12,-4,9,-3,8,1,0,false);
        plane(b,5,13,-3,10,-3,6,1,0,false); plane(b,3,2,-5,-1,-3,2,2,0,false);
        plane(b,5,2,-3,0,-3,1,1,0,false); plane(b,11,2,3,-1,-3,2,2,0,false);
        plane(b,10,2,2,0,-3,1,1,0,false); plane(b,5,-4,-5,-1,-3,0,10,6,true);
        plane(b,-1,-4,5,-1,-3,0,10,6,false); plane(b,9,3,-5,0,3,10,9,0,true);
        plane(b,11,2,-5,-1,3,2,1,0,true); plane(b,3,2,3,-1,3,2,1,0,true);
        plane(b,4,12,-4,9,3,8,1,0,true);
        plane(r,5,-4,-4.5f,-3,-3,0,6,6,true); plane(r,5,-1,1.5f,-3,-3,0,6,6,true);
        plane(r,-2,2,-4.5f,-3,-3,6,6,0,false); plane(r,-8,2,-4.5f,-3,3,6,6,0,true);
        plane(r,15,5,-4.5f,-3,-3,6,0,6,false);
        plane(l,-1,-1,-1.5f,-3,-3,0,6,6,false); plane(l,-1,-4,4.5f,-3,-3,0,6,6,false);
        plane(l,12,2,-1.5f,-3,-3,6,6,0,false); plane(l,5,5,-1.5f,-3,3,6,6,0,true);
        plane(l,15,5,-1.5f,-3,-3,6,0,6,false);
    }

    private static void legs(ModelPart w, ModelPart r, ModelPart l) {
        plane(w,3,0,-4.5f,6.5f,-2.5f,9,6,0,false); plane(w,6,-5,-4.5f,6.5f,-2.5f,0,6,5,false);
        plane(w,6,-5,4.5f,6.5f,-2.5f,0,6,5,false); plane(w,11,0,-4.5f,6.5f,2.5f,9,6,0,true);
        plane(r,6,0,-2.5f,-.5f,-2.5f,0,10,5,false); plane(r,9,5,2.5f,-.5f,-2.5f,0,10,5,true);
        plane(r,3,5,-2.5f,-.5f,-2.5f,5,10,0,false); plane(r,-1,5,-2.5f,-.5f,2.5f,5,10,0,true);
        plane(r,9,0,-2.5f,-.5f,-2.5f,5,0,5,false);
        plane(l,9,5,-2.5f,-.5f,-2.5f,0,10,5,false); plane(l,6,0,2.5f,-.5f,-2.5f,0,10,5,false);
        plane(l,7,5,-2.5f,-.5f,-2.5f,5,10,0,false); plane(l,3,5,-2.5f,-.5f,2.5f,5,10,0,true);
        plane(l,9,0,-2.5f,-.5f,-2.5f,5,0,5,false);
    }

    private static void feet(ModelPart r, ModelPart l) {
        plane(r,20,3,-3,6,-3,0,7,6,false); plane(r,21,3,3,6,-3,0,7,6,true);
        plane(r,2,7,-3,6,-3,6,7,0,false); plane(r,-2,9,-3,6,3,6,7,0,true);
        plane(r,-1,5,-3,13,-3,6,0,6,false);
        plane(l,21,3,-3,6,-3,0,7,6,true); plane(l,0,3,3,6,-3,0,7,6,true);
        plane(l,-8,7,-3,6,-3,6,7,0,false); plane(l,0,9,-3,6,3,6,7,0,true);
        plane(l,-1,5,-3,13,-3,6,0,6,false);
    }

    private static void plane(ModelPart part, int u, int v, float x, float y, float z,
            float width, float height, float depth, boolean flip) {
        ModelPart.Cube cube = new ModelPart.Cube(u, v, x, y, z, width, height, depth,
                0, 0, 0, flip, 16, 16);
        int index = width == 0 ? (flip ? 1 : 0) : height == 0 ? (flip ? 3 : 2) : (flip ? 5 : 4);
        try {
            Object polygons = POLYGONS.get(cube);
            Object onePolygon = Array.newInstance(polygons.getClass().getComponentType(), 1);
            Array.set(onePolygon, 0, Array.get(polygons, index));
            POLYGONS.set(cube, onePolygon);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to select Block Armor model face", e);
        }
        ((ModelPartAccessor)(Object)part).blockarmor$getCubes().add(cube);
    }

    private static Field findPolygonField() {
        for (Field field : ModelPart.Cube.class.getDeclaredFields()) {
            if (field.getType().isArray()) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException("Unable to locate ModelPart.Cube polygon data");
    }
}

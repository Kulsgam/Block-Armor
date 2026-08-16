package twopiradians.blockArmor.client.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import twopiradians.blockArmor.mixin.ModelPartAccessor;

/** The original Forge armor geometry. */
public final class ModelBAArmor extends HumanoidModel<HumanoidRenderState> {
    private final ModelPart waist;
    private final ModelPart rightFoot;
    private final ModelPart leftFoot;

    public ModelBAArmor(EquipmentSlot slot, TextureAtlasSprite sprite, int color) {
        this(slot);
    }

    public ModelBAArmor(EquipmentSlot slot, Identifier texture, int color) {
        this(slot);
    }

    public ModelBAArmor(EquipmentSlot slot) {
        super(makeRoot(slot));
        this.waist = root().getChild("waist");
        this.rightFoot = root().getChild("right_foot");
        this.leftFoot = root().getChild("left_foot");
        head.visible = hat.visible = body.visible = rightArm.visible = leftArm.visible =
                rightLeg.visible = leftLeg.visible = false;
        waist.visible = rightFoot.visible = leftFoot.visible = false;
        switch (slot) {
            case HEAD -> head.visible = true;
            case CHEST -> { body.visible = true; rightArm.visible = true; leftArm.visible = true; }
            case LEGS -> { waist.visible = true; rightLeg.visible = true; leftLeg.visible = true; }
            case FEET -> { rightFoot.visible = true; leftFoot.visible = true; }
            default -> { }
        }
    }

    @Override
    public void copyTransforms(Model<?> source) {
        super.copyTransforms(source);
        waist.loadPose(body.storePose());
        rightFoot.loadPose(rightLeg.storePose());
        leftFoot.loadPose(leftLeg.storePose());
        leftLeg.xRot -= 0.001F;
    }

    private static ModelPart makeRoot(EquipmentSlot slot) {
        ModelPart hat = part(0, 0, 0), head = part(0, 0, 0, Map.of("hat", hat));
        ModelPart body = part(0, 0, 0);
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
                Map.entry("head", head), Map.entry("body", body),
                Map.entry("right_arm", rightArm), Map.entry("left_arm", leftArm),
                Map.entry("right_leg", rightLeg), Map.entry("left_leg", leftLeg),
                Map.entry("waist", waist), Map.entry("right_foot", rightFoot), Map.entry("left_foot", leftFoot))));
    }

    private static ModelPart part(float x, float y, float z) {
        return part(x, y, z, Map.of());
    }

    private static ModelPart part(float x, float y, float z, Map<String, ModelPart> children) {
        ModelPart part = new ModelPart(Lists.newArrayList(), Maps.newHashMap(children));
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
        plane(b,11,2,-5,-1,3,2,0,0,true); plane(b,3,2,3,-1,3,2,1,0,true);
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
        int index = width == 0 ? (flip ? 1 : 0) : height == 0 ? (flip ? 3 : 2) : (flip ? 5 : 4);
        // The mapped 1.18.1 Cube constructor stores its six polygons by array
        // index as EAST, WEST, DOWN, UP, NORTH, SOUTH. Preserve that exact
        // index-to-face mapping when using 26.1's directional constructor.
        Direction face = new Direction[] {Direction.EAST, Direction.WEST, Direction.DOWN,
                Direction.UP, Direction.NORTH, Direction.SOUTH}[index];
        ModelPart.Cube cube = new ModelPart.Cube(u, v, x, y, z, width, height, depth,
                0, 0, 0, flip, 16, 16, Set.of(face));
        ((ModelPartAccessor)(Object)part).blockarmor$getCubes().add(cube);
    }
}

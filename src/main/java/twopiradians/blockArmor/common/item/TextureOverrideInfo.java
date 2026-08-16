package twopiradians.blockArmor.common.item;

import java.util.HashMap;

import com.google.common.collect.Maps;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;

public class TextureOverrideInfo {

	public HashMap<EquipmentSlot, Info> overrides = Maps.newHashMap();

	public void addSlot(EquipmentSlot slot, int color, Identifier loc) {
		this.overrides.put(slot, new Info(color, loc));
	}
	
	public class Info {
		
		public int color;
		public Identifier shortLoc;
		public Identifier longLoc;

		protected Info(int color, Identifier shortLoc) {
			this.color = color;
			this.shortLoc = shortLoc;
			this.longLoc = Identifier.fromNamespaceAndPath(shortLoc.getNamespace(), "textures/"+shortLoc.getPath()+".png");
		}			
		
	}

}

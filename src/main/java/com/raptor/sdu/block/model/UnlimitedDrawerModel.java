package com.raptor.sdu.block.model;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ImmutableList;
import com.jaquadro.minecraft.chameleon.model.CachedBuilderModel;
import com.jaquadro.minecraft.chameleon.model.PassLimitedModel;
import com.jaquadro.minecraft.chameleon.model.ProxyBuilderModel;
import com.jaquadro.minecraft.chameleon.resources.register.DefaultRegister;
import com.jaquadro.minecraft.storagedrawers.api.storage.EnumBasicDrawer;
import com.jaquadro.minecraft.storagedrawers.block.BlockDrawers;
import com.jaquadro.minecraft.storagedrawers.block.BlockStandardDrawers;
import com.jaquadro.minecraft.storagedrawers.block.BlockVariantDrawers;
import com.jaquadro.minecraft.storagedrawers.block.modeldata.DrawerStateModelData;
import com.jaquadro.minecraft.storagedrawers.client.model.component.DrawerDecoratorModel;
import com.jaquadro.minecraft.storagedrawers.client.model.component.DrawerSealedModel;
import com.jaquadro.minecraft.storagedrawers.core.ModBlocks;
import com.raptor.sdu.SDUnlimited;
import com.raptor.sdu.block.BlockUnlimitedDrawers;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.util.Constants;

public final class UnlimitedDrawerModel {
	public static class Register extends DefaultRegister<BlockUnlimitedDrawers> {
		public Register(BlockUnlimitedDrawers block) {
			super(block);
		}

		@Override
		public List<IBlockState> getBlockStates() {
			List<IBlockState> states = new ArrayList<>();

			for(EnumBasicDrawer drawer : EnumBasicDrawer.values()) {
				for(EnumFacing dir : EnumFacing.HORIZONTALS) {
					//for(BlockPlanks.EnumType woodType : BlockPlanks.EnumType.values()) {
						states.add(this.getBlock().getDefaultState()
								.withProperty(BlockStandardDrawers.BLOCK, drawer)
								.withProperty(BlockDrawers.FACING, dir));
					//}
				}
			}

			return states;
		}

		@Override
		public IBakedModel getModel(IBlockState state, IBakedModel existingModel) {
			return new CachedBuilderModel(new Model(existingModel));
		}

		@Override
		public IBakedModel getModel(ItemStack stack, IBakedModel existingModel) {
			return new CachedBuilderModel(new Model(existingModel));
		}

		@Override
		public List<ResourceLocation> getTextureResources() {
			List<ResourceLocation> resource = new ArrayList<>();
			resource.add(DrawerDecoratorModel.iconClaim);
			resource.add(DrawerDecoratorModel.iconClaimLock);
			resource.add(DrawerDecoratorModel.iconLock);
			resource.add(DrawerDecoratorModel.iconShroudCover);
			resource.add(DrawerDecoratorModel.iconVoid);
			resource.add(DrawerSealedModel.iconTapeCover);
			return resource;
		}
	}

	public static class Model extends ProxyBuilderModel {
		public Model(IBakedModel parent) {
			super(parent);
		}

		@Override
		protected IBakedModel buildModel(IBlockState state, IBakedModel parent) {
			try {
				EnumBasicDrawer drawer = state.getValue(BlockStandardDrawers.BLOCK);
				EnumFacing dir = state.getValue(BlockDrawers.FACING);

				if(!(state instanceof IExtendedBlockState)) {
				//	SDUnlimited.logger.info("state was not IExtendedBlockState!");
					return new PassLimitedModel(parent, BlockRenderLayer.CUTOUT_MIPPED);
				}

				IExtendedBlockState xstate = (IExtendedBlockState)state;
				DrawerStateModelData stateModel = xstate.getValue(BlockDrawers.STATE_MODEL);

				if(!DrawerDecoratorModel.shouldHandleState(stateModel)) {
				//	SDUnlimited.logger.info("not handling state " + stateModel);
					return new PassLimitedModel(parent, BlockRenderLayer.CUTOUT_MIPPED);
				}

				return new DrawerDecoratorModel(parent, xstate, drawer, dir, stateModel);
			} catch(Throwable t) {
				SDUnlimited.logger.log(Level.ERROR, t);
				return new PassLimitedModel(parent, BlockRenderLayer.CUTOUT_MIPPED);
			}
		}

		@Override
		public ItemOverrideList getOverrides() {
			return itemHandler;
		}

		@Override
		public List<Object> getKey(IBlockState state) {
			try {
				List<Object> key = new ArrayList<>();
				IExtendedBlockState xstate = (IExtendedBlockState)state;
				key.add(xstate.getValue(BlockDrawers.STATE_MODEL));

				return key;
			} catch(Throwable t) {
				return super.getKey(state);
			}
		}
	}

	private static class ItemHandler extends ItemOverrideList {
		public ItemHandler() {
			super(ImmutableList.<ItemOverride>of());
		}

		@Override
		public IBakedModel handleItemState(IBakedModel parent, @Nonnull ItemStack stack, World world,
				EntityLivingBase entity) {
			if(stack.isEmpty())
				return parent;

			if(!stack.hasTagCompound() || !stack.getTagCompound().hasKey("tile", Constants.NBT.TAG_COMPOUND))
				return parent;

			Block block = Block.getBlockFromItem(stack.getItem());
			IBlockState state = block.getStateFromMeta(stack.getMetadata());

			return new DrawerSealedModel(parent, state, true);
		}
	}

	private static final ItemHandler itemHandler = new ItemHandler();
}

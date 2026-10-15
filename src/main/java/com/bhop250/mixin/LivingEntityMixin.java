package com.bhop250.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import com.bhop250.config.Bhop250Config;
import com.bhop250.movement.MovementController;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Unique
	private static final int BHOP250_GROUND_TICKS_CAP = 1000;

	@Shadow
	private int noJumpDelay;

	@Unique
	private boolean bhop250$active;
	@Unique
	private boolean bhop250$hopping;
	@Unique
	private boolean bhop250$jumpedThisTick;
	@Unique
	private int bhop250$groundTicks;
	@Unique
	private float bhop250$blockFriction = 1.0F;
	@Unique
	private Vec3 bhop250$postMove;
	@Unique
	private double bhop250$preJumpX;
	@Unique
	private double bhop250$preJumpZ;

	@Unique
	private LivingEntity bhop250$self() {
		return (LivingEntity) (Object) this;
	}

	@Inject(method = "jumpFromGround()V", at = @At("HEAD"))
	private void bhop250$beforeJump(CallbackInfo ci) {
		LivingEntity self = this.bhop250$self();
		this.bhop250$hopping = MovementController.shouldApply(self);
		if (this.bhop250$hopping) {
			Vec3 delta = self.getDeltaMovement();
			this.bhop250$preJumpX = delta.x;
			this.bhop250$preJumpZ = delta.z;
		}
	}

	@Inject(method = "jumpFromGround()V", at = @At("RETURN"))
	private void bhop250$afterJump(CallbackInfo ci) {
		if (!this.bhop250$hopping) {
			return;
		}
		this.bhop250$hopping = false;
		this.bhop250$jumpedThisTick = true;

		LivingEntity self = this.bhop250$self();
		self.setDeltaMovement(MovementController.afterHop(self, this.bhop250$preJumpX, this.bhop250$preJumpZ, self.getDeltaMovement()));
	}

	@Inject(method = "travel(Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"))
	private void bhop250$beginTravel(Vec3 input, CallbackInfo ci) {
		this.bhop250$active = MovementController.shouldApply(this.bhop250$self());
		this.bhop250$postMove = null;
		if (!this.bhop250$active) {
			this.bhop250$groundTicks = 0;
			this.bhop250$jumpedThisTick = false;
		}
	}

	@Inject(method = "handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"))
	private void bhop250$captureBlockFriction(Vec3 input, float blockFriction, CallbackInfoReturnable<Vec3> cir) {
		this.bhop250$blockFriction = blockFriction;
	}

	@WrapOperation(
			method = "handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V")
	)
	private void bhop250$sourceAccelerate(LivingEntity entity, float speed, Vec3 input, Operation<Void> original) {
		if (!this.bhop250$active) {
			original.call(entity, speed, input);
			return;
		}

		boolean grounded = entity.onGround() && !this.bhop250$jumpedThisTick;
		boolean inPerfectHopWindow = grounded && this.bhop250$groundTicks < Bhop250Config.get().perfectHopWindowTicks;

		entity.setDeltaMovement(MovementController.accelerate(entity, input, this.bhop250$blockFriction, grounded && !inPerfectHopWindow));
		this.bhop250$groundTicks = grounded ? Math.min(this.bhop250$groundTicks + 1, BHOP250_GROUND_TICKS_CAP) : 0;
	}

	@Inject(method = "handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"))
	private void bhop250$capturePostMove(Vec3 input, float blockFriction, CallbackInfoReturnable<Vec3> cir) {
		if (this.bhop250$active) {
			this.bhop250$postMove = cir.getReturnValue();
		}
	}

	@Inject(method = "travel(Lnet/minecraft/world/phys/Vec3;)V", at = @At("RETURN"))
	private void bhop250$endTravel(Vec3 input, CallbackInfo ci) {
		if (!this.bhop250$active) {
			return;
		}
		this.bhop250$active = false;
		this.bhop250$jumpedThisTick = false;

		LivingEntity self = this.bhop250$self();
		if (this.bhop250$postMove != null) {
			Vec3 now = self.getDeltaMovement();
			self.setDeltaMovement(new Vec3(this.bhop250$postMove.x, now.y, this.bhop250$postMove.z));
			this.bhop250$postMove = null;
		}

		if (Bhop250Config.get().autoBhop) {
			this.noJumpDelay = 0;
		}
	}
}
